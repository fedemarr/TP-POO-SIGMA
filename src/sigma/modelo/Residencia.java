package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class Residencia implements Serializable {
    private TipoResidente tipo;
    private LocalDate desde;
    private LocalDate hasta;
    private boolean autorizadaPorTitular;
    private Persona persona;

    public Residencia(Persona persona, TipoResidente tipo, LocalDate desde, LocalDate hasta, boolean autorizadaPorTitular) {
        if (persona == null || tipo == null || desde == null) {
            throw new IllegalArgumentException("Persona, tipo y fecha de inicio son obligatorios");
        }
        if (hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior al inicio");
        }
        this.persona = persona;
        this.tipo = tipo;
        this.desde = desde;
        this.hasta = hasta;
        this.autorizadaPorTitular = autorizadaPorTitular;
    }

    // hasta en null significa que la residencia sigue vigente
    public boolean estaVigente(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria");
        return !fecha.isBefore(desde) && (hasta == null || !fecha.isAfter(hasta));
    }

    public void autorizar() {
        autorizadaPorTitular = true;
    }

    public void finalizar(LocalDate fecha) {
        if (fecha == null || fecha.isBefore(desde)) {
            throw new IllegalArgumentException("Fecha de finalizacion invalida");
        }
        hasta = fecha;
    }

    public Persona getPersona() { return persona; }
    public TipoResidente getTipo() { return tipo; }
    public LocalDate getDesde() { return desde; }
    public boolean isAutorizadaPorTitular() { return autorizadaPorTitular; }

    @Override
    public String toString() {
        String texto = tipo + ": " + persona;
        if (tipo != TipoResidente.TITULAR && autorizadaPorTitular) {
            texto = texto + ", autorizado";
        }
        return texto;
    }
}
