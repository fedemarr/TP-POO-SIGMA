import sigma.modelo.AjustePago;
import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.modelo.Pago;
import sigma.repositorios.RepositorioPagos;

import java.util.ArrayList;
import java.util.List;

public class RepositorioPagosMemoria implements RepositorioPagos {
    private final List<Pago> pagos = new ArrayList<>();
    private int ajustesConfirmados = 0;

    @Override
    public int siguienteNumero() {
        return pagos.size() + 1;
    }

    @Override
    public void guardar(Pago pago) {
        if (!pagos.contains(pago)) pagos.add(pago);
    }

    @Override
    public void confirmarPago(Pago pago, List<Expensa> expensas, Lote lote) {
        guardar(pago);
    }

    @Override
    public List<Pago> buscarPorLote(Lote lote) {
        List<Pago> delLote = new ArrayList<>();
        for (Pago pago : pagos) {
            if (pago.getLote().getNumero() == lote.getNumero()) delLote.add(pago);
        }
        return delLote;
    }

    @Override
    public void confirmarAjuste(Pago pago, AjustePago ajuste, List<Expensa> expensas, Lote lote) {
        ajustesConfirmados++;
    }

    public int getAjustesConfirmados() {
        return ajustesConfirmados;
    }
}
