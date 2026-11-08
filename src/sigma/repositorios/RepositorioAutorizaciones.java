package sigma.repositorios;

import sigma.modelo.Autorizacion;
import sigma.modelo.Lote;

import java.io.IOException;
import java.util.List;

public interface RepositorioAutorizaciones {
    List<Autorizacion> buscar(String documento, Lote lote);

    void guardar(Autorizacion autorizacion) throws IOException;
}
