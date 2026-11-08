package sigma.modelo;

import sigma.excepciones.TransicionInvalidaException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reclamo implements Comparable<Reclamo>, Serializable {
    private static final int DIAS_PARA_REABRIR = 5;
    private static final int HORAS_PARA_ESCALAR = 24;

    private int numero;
    private LocalDateTime fecha;
    private TipoReclamo tipo;
    private String descripcion;
    private PrioridadReclamo prioridad;
    private EstadoReclamo estado;
    private Empleado responsable;
    private Lote lote;
    private Persona solicitante;
    private LocalDateTime fechaResolucion;
    private List<CambioEstado> historial = new ArrayList<>();
    private List<Comentario> comentarios = new ArrayList<>();

    public Reclamo(int numero, LocalDateTime fecha, TipoReclamo tipo, String descripcion) {
        if (numero <= 0 || fecha == null || tipo == null || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Datos de reclamo invalidos");
        }
        this.numero = numero;
        this.fecha = fecha;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.prioridad = calcularPrioridad(tipo);
        this.estado = EstadoReclamo.ABIERTO;
        // Creador: el reclamo crea su propio historial de cambios
        historial.add(new CambioEstado(fecha, estado, "Registro del reclamo"));
    }

    private static PrioridadReclamo calcularPrioridad(TipoReclamo tipo) {
        switch (tipo) {
            case SEGURIDAD:
                return PrioridadReclamo.ALTA;
            case MANTENIMIENTO:
                return PrioridadReclamo.MEDIA;
            default:
                return PrioridadReclamo.BAJA;
        }
    }

    public void vincular(Lote lote, Persona solicitante) {
        if (lote == null || solicitante == null) throw new IllegalArgumentException("Lote y solicitante obligatorios");
        if (this.lote != null) throw new IllegalStateException("El reclamo ya tiene lote y solicitante");
        this.lote = lote;
        this.solicitante = solicitante;
    }

    public void asignar(Empleado responsable, LocalDateTime fecha) throws TransicionInvalidaException {
        if (responsable == null) throw new IllegalArgumentException("El responsable es obligatorio");
        if (estado != EstadoReclamo.ABIERTO) {
            throw new TransicionInvalidaException("Solo se puede asignar un reclamo ABIERTO");
        }
        cambiarEstado(EstadoReclamo.ASIGNADO, "Asignado a " + responsable.getLegajo(), fecha);
        this.responsable = responsable;
    }

    /** Solo se puede avanzar al estado siguiente; cada cambio queda en el historial. */
    public void cambiarEstado(EstadoReclamo nuevo, String observacion, LocalDateTime fecha) throws TransicionInvalidaException {
        if (nuevo == null || fecha == null) throw new IllegalArgumentException("Estado y fecha obligatorios");
        if (fecha.isBefore(ultimoCambio().getFecha())) {
            throw new IllegalArgumentException("La fecha es anterior al ultimo cambio de estado");
        }
        if (!esEstadoSiguiente(nuevo)) {
            throw new TransicionInvalidaException("No se puede pasar de " + estado + " a " + nuevo);
        }
        historial.add(new CambioEstado(fecha, nuevo, observacion));
        estado = nuevo;
        if (nuevo == EstadoReclamo.RESUELTO) {
            fechaResolucion = fecha;
        }
    }

    private boolean esEstadoSiguiente(EstadoReclamo nuevo) {
        switch (estado) {
            case ABIERTO:
                return nuevo == EstadoReclamo.ASIGNADO;
            case ASIGNADO:
                return nuevo == EstadoReclamo.EN_CURSO;
            case EN_CURSO:
                return nuevo == EstadoReclamo.RESUELTO;
            case RESUELTO:
                return nuevo == EstadoReclamo.CERRADO;
            default:
                return false;
        }
    }

    /** El vecino puede reabrir un reclamo resuelto dentro de los 5 dias: vuelve a EN_CURSO. */
    public void reabrir(String motivo, LocalDateTime fecha) throws TransicionInvalidaException {
        if (motivo == null || motivo.isBlank() || fecha == null) throw new IllegalArgumentException("Motivo y fecha obligatorios");
        if (estado != EstadoReclamo.RESUELTO) {
            throw new TransicionInvalidaException("Solo se puede reabrir un reclamo RESUELTO");
        }
        if (fecha.isBefore(fechaResolucion) || fecha.isAfter(fechaResolucion.plusDays(DIAS_PARA_REABRIR))) {
            throw new TransicionInvalidaException("Vencio el plazo de " + DIAS_PARA_REABRIR + " dias para reabrir");
        }
        historial.add(new CambioEstado(fecha, EstadoReclamo.EN_CURSO, "Reabierto: " + motivo));
        estado = EstadoReclamo.EN_CURSO;
        fechaResolucion = null;
    }

    public void agregarComentario(String autor, String texto, LocalDateTime fecha) {
        if (fecha == null || fecha.isBefore(this.fecha)) throw new IllegalArgumentException("Fecha invalida");
        comentarios.add(new Comentario(autor, fecha, texto));
    }

    /** Un reclamo de prioridad alta que no paso a EN_CURSO en 24 horas se escala al administrador. */
    public boolean requiereEscalamiento(LocalDateTime ahora) {
        if (ahora == null) throw new IllegalArgumentException("La fecha es obligatoria");
        boolean sinAtender = estado == EstadoReclamo.ABIERTO || estado == EstadoReclamo.ASIGNADO;
        return prioridad == PrioridadReclamo.ALTA && sinAtender && !ahora.isBefore(fecha.plusHours(HORAS_PARA_ESCALAR));
    }

    /** Pasados 5 dias desde que se resolvio sin que el vecino lo reabra, se cierra. */
    public void cerrarPorPlazo(LocalDateTime ahora) throws TransicionInvalidaException {
        if (ahora == null) throw new IllegalArgumentException("La fecha es obligatoria");
        if (estado == EstadoReclamo.RESUELTO && ahora.isAfter(fechaResolucion.plusDays(DIAS_PARA_REABRIR))) {
            cambiarEstado(EstadoReclamo.CERRADO, "Cierre por vencimiento del plazo", ahora);
        }
    }

    public boolean perteneceA(Persona persona) {
        return persona != null && persona.equals(solicitante);
    }

    // CU-07: son pendientes todos los reclamos que todavia no se cerraron
    public boolean estaPendiente() {
        return estado != EstadoReclamo.CERRADO;
    }

    public boolean estaEnCursoDe(Empleado empleado) {
        return estado == EstadoReclamo.EN_CURSO && responsable == empleado;
    }

    private CambioEstado ultimoCambio() {
        return historial.get(historial.size() - 1);
    }

    // Orden natural: primero la prioridad mas alta, luego el mas antiguo
    @Override
    public int compareTo(Reclamo otro) {
        int porPrioridad = Integer.compare(valorPrioridad(), otro.valorPrioridad());
        if (porPrioridad != 0) return porPrioridad;
        int porFecha = fecha.compareTo(otro.fecha);
        if (porFecha != 0) return porFecha;
        return Integer.compare(numero, otro.numero);
    }

    // 1 es la mas urgente, para ordenar de mayor a menor prioridad
    private int valorPrioridad() {
        switch (prioridad) {
            case ALTA:
                return 1;
            case MEDIA:
                return 2;
            default:
                return 3;
        }
    }

    public int getNumero() { return numero; }
    public LocalDateTime getFecha() { return fecha; }
    public TipoReclamo getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public PrioridadReclamo getPrioridad() { return prioridad; }
    public EstadoReclamo getEstado() { return estado; }
    public Empleado getResponsable() { return responsable; }
    public Lote getLote() { return lote; }
    public Persona getSolicitante() { return solicitante; }
    public List<CambioEstado> getHistorial() { return new ArrayList<>(historial); }
    public List<Comentario> getComentarios() { return new ArrayList<>(comentarios); }

    @Override
    public String toString() {
        String texto = "Reclamo #" + numero + " | " + tipo + " | prioridad " + prioridad + " | " + estado;
        if (lote != null) {
            texto = texto + " | lote " + lote.getNumero();
        }
        if (responsable == null) {
            texto = texto + " | sin asignar";
        } else {
            texto = texto + " | " + responsable.getNombre();
        }
        return texto + " | " + descripcion;
    }
}
