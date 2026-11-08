import sigma.modelo.Empleado;
import sigma.modelo.Reclamo;
import sigma.repositorios.RepositorioReclamos;

import java.util.ArrayList;
import java.util.List;

public class RepositorioReclamosMemoria implements RepositorioReclamos {
    private final List<Reclamo> reclamos = new ArrayList<>();

    @Override
    public List<Reclamo> listar() {
        return new ArrayList<>(reclamos);
    }

    @Override
    public int contarEnCurso(Empleado empleado) {
        int total = 0;
        for (Reclamo reclamo : reclamos) {
            if (reclamo.estaEnCursoDe(empleado)) total++;
        }
        return total;
    }

    @Override
    public void guardar(Reclamo reclamo) {
        if (!reclamos.contains(reclamo)) reclamos.add(reclamo);
    }
}
