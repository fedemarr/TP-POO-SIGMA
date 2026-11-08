package sigma.repositorios;

import sigma.modelo.Equipo;
import sigma.modelo.OrdenMantenimiento;

import java.io.IOException;
import java.util.List;

public interface RepositorioEquipos {
    List<Equipo> listar();

    void guardar(Equipo equipo) throws IOException;

    void guardarOrden(OrdenMantenimiento orden) throws IOException;

    List<OrdenMantenimiento> listarOrdenes();
}
