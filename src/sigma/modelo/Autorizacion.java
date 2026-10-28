package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import sigma.excepciones.AutorizacionInvalidaException;

public class Autorizacion implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private final Persona otorgante;
    private final Lote lote;
    private final Visitante visitante;
    private final LocalDate desde;
    private final LocalDate hasta;
    private final LocalTime horaDesde;
    private final LocalTime horaHasta;
    private final boolean recurrente;

    public Autorizacion(Persona otorgante, Lote lote, Visitante visitante, LocalDate desde,
            LocalDate hasta, LocalTime horaDesde, LocalTime horaHasta, boolean recurrente)
            throws AutorizacionInvalidaException {
        this.otorgante = Objects.requireNonNull(otorgante, "otorgante");
        this.lote = Objects.requireNonNull(lote, "lote");
        this.visitante = Objects.requireNonNull(visitante, "visitante");
        this.desde = Objects.requireNonNull(desde, "desde");
        this.hasta = Objects.requireNonNull(hasta, "hasta");
        this.horaDesde = Objects.requireNonNull(horaDesde, "horaDesde");
        this.horaHasta = Objects.requireNonNull(horaHasta, "horaHasta");
        this.recurrente = recurrente;
        validarDuracion();
    }

    public final void validarDuracion() throws AutorizacionInvalidaException {
        if (hasta.isBefore(desde) || !horaHasta.isAfter(horaDesde)) {
            throw new AutorizacionInvalidaException("Fechas invertidas o franja horaria invalida.");
        }
        if (!recurrente && !desde.equals(hasta)) {
            throw new AutorizacionInvalidaException("Una visita no recurrente debe ser del mismo dia.");
        }
        // Se cuentan ambos extremos: del dia 1 al dia 30 son treinta dias.
        if (recurrente && ChronoUnit.DAYS.between(desde, hasta) + 1 > 30) {
            throw new AutorizacionInvalidaException("La recurrencia no puede superar treinta dias.");
        }
    }

    public boolean estaVigente(LocalDateTime fecha) {
        if (fecha == null) { return false; }
        return !fecha.toLocalDate().isBefore(desde) && !fecha.toLocalDate().isAfter(hasta)
                && !fecha.toLocalTime().isBefore(horaDesde) && !fecha.toLocalTime().isAfter(horaHasta);
    }

    public boolean mismaVigencia(Autorizacion otra) {
        return lote.getNumero() == otra.lote.getNumero()
                && visitante.getDocumento().equals(otra.visitante.getDocumento())
                && otorgante.equals(otra.otorgante) && desde.equals(otra.desde) && hasta.equals(otra.hasta)
                && horaDesde.equals(otra.horaDesde) && horaHasta.equals(otra.horaHasta)
                && recurrente == otra.recurrente;
    }

    public String getId() { return id; }
    public Persona getOtorgante() { return otorgante; }
    public Lote getLote() { return lote; }
    public Visitante getVisitante() { return visitante; }
    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public LocalTime getHoraDesde() { return horaDesde; }
    public LocalTime getHoraHasta() { return horaHasta; }
    public boolean isRecurrente() { return recurrente; }
}
