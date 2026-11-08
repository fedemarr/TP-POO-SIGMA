package sigma.persistencia;

import sigma.modelo.AjustePago;
import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.modelo.Pago;
import sigma.repositorios.RepositorioPagos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioPagosArchivo implements RepositorioPagos {
    private final AlmacenArchivos almacen;

    public RepositorioPagosArchivo(AlmacenArchivos almacen) {
        if (almacen == null) throw new IllegalArgumentException("El almacen es obligatorio");
        this.almacen = almacen;
    }

    @Override
    public int siguienteNumero() {
        int mayor = 0;
        for (Pago pago : almacen.getDatos().getPagos()) {
            if (pago.getNumero() > mayor) {
                mayor = pago.getNumero();
            }
        }
        return mayor + 1;
    }

    @Override
    public void guardar(Pago pago) throws IOException {
        agregarSiNoExiste(pago);
        almacen.guardar();
    }

    // El pago, sus imputaciones, las expensas y el lote se guardan juntos en una sola escritura
    @Override
    public void confirmarPago(Pago pago, List<Expensa> expensas, Lote lote) throws IOException {
        agregarSiNoExiste(pago);
        almacen.guardar();
    }

    @Override
    public List<Pago> buscarPorLote(Lote lote) {
        List<Pago> delLote = new ArrayList<>();
        for (Pago pago : almacen.getDatos().getPagos()) {
            if (pago.getLote().getNumero() == lote.getNumero()) delLote.add(pago);
        }
        return delLote;
    }

    @Override
    public void confirmarAjuste(Pago pago, AjustePago ajuste, List<Expensa> expensas, Lote lote) throws IOException {
        if (pago == null || ajuste == null) throw new IllegalArgumentException("Pago y ajuste obligatorios");
        almacen.guardar();
    }

    private void agregarSiNoExiste(Pago pago) {
        if (pago == null) throw new IllegalArgumentException("El pago es obligatorio");
        List<Pago> pagos = almacen.getDatos().getPagos();
        if (!pagos.contains(pago)) pagos.add(pago);
    }
}
