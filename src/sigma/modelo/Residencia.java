package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Residencia implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Persona persona;
    private final TipoResidente tipo;
    private final LocalDate desde;
    private LocalDate hasta;
    private boolean autorizadaPorTitular;

    public Residencia(Persona persona, TipoResidente tipo, LocalDate desde) {
        this.persona = Objects.requireNonNull(persona, "persona");
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.desde = Objects.requireNonNull(desde, "desde");
        this.autorizadaPorTitular = tipo == TipoResidente.TITULAR;
    }

    public boolean estaVigente(LocalDate fecha) {
        Objects.requireNonNull(fecha, "fecha");
        return !fecha.isBefore(desde) && (hasta == null || !fecha.isAfter(hasta));
    }

    public void autorizar() { autorizadaPorTitular = true; }

    public void finalizar(LocalDate fecha) {
        Objects.requireNonNull(fecha, "fecha");
        if (fecha.isBefore(desde)) {
            throw new IllegalArgumentException("La residencia no puede finalizar antes de comenzar.");
        }
        if (hasta != null) {
            throw new IllegalStateException("La residencia ya tiene fecha de finalizacion.");
        }
        hasta = fecha;
    }

    public Persona getPersona() { return persona; }
    public TipoResidente getTipo() { return tipo; }
    public boolean isAutorizadaPorTitular() { return autorizadaPorTitular; }
}
