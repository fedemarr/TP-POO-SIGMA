package sigma.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Parte de un pago aplicada a una expensa: primero intereses, despues capital. */
public class Imputacion implements Serializable {
    private LocalDate fecha;
    private BigDecimal importeInteres;
    private BigDecimal importeCapital;
    private Expensa expensa;

    public Imputacion(Expensa expensa, LocalDate fecha, BigDecimal interes, BigDecimal capital) {
        if (expensa == null || fecha == null || interes == null || capital == null) {
            throw new IllegalArgumentException("Datos obligatorios");
        }
        this.expensa = expensa;
        this.fecha = fecha;
        this.importeInteres = interes;
        this.importeCapital = capital;
    }

    public BigDecimal calcularTotal() {
        return importeInteres.add(importeCapital);
    }

    /** Contraimputacion: los mismos importes en negativo, para revertir sin borrar la original. */
    public Imputacion crearReversion(LocalDate fecha) {
        if (fecha == null || fecha.isBefore(this.fecha)) {
            throw new IllegalArgumentException("Fecha de reversion invalida");
        }
        return new Imputacion(expensa, fecha, importeInteres.negate(), importeCapital.negate());
    }

    public LocalDate getFecha() { return fecha; }
    public BigDecimal getImporteInteres() { return importeInteres; }
    public BigDecimal getImporteCapital() { return importeCapital; }
    public Expensa getExpensa() { return expensa; }
}
