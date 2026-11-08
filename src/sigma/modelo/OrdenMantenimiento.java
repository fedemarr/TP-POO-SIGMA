package sigma.modelo;

import sigma.excepciones.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdenMantenimiento implements Serializable {
    private int numero;
    private LocalDateTime apertura;
    private PrioridadOrden prioridad;
    private String descripcionTrabajo;
    private LocalDateTime cierre;
    private String descripcionReparacion;
    private Equipo equipo;
    private FallaEquipo falla;
    private EmpleadoMantenimiento tecnico;
    private List<String> observaciones = new ArrayList<>();

    // El tecnico puede ser null: la orden queda abierta esperando un tecnico disponible
    public OrdenMantenimiento(int numero, LocalDateTime apertura, PrioridadOrden prioridad,
                              String descripcionTrabajo, Equipo equipo, FallaEquipo falla,
                              EmpleadoMantenimiento tecnico) {
        if (numero <= 0 || apertura == null || prioridad == null || descripcionTrabajo == null
                || descripcionTrabajo.isBlank() || equipo == null || falla == null) {
            throw new IllegalArgumentException("Datos de orden invalidos");
        }
        this.numero = numero;
        this.apertura = apertura;
        this.prioridad = prioridad;
        this.descripcionTrabajo = descripcionTrabajo;
        this.equipo = equipo;
        this.falla = falla;
        this.tecnico = tecnico;
    }

    public void asignarTecnico(EmpleadoMantenimiento tecnico) throws ReglaNegocioException {
        if (tecnico == null) throw new IllegalArgumentException("El tecnico es obligatorio");
        if (!estaAbierta()) throw new ReglaNegocioException("La orden " + numero + " ya esta cerrada");
        this.tecnico = tecnico;
    }

    public void iniciarReparacion() throws ReglaNegocioException {
        if (!estaAbierta()) throw new ReglaNegocioException("La orden " + numero + " ya esta cerrada");
        if (tecnico == null) throw new ReglaNegocioException("La orden " + numero + " no tiene tecnico asignado");
        equipo.cambiarEstado(EstadoEquipo.EN_REPARACION);
    }

    /** Cierre con reparacion: el equipo vuelve a estar operativo. */
    public void cerrar(LocalDateTime fecha, EmpleadoMantenimiento tecnico, String descripcion) throws ReglaNegocioException {
        registrarCierre(fecha, tecnico, descripcion);
        equipo.cambiarEstado(EstadoEquipo.OPERATIVO);
    }

    /** Cierre cuando el equipo no se pudo reparar: queda fuera de servicio. */
    public void cerrarSinReparacion(LocalDateTime fecha, EmpleadoMantenimiento tecnico, String descripcion) throws ReglaNegocioException {
        registrarCierre(fecha, tecnico, descripcion);
        equipo.cambiarEstado(EstadoEquipo.FUERA_DE_SERVICIO);
    }

    // Una orden no se puede cerrar sin fecha, tecnico y descripcion de la reparacion
    private void registrarCierre(LocalDateTime fecha, EmpleadoMantenimiento tecnico, String descripcion) throws ReglaNegocioException {
        if (!estaAbierta()) {
            throw new ReglaNegocioException("La orden " + numero + " ya esta cerrada");
        }
        if (fecha == null || tecnico == null || descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException("Para cerrar la orden se necesita fecha, tecnico y descripcion de la reparacion");
        }
        if (fecha.isBefore(apertura)) {
            throw new ReglaNegocioException("La fecha de cierre no puede ser anterior a la apertura");
        }
        if (this.tecnico != null && this.tecnico != tecnico) {
            throw new ReglaNegocioException("La orden esta asignada a otro tecnico");
        }
        this.tecnico = tecnico;
        this.cierre = fecha;
        this.descripcionReparacion = descripcion;
    }

    public void elevarPrioridad() {
        prioridad = PrioridadOrden.ALTA;
    }

    public void agregarObservacion(String texto) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("La observacion es obligatoria");
        observaciones.add(texto);
    }

    public boolean estaAbierta() {
        return cierre == null;
    }

    public int getNumero() { return numero; }
    public LocalDateTime getApertura() { return apertura; }
    public PrioridadOrden getPrioridad() { return prioridad; }
    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    public LocalDateTime getCierre() { return cierre; }
    public String getDescripcionReparacion() { return descripcionReparacion; }
    public Equipo getEquipo() { return equipo; }
    public FallaEquipo getFalla() { return falla; }
    public EmpleadoMantenimiento getTecnico() { return tecnico; }
    public List<String> getObservaciones() { return new ArrayList<>(observaciones); }

    @Override
    public String toString() {
        String texto = "Orden " + numero + " | " + equipo.getTipo() + " " + equipo.getCodigo() + " | prioridad " + prioridad;
        if (estaAbierta()) {
            texto = texto + " | ABIERTA";
        } else {
            texto = texto + " | CERRADA " + cierre;
        }
        if (tecnico == null) {
            texto = texto + " | sin tecnico";
        } else {
            texto = texto + " | " + tecnico.getNombre();
        }
        return texto + " | " + descripcionTrabajo;
    }
}
