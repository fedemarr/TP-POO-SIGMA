import sigma.modelo.CalculadoraMora;
import sigma.modelo.EstadoExpensa;
import sigma.modelo.Expensa;
import sigma.modelo.Imputacion;
import sigma.modelo.Lote;
import sigma.modelo.MedioPago;
import sigma.modelo.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/** Intereses por mora e imputacion de pagos (ejemplo de docs/05-criterios-diseno.md). */
public class PruebaExpensas {
    public static void main(String[] args) {
        CalculadoraMora mora = new CalculadoraMora(new BigDecimal("0.03"));
        Lote lote = new Lote(1, 500);
        LocalDate vence = LocalDate.of(2026, 10, 10);
        Expensa expensa = new Expensa(lote, YearMonth.of(2026, 10), new BigDecimal("10000"), vence);

        // El dia del vencimiento no genera interes
        Verificar.igual("0", mora.calcular(expensa, vence), "interes el dia del vencimiento");
        // $10.000 durante 10 dias generan $100
        Verificar.igual("100", mora.calcular(expensa, vence.plusDays(10)), "interes de 10 dias");

        // Pago de $5.100: cancela los $100 de interes y $5.000 de capital
        expensa.aplicarImputacion(new Imputacion(expensa, vence.plusDays(10), new BigDecimal("100"), new BigDecimal("5000")));
        Verificar.igual("5000", expensa.capitalPendiente(), "capital despues del pago parcial");
        Verificar.verdadero(expensa.getEstado() == EstadoExpensa.PARCIAL, "estado parcial");
        // Diez dias despues, los $5.000 restantes generan $50
        Verificar.igual("50", expensa.interesPendiente(vence.plusDays(20), mora), "interes del segundo tramo");
        Verificar.igual("5050", expensa.calcularSaldo(vence.plusDays(20), mora), "saldo total");

        // Un pago se imputa primero a intereses y despues a capital, desde el periodo mas antiguo
        Expensa septiembre = new Expensa(lote, YearMonth.of(2026, 9), new BigDecimal("1000"), LocalDate.of(2026, 9, 10));
        Expensa octubre = new Expensa(lote, YearMonth.of(2026, 10), new BigDecimal("1000"), LocalDate.of(2026, 10, 10));
        LocalDate fechaPago = LocalDate.of(2026, 10, 10);
        // Septiembre lleva 30 dias vencida: 1000 x 0,03 = $30 de interes
        Pago pago = new Pago(1, lote, fechaPago, new BigDecimal("1500"), MedioPago.EFECTIVO);
        pago.imputar(List.of(octubre, septiembre), fechaPago, mora);
        Verificar.verdadero(septiembre.getEstado() == EstadoExpensa.PAGADA, "se cancela primero el periodo mas antiguo");
        Verificar.igual("530", octubre.capitalPendiente(), "lo que sobra va al periodo siguiente (1500 - 1030 = 470)");
        Verificar.igual("0", pago.saldoSinImputar(), "el pago quedo imputado completo");

        System.out.println("PruebaExpensas: OK");
    }
}
