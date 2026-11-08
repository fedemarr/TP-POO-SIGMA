package sigma.persistencia;

import sigma.modelo.Barrio;
import sigma.modelo.Notificacion;
import sigma.repositorios.RepositorioNotificaciones;

import java.io.IOException;
import java.util.List;

public class RepositorioNotificacionesArchivo implements RepositorioNotificaciones {
    private final AlmacenArchivos almacen;

    public RepositorioNotificacionesArchivo(AlmacenArchivos almacen) {
        if (almacen == null) throw new IllegalArgumentException("El almacen es obligatorio");
        this.almacen = almacen;
    }

    @Override
    public boolean guardarSiNoExiste(Notificacion notificacion) throws IOException {
        if (notificacion == null) throw new IllegalArgumentException("La notificacion es obligatoria");
        Barrio barrio = almacen.getDatos().getBarrio();
        for (Notificacion existente : barrio.getNotificaciones()) {
            if (existente.getClaveUnica().equals(notificacion.getClaveUnica())) return false;
        }
        barrio.agregarNotificacion(notificacion);
        almacen.guardar();
        return true;
    }

    @Override
    public List<Notificacion> listar() {
        return almacen.getDatos().getBarrio().getNotificaciones();
    }
}
