import sigma.excepciones.ReglaNegocioException;
import sigma.gestores.GestorMantenimiento;
import sigma.modelo.Administrador;
import sigma.modelo.Camara;
import sigma.modelo.EmpleadoMantenimiento;
import sigma.modelo.Especialidad;
import sigma.modelo.EstadoEquipo;
import sigma.modelo.FallaEquipo;
import sigma.modelo.Luminaria;
import sigma.modelo.OrdenMantenimiento;
import sigma.modelo.PrioridadOrden;
import sigma.modelo.Sector;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Fallas, ordenes, prioridades por tipo de equipo, cierre y aviso de camara caida. */
public class PruebaMantenimiento {
    public static void main(String[] args) throws Exception {
        RepositorioEquiposMemoria repositorio = new RepositorioEquiposMemoria();
        RepositorioNotificacionesMemoria notificaciones = new RepositorioNotificacionesMemoria();
        GestorMantenimiento gestor = new GestorMantenimiento(repositorio, notificaciones);
        LocalDate instalacion = LocalDate.of(2025, 1, 1);
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 2, 10, 0);
        Administrador admin = new Administrador("A1", "Laura", instalacion);
        EmpleadoMantenimiento electricista = new EmpleadoMantenimiento("M1", "Jorge", instalacion, Especialidad.ELECTRICIDAD);
        EmpleadoMantenimiento plomero = new EmpleadoMantenimiento("M2", "Rita", instalacion, Especialidad.PLOMERIA);

        // Camara: siempre prioridad alta
        Camara camara = new Camara("C1", new Sector("Acceso"), instalacion, "HD", 7);
        gestor.registrarEquipo(camara);
        FallaEquipo fallaCamara = gestor.registrarFalla(camara, "Guardia", "Sin imagen", ahora);
        OrdenMantenimiento ordenCamara = gestor.generarOrden(admin, fallaCamara, null, ahora);
        Verificar.verdadero(ordenCamara.getPrioridad() == PrioridadOrden.ALTA, "camara con prioridad alta");

        // Un segundo aviso de la misma falla se agrega a la orden abierta (SUP-05)
        FallaEquipo repetida = gestor.registrarFalla(camara, "Vecino", "Sigue sin imagen", ahora.plusHours(1));
        Verificar.verdadero(repetida == fallaCamara, "no se crea otra falla");
        Verificar.verdadero(ordenCamara.getObservaciones().size() == 1, "el aviso se suma a la orden");

        // Mas de 48 horas sin funcionar: aviso al administrador, una sola vez
        gestor.verificarPlazos(ahora.plusHours(47));
        Verificar.verdadero(notificaciones.listar().isEmpty(), "antes de 48 horas no avisa");
        gestor.verificarPlazos(ahora.plusHours(49));
        gestor.verificarPlazos(ahora.plusHours(50));
        Verificar.verdadero(notificaciones.listar().size() == 1, "un solo aviso de camara");

        // La reparacion la hace un electricista (SUP-06)
        try {
            gestor.asignarTecnico(ordenCamara, plomero);
            throw new AssertionError("Se asigno un plomero a una camara");
        } catch (ReglaNegocioException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }
        gestor.asignarTecnico(ordenCamara, electricista);
        gestor.iniciarReparacion(ordenCamara);
        Verificar.verdadero(camara.getEstado() == EstadoEquipo.EN_REPARACION, "en reparacion");

        // No se cierra sin descripcion de la reparacion
        try {
            gestor.cerrarOrden(ordenCamara, electricista, "", ahora.plusDays(3));
            throw new AssertionError("Se cerro sin descripcion");
        } catch (ReglaNegocioException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }
        gestor.cerrarOrden(ordenCamara, electricista, "Cambio de fuente", ahora.plusDays(3));
        Verificar.verdadero(camara.getEstado() == EstadoEquipo.OPERATIVO, "reparada vuelve a operativa");

        // Luminarias: normal, pero con mas de 3 falladas en el sector pasan todas a alta
        Sector calle = new Sector("Calle 1");
        OrdenMantenimiento primera = null;
        OrdenMantenimiento cuarta = null;
        for (int i = 1; i <= 4; i++) {
            Luminaria luminaria = new Luminaria("L" + i, calle, instalacion, "LED", 60);
            gestor.registrarEquipo(luminaria);
            FallaEquipo falla = gestor.registrarFalla(luminaria, "Vecino", "No enciende", ahora);
            OrdenMantenimiento orden = gestor.generarOrden(admin, falla, electricista, ahora);
            if (i == 1) primera = orden;
            if (i == 4) cuarta = orden;
        }
        Verificar.verdadero(cuarta.getPrioridad() == PrioridadOrden.ALTA, "cuarta luminaria fallada: alta");
        Verificar.verdadero(primera.getPrioridad() == PrioridadOrden.ALTA, "se elevan las ordenes abiertas del sector");

        // Si no se puede reparar, queda fuera de servicio
        gestor.cerrarOrdenSinReparacion(primera, electricista, "Poste danado", ahora.plusDays(1));
        Verificar.verdadero(primera.getEquipo().getEstado() == EstadoEquipo.FUERA_DE_SERVICIO, "fuera de servicio");

        System.out.println("PruebaMantenimiento: OK");
    }
}
