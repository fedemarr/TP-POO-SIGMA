package sigma.persistencia;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import sigma.modelo.RegistroIngreso;
import sigma.repositorios.RepositorioIngresos;

public class RepositorioIngresosArchivo implements RepositorioIngresos {
    private final AlmacenAccesos almacen;

    public RepositorioIngresosArchivo(AlmacenAccesos almacen) { this.almacen = almacen; }

    @Override
    public RegistroIngreso buscarAbierto(String documento) throws IOException {
        for (RegistroIngreso registro : listar()) {
            if (registro.estaAbierto()
                    && registro.getAutorizacion().getVisitante().getDocumento().equals(documento)) {
                return registro;
            }
        }
        return null;
    }

    @Override
    public List<RegistroIngreso> listar() throws IOException {
        List<RegistroIngreso> resultado = almacen.leerIngresos();
        Collections.sort(resultado);
        return resultado;
    }

    @Override
    public void guardar(RegistroIngreso registro) throws IOException { almacen.guardarIngreso(registro); }
}
