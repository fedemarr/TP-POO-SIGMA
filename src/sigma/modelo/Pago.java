package sigma.modelo;

import sigma.excepciones.PagoInvalidoException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Pago implements Serializable {
    private int numero;
    private LocalDate fecha;
    private BigDecimal importe;
    private MedioPago medio;
    private Lote lote;
    private List<Imputacion> imputaciones = new ArrayList<>();
    private List<AjustePago> ajustes = new ArrayList<>();

    public Pago(int numero, Lote lote, LocalDate fecha, BigDecimal importe, MedioPago medio) {
        if (numero <= 0 || lote == null || fecha == null || importe == null || importe.signum() <= 0 || medio == null) {
            throw new IllegalArgumentException("Datos de pago invalidos");
        }
        this.numero = numero;
        this.lote = lote;
        this.fecha = fecha;
        this.importe = importe;
        this.medio = medio;
    }

    /** Lo que queda del pago sin aplicar a ninguna expensa: es el saldo a favor del lote. */
    public BigDecimal saldoSinImputar() {
        BigDecimal saldo = importe;
        for (Imputacion imputacion : imputaciones) {
            saldo = saldo.subtract(imputacion.calcularTotal());
        }
        for (AjustePago ajuste : ajustes) {
            saldo = saldo.add(ajuste.getImporte());
        }
        return saldo;
    }

    /**
     * Imputa el pago empezando por la expensa mas antigua: primero sus intereses,
     * despues su capital, y recien entonces pasa a la siguiente.
     * La fecha se recibe aparte porque un saldo a favor se puede aplicar despues del pago.
     */
    public void imputar(List<Expensa> expensas, LocalDate fecha, CalculadoraMora mora) {
        if (expensas == null || fecha == null || mora == null) throw new IllegalArgumentException("Datos obligatorios");
        if (fecha.isBefore(this.fecha)) throw new IllegalArgumentException("La fecha es anterior al pago");

        List<Expensa> ordenadas = new ArrayList<>(expensas);
        ordenadas.sort(Comparator.comparing(Expensa::getVencimiento));

        for (Expensa expensa : ordenadas) {
            BigDecimal disponible = saldoSinImputar();
            if (disponible.signum() <= 0) break;
            BigDecimal interes = expensa.interesPendiente(fecha, mora).min(disponible);
            BigDecimal capital = expensa.capitalPendiente().min(disponible.subtract(interes));
            if (interes.signum() == 0 && capital.signum() == 0) continue;
            Imputacion imputacion = new Imputacion(expensa, fecha, interes, capital);
            expensa.aplicarImputacion(imputacion);
            imputaciones.add(imputacion);
        }
    }

    /**
     * Reversion total con asiento de ajuste: el pago y sus imputaciones originales se conservan,
     * y se agregan contraimputaciones identificadas por el ajuste.
     */
    public AjustePago registrarAjuste(BigDecimal importe, String motivo, LocalDate fecha) throws PagoInvalidoException {
        if (importe == null || fecha == null || motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Importe, motivo y fecha son obligatorios");
        }
        if (estaRevertido()) {
            throw new PagoInvalidoException("El pago " + numero + " ya fue revertido");
        }
        if (importe.compareTo(this.importe.negate()) != 0) {
            throw new PagoInvalidoException("El ajuste debe revertir el total del pago: " + this.importe.negate());
        }
        List<Imputacion> originales = new ArrayList<>(imputaciones);
        // Se valida antes de tocar nada para no dejar el pago a medio revertir
        for (Imputacion original : originales) {
            LocalDate ultimo = original.getExpensa().fechaUltimoMovimiento();
            if (fecha.isBefore(ultimo)) {
                throw new PagoInvalidoException("El ajuste no puede ser anterior al ultimo movimiento (" + ultimo + ")");
            }
        }
        AjustePago ajuste = new AjustePago(ajustes.size() + 1, fecha, importe, motivo);
        for (Imputacion original : originales) {
            Imputacion contraimputacion = original.crearReversion(fecha);
            original.getExpensa().aplicarImputacion(contraimputacion);
            imputaciones.add(contraimputacion);
            ajuste.agregarContraimputacion(contraimputacion);
        }
        ajustes.add(ajuste);
        return ajuste;
    }

    public boolean estaRevertido() {
        BigDecimal totalAjustes = BigDecimal.ZERO;
        for (AjustePago ajuste : ajustes) {
            totalAjustes = totalAjustes.add(ajuste.getImporte());
        }
        return totalAjustes.compareTo(importe.negate()) == 0;
    }

    public int getNumero() { return numero; }
    public LocalDate getFecha() { return fecha; }
    public BigDecimal getImporte() { return importe; }
    public MedioPago getMedio() { return medio; }
    public Lote getLote() { return lote; }
    public List<Imputacion> getImputaciones() { return new ArrayList<>(imputaciones); }
    public List<AjustePago> getAjustes() { return new ArrayList<>(ajustes); }

    @Override
    public String toString() {
        String texto = "Pago " + numero + " | lote " + lote.getNumero() + " | " + fecha + " | $" + importe
                + " | " + medio + " | saldo a favor $" + saldoSinImputar();
        if (estaRevertido()) {
            texto = texto + " | REVERTIDO";
        }
        return texto;
    }
}
