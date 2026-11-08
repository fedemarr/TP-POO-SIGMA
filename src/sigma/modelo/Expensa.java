package sigma.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Expensa implements Serializable {
    private YearMonth periodo;
    private BigDecimal importe;
    private LocalDate vencimiento;
    private EstadoExpensa estado;
    private Lote lote;
    private List<Imputacion> imputaciones = new ArrayList<>();

    public Expensa(Lote lote, YearMonth periodo, BigDecimal importe, LocalDate vencimiento) {
        if (lote == null || periodo == null || importe == null || vencimiento == null || importe.signum() < 0) {
            throw new IllegalArgumentException("Datos de expensa invalidos");
        }
        this.lote = lote;
        this.periodo = periodo;
        this.importe = importe.setScale(2, RoundingMode.HALF_UP);
        this.vencimiento = vencimiento;
        this.estado = EstadoExpensa.IMPAGA;
    }

    public BigDecimal capitalPendiente() {
        BigDecimal capital = importe;
        for (Imputacion imputacion : imputaciones) {
            capital = capital.subtract(imputacion.getImporteCapital());
        }
        return capital.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Experto en informacion: la expensa conoce su capital y sus imputaciones,
     * por eso calcula su interes. Recorre los tramos entre imputaciones porque
     * cada pago parcial cambia el capital sobre el que se calcula el tramo siguiente.
     */
    public BigDecimal interesPendiente(LocalDate fecha, CalculadoraMora mora) {
        if (fecha == null || mora == null) throw new IllegalArgumentException("Datos obligatorios");
        if (fecha.isBefore(vencimiento)) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal capital = importe;
        BigDecimal intereses = BigDecimal.ZERO.setScale(2);
        LocalDate inicioTramo = vencimiento;
        for (Imputacion imputacion : imputaciones) {
            if (imputacion.getFecha().isAfter(fecha)) break;
            LocalDate finTramo = imputacion.getFecha();
            if (finTramo.isAfter(inicioTramo)) {
                long dias = ChronoUnit.DAYS.between(inicioTramo, finTramo);
                intereses = intereses.add(mora.interesPorDias(capital, dias));
                inicioTramo = finTramo;
            }
            intereses = intereses.subtract(imputacion.getImporteInteres());
            capital = capital.subtract(imputacion.getImporteCapital());
        }
        if (fecha.isAfter(inicioTramo)) {
            long dias = ChronoUnit.DAYS.between(inicioTramo, fecha);
            intereses = intereses.add(mora.interesPorDias(capital, dias));
        }
        if (intereses.signum() < 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return intereses.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularSaldo(LocalDate fecha, CalculadoraMora mora) {
        return capitalPendiente().add(interesPendiente(fecha, mora));
    }

    public void aplicarImputacion(Imputacion imputacion) {
        if (imputacion == null || imputacion.getExpensa() != this) {
            throw new IllegalArgumentException("La imputacion no corresponde a esta expensa");
        }
        LocalDate ultimo = fechaUltimoMovimiento();
        if (ultimo != null && imputacion.getFecha().isBefore(ultimo)) {
            throw new IllegalArgumentException("No se admiten movimientos anteriores al ultimo confirmado");
        }
        BigDecimal nuevoCapital = capitalPendiente().subtract(imputacion.getImporteCapital());
        if (nuevoCapital.signum() < 0 || nuevoCapital.compareTo(importe) > 0) {
            throw new IllegalArgumentException("Imputacion de capital invalida");
        }
        imputaciones.add(imputacion);
        actualizarEstado(nuevoCapital);
    }

    private void actualizarEstado(BigDecimal capitalRestante) {
        if (capitalRestante.signum() == 0) {
            estado = EstadoExpensa.PAGADA;
        } else if (capitalRestante.compareTo(importe) == 0) {
            estado = EstadoExpensa.IMPAGA;
        } else {
            estado = EstadoExpensa.PARCIAL;
        }
    }

    // El dia del vencimiento no cuenta como atraso
    public boolean estaVencida(LocalDate fecha) {
        return fecha.isAfter(vencimiento);
    }

    public LocalDate fechaUltimoMovimiento() {
        if (imputaciones.isEmpty()) return null;
        return imputaciones.get(imputaciones.size() - 1).getFecha();
    }

    public Lote getLote() { return lote; }
    public YearMonth getPeriodo() { return periodo; }
    public BigDecimal getImporte() { return importe; }
    public LocalDate getVencimiento() { return vencimiento; }
    public EstadoExpensa getEstado() { return estado; }

    @Override
    public String toString() {
        return "Expensa " + periodo + " | lote " + lote.getNumero() + " | $" + importe
                + " | vence " + vencimiento + " | " + estado + " | capital pendiente $" + capitalPendiente();
    }
}
