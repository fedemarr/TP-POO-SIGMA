package sigma.repositorios;

import java.io.IOException;
import java.util.List;
import sigma.modelo.Autorizacion;
import sigma.modelo.Lote;

public interface RepositorioAutorizaciones {
    List<Autorizacion> buscar(String documento, Lote lote) throws IOException;
    List<Autorizacion> listar() throws IOException;
    void guardar(Autorizacion autorizacion) throws IOException;
}
