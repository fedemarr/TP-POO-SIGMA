import sigma.excepciones.LimiteReclamosException;
import sigma.excepciones.ReglaNegocioException;
import sigma.gestores.GestorReclamos;
import sigma.modelo.Administrador;
import sigma.modelo.EmpleadoMantenimiento;
import sigma.modelo.Especialidad;
import sigma.modelo.EstadoReclamo;
import sigma.modelo.Lote;
import sigma.modelo.Persona;
import sigma.modelo.Reclamo;
import sigma.modelo.Residencia;
import sigma.modelo.TipoReclamo;
import sigma.modelo.TipoResidente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Reglas del gestor de reclamos: quien puede reclamar, limite de 5 en curso y escalamiento. */
public class PruebaGestorReclamos {
    public static void main(String[] args) throws Exception {
        RepositorioNotificacionesMemoria notificaciones = new RepositorioNotificacionesMemoria();
        GestorReclamos gestor = new GestorReclamos(new RepositorioReclamosMemoria(), notificaciones);
        LocalDate enero = LocalDate.of(2026, 1, 1);
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 2, 10, 0);

        Persona ana = new Persona("Ana", "Perez", "123", "11", "a@b.com");
        Persona inquilino = new Persona("Leo", "Diaz", "789", "22", "l@b.com");
        Lote lote = new Lote(1, 500);
        lote.agregarResidencia(new Residencia(ana, TipoResidente.TITULAR, enero, null, true));
        lote.agregarResidencia(new Residencia(inquilino, TipoResidente.INQUILINO, enero, null, false));

        // Un inquilino sin autorizacion del titular no puede reclamar
        try {
            gestor.registrarReclamo(lote, inquilino, TipoReclamo.CONVIVENCIA, "Ruidos", fecha);
            throw new AssertionError("Reclamo de persona no autorizada");
        } catch (ReglaNegocioException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        // Maximo 5 reclamos EN_CURSO por empleado
        EmpleadoMantenimiento tecnico = new EmpleadoMantenimiento("M1", "Jorge", enero, Especialidad.PLOMERIA);
        for (int i = 1; i <= 6; i++) {
            Reclamo reclamo = gestor.registrarReclamo(lote, ana, TipoReclamo.MANTENIMIENTO, "Perdida " + i, fecha);
            gestor.asignarReclamo(reclamo, tecnico, fecha.plusMinutes(1));
            if (i <= 5) {
                gestor.cambiarEstado(reclamo, tecnico, EstadoReclamo.EN_CURSO, "Atendiendo", fecha.plusMinutes(2));
            } else {
                try {
                    gestor.cambiarEstado(reclamo, tecnico, EstadoReclamo.EN_CURSO, "Atendiendo", fecha.plusMinutes(2));
                    throw new AssertionError("Se permitio un sexto reclamo en curso");
                } catch (LimiteReclamosException esperada) {
                    System.out.println("  Rechazo esperado: " + esperada.getMessage());
                }
            }
        }

        // Escalamiento: un reclamo de seguridad sin atender por 24 horas avisa al administrador una sola vez
        Reclamo urgente = gestor.registrarReclamo(lote, ana, TipoReclamo.SEGURIDAD, "Camara rota", fecha);
        gestor.verificarPlazos(fecha.plusHours(23));
        Verificar.verdadero(notificaciones.listar().isEmpty(), "antes de 24 horas no se escala");
        gestor.verificarPlazos(fecha.plusHours(25));
        gestor.verificarPlazos(fecha.plusHours(26));
        Verificar.verdadero(notificaciones.listar().size() == 1, "un solo aviso de escalamiento");

        // Pendientes ordenados: el de seguridad primero
        List<Reclamo> pendientes = gestor.consultarPendientes(fecha.plusHours(26));
        Verificar.verdadero(pendientes.get(0) == urgente, "orden por prioridad");
        Verificar.verdadero(gestor.consultarSeguimiento(urgente, ana) == urgente, "seguimiento del vecino");

        // CU-04: el administrador tambien puede cambiar el estado
        Administrador admin = new Administrador("A1", "Laura", enero);
        EmpleadoMantenimiento otroTecnico = new EmpleadoMantenimiento("M2", "Rita", enero, Especialidad.PLOMERIA);
        Reclamo perdida = gestor.registrarReclamo(lote, ana, TipoReclamo.MANTENIMIENTO, "Canilla", fecha);
        gestor.asignarReclamo(perdida, otroTecnico, fecha.plusMinutes(1));
        gestor.cambiarEstado(perdida, admin, EstadoReclamo.EN_CURSO, "Lo pasa el administrador", fecha.plusMinutes(2));
        gestor.cambiarEstado(perdida, otroTecnico, EstadoReclamo.RESUELTO, "Arreglada", fecha.plusDays(1));

        // CU-06 A1: pasado el plazo de 5 dias, al intentar reabrir se cierra y se rechaza
        try {
            gestor.reabrir(perdida, ana, "Volvio a gotear", fecha.plusDays(8));
            throw new AssertionError("Se reabrio fuera de plazo");
        } catch (ReglaNegocioException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }
        Verificar.verdadero(perdida.getEstado() == EstadoReclamo.CERRADO, "fuera de plazo queda cerrado");

        System.out.println("PruebaGestorReclamos: OK");
    }
}
