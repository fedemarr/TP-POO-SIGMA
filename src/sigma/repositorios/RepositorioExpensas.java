package sigma.repositorios;

import sigma.modelo.Expensa;
import sigma.modelo.Lote;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

public interface RepositorioExpensas {
    List<Expensa> buscarPorLote(Lote lote);

    boolean existePeriodo(YearMonth periodo);

    void guardar(Expensa expensa) throws IOException;

    /** Lotes que tienen expensas emitidas (para revisar morosidad y emitir reportes). */
    List<Lote> listarLotes();
}
