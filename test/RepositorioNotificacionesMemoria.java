import sigma.modelo.Notificacion;
import sigma.repositorios.RepositorioNotificaciones;

import java.util.ArrayList;
import java.util.List;

public class RepositorioNotificacionesMemoria implements RepositorioNotificaciones {
    private final List<Notificacion> notificaciones = new ArrayList<>();

    @Override
    public boolean guardarSiNoExiste(Notificacion notificacion) {
        for (Notificacion existente : notificaciones) {
            if (existente.getClaveUnica().equals(notificacion.getClaveUnica())) return false;
        }
        notificaciones.add(notificacion);
        return true;
    }

    @Override
    public List<Notificacion> listar() {
        return new ArrayList<>(notificaciones);
    }
}
