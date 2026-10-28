package sigma.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import sigma.modelo.Autorizacion;
import sigma.modelo.RegistroIngreso;

/** Una instantanea compartida por ambos repositorios. Uso local, una instancia a la vez. */
public class AlmacenAccesos {
    private final Path archivo;

    public AlmacenAccesos(Path archivo) {
        this.archivo = archivo.toAbsolutePath().normalize();
    }

    // Leer desde disco devuelve objetos independientes: un cambio fallido no altera los confirmados.
    private Estado leer() throws IOException {
        if (!Files.exists(archivo)) { return new Estado(); }
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(archivo))) {
            Object leido = entrada.readObject();
            if (!(leido instanceof Estado)) {
                throw new IOException("El archivo no contiene datos de accesos de SIGMA.");
            }
            return (Estado) leido;
        } catch (ClassNotFoundException e) {
            throw new IOException("No se reconoce el formato del archivo de accesos.", e);
        }
    }

    private void escribir(Estado estado) throws IOException {
        Files.createDirectories(archivo.getParent());
        Path temporal = Files.createTempFile(archivo.getParent(), "sigma-accesos-", ".tmp");
        try {
            try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(temporal))) {
                salida.writeObject(estado);
            }
            // Si el sistema de archivos no permite reemplazo atomico, se informa el error.
            Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public synchronized List<Autorizacion> leerAutorizaciones() throws IOException {
        return new ArrayList<Autorizacion>(leer().autorizaciones);
    }

    public synchronized List<RegistroIngreso> leerIngresos() throws IOException {
        return new ArrayList<RegistroIngreso>(leer().ingresos);
    }

    public synchronized void guardarAutorizacion(Autorizacion autorizacion) throws IOException {
        Estado estado = leer();
        for (Autorizacion existente : estado.autorizaciones) {
            if (existente.getId().equals(autorizacion.getId())) { return; }
        }
        estado.autorizaciones.add(autorizacion);
        escribir(estado);
    }

    public synchronized void guardarIngreso(RegistroIngreso registro) throws IOException {
        Estado estado = leer();
        for (int i = 0; i < estado.ingresos.size(); i++) {
            RegistroIngreso actual = estado.ingresos.get(i);
            if (actual.getId().equals(registro.getId())) {
                if (!actual.estaAbierto() || !actual.getIngreso().equals(registro.getIngreso())
                        || !actual.getAutorizacion().getId().equals(registro.getAutorizacion().getId())) {
                    throw new IOException("No se puede reemplazar un ingreso cerrado ni cambiar su origen.");
                }
                estado.ingresos.set(i, registro);
                escribir(estado);
                return;
            }
            if (actual.estaAbierto() && registro.estaAbierto()
                    && actual.getAutorizacion().getVisitante().getDocumento()
                    .equals(registro.getAutorizacion().getVisitante().getDocumento())) {
                throw new IOException("Ya existe un ingreso abierto para esa persona.");
            }
        }
        estado.ingresos.add(registro);
        escribir(estado);
    }

    private static class Estado implements Serializable {
        private static final long serialVersionUID = 1L;
        private final ArrayList<Autorizacion> autorizaciones = new ArrayList<Autorizacion>();
        private final ArrayList<RegistroIngreso> ingresos = new ArrayList<RegistroIngreso>();
    }
}
