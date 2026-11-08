package sigma.repositorios;

import sigma.modelo.Notificacion;

import java.io.IOException;
import java.util.List;

public interface RepositorioNotificaciones {
    /** Guarda el aviso solo si su clave unica no existe. Devuelve true si lo guardo. */
    boolean guardarSiNoExiste(Notificacion notificacion) throws IOException;

    List<Notificacion> listar();
}
