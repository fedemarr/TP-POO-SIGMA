import sigma.modelo.Autorizacion;
import sigma.modelo.Lote;
import sigma.repositorios.RepositorioAutorizaciones;

import java.util.ArrayList;
import java.util.List;

public class RepositorioAutorizacionesMemoria implements RepositorioAutorizaciones {
    private final List<Autorizacion> autorizaciones = new ArrayList<>();

    @Override
    public List<Autorizacion> buscar(String documento, Lote lote) {
        List<Autorizacion> encontradas = new ArrayList<>();
        for (Autorizacion autorizacion : autorizaciones) {
            if (autorizacion.getVisitante().getDocumento().equals(documento) && autorizacion.getLote() == lote) {
                encontradas.add(autorizacion);
            }
        }
        return encontradas;
    }

    @Override
    public void guardar(Autorizacion autorizacion) {
        autorizaciones.add(autorizacion);
    }
}
