import sigma.excepciones.TransicionInvalidaException;
import sigma.modelo.EstadoReclamo;
import sigma.modelo.Guardia;
import sigma.modelo.PrioridadReclamo;
import sigma.modelo.PuestoGuardia;
import sigma.modelo.Reclamo;
import sigma.modelo.TipoReclamo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Estados, historial, reapertura, cierre por plazo y orden natural de Reclamo. */
public class PruebaReclamos {
    public static void main(String[] args) throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 1, 9, 0);
        Reclamo reclamo = new Reclamo(1, inicio, TipoReclamo.SEGURIDAD, "Porton abierto");
        Verificar.verdadero(reclamo.getPrioridad() == PrioridadReclamo.ALTA, "seguridad es prioridad alta");
        Verificar.verdadero(reclamo.getHistorial().size() == 1, "el alta queda en el historial");
        Verificar.verdadero(reclamo.requiereEscalamiento(inicio.plusHours(24)), "24 horas sin atender se escala");

        // Solo se puede avanzar al estado siguiente
        try {
            reclamo.cambiarEstado(EstadoReclamo.RESUELTO, "Salteo estados", inicio.plusHours(1));
            throw new AssertionError("Se salteo un estado");
        } catch (TransicionInvalidaException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        Guardia guardia = new Guardia("G1", "Mario", LocalDate.of(2026, 1, 1), "Manana", PuestoGuardia.PORTERIA);
        reclamo.asignar(guardia, inicio.plusHours(1));
        reclamo.cambiarEstado(EstadoReclamo.EN_CURSO, "Atendiendo", inicio.plusHours(2));
        Verificar.verdadero(!reclamo.requiereEscalamiento(inicio.plusHours(30)), "ya esta en curso: no se escala");
        reclamo.agregarComentario("123", "Gracias", inicio.plusHours(3));
        reclamo.cambiarEstado(EstadoReclamo.RESUELTO, "Resuelto", inicio.plusDays(1));

        // Reapertura dentro de los 5 dias
        reclamo.reabrir("Sigue abierto", inicio.plusDays(2));
        Verificar.verdadero(reclamo.getEstado() == EstadoReclamo.EN_CURSO, "reabierto vuelve a en curso");
        reclamo.cambiarEstado(EstadoReclamo.RESUELTO, "Resuelto de nuevo", inicio.plusDays(3));

        // Pasado el plazo ya no se puede reabrir, y se cierra
        try {
            reclamo.reabrir("Tarde", inicio.plusDays(9));
            throw new AssertionError("Se reabrio fuera de plazo");
        } catch (TransicionInvalidaException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }
        reclamo.cerrarPorPlazo(inicio.plusDays(9));
        Verificar.verdadero(reclamo.getEstado() == EstadoReclamo.CERRADO, "cerrado por plazo");
        Verificar.verdadero(reclamo.getHistorial().size() == 7, "historial completo");
        Verificar.verdadero(reclamo.getComentarios().size() == 1, "comentarios");

        // Comparable: primero los de mayor prioridad
        Reclamo convivencia = new Reclamo(2, inicio.minusDays(1), TipoReclamo.CONVIVENCIA, "Ruidos");
        Verificar.verdadero(reclamo.compareTo(convivencia) < 0, "alta antes que baja aunque sea mas nuevo");

        System.out.println("PruebaReclamos: OK");
    }
}
