import sigma.modelo.Equipo;
import sigma.modelo.OrdenMantenimiento;
import sigma.repositorios.RepositorioEquipos;

import java.util.ArrayList;
import java.util.List;

public class RepositorioEquiposMemoria implements RepositorioEquipos {
    private final List<Equipo> equipos = new ArrayList<>();
    private final List<OrdenMantenimiento> ordenes = new ArrayList<>();

    @Override
    public List<Equipo> listar() {
        return new ArrayList<>(equipos);
    }

    @Override
    public void guardar(Equipo equipo) {
        if (!equipos.contains(equipo)) equipos.add(equipo);
    }

    @Override
    public void guardarOrden(OrdenMantenimiento orden) {
        if (!ordenes.contains(orden)) ordenes.add(orden);
    }

    @Override
    public List<OrdenMantenimiento> listarOrdenes() {
        return new ArrayList<>(ordenes);
    }
}
