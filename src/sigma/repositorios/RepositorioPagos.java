package sigma.repositorios;

import sigma.modelo.AjustePago;
import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.modelo.Pago;

import java.io.IOException;
import java.util.List;

public interface RepositorioPagos {
    int siguienteNumero();

    void guardar(Pago pago) throws IOException;

    /** Confirma de forma conjunta el pago, sus imputaciones, las expensas modificadas y el lote. */
    void confirmarPago(Pago pago, List<Expensa> expensas, Lote lote) throws IOException;

    List<Pago> buscarPorLote(Lote lote);

    void confirmarAjuste(Pago pago, AjustePago ajuste, List<Expensa> expensas, Lote lote) throws IOException;
}
