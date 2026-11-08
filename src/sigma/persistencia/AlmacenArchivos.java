package sigma.persistencia;

import sigma.modelo.Barrio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Almacenamiento compartido por todos los repositorios de archivo.
 * Cada vez que se confirma una operacion se guarda el estado completo:
 * primero en un archivo temporal y, cuando quedo bien escrito, se reemplaza el archivo real.
 * Si algo falla, se descartan los cambios en memoria y se vuelve a lo ultimo guardado.
 */
public class AlmacenArchivos {
    private final Path ruta;
    private final Path temporal;
    private final String nombreBarrio;
    private DatosSIGMA datos;

    public AlmacenArchivos(Path ruta, String nombreBarrio) throws IOException {
        if (ruta == null || nombreBarrio == null) throw new IllegalArgumentException("Ruta y nombre obligatorios");
        this.ruta = ruta;
        this.temporal = Paths.get(ruta.toString() + ".tmp");
        this.nombreBarrio = nombreBarrio;
        recuperarTemporal();
        this.datos = leer();
    }

    public DatosSIGMA getDatos() {
        return datos;
    }

    public void guardar() throws IOException {
        try {
            Path carpeta = ruta.toAbsolutePath().getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
            // 1) Se escribe todo en el archivo temporal
            try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(temporal.toString()))) {
                salida.writeObject(datos);
            }
            // 2) Recien cuando el temporal esta completo, reemplaza al archivo real
            if (Files.exists(ruta)) {
                Files.delete(ruta);
            }
            Files.move(temporal, ruta);
        } catch (IOException e) {
            // Se descartan las copias de trabajo: se vuelve al ultimo estado guardado
            if (Files.exists(ruta)) {
                datos = leer();
            }
            throw e;
        } finally {
            // Si el archivo real esta bien, un temporal que haya quedado ya no sirve
            if (Files.exists(ruta) && Files.exists(temporal)) {
                Files.delete(temporal);
            }
        }
    }

    // Si el programa se corto justo despues de borrar el archivo real, el temporal tiene los datos completos
    private void recuperarTemporal() throws IOException {
        if (!Files.exists(ruta) && Files.exists(temporal)) {
            Files.move(temporal, ruta);
        }
    }

    private DatosSIGMA leer() throws IOException {
        if (!Files.exists(ruta)) {
            return new DatosSIGMA(new Barrio(nombreBarrio));
        }
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ruta.toString()))) {
            return (DatosSIGMA) entrada.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("El archivo " + ruta + " no tiene el formato esperado", e);
        }
    }
}
