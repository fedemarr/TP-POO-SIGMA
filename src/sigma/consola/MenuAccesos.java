package sigma.consola;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import sigma.excepciones.ReglaNegocioException;
import sigma.gestores.GestorAccesos;
import sigma.modelo.*;
import sigma.repositorios.RepositorioAutorizaciones;
import sigma.repositorios.RepositorioIngresos;

public class MenuAccesos {
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private final Scanner entrada;
    private final GestorAccesos gestor;
    private final RepositorioAutorizaciones autorizaciones;
    private final RepositorioIngresos ingresos;
    private final DatosDemostracion datos = new DatosDemostracion();

    public MenuAccesos(Scanner entrada, GestorAccesos gestor,
            RepositorioAutorizaciones autorizaciones, RepositorioIngresos ingresos) {
        this.entrada = entrada;
        this.gestor = gestor;
        this.autorizaciones = autorizaciones;
        this.ingresos = ingresos;
    }

    public void ejecutar() {
        System.out.println("SIGMA - Fase 2 en desarrollo: modulo de accesos");
        System.out.println("Datos ficticios: vecino 1 Ana (lote 1), vecino 2 Luis (lote 2).");
        boolean salir = false;
        while (!salir) {
            System.out.println("\n1 Autorizar visita | 2 Ingreso | 3 Egreso | 4 Autorizaciones | 5 Historial | 0 Salir");
            try {
                switch (leer("Opcion: ")) {
                    case "1": autorizar(); break;
                    case "2": ingresar(); break;
                    case "3": egresar(); break;
                    case "4": mostrarAutorizaciones(); break;
                    case "5": mostrarIngresos(); break;
                    case "0": salir = true; break;
                    default: System.out.println("Opcion invalida.");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Operacion rechazada: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error de archivo: " + e.getMessage() + ". Operacion no confirmada.");
            } catch (DateTimeParseException e) {
                System.out.println("Fecha u hora invalida. Use los formatos indicados.");
            } catch (IllegalArgumentException e) {
                System.out.println("Dato invalido: " + e.getMessage());
            } catch (NoSuchElementException e) {
                salir = true;
            }
        }
        System.out.println("Hasta luego. Las operaciones confirmadas quedaron guardadas.");
    }

    private void autorizar() throws ReglaNegocioException, IOException {
        Persona otorgante = datos.vecino(numero("Vecino que autoriza (1 Ana, 2 Luis): "));
        Lote lote = datos.lote(numero("Lote a visitar (1 o 2): "));
        Visitante visitante = new Visitante(leer("Nombre del visitante: "), leer("Documento del visitante: "));
        LocalDate desde = LocalDate.parse(leer("Fecha inicial (AAAA-MM-DD): "));
        String opcion = leer("Proveedor recurrente (s/n): ");
        if (!opcion.equalsIgnoreCase("s") && !opcion.equalsIgnoreCase("n")) {
            throw new IllegalArgumentException("Indique s o n.");
        }
        boolean recurrente = opcion.equalsIgnoreCase("s");
        LocalDate hasta = recurrente ? LocalDate.parse(leer("Fecha final (AAAA-MM-DD, maximo 30 dias): ")) : desde;
        LocalTime horaDesde = LocalTime.parse(leer("Hora inicial (HH:MM): "));
        LocalTime horaHasta = LocalTime.parse(leer("Hora final (HH:MM): "));
        Autorizacion autorizacion = gestor.autorizarVisita(otorgante, lote, visitante, desde, hasta,
                horaDesde, horaHasta, recurrente);
        System.out.println("Autorizacion confirmada (se reutiliza si ya existe): " + autorizacion.getId());
    }

    private void ingresar() throws ReglaNegocioException, IOException {
        String documento = leer("Documento del visitante: ");
        Lote lote = datos.lote(numero("Lote (1 o 2): "));
        Guardia guardia = datos.guardia(numero("Guardia (1 porteria, 2 ronda): "));
        LocalDateTime fecha = momento();
        RegistroIngreso registro = gestor.registrarIngreso(documento, lote, guardia, fecha);
        System.out.println("Ingreso confirmado: " + registro.getId());
    }

    private void egresar() throws ReglaNegocioException, IOException {
        String documento = leer("Documento del visitante: ");
        gestor.registrarEgreso(documento, momento());
        System.out.println("Egreso confirmado.");
    }

    private void mostrarAutorizaciones() throws IOException {
        List<Autorizacion> lista = autorizaciones.listar();
        if (lista.isEmpty()) { System.out.println("No hay autorizaciones."); }
        for (Autorizacion autorizacion : lista) {
            System.out.println(autorizacion.getVisitante().getNombre() + " - DNI "
                    + autorizacion.getVisitante().getDocumento() + " - lote " + autorizacion.getLote().getNumero()
                    + " - " + autorizacion.getDesde() + " a " + autorizacion.getHasta()
                    + " - " + autorizacion.getHoraDesde() + " a " + autorizacion.getHoraHasta());
        }
    }

    private void mostrarIngresos() throws IOException {
        List<RegistroIngreso> lista = ingresos.listar();
        String orden = leer("Orden (1 fecha, 2 lote y fecha): ");
        if (orden.equals("2")) {
            lista.sort(Comparator.comparingInt((RegistroIngreso registro) -> registro.getAutorizacion()
                    .getLote().getNumero()).thenComparing(Comparator.naturalOrder()));
        } else if (!orden.equals("1")) {
            throw new IllegalArgumentException("Elija 1 o 2.");
        }
        if (lista.isEmpty()) { System.out.println("No hay ingresos."); }
        for (RegistroIngreso registro : lista) {
            System.out.println("DNI " + registro.getAutorizacion().getVisitante().getDocumento()
                    + " - lote " + registro.getAutorizacion().getLote().getNumero()
                    + " - ingreso " + registro.getIngreso()
                    + " - egreso " + (registro.estaAbierto() ? "PENDIENTE" : registro.getEgreso()));
        }
    }

    private LocalDateTime momento() {
        String texto = leer("Fecha y hora (AAAA-MM-DD HH:MM, Enter = ahora): ");
        return texto.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(texto, FECHA_HORA);
    }

    private int numero(String mensaje) { return Integer.parseInt(leer(mensaje)); }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine().trim();
    }
}
