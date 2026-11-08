package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CambioEstado implements Serializable {
    private LocalDateTime fecha;
    private EstadoReclamo estadoNuevo;
    private String observacion;

    public CambioEstado(LocalDateTime fecha, EstadoReclamo estadoNuevo, String observacion) {
        if (fecha == null || estadoNuevo == null) throw new IllegalArgumentException("Fecha y estado obligatorios");
        this.fecha = fecha;
        this.estadoNuevo = estadoNuevo;
        this.observacion = observacion;
    }

    public LocalDateTime getFecha() { return fecha; }
    public EstadoReclamo getEstadoNuevo() { return estadoNuevo; }
    public String getObservacion() { return observacion; }

    @Override
    public String toString() {
        String texto = fecha + " -> " + estadoNuevo;
        if (observacion != null) {
            texto = texto + " (" + observacion + ")";
        }
        return texto;
    }
}
