package sigma.pruebas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import sigma.excepciones.*;
import sigma.gestores.GestorAccesos;
import sigma.modelo.*;
import sigma.persistencia.*;
import sigma.repositorios.*;

/** Pruebas con verificaciones explicitas; no requieren -ea ni librerias externas. */
public class PruebasAccesos {
    private static int aprobadas;
    private static final LocalDate DIA = LocalDate.of(2026, 10, 28);
    private static final LocalTime DESDE = LocalTime.of(9, 0);
    private static final LocalTime HASTA = LocalTime.of(18, 0);
    private static final LocalDateTime INGRESO = DIA.atTime(10, 0);

    private interface Accion { void ejecutar() throws Exception; }

    public static void main(String[] argumentos) throws Exception {
        probar("Franja: limites incluidos, antes y despues rechazados", () -> {
            Caso c = new Caso();
            Autorizacion a = c.autorizar(c.lote, DIA, DIA, false);
            verificar(a.estaVigente(DIA.atTime(DESDE)), "Hora inicial");
            verificar(a.estaVigente(DIA.atTime(HASTA)), "Hora final");
            verificar(!a.estaVigente(DIA.atTime(8, 59)), "Antes de la franja");
            verificar(!a.estaVigente(DIA.atTime(18, 1)), "Despues de la franja");
            verificar(!a.estaVigente(DIA.plusDays(1).atTime(10, 0)), "Otro dia");
        });
        probar("Proveedor: treinta dias inclusivos permitidos", () -> {
            Caso c = new Caso();
            Autorizacion a = c.autorizar(c.lote, DIA, DIA.plusDays(29), true);
            verificar(a.estaVigente(DIA.plusDays(29).atTime(12, 0)), "Ultimo dia");
        });
        probar("Proveedor: treinta y un dias rechazados sin guardar", () -> {
            Caso c = new Caso();
            esperar(AutorizacionInvalidaException.class,
                    () -> c.autorizar(c.lote, DIA, DIA.plusDays(30), true));
            verificar(c.autorizaciones.listar().isEmpty(), "No se guardo");
        });
        probar("Visita no recurrente de varios dias rechazada", () -> {
            Caso c = new Caso();
            esperar(AutorizacionInvalidaException.class,
                    () -> c.autorizar(c.lote, DIA, DIA.plusDays(1), false));
        });
        probar("Fechas invertidas rechazadas", () -> {
            Caso c = new Caso();
            esperar(AutorizacionInvalidaException.class,
                    () -> c.autorizar(c.lote, DIA, DIA.minusDays(1), true));
        });
        probar("Cruce de medianoche y franja vacia rechazados", () -> {
            Caso c = new Caso();
            esperar(AutorizacionInvalidaException.class, () -> new Autorizacion(c.vecino, c.lote,
                    c.visitante, DIA, DIA, HASTA, DESDE, false));
            esperar(AutorizacionInvalidaException.class, () -> new Autorizacion(c.vecino, c.lote,
                    c.visitante, DIA, DIA, DESDE, DESDE, false));
        });
        probar("Un vecino no puede autorizar en un lote ajeno", () -> {
            Caso c = new Caso();
            Lote ajeno = new Lote(3, 500);
            esperar(AutorizacionInvalidaException.class, () -> c.autorizar(ajeno, DIA, DIA, false));
            verificar(c.autorizaciones.listar().isEmpty(), "No se guardo");
        });
        probar("Residente: requiere permiso y vigencia durante toda la autorizacion", () -> {
            Caso c = new Caso();
            Persona familiar = new Persona("Eva", "Prueba", "999", "11", "eva@example.com");
            Residencia residencia = new Residencia(familiar, TipoResidente.FAMILIAR, DIA);
            c.lote.agregarResidencia(residencia);
            verificar(!c.lote.estaAutorizada(familiar, DIA), "Sin permiso");
            residencia.autorizar();
            residencia.finalizar(DIA.plusDays(1));
            c.gestor.autorizarVisita(familiar, c.lote, c.visitante, DIA, DIA, DESDE, HASTA, false);
            esperar(AutorizacionInvalidaException.class, () -> c.gestor.autorizarVisita(familiar,
                    c.lote, c.visitante, DIA, DIA.plusDays(2), DESDE, HASTA, true));
        });
        probar("Autorizacion duplicada devuelve la existente", () -> {
            Caso c = new Caso();
            Autorizacion primera = c.autorizar(c.lote, DIA, DIA, false);
            Autorizacion segunda = c.autorizar(c.lote, DIA, DIA, false);
            verificar(primera.getId().equals(segunda.getId()), "Identificador conservado");
            verificar(c.autorizaciones.listar().size() == 1, "Un solo registro");
        });
        probar("Guardia fuera de porteria rechazado", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            Guardia ronda = new Guardia("G2", "Ronda", DIA, "Manana", PuestoGuardia.RONDA);
            esperar(ReglaNegocioException.class,
                    () -> c.gestor.registrarIngreso("123", c.lote, ronda, INGRESO));
            verificar(c.ingresos.listar().isEmpty(), "Sin ingreso");
        });
        probar("Sin autorizacion rechaza ingreso", () -> {
            Caso c = new Caso();
            esperar(AutorizacionInvalidaException.class, () -> c.ingresar(c.lote, INGRESO));
        });
        probar("Autorizacion vencida primero: encuentra otra vigente", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA.minusDays(1), DIA.minusDays(1), false);
            Autorizacion vigente = c.autorizar(c.lote, DIA, DIA, false);
            RegistroIngreso r = c.ingresar(c.lote, INGRESO);
            verificar(r.getAutorizacion().getId().equals(vigente.getId()), "Candidata correcta");
        });
        probar("Fuera de horario no ingresa", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            esperar(AutorizacionInvalidaException.class, () -> c.ingresar(c.lote, DIA.atTime(8, 0)));
        });
        probar("Autorizacion para otro lote no habilita ingreso", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            esperar(AutorizacionInvalidaException.class, () -> c.ingresar(c.otroLote, INGRESO));
        });
        probar("Documento con espacios se normaliza al ingresar", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            c.gestor.registrarIngreso(" 123 ", c.lote, c.guardia, INGRESO);
            verificar(c.ingresos.buscarAbierto("123") != null, "Documento normalizado");
        });
        probar("Segundo ingreso abierto rechazado en el mismo lote", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            c.ingresar(c.lote, INGRESO);
            esperar(IngresoDuplicadoException.class, () -> c.ingresar(c.lote, INGRESO.plusMinutes(1)));
            verificar(c.ingresos.listar().size() == 1, "Un solo ingreso");
        });
        probar("Segundo ingreso abierto rechazado incluso para otro lote", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            c.autorizar(c.otroLote, DIA, DIA, false);
            c.ingresar(c.lote, INGRESO);
            esperar(IngresoDuplicadoException.class, () -> c.ingresar(c.otroLote, INGRESO.plusMinutes(1)));
        });
        probar("Egreso anterior al ingreso rechazado sin modificar datos", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            c.ingresar(c.lote, INGRESO);
            esperar(ReglaNegocioException.class, () -> c.gestor.registrarEgreso("123", INGRESO.minusMinutes(1)));
            verificar(c.ingresos.buscarAbierto("123") != null, "Sigue abierto");
        });
        probar("Egreso sin ingreso rechazado", () -> {
            Caso c = new Caso();
            esperar(ReglaNegocioException.class, () -> c.gestor.registrarEgreso("123", INGRESO));
        });
        probar("Puede volver a ingresar despues de registrar egreso", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            c.ingresar(c.lote, INGRESO);
            c.gestor.registrarEgreso("123", INGRESO.plusHours(1));
            c.ingresar(c.lote, INGRESO.plusHours(2));
            verificar(c.ingresos.listar().size() == 2, "Se conserva el historial");
        });
        probar("Reingreso anterior al ultimo egreso rechazado", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            c.ingresar(c.lote, INGRESO);
            c.gestor.registrarEgreso("123", INGRESO.plusHours(2));
            esperar(ReglaNegocioException.class, () -> c.ingresar(c.lote, INGRESO.plusHours(1)));
        });
        probar("Fallo al guardar ingreso: error, sin confirmacion", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            GestorAccesos falla = new GestorAccesos(c.autorizaciones, fallaEscritura(c.ingresos));
            esperar(IOException.class, () -> falla.registrarIngreso("123", c.lote, c.guardia, INGRESO));
            verificar(c.ingresos.listar().isEmpty(), "No hay ingreso confirmado");
        });
        probar("Fallo al guardar egreso: el ingreso confirmado sigue abierto", () -> {
            Caso c = new Caso();
            c.autorizar(c.lote, DIA, DIA, false);
            c.ingresar(c.lote, INGRESO);
            GestorAccesos falla = new GestorAccesos(c.autorizaciones, fallaEscritura(c.ingresos));
            esperar(IOException.class, () -> falla.registrarEgreso("123", INGRESO.plusHours(1)));
            verificar(c.ingresos.buscarAbierto("123") != null, "Sigue abierto en el repositorio");
        });
        probar("Persistencia: reinicio conserva autorizaciones, ingresos y egresos", () -> {
            Path archivo = temporal("reinicio").resolve("accesos.dat");
            AlmacenAccesos almacen = new AlmacenAccesos(archivo);
            RepositorioAutorizaciones ra = new RepositorioAutorizacionesArchivo(almacen);
            RepositorioIngresos ri = new RepositorioIngresosArchivo(almacen);
            Caso c = new Caso();
            GestorAccesos gestor = new GestorAccesos(ra, ri);
            gestor.autorizarVisita(c.vecino, c.lote, c.visitante, DIA, DIA, DESDE, HASTA, false);
            gestor.registrarIngreso("123", c.lote, c.guardia, INGRESO);
            AlmacenAccesos reinicio = new AlmacenAccesos(archivo);
            RepositorioAutorizaciones ra2 = new RepositorioAutorizacionesArchivo(reinicio);
            RepositorioIngresos ri2 = new RepositorioIngresosArchivo(reinicio);
            verificar(ra2.buscar("123", c.lote).size() == 1, "Autorizacion recuperada");
            verificar(ri2.buscarAbierto("123") != null, "Ingreso recuperado");
            GestorAccesos gestor2 = new GestorAccesos(ra2, ri2);
            gestor2.registrarEgreso("123", INGRESO.plusHours(1));
            RepositorioIngresos ri3 = new RepositorioIngresosArchivo(new AlmacenAccesos(archivo));
            verificar(ri3.buscarAbierto("123") == null, "Egreso recuperado");
            verificar(ri3.listar().size() == 1, "Historial conservado");
        });
        probar("Archivo corrupto: informa error y conserva los bytes", () -> {
            Path archivo = temporal("corrupto").resolve("accesos.dat");
            byte[] contenido = new byte[] {1, 2, 3, 4};
            Files.write(archivo, contenido);
            AlmacenAccesos almacen = new AlmacenAccesos(archivo);
            esperar(IOException.class, () -> almacen.leerAutorizaciones());
            Caso c = new Caso();
            Autorizacion autorizacion = c.autorizar(c.lote, DIA, DIA, false);
            esperar(IOException.class, () -> almacen.guardarAutorizacion(autorizacion));
            verificar(Arrays.equals(contenido, Files.readAllBytes(archivo)), "No se reemplazo");
        });
        probar("Ruta no escribible: propaga error sin crear datos", () -> {
            Path bloqueante = temporal("bloqueada").resolve("archivo");
            Files.write(bloqueante, new byte[] {5});
            AlmacenAccesos almacen = new AlmacenAccesos(bloqueante.resolve("accesos.dat"));
            Caso c = new Caso();
            Autorizacion a = c.autorizar(c.lote, DIA, DIA, false);
            esperar(IOException.class, () -> almacen.guardarAutorizacion(a));
            verificar(Arrays.equals(new byte[] {5}, Files.readAllBytes(bloqueante)), "Conserva el archivo");
        });
        probar("Comparable por fecha y Comparator por lote", () -> {
            Caso c = new Caso();
            Autorizacion a = c.autorizar(c.lote, DIA, DIA, false);
            Autorizacion b = c.autorizar(c.otroLote, DIA, DIA, false);
            RegistroIngreso primero = new RegistroIngreso(b, c.guardia, INGRESO);
            RegistroIngreso segundo = new RegistroIngreso(a, c.guardia, INGRESO.plusHours(1));
            List<RegistroIngreso> lista = Arrays.asList(segundo, primero);
            Collections.sort(lista);
            verificar(lista.get(0) == primero, "Orden natural");
            lista.sort(Comparator.comparingInt((RegistroIngreso r) -> r.getAutorizacion().getLote().getNumero())
                    .thenComparing(Comparator.naturalOrder()));
            verificar(lista.get(0) == segundo, "Orden personalizado");
        });
        probar("Leer el archivo no expone objetos internos confirmados", () -> {
            Path archivo = temporal("copias").resolve("accesos.dat");
            AlmacenAccesos almacen = new AlmacenAccesos(archivo);
            Caso c = new Caso();
            Autorizacion a = c.autorizar(c.lote, DIA, DIA, false);
            almacen.guardarAutorizacion(a);
            almacen.guardarIngreso(new RegistroIngreso(a, c.guardia, INGRESO));
            almacen.leerIngresos().get(0).registrarEgreso(INGRESO.plusHours(1));
            verificar(almacen.leerIngresos().get(0).estaAbierto(), "Cambio sin guardar no se confirma");
        });
        probar("Registro cerrado no admite otro egreso", () -> {
            Caso c = new Caso();
            RegistroIngreso r = new RegistroIngreso(c.autorizar(c.lote, DIA, DIA, false), c.guardia, INGRESO);
            r.registrarEgreso(INGRESO.plusHours(1));
            esperar(ReglaNegocioException.class, () -> r.registrarEgreso(INGRESO.plusHours(2)));
        });
        probar("Herencia: Guardia responde al contrato Empleado", () -> {
            Empleado empleado = new Caso().guardia;
            verificar(empleado.describirRol().contains("PORTERIA"), "Sobreescritura");
        });
        System.out.println("RESULTADO: " + aprobadas + " pruebas aprobadas.");
    }

    private static RepositorioIngresos fallaEscritura(RepositorioIngresos base) {
        return new RepositorioIngresos() {
            public RegistroIngreso buscarAbierto(String documento) throws IOException {
                return base.buscarAbierto(documento);
            }
            public List<RegistroIngreso> listar() throws IOException { return base.listar(); }
            public void guardar(RegistroIngreso registro) throws IOException {
                throw new IOException("Fallo de escritura simulado");
            }
        };
    }

    private static Path temporal(String nombre) throws IOException {
        Path carpeta = java.nio.file.Paths.get("build", "pruebas");
        Files.createDirectories(carpeta);
        return Files.createTempDirectory(carpeta, nombre + "-");
    }

    private static void probar(String nombre, Accion accion) throws Exception {
        try {
            accion.ejecutar();
            aprobadas++;
            System.out.println("OK " + nombre);
        } catch (Exception | AssertionError error) {
            System.err.println("FALLO " + nombre);
            throw error;
        }
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) { throw new AssertionError(mensaje); }
    }

    private static void esperar(Class<? extends Exception> tipo, Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (Exception error) {
            if (tipo.isInstance(error)) { return; }
            throw error;
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }

    private static class Caso {
        final Persona vecino = new Persona("Ana", "Prueba", "111", "11", "ana@example.com");
        final Visitante visitante = new Visitante("Visita", "123");
        final Lote lote = new Lote(1, 500);
        final Lote otroLote = new Lote(2, 600);
        final Guardia guardia = new Guardia("G1", "Porteria", DIA, "Manana", PuestoGuardia.PORTERIA);
        final RepositorioAutorizacionesMemoria autorizaciones = new RepositorioAutorizacionesMemoria();
        final RepositorioIngresosMemoria ingresos = new RepositorioIngresosMemoria();
        final GestorAccesos gestor = new GestorAccesos(autorizaciones, ingresos);

        Caso() {
            lote.agregarResidencia(new Residencia(vecino, TipoResidente.TITULAR, DIA.minusYears(1)));
            otroLote.agregarResidencia(new Residencia(vecino, TipoResidente.TITULAR, DIA.minusYears(1)));
        }

        Autorizacion autorizar(Lote destino, LocalDate desde, LocalDate hasta, boolean recurrente)
                throws Exception {
            return gestor.autorizarVisita(vecino, destino, visitante, desde, hasta, DESDE, HASTA, recurrente);
        }

        RegistroIngreso ingresar(Lote destino, LocalDateTime fecha) throws Exception {
            return gestor.registrarIngreso("123", destino, guardia, fecha);
        }
    }
}
