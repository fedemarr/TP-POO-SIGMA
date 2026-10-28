package sigma.pruebas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sigma.modelo.RegistroIngreso;
import sigma.repositorios.RepositorioIngresos;

public class RepositorioIngresosMemoria implements RepositorioIngresos {
    private final List<RegistroIngreso> datos = new ArrayList<RegistroIngreso>();

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
        List<RegistroIngreso> resultado = new ArrayList<RegistroIngreso>();
        for (RegistroIngreso registro : datos) { resultado.add(Copias.de(registro, RegistroIngreso.class)); }
        Collections.sort(resultado);
        return resultado;
    }

    @Override
    public void guardar(RegistroIngreso registro) throws IOException {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId().equals(registro.getId())) {
                datos.set(i, Copias.de(registro, RegistroIngreso.class));
                return;
            }
        }
        datos.add(Copias.de(registro, RegistroIngreso.class));
    }
}
