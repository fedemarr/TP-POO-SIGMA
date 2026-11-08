package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RegistroIngreso implements Serializable {
    private LocalDateTime ingreso;
    private LocalDateTime egreso;
    private Autorizacion autorizacion;
    private Guardia guardia;

    public RegistroIngreso(Autorizacion autorizacion, Guardia guardia, LocalDateTime ingreso) {
        if (autorizacion == null || guardia == null || ingreso == null) {
            throw new IllegalArgumentException("Autorizacion, guardia y hora de ingreso son obligatorios");
        }
        this.autorizacion = autorizacion;
        this.guardia = guardia;
        this.ingreso = ingreso;
    }

    // egreso en null significa que la visita sigue dentro del barrio
    public boolean estaAbierto() {
        return egreso == null;
    }

    public void registrarEgreso(LocalDateTime fecha) {
        if (!estaAbierto() || fecha == null || fecha.isBefore(ingreso)) {
            throw new IllegalArgumentException("Egreso invalido");
        }
        egreso = fecha;
    }

    public String getDocumento() {
        return autorizacion.getVisitante().getDocumento();
    }

    public LocalDateTime getIngreso() { return ingreso; }
    public LocalDateTime getEgreso() { return egreso; }
    public Autorizacion getAutorizacion() { return autorizacion; }
    public Guardia getGuardia() { return guardia; }

    @Override
    public String toString() {
        String texto = autorizacion.getVisitante() + " | lote " + autorizacion.getLote().getNumero()
                + " | entro " + ingreso;
        if (estaAbierto()) {
            texto = texto + " | dentro del barrio";
        } else {
            texto = texto + " | salio " + egreso;
        }
        return texto;
    }
}
