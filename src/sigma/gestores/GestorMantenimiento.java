package sigma.gestores;

import sigma.excepciones.ReglaNegocioException;
import sigma.modelo.Administrador;
import sigma.modelo.Camara;
import sigma.modelo.EmpleadoMantenimiento;
import sigma.modelo.Equipo;
import sigma.modelo.Especialidad;
import sigma.modelo.FallaEquipo;
import sigma.modelo.Luminaria;
import sigma.modelo.Notificacion;
import sigma.modelo.OrdenMantenimiento;
import sigma.modelo.PrioridadOrden;
import sigma.repositorios.RepositorioEquipos;
import sigma.repositorios.RepositorioNotificaciones;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controlador de equipos y ordenes de mantenimiento.
 * Trabaja con la abstraccion Equipo: un nuevo tipo de equipo no obliga a modificar este gestor (OCP).
 */
public class GestorMantenimiento {
    private final RepositorioEquipos equipos;
    private final RepositorioNotificaciones notificaciones;

    public GestorMantenimiento(RepositorioEquipos equipos, RepositorioNotificaciones notificaciones) {
        if (equipos == null || notificaciones == null) throw new IllegalArgumentException("Repositorios obligatorios");
        this.equipos = equipos;
        this.notificaciones = notificaciones;
    }

    public void registrarEquipo(Equipo equipo) throws ReglaNegocioException, IOException {
        if (equipo == null) throw new IllegalArgumentException("El equipo es obligatorio");
        if (buscarEquipo(equipo.getCodigo()) != null) {
            throw new ReglaNegocioException("Ya existe un equipo con el codigo " + equipo.getCodigo());
        }
        equipos.guardar(equipo);
    }

    /**
     * Si el equipo ya tiene una falla abierta, el nuevo aviso se agrega a esa falla
     * o a su orden en lugar de crear otra (supuesto SUP-05).
     */
    public FallaEquipo registrarFalla(Equipo equipo, String autor, String descripcion, LocalDateTime fecha) throws IOException {
        if (equipo == null || autor == null || autor.isBlank() || descripcion == null || descripcion.isBlank() || fecha == null) {
            throw new IllegalArgumentException("Datos de la falla obligatorios");
        }
        FallaEquipo abierta = equipo.fallaAbierta();
        if (abierta != null) {
            OrdenMantenimiento orden = buscarOrdenAbierta(abierta);
            if (orden != null) {
                orden.agregarObservacion(fecha + " " + autor + ": " + descripcion);
                equipos.guardarOrden(orden);
            } else {
                abierta.agregarObservacion(autor, descripcion, fecha);
                equipos.guardar(equipo);
            }
            return abierta;
        }
        FallaEquipo falla = equipo.registrarFalla(descripcion, fecha, autor);
        equipos.guardar(equipo);
        return falla;
    }

    public OrdenMantenimiento generarOrden(Administrador administrador, FallaEquipo falla, EmpleadoMantenimiento tecnico,
                                          LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (administrador == null || falla == null || fecha == null) throw new IllegalArgumentException("Datos de la orden obligatorios");
        Equipo equipo = buscarEquipoDeLaFalla(falla);
        // CU-18 A2: no se duplica la orden; se informa cual es la existente
        OrdenMantenimiento existente = buscarOrdenAbierta(falla);
        if (existente != null) {
            throw new ReglaNegocioException("La falla ya tiene una orden abierta: " + existente);
        }
        verificarEspecialidad(tecnico);

        // Polimorfismo: cada equipo calcula su prioridad
        int luminariasFalladas = equipo.getSector().contarLuminariasFalladas();
        PrioridadOrden prioridad = equipo.prioridadDeFalla(luminariasFalladas);

        int numero = equipos.listarOrdenes().size() + 1;
        OrdenMantenimiento orden = new OrdenMantenimiento(numero, fecha, prioridad, falla.getDescripcion(), equipo, falla, tecnico);
        equipos.guardarOrden(orden);

        // Si fallan mas de 3 luminarias del sector, las demas ordenes abiertas del sector pasan a prioridad alta
        if (equipo instanceof Luminaria && prioridad == PrioridadOrden.ALTA) {
            elevarOrdenesDelSector(equipo);
        }
        return orden;
    }

    public void asignarTecnico(OrdenMantenimiento orden, EmpleadoMantenimiento tecnico) throws ReglaNegocioException, IOException {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria");
        verificarEspecialidad(tecnico);
        orden.asignarTecnico(tecnico);
        equipos.guardarOrden(orden);
    }

    public void iniciarReparacion(OrdenMantenimiento orden) throws ReglaNegocioException, IOException {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria");
        orden.iniciarReparacion();
        equipos.guardarOrden(orden);
    }

    public void cerrarOrden(OrdenMantenimiento orden, EmpleadoMantenimiento tecnico, String descripcion,
                            LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria");
        orden.cerrar(fecha, tecnico, descripcion);
        equipos.guardarOrden(orden);
    }

    /** El equipo no se pudo reparar: se cierra la orden y queda fuera de servicio. */
    public void cerrarOrdenSinReparacion(OrdenMantenimiento orden, EmpleadoMantenimiento tecnico, String descripcion,
                                         LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria");
        orden.cerrarSinReparacion(fecha, tecnico, descripcion);
        equipos.guardarOrden(orden);
    }

    /** Equipos ordenados por codigo (orden personalizado con Comparator). */
    public List<Equipo> consultarEquipos(LocalDateTime ahora) {
        if (ahora == null) throw new IllegalArgumentException("La fecha es obligatoria");
        List<Equipo> lista = equipos.listar();
        lista.sort(Comparator.comparing(Equipo::getCodigo));
        return lista;
    }

    /** Ordenes todavia abiertas de un equipo (CU-20). */
    public List<OrdenMantenimiento> consultarOrdenesAbiertas(Equipo equipo) {
        List<OrdenMantenimiento> abiertas = new ArrayList<>();
        for (OrdenMantenimiento orden : equipos.listarOrdenes()) {
            if (orden.estaAbierta() && orden.getEquipo() == equipo) {
                abiertas.add(orden);
            }
        }
        return abiertas;
    }

    public List<OrdenMantenimiento> consultarOrdenes() {
        return equipos.listarOrdenes();
    }

    public OrdenMantenimiento buscarOrden(int numero) {
        for (OrdenMantenimiento orden : equipos.listarOrdenes()) {
            if (orden.getNumero() == numero) return orden;
        }
        return null;
    }

    public Equipo buscarEquipo(String codigo) {
        for (Equipo equipo : equipos.listar()) {
            if (equipo.getCodigo().equalsIgnoreCase(codigo)) return equipo;
        }
        return null;
    }

    /** Avisa al administrador por cada camara sin funcionar por mas de 48 horas. */
    public void verificarPlazos(LocalDateTime ahora) throws IOException {
        if (ahora == null) throw new IllegalArgumentException("La fecha es obligatoria");
        for (Equipo equipo : equipos.listar()) {
            if (equipo instanceof Camara) {
                Camara camara = (Camara) equipo;
                if (camara.requiereAviso(ahora)) {
                    Notificacion aviso = new Notificacion(ahora, "Camara sin funcionar por mas de 48 horas",
                            "Administrador", "Camara " + camara.getCodigo(),
                            "CAMARA-48H-" + camara.getCodigo() + "-" + camara.getUltimaFalla().getFecha());
                    notificaciones.guardarSiNoExiste(aviso);
                }
            }
        }
    }

    // Supuesto SUP-06: camaras y luminarias son equipos electricos y los repara un electricista
    private void verificarEspecialidad(EmpleadoMantenimiento tecnico) throws ReglaNegocioException {
        if (tecnico != null && tecnico.getEspecialidad() != Especialidad.ELECTRICIDAD) {
            throw new ReglaNegocioException("Las camaras y luminarias las repara un tecnico de ELECTRICIDAD");
        }
    }

    private void elevarOrdenesDelSector(Equipo equipo) throws IOException {
        for (OrdenMantenimiento otra : equipos.listarOrdenes()) {
            boolean mismaZona = otra.getEquipo().getSector() == equipo.getSector();
            if (otra.estaAbierta() && otra.getEquipo() instanceof Luminaria && mismaZona
                    && otra.getPrioridad() != PrioridadOrden.ALTA) {
                otra.elevarPrioridad();
                equipos.guardarOrden(otra);
            }
        }
    }

    private Equipo buscarEquipoDeLaFalla(FallaEquipo falla) {
        for (Equipo equipo : equipos.listar()) {
            if (equipo.contieneFalla(falla)) return equipo;
        }
        throw new IllegalArgumentException("La falla no pertenece a ningun equipo registrado");
    }

    private OrdenMantenimiento buscarOrdenAbierta(FallaEquipo falla) {
        for (OrdenMantenimiento orden : equipos.listarOrdenes()) {
            if (orden.estaAbierta() && orden.getFalla() == falla) return orden;
        }
        return null;
    }
}
