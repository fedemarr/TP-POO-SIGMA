package sigma.persistencia;

import sigma.modelo.Autorizacion;
import sigma.modelo.Lote;
import sigma.repositorios.RepositorioAutorizaciones;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioAutorizacionesArchivo implements RepositorioAutorizaciones {
    private final AlmacenArchivos almacen;

    public RepositorioAutorizacionesArchivo(AlmacenArchivos almacen) {
        if (almacen == null) throw new IllegalArgumentException("El almacen es obligatorio");
        this.almacen = almacen;
    }

    @Override
    public List<Autorizacion> buscar(String documento, Lote lote) {
        List<Autorizacion> encontradas = new ArrayList<>();
        for (Autorizacion autorizacion : almacen.getDatos().getAutorizaciones()) {
            boolean mismoVisitante = autorizacion.getVisitante().getDocumento().equals(documento);
            boolean mismoLote = autorizacion.getLote().getNumero() == lote.getNumero();
            if (mismoVisitante && mismoLote) encontradas.add(autorizacion);
        }
        return encontradas;
    }

    @Override
    public void guardar(Autorizacion autorizacion) throws IOException {
        if (autorizacion == null) throw new IllegalArgumentException("La autorizacion es obligatoria");
        almacen.getDatos().getAutorizaciones().add(autorizacion);
        almacen.guardar();
    }
}
