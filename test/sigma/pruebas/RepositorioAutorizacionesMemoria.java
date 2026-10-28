package sigma.pruebas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sigma.modelo.Autorizacion;
import sigma.modelo.Lote;
import sigma.repositorios.RepositorioAutorizaciones;

public class RepositorioAutorizacionesMemoria implements RepositorioAutorizaciones {
    private final List<Autorizacion> datos = new ArrayList<Autorizacion>();

    @Override
    public List<Autorizacion> buscar(String documento, Lote lote) throws IOException {
        List<Autorizacion> resultado = new ArrayList<Autorizacion>();
        for (Autorizacion autorizacion : listar()) {
            if (autorizacion.getVisitante().getDocumento().equals(documento)
                    && autorizacion.getLote().getNumero() == lote.getNumero()) {
                resultado.add(autorizacion);
            }
        }
        return resultado;
    }

    @Override
    public List<Autorizacion> listar() throws IOException {
        List<Autorizacion> resultado = new ArrayList<Autorizacion>();
        for (Autorizacion autorizacion : datos) { resultado.add(Copias.de(autorizacion, Autorizacion.class)); }
        return resultado;
    }

    @Override
    public void guardar(Autorizacion autorizacion) throws IOException {
        for (Autorizacion actual : datos) {
            if (actual.getId().equals(autorizacion.getId())) { return; }
        }
        datos.add(Copias.de(autorizacion, Autorizacion.class));
    }
}
