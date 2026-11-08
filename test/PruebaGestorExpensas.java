import sigma.excepciones.PagoInvalidoException;
import sigma.excepciones.ReglaNegocioException;
import sigma.gestores.GestorExpensas;
import sigma.modelo.Barrio;
import sigma.modelo.CalculadoraMora;
import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.modelo.MedioPago;
import sigma.modelo.Pago;
import sigma.modelo.Persona;
import sigma.modelo.Residencia;
import sigma.modelo.TipoResidente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/** Emision, pagos, saldo a favor, ajustes y morosidad, con repositorios en memoria. */
public class PruebaGestorExpensas {
    public static void main(String[] args) throws Exception {
        Barrio barrio = new Barrio("Los Aromos");
        Lote lote1 = new Lote(1, 100);
        Lote lote2 = new Lote(2, 100);
        Lote lote3 = new Lote(3, 100);
        barrio.agregarLote(lote1);
        barrio.agregarLote(lote2);
        barrio.agregarLote(lote3);
        lote1.agregarResidencia(new Residencia(new Persona("Ana", "Perez", "123", "1", "a@a.com"),
                TipoResidente.TITULAR, LocalDate.of(2026, 1, 1), null, true));

        RepositorioExpensasMemoria expensas = new RepositorioExpensasMemoria();
        RepositorioPagosMemoria pagos = new RepositorioPagosMemoria();
        RepositorioNotificacionesMemoria notificaciones = new RepositorioNotificacionesMemoria();
        GestorExpensas gestor = new GestorExpensas(expensas, pagos, new CalculadoraMora(new BigDecimal("0.03")), notificaciones);

        // $100 entre 3 lotes iguales: 33,33 cada uno y el centavo que sobra va al lote 1
        gestor.emitirExpensas(barrio, YearMonth.of(2026, 5), new BigDecimal("100"));
        Verificar.igual("33.34", expensas.buscarPorLote(lote1).get(0).getImporte(), "el centavo residual va al lote 1");
        Verificar.igual("33.33", expensas.buscarPorLote(lote3).get(0).getImporte(), "importe del lote 3");
        Verificar.verdadero(expensas.buscarPorLote(lote1).get(0).getVencimiento().getDayOfMonth() == 10, "vence el dia 10");

        try {
            gestor.emitirExpensas(barrio, YearMonth.of(2026, 5), new BigDecimal("100"));
            throw new AssertionError("Se emitio dos veces el mismo periodo");
        } catch (ReglaNegocioException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        // Pago con excedente: el sobrante queda en el pago como saldo a favor
        Pago pago = gestor.registrarPago(lote2, new BigDecimal("50"), MedioPago.TRANSFERENCIA, LocalDate.of(2026, 5, 5));
        Verificar.igual("16.67", pago.saldoSinImputar(), "saldo a favor en el pago");
        Verificar.igual("-16.67", gestor.consultarCuenta(lote2, LocalDate.of(2026, 5, 5)), "cuenta con saldo a favor");

        // Un lote sin deuda no puede pagar
        try {
            gestor.registrarPago(lote2, new BigDecimal("10"), MedioPago.EFECTIVO, LocalDate.of(2026, 5, 6));
            throw new AssertionError("Se acepto un pago sin deuda");
        } catch (PagoInvalidoException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        // Al emitir el mes siguiente, el saldo a favor se aplica a la nueva expensa
        gestor.emitirExpensas(barrio, YearMonth.of(2026, 6), new BigDecimal("100"));
        Expensa junioLote2 = expensas.buscarPorLote(lote2).get(1);
        Verificar.igual("16.66", junioLote2.capitalPendiente(), "saldo a favor aplicado (33,33 - 16,67)");
        Verificar.igual("0", pago.saldoSinImputar(), "el pago ya no tiene saldo a favor");

        // Ajuste: revierte el pago completo sin borrarlo
        gestor.registrarAjuste(pago, new BigDecimal("-50"), "Transferencia rechazada", LocalDate.of(2026, 6, 2));
        Verificar.verdadero(pago.estaRevertido(), "pago revertido");
        Verificar.igual("33.33", junioLote2.capitalPendiente(), "la expensa vuelve a deber su total");
        Verificar.verdadero(pagos.getAjustesConfirmados() == 1, "el ajuste se confirma en el repositorio");
        try {
            gestor.registrarAjuste(pago, new BigDecimal("-50"), "Otra vez", LocalDate.of(2026, 6, 3));
            throw new AssertionError("Se revirtio dos veces el mismo pago");
        } catch (PagoInvalidoException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        // Morosidad: con 3 periodos vencidos todavia no es moroso; con 4 si
        gestor.emitirExpensas(barrio, YearMonth.of(2026, 7), new BigDecimal("100"));
        gestor.emitirExpensas(barrio, YearMonth.of(2026, 8), new BigDecimal("100"));
        gestor.verificarMorosidad(LocalDate.of(2026, 7, 20));
        Verificar.verdadero(!lote1.isMoroso(), "3 periodos vencidos no alcanzan");
        gestor.verificarMorosidad(LocalDate.of(2026, 8, 20));
        Verificar.verdadero(lote1.isMoroso(), "4 periodos vencidos: moroso");
        Verificar.verdadero(notificaciones.listar().size() == 3, "una notificacion por lote moroso");
        Verificar.verdadero(notificaciones.listar().get(0).getDestinatario().contains("Ana"), "se notifica al titular");
        gestor.verificarMorosidad(LocalDate.of(2026, 8, 21));
        Verificar.verdadero(notificaciones.listar().size() == 3, "no se repiten notificaciones");

        // Al regularizar la deuda deja de ser moroso
        gestor.registrarPago(lote1, new BigDecimal("1000"), MedioPago.EFECTIVO, LocalDate.of(2026, 8, 25));
        Verificar.verdadero(!lote1.isMoroso(), "regularizo la deuda");

        System.out.println("PruebaGestorExpensas: OK");
    }
}
