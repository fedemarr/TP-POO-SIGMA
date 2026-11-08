package sigma.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Calcula el interes punitorio: interes simple, no capitalizable.
 * interes = capital pendiente x tasa mensual x dias / 30
 */
public class CalculadoraMora implements Serializable {
    private static final BigDecimal DIAS_DEL_MES = new BigDecimal("30");

    private BigDecimal tasaMensual;

    public CalculadoraMora(BigDecimal tasaMensual) {
        if (tasaMensual == null || tasaMensual.signum() < 0) {
            throw new IllegalArgumentException("Tasa mensual invalida");
        }
        this.tasaMensual = tasaMensual;
    }

    public BigDecimal calcular(Expensa expensa, LocalDate fecha) {
        if (expensa == null || fecha == null) throw new IllegalArgumentException("Datos obligatorios");
        return expensa.interesPendiente(fecha, this);
    }

    BigDecimal interesPorDias(BigDecimal capital, long dias) {
        if (dias <= 0 || capital.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal interes = capital.multiply(tasaMensual).multiply(BigDecimal.valueOf(dias));
        return interes.divide(DIAS_DEL_MES, 2, RoundingMode.HALF_UP);
    }
}
