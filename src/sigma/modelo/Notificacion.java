package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Aviso formal del sistema. La clave unica evita repetir el mismo aviso. */
public class Notificacion implements Serializable {
    private LocalDateTime fecha;
    private String motivo;
    private String destinatario;
    private String referencia;
    private boolean leida;
    private String claveUnica;

    public Notificacion(LocalDateTime fecha, String motivo, String destinatario, String referencia, String claveUnica) {
        if (fecha == null || motivo == null || destinatario == null || claveUnica == null) {
            throw new IllegalArgumentException("Faltan datos de la notificacion");
        }
        this.fecha = fecha;
        this.motivo = motivo;
        this.destinatario = destinatario;
        this.referencia = referencia;
        this.claveUnica = claveUnica;
    }

    public void marcarLeida() {
        leida = true;
    }

    public LocalDateTime getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public String getDestinatario() { return destinatario; }
    public String getReferencia() { return referencia; }
    public boolean isLeida() { return leida; }
    public String getClaveUnica() { return claveUnica; }

    @Override
    public String toString() {
        return fecha + " | Para: " + destinatario + " | " + motivo + " | " + referencia;
    }
}
