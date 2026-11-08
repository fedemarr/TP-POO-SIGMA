package sigma.persistencia;

import sigma.modelo.RegistroIngreso;
import sigma.repositorios.RepositorioIngresos;

import java.io.IOException;
import java.util.List;

public class RepositorioIngresosArchivo implements RepositorioIngresos {
    private final AlmacenArchivos almacen;

    public RepositorioIngresosArchivo(AlmacenArchivos almacen) {
        if (almacen == null) throw new IllegalArgumentException("El almacen es obligatorio");
        this.almacen = almacen;
    }

    @Override
    public RegistroIngreso buscarAbierto(String documento) {
        for (RegistroIngreso registro : almacen.getDatos().getIngresos()) {
            if (registro.estaAbierto() && registro.getDocumento().equals(documento)) return registro;
        }
        return null;
    }

    @Override
    public void guardar(RegistroIngreso registro) throws IOException {
        if (registro == null) throw new IllegalArgumentException("El registro es obligatorio");
        List<RegistroIngreso> ingresos = almacen.getDatos().getIngresos();
        if (!ingresos.contains(registro)) ingresos.add(registro);
        almacen.guardar();
    }
}
