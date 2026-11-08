import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.repositorios.RepositorioExpensas;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Implementacion en memoria para pruebas (no usa archivos). */
public class RepositorioExpensasMemoria implements RepositorioExpensas {
    private final List<Expensa> expensas = new ArrayList<>();

    @Override
    public List<Expensa> buscarPorLote(Lote lote) {
        List<Expensa> delLote = new ArrayList<>();
        for (Expensa expensa : expensas) {
            if (expensa.getLote().getNumero() == lote.getNumero()) delLote.add(expensa);
        }
        return delLote;
    }

    @Override
    public boolean existePeriodo(YearMonth periodo) {
        for (Expensa expensa : expensas) {
            if (expensa.getPeriodo().equals(periodo)) return true;
        }
        return false;
    }

    @Override
    public void guardar(Expensa expensa) {
        if (!expensas.contains(expensa)) expensas.add(expensa);
    }

    @Override
    public List<Lote> listarLotes() {
        List<Lote> lotes = new ArrayList<>();
        for (Expensa expensa : expensas) {
            if (!lotes.contains(expensa.getLote())) lotes.add(expensa.getLote());
        }
        return lotes;
    }
}
