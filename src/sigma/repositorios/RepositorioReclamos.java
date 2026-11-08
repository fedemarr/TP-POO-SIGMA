package sigma.repositorios;

import sigma.modelo.Empleado;
import sigma.modelo.Reclamo;

import java.io.IOException;
import java.util.List;

public interface RepositorioReclamos {
    List<Reclamo> listar();

    int contarEnCurso(Empleado empleado);

    void guardar(Reclamo reclamo) throws IOException;
}
