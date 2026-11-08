package sigma;

import sigma.excepciones.ReglaNegocioException;
import sigma.gestores.GestorAccesos;
import sigma.gestores.GestorExpensas;
import sigma.gestores.GestorMantenimiento;
import sigma.gestores.GestorReclamos;
import sigma.gestores.GestorReportes;
import sigma.gestores.PlanificadorPlazos;
import sigma.modelo.Administrador;
import sigma.modelo.Barrio;
import sigma.modelo.CalculadoraMora;
import sigma.modelo.Camara;
import sigma.modelo.EmpleadoMantenimiento;
import sigma.modelo.Especialidad;
import sigma.modelo.Guardia;
import sigma.modelo.Lote;
import sigma.modelo.Luminaria;
import sigma.modelo.Notificacion;
import sigma.modelo.Persona;
import sigma.modelo.PuestoGuardia;
import sigma.modelo.Residencia;
import sigma.modelo.TipoResidente;
import sigma.persistencia.AlmacenArchivos;
import sigma.persistencia.RepositorioAutorizacionesArchivo;
import sigma.persistencia.RepositorioEquiposArchivo;
import sigma.persistencia.RepositorioExpensasArchivo;
import sigma.persistencia.RepositorioIngresosArchivo;
import sigma.persistencia.RepositorioNotificacionesArchivo;
import sigma.persistencia.RepositorioPagosArchivo;
import sigma.persistencia.RepositorioReclamosArchivo;
import sigma.repositorios.RepositorioEquipos;
import sigma.repositorios.RepositorioExpensas;
import sigma.repositorios.RepositorioNotificaciones;
import sigma.repositorios.RepositorioReclamos;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Arma la aplicacion: crea el almacenamiento, los repositorios y los gestores,
 * y los conecta entre si. Las vistas (consola y ventana) usan esta clase.
 */
public class AplicacionSIGMA {
    public static final Path ARCHIVO_DATOS = Paths.get("data", "sigma.dat");
    private static final BigDecimal TASA_MENSUAL = new BigDecimal("0.03");

    private final AlmacenArchivos almacen;
    private final RepositorioNotificaciones notificaciones;
    private final GestorReclamos gestorReclamos;
    private final GestorExpensas gestorExpensas;
    private final GestorAccesos gestorAccesos;
    private final GestorMantenimiento gestorMantenimiento;
    private final GestorReportes gestorReportes;
    private final PlanificadorPlazos planificador;

    public AplicacionSIGMA(Path archivo) throws IOException, ReglaNegocioException {
        almacen = new AlmacenArchivos(archivo, "Los Aromos");
        CalculadoraMora mora = new CalculadoraMora(TASA_MENSUAL);

        RepositorioReclamos reclamos = new RepositorioReclamosArchivo(almacen);
        RepositorioExpensas expensas = new RepositorioExpensasArchivo(almacen);
        RepositorioEquipos equipos = new RepositorioEquiposArchivo(almacen);
        notificaciones = new RepositorioNotificacionesArchivo(almacen);

        gestorReclamos = new GestorReclamos(reclamos, notificaciones);
        gestorExpensas = new GestorExpensas(expensas, new RepositorioPagosArchivo(almacen), mora, notificaciones);
        gestorAccesos = new GestorAccesos(new RepositorioAutorizacionesArchivo(almacen), new RepositorioIngresosArchivo(almacen));
        gestorMantenimiento = new GestorMantenimiento(equipos, notificaciones);
        gestorReportes = new GestorReportes(reclamos, expensas, equipos, mora);
        planificador = new PlanificadorPlazos(gestorReclamos, gestorMantenimiento, gestorExpensas);

        if (getBarrio().getLotes().isEmpty()) {
            cargarDatosIniciales();
        }
    }

    /** Revisa los plazos (reclamos, camaras, morosidad) con la fecha y hora actual. */
    public void revisarPlazos() throws ReglaNegocioException, IOException {
        planificador.ejecutar(LocalDateTime.now());
    }

    /** Guarda cambios hechos directamente sobre el barrio (residentes, personal). */
    public void guardarCambios() throws IOException {
        almacen.guardar();
    }

    // Se pide siempre al almacen: si una escritura falla, el almacen recarga los datos
    public Barrio getBarrio() { return almacen.getDatos().getBarrio(); }
    public List<Notificacion> getNotificaciones() { return notificaciones.listar(); }
    public GestorReclamos getGestorReclamos() { return gestorReclamos; }
    public GestorExpensas getGestorExpensas() { return gestorExpensas; }
    public GestorAccesos getGestorAccesos() { return gestorAccesos; }
    public GestorMantenimiento getGestorMantenimiento() { return gestorMantenimiento; }
    public GestorReportes getGestorReportes() { return gestorReportes; }

    /** Datos de prueba: los 150 lotes, algunos vecinos, el personal y algunos equipos. */
    private void cargarDatosIniciales() throws IOException, ReglaNegocioException {
        Barrio barrio = getBarrio();
        for (int numero = 1; numero <= Barrio.CANTIDAD_LOTES; numero++) {
            double superficie = 400 + (numero % 5) * 100;
            barrio.agregarLote(new Lote(numero, superficie));
        }
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        Persona ana = new Persona("Ana", "Perez", "30111222", "1155550001", "ana@mail.com");
        Persona bruno = new Persona("Bruno", "Gomez", "28999888", "1155550002", "bruno@mail.com");
        Persona carla = new Persona("Carla", "Diaz", "33444555", "1155550003", "carla@mail.com");
        Persona diego = new Persona("Diego", "Diaz", "40111000", "1155550004", "diego@mail.com");
        barrio.obtenerLote(1).agregarResidencia(new Residencia(ana, TipoResidente.TITULAR, inicio, null, true));
        barrio.obtenerLote(2).agregarResidencia(new Residencia(bruno, TipoResidente.TITULAR, inicio, null, true));
        barrio.obtenerLote(3).agregarResidencia(new Residencia(carla, TipoResidente.TITULAR, inicio, null, true));
        barrio.obtenerLote(3).agregarResidencia(new Residencia(diego, TipoResidente.FAMILIAR, inicio, null, true));

        barrio.agregarEmpleado(new Administrador("ADM-01", "Laura Sosa", LocalDate.of(2020, 3, 1)));
        barrio.agregarEmpleado(new Guardia("GUA-01", "Mario Ruiz", LocalDate.of(2022, 5, 10), "Manana", PuestoGuardia.PORTERIA));
        barrio.agregarEmpleado(new Guardia("GUA-02", "Pedro Rios", LocalDate.of(2023, 7, 1), "Noche", PuestoGuardia.RONDA));
        barrio.agregarEmpleado(new EmpleadoMantenimiento("MAN-01", "Jorge Paz", LocalDate.of(2021, 2, 15), Especialidad.ELECTRICIDAD));
        barrio.agregarEmpleado(new EmpleadoMantenimiento("MAN-02", "Rita Luna", LocalDate.of(2024, 8, 20), Especialidad.PLOMERIA));

        LocalDate instalacion = LocalDate.of(2025, 6, 1);
        barrio.agregarEquipo(new Camara("CAM-01", barrio.obtenerSector("Acceso principal"), instalacion, "1080p", 15));
        barrio.agregarEquipo(new Camara("CAM-02", barrio.obtenerSector("Espacios comunes"), instalacion, "720p", 7));
        for (int i = 1; i <= 5; i++) {
            barrio.agregarEquipo(new Luminaria("LUM-0" + i, barrio.obtenerSector("Calle interna 1"), instalacion, "LED", 60));
        }
        almacen.guardar();
    }
}
