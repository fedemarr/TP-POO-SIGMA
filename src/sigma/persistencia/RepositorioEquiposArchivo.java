package sigma.persistencia;

import sigma.modelo.Equipo;
import sigma.modelo.OrdenMantenimiento;
import sigma.repositorios.RepositorioEquipos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Los equipos pertenecen al barrio; las ordenes se guardan aparte en el mismo archivo. */
public class RepositorioEquiposArchivo implements RepositorioEquipos {
    private final AlmacenArchivos almacen;

    public RepositorioEquiposArchivo(AlmacenArchivos almacen) {
        if (almacen == null) throw new IllegalArgumentException("El almacen es obligatorio");
        this.almacen = almacen;
    }

    @Override
    public List<Equipo> listar() {
        return almacen.getDatos().getBarrio().getEquipos();
    }

    @Override
    public void guardar(Equipo equipo) throws IOException {
        almacen.getDatos().getBarrio().agregarEquipo(equipo);
        almacen.guardar();
    }

    @Override
    public void guardarOrden(OrdenMantenimiento orden) throws IOException {
        if (orden == null) throw new IllegalArgumentException("La orden es obligatoria");
        List<OrdenMantenimiento> ordenes = almacen.getDatos().getOrdenes();
        if (!ordenes.contains(orden)) ordenes.add(orden);
        almacen.guardar();
    }

    @Override
    public List<OrdenMantenimiento> listarOrdenes() {
        return new ArrayList<>(almacen.getDatos().getOrdenes());
    }
}
