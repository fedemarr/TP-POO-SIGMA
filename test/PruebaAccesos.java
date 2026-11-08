import sigma.excepciones.AutorizacionInvalidaException;
import sigma.excepciones.IngresoDuplicadoException;
import sigma.excepciones.ReglaNegocioException;
import sigma.gestores.GestorAccesos;
import sigma.modelo.Autorizacion;
import sigma.modelo.Guardia;
import sigma.modelo.Lote;
import sigma.modelo.Persona;
import sigma.modelo.PuestoGuardia;
import sigma.modelo.RegistroIngreso;
import sigma.modelo.Residencia;
import sigma.modelo.TipoResidente;
import sigma.modelo.Visitante;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Ingreso de visitas: sigue los caminos del diagrama de secuencia CU-22. */
public class PruebaAccesos {
    public static void main(String[] args) throws Exception {
        Persona titular = new Persona("Ana", "Perez", "123", "11", "a@b.com");
        Lote lote = new Lote(1, 500);
        lote.agregarResidencia(new Residencia(titular, TipoResidente.TITULAR, LocalDate.of(2026, 1, 1), null, true));
        GestorAccesos gestor = new GestorAccesos(new RepositorioAutorizacionesMemoria(), new RepositorioIngresosMemoria());
        Guardia porteria = new Guardia("G1", "Mario", LocalDate.of(2025, 1, 1), "Manana", PuestoGuardia.PORTERIA);
        Guardia ronda = new Guardia("G2", "Pedro", LocalDate.of(2025, 1, 1), "Noche", PuestoGuardia.RONDA);
        LocalTime ocho = LocalTime.of(8, 0);
        LocalTime veinte = LocalTime.of(20, 0);
        LocalDate dia = LocalDate.of(2026, 11, 7);
        LocalDateTime mediodia = LocalDateTime.of(2026, 11, 7, 12, 0);

        // Sin autorizacion no se habilita el ingreso
        try {
            gestor.registrarIngreso("999", lote, porteria, mediodia);
            throw new AssertionError("Ingreso sin autorizacion");
        } catch (AutorizacionInvalidaException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        // Una autorizacion vencida y una vigente: se elige la vigente
        Visitante visita = new Visitante("Luis", "999");
        gestor.autorizarVisita(titular, lote, visita, dia.minusDays(3), dia.minusDays(3), ocho, veinte, false);
        Autorizacion vigente = gestor.autorizarVisita(titular, lote, visita, dia, dia, ocho, veinte, false);

        // CU-21 A3: la misma autorizacion no se duplica, se devuelve la existente
        Autorizacion repetida = gestor.autorizarVisita(titular, lote, visita, dia, dia, ocho, veinte, false);
        Verificar.verdadero(repetida == vigente, "no duplica la autorizacion");

        // Solo registra ingresos el guardia de porteria
        try {
            gestor.registrarIngreso("999", lote, ronda, mediodia);
            throw new AssertionError("Registro un guardia de ronda");
        } catch (ReglaNegocioException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        RegistroIngreso registro = gestor.registrarIngreso("999", lote, porteria, mediodia);
        Verificar.verdadero(registro.estaAbierto(), "ingreso abierto");

        // No puede volver a entrar sin haber salido
        try {
            gestor.registrarIngreso("999", lote, porteria, mediodia.plusMinutes(10));
            throw new AssertionError("Doble ingreso");
        } catch (IngresoDuplicadoException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }
        gestor.registrarEgreso("999", mediodia.plusHours(1));
        Verificar.verdadero(!registro.estaAbierto(), "egreso registrado");

        // Fuera de la franja horaria no puede entrar
        try {
            gestor.registrarIngreso("999", lote, porteria, LocalDateTime.of(2026, 11, 7, 22, 0));
            throw new AssertionError("Ingreso fuera de horario");
        } catch (AutorizacionInvalidaException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        // Proveedor habitual: recurrente de hasta 30 dias
        Visitante proveedor = new Visitante("Jardines SRL", "555");
        gestor.autorizarVisita(titular, lote, proveedor, dia, dia.plusDays(29), ocho, veinte, true);
        try {
            gestor.autorizarVisita(titular, lote, proveedor, dia, dia.plusDays(30), ocho, veinte, true);
            throw new AssertionError("Recurrente de mas de 30 dias");
        } catch (AutorizacionInvalidaException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        System.out.println("PruebaAccesos: OK");
    }
}
