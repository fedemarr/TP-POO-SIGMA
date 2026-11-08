package sigma.persistencia;

import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.repositorios.RepositorioExpensas;

import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Las expensas viven dentro de cada lote del barrio (composicion Lote - Expensa). */
public class RepositorioExpensasArchivo implements RepositorioExpensas {
    private final AlmacenArchivos almacen;

    public RepositorioExpensasArchivo(AlmacenArchivos almacen) {
        if (almacen == null) throw new IllegalArgumentException("El almacen es obligatorio");
        this.almacen = almacen;
    }

    @Override
    public List<Expensa> buscarPorLote(Lote lote) {
        Lote guardado = almacen.getDatos().getBarrio().obtenerLote(lote.getNumero());
        if (guardado == null) return new ArrayList<>();
        return guardado.getExpensas();
    }

    @Override
    public boolean existePeriodo(YearMonth periodo) {
        for (Lote lote : almacen.getDatos().getBarrio().getLotes()) {
            for (Expensa expensa : lote.getExpensas()) {
                if (expensa.getPeriodo().equals(periodo)) return true;
            }
        }
        return false;
    }

    @Override
    public void guardar(Expensa expensa) throws IOException {
        if (expensa == null) throw new IllegalArgumentException("La expensa es obligatoria");
        almacen.guardar();
    }

    @Override
    public List<Lote> listarLotes() {
        List<Lote> conExpensas = new ArrayList<>();
        for (Lote lote : almacen.getDatos().getBarrio().getLotes()) {
            if (!lote.getExpensas().isEmpty()) conExpensas.add(lote);
        }
        return conExpensas;
    }
}
