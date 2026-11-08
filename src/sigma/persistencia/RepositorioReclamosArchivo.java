package sigma.persistencia;

import sigma.modelo.Empleado;
import sigma.modelo.Reclamo;
import sigma.repositorios.RepositorioReclamos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioReclamosArchivo implements RepositorioReclamos {
    private final AlmacenArchivos almacen;

    public RepositorioReclamosArchivo(AlmacenArchivos almacen) {
        if (almacen == null) throw new IllegalArgumentException("El almacen es obligatorio");
        this.almacen = almacen;
    }

    @Override
    public List<Reclamo> listar() {
        return new ArrayList<>(almacen.getDatos().getReclamos());
    }

    @Override
    public int contarEnCurso(Empleado empleado) {
        int total = 0;
        for (Reclamo reclamo : almacen.getDatos().getReclamos()) {
            if (reclamo.estaEnCursoDe(empleado)) total++;
        }
        return total;
    }

    @Override
    public void guardar(Reclamo reclamo) throws IOException {
        if (reclamo == null) throw new IllegalArgumentException("El reclamo es obligatorio");
        List<Reclamo> reclamos = almacen.getDatos().getReclamos();
        if (!reclamos.contains(reclamo)) reclamos.add(reclamo);
        almacen.guardar();
    }
}
