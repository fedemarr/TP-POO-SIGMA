package sigma.repositorios;

import sigma.modelo.RegistroIngreso;

import java.io.IOException;

public interface RepositorioIngresos {
    /** Devuelve el ingreso sin egreso de ese documento, o null si no hay. */
    RegistroIngreso buscarAbierto(String documento);

    void guardar(RegistroIngreso registro) throws IOException;
}
