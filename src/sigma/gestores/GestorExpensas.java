package sigma.gestores;

import sigma.excepciones.PagoInvalidoException;
import sigma.excepciones.ReglaNegocioException;
import sigma.modelo.AjustePago;
import sigma.modelo.Barrio;
import sigma.modelo.CalculadoraMora;
import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.modelo.MedioPago;
import sigma.modelo.Notificacion;
import sigma.modelo.Pago;
import sigma.modelo.Persona;
import sigma.repositorios.RepositorioExpensas;
import sigma.repositorios.RepositorioNotificaciones;
import sigma.repositorios.RepositorioPagos;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controlador de expensas y pagos.
 * Inversion de dependencias (DIP): solo conoce las interfaces de repositorio,
 * por eso funciona igual con archivos o con repositorios en memoria para pruebas.
 */
public class GestorExpensas {
    private static final int DIA_DE_VENCIMIENTO = 10;
    private static final BigDecimal UN_CENTAVO = new BigDecimal("0.01");

    private final RepositorioExpensas expensas;
    private final RepositorioPagos pagos;
    private final CalculadoraMora mora;
    private final RepositorioNotificaciones notificaciones;

    public GestorExpensas(RepositorioExpensas expensas, RepositorioPagos pagos, CalculadoraMora mora,
                          RepositorioNotificaciones notificaciones) {
        if (expensas == null || pagos == null || mora == null || notificaciones == null) {
            throw new IllegalArgumentException("Dependencias obligatorias");
        }
        this.expensas = expensas;
        this.pagos = pagos;
        this.mora = mora;
        this.notificaciones = notificaciones;
    }

    /**
     * Expensa de cada lote = presupuesto x coeficiente (superficie del lote / superficie total).
     * Cada importe se redondea hacia abajo y los centavos que sobran se reparten
     * de a uno por numero de lote, para que el total coincida con el presupuesto.
     */
    public void emitirExpensas(Barrio barrio, YearMonth periodo, BigDecimal presupuesto) throws ReglaNegocioException, IOException {
        if (barrio == null || periodo == null || presupuesto == null || presupuesto.signum() <= 0) {
            throw new IllegalArgumentException("Barrio, periodo y presupuesto son obligatorios");
        }
        if (expensas.existePeriodo(periodo)) {
            throw new ReglaNegocioException("Las expensas de " + periodo + " ya fueron emitidas");
        }
        List<Lote> lotes = barrio.getLotes();
        if (lotes.isEmpty()) throw new ReglaNegocioException("El barrio no tiene lotes");
        lotes.sort(Comparator.comparingInt(Lote::getNumero));

        BigDecimal superficieTotal = BigDecimal.valueOf(barrio.calcularSuperficieTotal());
        List<BigDecimal> importes = new ArrayList<>();
        BigDecimal distribuido = BigDecimal.ZERO;
        for (Lote lote : lotes) {
            BigDecimal importe = presupuesto.multiply(BigDecimal.valueOf(lote.getSuperficie()))
                    .divide(superficieTotal, 2, RoundingMode.DOWN);
            importes.add(importe);
            distribuido = distribuido.add(importe);
        }
        // Reparto de los centavos residuales por numero de lote
        BigDecimal sobrante = presupuesto.subtract(distribuido);
        int posicion = 0;
        while (sobrante.signum() > 0) {
            importes.set(posicion, importes.get(posicion).add(UN_CENTAVO));
            sobrante = sobrante.subtract(UN_CENTAVO);
            posicion = (posicion + 1) % lotes.size();
        }

        LocalDate vencimiento = periodo.atDay(DIA_DE_VENCIMIENTO);
        for (int i = 0; i < lotes.size(); i++) {
            Lote lote = lotes.get(i);
            Expensa expensa = new Expensa(lote, periodo, importes.get(i), vencimiento);
            lote.agregarExpensa(expensa);
            expensas.guardar(expensa);
            // Si el lote tiene saldo a favor de pagos anteriores, se aplica a la nueva expensa
            aplicarSaldosAFavor(lote, periodo.atDay(1));
        }
        // CU-11: al emitir tambien se revisa la morosidad de los lotes
        verificarMorosidad(periodo.atDay(1));
    }

    public Pago registrarPago(Lote lote, BigDecimal importe, MedioPago medio, LocalDate fecha) throws PagoInvalidoException, IOException {
        if (lote == null || importe == null || fecha == null) {
            throw new IllegalArgumentException("Datos del pago obligatorios");
        }
        // CU-12 A1: importe no positivo, medio invalido o fecha futura
        if (importe.signum() <= 0) {
            throw new PagoInvalidoException("El importe debe ser mayor a cero");
        }
        if (medio == null) {
            throw new PagoInvalidoException("El medio de pago es obligatorio");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new PagoInvalidoException("No se admiten pagos con fecha futura");
        }
        List<Expensa> cuenta = expensas.buscarPorLote(lote);
        LocalDate ultimoMovimiento = ultimoMovimiento(cuenta);
        if (ultimoMovimiento != null && fecha.isBefore(ultimoMovimiento)) {
            throw new PagoInvalidoException("La fecha es anterior al ultimo movimiento de la cuenta (" + ultimoMovimiento + ")");
        }
        if (calcularDeuda(cuenta, fecha).signum() == 0) {
            throw new PagoInvalidoException("El lote " + lote.getNumero() + " no tiene deuda");
        }
        Pago pago = new Pago(pagos.siguienteNumero(), lote, fecha, importe, medio);
        pago.imputar(cuenta, fecha, mora);
        // Si regulariza la deuda, el lote deja de ser moroso
        lote.actualizarMorosidad(cuenta, fecha);
        // Lo que sobra queda en el pago como saldo a favor del lote
        pagos.confirmarPago(pago, cuenta, lote);
        return pago;
    }

    /** Revierte un pago completo con un asiento de ajuste; el pago original no se borra. */
    public AjustePago registrarAjuste(Pago pago, BigDecimal importe, String motivo, LocalDate fecha) throws PagoInvalidoException, IOException {
        if (pago == null) throw new IllegalArgumentException("El pago es obligatorio");
        AjustePago ajuste = pago.registrarAjuste(importe, motivo, fecha);
        Lote lote = pago.getLote();
        List<Expensa> cuenta = expensas.buscarPorLote(lote);
        lote.actualizarMorosidad(cuenta, fecha);
        pagos.confirmarAjuste(pago, ajuste, cuenta, lote);
        return ajuste;
    }

    /** Deuda total (capital + intereses) menos el saldo a favor del lote. */
    public BigDecimal consultarCuenta(Lote lote, LocalDate fecha) {
        if (lote == null || fecha == null) throw new IllegalArgumentException("Datos obligatorios");
        BigDecimal deuda = calcularDeuda(expensas.buscarPorLote(lote), fecha);
        return deuda.subtract(saldoAFavor(lote));
    }

    public BigDecimal saldoAFavor(Lote lote) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pago pago : pagos.buscarPorLote(lote)) {
            if (pago.saldoSinImputar().signum() > 0) {
                total = total.add(pago.saldoSinImputar());
            }
        }
        return total;
    }

    /** Aplica los saldos a favor empezando por el pago mas antiguo. */
    public void aplicarSaldosAFavor(Lote lote, LocalDate fecha) throws IOException {
        List<Pago> pagosDelLote = pagos.buscarPorLote(lote);
        pagosDelLote.sort(Comparator.comparing(Pago::getFecha));
        for (Pago pago : pagosDelLote) {
            if (pago.saldoSinImputar().signum() > 0) {
                List<Expensa> cuenta = expensas.buscarPorLote(lote);
                // El saldo no se puede aplicar antes de la fecha del pago
                LocalDate fechaAplicacion = fecha;
                if (fecha.isBefore(pago.getFecha())) {
                    fechaAplicacion = pago.getFecha();
                }
                pago.imputar(cuenta, fechaAplicacion, mora);
                pagos.confirmarPago(pago, cuenta, lote);
            }
        }
    }

    /** Marca los lotes morosos y genera la notificacion formal al titular. */
    public void verificarMorosidad(LocalDate fecha) throws IOException {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria");
        for (Lote lote : expensas.listarLotes()) {
            boolean eraMoroso = lote.isMoroso();
            boolean esMoroso = lote.actualizarMorosidad(expensas.buscarPorLote(lote), fecha);
            if (!eraMoroso && esMoroso) {
                Notificacion aviso = new Notificacion(LocalDateTime.now(),
                        "Notificacion formal: el lote tiene mas de 3 periodos impagos y fue marcado como moroso",
                        destinatarioTitular(lote, fecha), "Lote " + lote.getNumero(),
                        "MOROSIDAD-LOTE-" + lote.getNumero() + "-" + fecha);
                notificaciones.guardarSiNoExiste(aviso);
            }
        }
    }

    public List<Pago> consultarPagos(Lote lote) {
        return pagos.buscarPorLote(lote);
    }

    public List<Expensa> consultarExpensas(Lote lote) {
        return expensas.buscarPorLote(lote);
    }

    private String destinatarioTitular(Lote lote, LocalDate fecha) {
        Persona titular = lote.obtenerTitular(fecha);
        if (titular == null) return "Titular del lote " + lote.getNumero();
        return titular.toString();
    }

    private BigDecimal calcularDeuda(List<Expensa> cuenta, LocalDate fecha) {
        BigDecimal deuda = BigDecimal.ZERO;
        for (Expensa expensa : cuenta) {
            deuda = deuda.add(expensa.calcularSaldo(fecha, mora));
        }
        return deuda;
    }

    private LocalDate ultimoMovimiento(List<Expensa> cuenta) {
        LocalDate ultimo = null;
        for (Expensa expensa : cuenta) {
            LocalDate fecha = expensa.fechaUltimoMovimiento();
            if (fecha != null && (ultimo == null || fecha.isAfter(ultimo))) {
                ultimo = fecha;
            }
        }
        return ultimo;
    }
}
