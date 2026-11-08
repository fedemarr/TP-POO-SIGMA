package sigma.modelo;

import sigma.excepciones.AutorizacionInvalidaException;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Autorizacion implements Serializable {
    private static final int MAXIMO_DIAS_RECURRENTE = 30;

    private LocalDate desde;
    private LocalDate hasta;
    private LocalTime horaDesde;
    private LocalTime horaHasta;
    private boolean recurrente;
    private Persona otorgante;
    private Lote lote;
    private Visitante visitante;

    public Autorizacion(Persona otorgante, Lote lote, Visitante visitante, LocalDate desde, LocalDate hasta,
                        LocalTime horaDesde, LocalTime horaHasta, boolean recurrente) throws AutorizacionInvalidaException {
        if (otorgante == null || lote == null || visitante == null || desde == null || hasta == null
                || horaDesde == null || horaHasta == null) {
            throw new IllegalArgumentException("Faltan datos de la autorizacion");
        }
        this.otorgante = otorgante;
        this.lote = lote;
        this.visitante = visitante;
        this.desde = desde;
        this.hasta = hasta;
        this.horaDesde = horaDesde;
        this.horaHasta = horaHasta;
        this.recurrente = recurrente;
        validarDuracion();
    }

    /**
     * Una visita comun es para un solo dia. Solo los proveedores habituales
     * tienen autorizacion recurrente, de hasta 30 dias.
     * La franja horaria empieza y termina el mismo dia (supuesto SUP-04).
     */
    public void validarDuracion() throws AutorizacionInvalidaException {
        if (hasta.isBefore(desde)) {
            throw new AutorizacionInvalidaException("La fecha final es anterior a la inicial");
        }
        if (!horaHasta.isAfter(horaDesde)) {
            throw new AutorizacionInvalidaException("La franja horaria debe empezar y terminar el mismo dia");
        }
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (!recurrente && dias > 1) {
            throw new AutorizacionInvalidaException("Una visita comun se autoriza para un solo dia");
        }
        if (recurrente && dias > MAXIMO_DIAS_RECURRENTE) {
            throw new AutorizacionInvalidaException("Una autorizacion recurrente no puede superar los "
                    + MAXIMO_DIAS_RECURRENTE + " dias");
        }
    }

    public boolean estaVigente(LocalDateTime fecha) {
        if (fecha == null) return false;
        LocalDate dia = fecha.toLocalDate();
        LocalTime hora = fecha.toLocalTime();
        boolean dentroDeLasFechas = !dia.isBefore(desde) && !dia.isAfter(hasta);
        boolean dentroDeLaFranja = !hora.isBefore(horaDesde) && !hora.isAfter(horaHasta);
        return dentroDeLasFechas && dentroDeLaFranja;
    }

    /** Misma persona, mismo lote, mismas fechas y misma franja horaria. */
    public boolean esIgualA(Autorizacion otra) {
        return otra != null
                && visitante.getDocumento().equals(otra.visitante.getDocumento())
                && lote.getNumero() == otra.lote.getNumero()
                && desde.equals(otra.desde) && hasta.equals(otra.hasta)
                && horaDesde.equals(otra.horaDesde) && horaHasta.equals(otra.horaHasta);
    }

    public Visitante getVisitante() { return visitante; }
    public Lote getLote() { return lote; }
    public Persona getOtorgante() { return otorgante; }
    public boolean isRecurrente() { return recurrente; }

    @Override
    public String toString() {
        String texto = visitante + " -> lote " + lote.getNumero() + " | " + desde + " a " + hasta
                + " | " + horaDesde + "-" + horaHasta;
        if (recurrente) {
            texto = texto + " | recurrente";
        }
        return texto;
    }
}
