package sigma.repositorios;

import java.io.IOException;
import java.util.List;
import sigma.modelo.RegistroIngreso;

public interface RepositorioIngresos {
    RegistroIngreso buscarAbierto(String documento) throws IOException;
    List<RegistroIngreso> listar() throws IOException;
    void guardar(RegistroIngreso registro) throws IOException;
}
