package sigma.gestores;

import sigma.excepciones.LimiteReclamosException;
import sigma.excepciones.ReglaNegocioException;
import sigma.excepciones.TransicionInvalidaException;
import sigma.modelo.Administrador;
import sigma.modelo.Empleado;
import sigma.modelo.EstadoReclamo;
import sigma.modelo.Lote;
import sigma.modelo.Notificacion;
import sigma.modelo.Persona;
import sigma.modelo.Reclamo;
import sigma.modelo.TipoReclamo;
import sigma.repositorios.RepositorioNotificaciones;
import sigma.repositorios.RepositorioReclamos;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Controlador (GRASP) de los casos de uso de reclamos. */
public class GestorReclamos {
    private static final int MAXIMO_EN_CURSO = 5;

    private final RepositorioReclamos reclamos;
    private final RepositorioNotificaciones notificaciones;

    public GestorReclamos(RepositorioReclamos reclamos, RepositorioNotificaciones notificaciones) {
        if (reclamos == null || notificaciones == null) throw new IllegalArgumentException("Repositorios obligatorios");
        this.reclamos = reclamos;
        this.notificaciones = notificaciones;
    }

    public Reclamo registrarReclamo(Lote lote, Persona solicitante, TipoReclamo tipo, String descripcion,
                                    LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (lote == null || solicitante == null || fecha == null) throw new IllegalArgumentException("Datos obligatorios");
        if (!lote.estaAutorizada(solicitante, fecha.toLocalDate())) {
            throw new ReglaNegocioException("Solo el titular del lote o una persona autorizada por el puede presentar reclamos");
        }
        Reclamo reclamo = new Reclamo(siguienteNumero(), fecha, tipo, descripcion);
        reclamo.vincular(lote, solicitante);
        reclamos.guardar(reclamo);
        return reclamo;
    }

    public void asignarReclamo(Reclamo reclamo, Empleado empleado, LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (reclamo == null || empleado == null) throw new IllegalArgumentException("Datos obligatorios");
        reclamo.asignar(empleado, fecha);
        reclamos.guardar(reclamo);
    }

    public void cambiarEstado(Reclamo reclamo, Empleado empleado, EstadoReclamo nuevo, String observacion,
                              LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (reclamo == null || empleado == null) throw new IllegalArgumentException("Datos obligatorios");
        // CU-04: puede cambiar el estado el responsable del reclamo o el administrador
        boolean esAdministrador = empleado instanceof Administrador;
        if (reclamo.getResponsable() != empleado && !esAdministrador) {
            throw new ReglaNegocioException("Solo el responsable asignado o el administrador pueden cambiar el estado");
        }
        // El limite de 5 se controla antes de que el reclamo entre EN_CURSO (se cuenta al responsable)
        if (nuevo == EstadoReclamo.EN_CURSO && reclamo.getResponsable() != null) {
            verificarLimite(reclamo.getResponsable());
        }
        reclamo.cambiarEstado(nuevo, observacion, fecha);
        reclamos.guardar(reclamo);
    }

    public void reabrir(Reclamo reclamo, Persona vecino, String motivo, LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (reclamo == null || fecha == null) throw new IllegalArgumentException("Reclamo y fecha obligatorios");
        if (!reclamo.perteneceA(vecino)) {
            throw new ReglaNegocioException("Solo el vecino que presento el reclamo puede reabrirlo");
        }
        // CU-06 A1: si el plazo ya vencio, el reclamo se cierra y se rechaza la reapertura
        EstadoReclamo anterior = reclamo.getEstado();
        reclamo.cerrarPorPlazo(fecha);
        if (reclamo.getEstado() != anterior) {
            reclamos.guardar(reclamo);
            throw new TransicionInvalidaException("Vencio el plazo de 5 dias para reabrir: el reclamo se cerro");
        }
        // CU-06 A2: si el responsable ya tiene 5 en curso, no se reabre y se avisa al administrador
        Empleado responsable = reclamo.getResponsable();
        if (responsable != null && reclamos.contarEnCurso(responsable) >= MAXIMO_EN_CURSO) {
            Notificacion aviso = new Notificacion(fecha, "No se pudo reabrir un reclamo: el responsable tiene "
                    + MAXIMO_EN_CURSO + " reclamos en curso", "Administrador", "Reclamo #" + reclamo.getNumero(),
                    "LIMITE-REAPERTURA-" + reclamo.getNumero() + "-" + fecha);
            notificaciones.guardarSiNoExiste(aviso);
            verificarLimite(responsable);
        }
        reclamo.reabrir(motivo, fecha);
        reclamos.guardar(reclamo);
    }

    private void verificarLimite(Empleado empleado) throws LimiteReclamosException {
        if (reclamos.contarEnCurso(empleado) >= MAXIMO_EN_CURSO) {
            throw new LimiteReclamosException(empleado.getNombre() + " ya tiene " + MAXIMO_EN_CURSO + " reclamos en curso");
        }
    }

    public void agregarComentario(Reclamo reclamo, String autor, String texto, LocalDateTime fecha) throws IOException {
        if (reclamo == null) throw new IllegalArgumentException("El reclamo es obligatorio");
        reclamo.agregarComentario(autor, texto, fecha);
        reclamos.guardar(reclamo);
    }

    public Reclamo consultarSeguimiento(Reclamo reclamo, Persona vecino) throws ReglaNegocioException {
        if (reclamo == null) throw new IllegalArgumentException("El reclamo es obligatorio");
        if (!reclamo.perteneceA(vecino)) {
            throw new ReglaNegocioException("Solo el vecino que presento el reclamo puede consultarlo");
        }
        return reclamo;
    }

    /** Reclamos no cerrados, ordenados por prioridad y fecha (orden natural, Comparable). */
    public List<Reclamo> consultarPendientes(LocalDateTime ahora) {
        if (ahora == null) throw new IllegalArgumentException("La fecha es obligatoria");
        List<Reclamo> pendientes = new ArrayList<>();
        for (Reclamo reclamo : reclamos.listar()) {
            if (reclamo.estaPendiente()) {
                pendientes.add(reclamo);
            }
        }
        Collections.sort(pendientes);
        return pendientes;
    }

    public Reclamo buscarReclamo(int numero) {
        for (Reclamo reclamo : reclamos.listar()) {
            if (reclamo.getNumero() == numero) return reclamo;
        }
        return null;
    }

    /** Escala los reclamos urgentes sin atender y cierra los resueltos con plazo vencido. */
    public void verificarPlazos(LocalDateTime ahora) throws ReglaNegocioException, IOException {
        if (ahora == null) throw new IllegalArgumentException("La fecha es obligatoria");
        for (Reclamo reclamo : reclamos.listar()) {
            if (reclamo.requiereEscalamiento(ahora)) {
                Notificacion aviso = new Notificacion(ahora, "Reclamo de prioridad alta sin atender por 24 horas",
                        "Administrador", "Reclamo #" + reclamo.getNumero(), "ESCALAMIENTO-RECLAMO-" + reclamo.getNumero());
                notificaciones.guardarSiNoExiste(aviso);
            }
            EstadoReclamo anterior = reclamo.getEstado();
            reclamo.cerrarPorPlazo(ahora);
            if (reclamo.getEstado() != anterior) {
                reclamos.guardar(reclamo);
            }
        }
    }

    private int siguienteNumero() {
        int mayor = 0;
        for (Reclamo reclamo : reclamos.listar()) {
            if (reclamo.getNumero() > mayor) {
                mayor = reclamo.getNumero();
            }
        }
        return mayor + 1;
    }
}
