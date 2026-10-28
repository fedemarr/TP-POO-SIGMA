package sigma.persistencia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import sigma.modelo.Autorizacion;
import sigma.modelo.Lote;
import sigma.repositorios.RepositorioAutorizaciones;

public class RepositorioAutorizacionesArchivo implements RepositorioAutorizaciones {
    private final AlmacenAccesos almacen;

    public RepositorioAutorizacionesArchivo(AlmacenAccesos almacen) { this.almacen = almacen; }

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
    public List<Autorizacion> listar() throws IOException { return almacen.leerAutorizaciones(); }

    @Override
    public void guardar(Autorizacion autorizacion) throws IOException {
        almacen.guardarAutorizacion(autorizacion);
    }
}
