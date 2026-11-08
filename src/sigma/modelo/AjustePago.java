package sigma.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Asiento de ajuste: corrige un pago sin borrarlo. Identifica las contraimputaciones que genero. */
public class AjustePago implements Serializable {
    private int numero;
    private LocalDate fecha;
    private BigDecimal importe;
    private String motivo;
    private List<Imputacion> contraimputaciones = new ArrayList<>();

    public AjustePago(int numero, LocalDate fecha, BigDecimal importe, String motivo) {
        if (numero <= 0 || fecha == null || importe == null || motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Datos de ajuste invalidos");
        }
        this.numero = numero;
        this.fecha = fecha;
        this.importe = importe;
        this.motivo = motivo;
    }

    public void agregarContraimputacion(Imputacion imputacion) {
        if (imputacion == null) throw new IllegalArgumentException("La imputacion es obligatoria");
        contraimputaciones.add(imputacion);
    }

    public int getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public BigDecimal getImporte() { return importe; }
    public String getMotivo() { return motivo; }
    public List<Imputacion> getContraimputaciones() { return new ArrayList<>(contraimputaciones); }

    @Override
    public String toString() {
        return "Ajuste " + numero + " del " + fecha + " por $" + importe + " (" + motivo + ")";
    }
}
