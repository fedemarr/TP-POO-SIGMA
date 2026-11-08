import sigma.modelo.RegistroIngreso;
import sigma.repositorios.RepositorioIngresos;

import java.util.ArrayList;
import java.util.List;

public class RepositorioIngresosMemoria implements RepositorioIngresos {
    private final List<RegistroIngreso> ingresos = new ArrayList<>();

    @Override
    public RegistroIngreso buscarAbierto(String documento) {
        for (RegistroIngreso registro : ingresos) {
            if (registro.estaAbierto() && registro.getDocumento().equals(documento)) return registro;
        }
        return null;
    }

    @Override
    public void guardar(RegistroIngreso registro) {
        if (!ingresos.contains(registro)) ingresos.add(registro);
    }
}
