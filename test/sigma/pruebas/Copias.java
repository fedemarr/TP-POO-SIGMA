package sigma.pruebas;

import java.io.*;

final class Copias {
    private Copias() { }

    static <T extends Serializable> T de(T objeto, Class<T> tipo) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) { salida.writeObject(objeto); }
        try (ObjectInputStream entrada = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return tipo.cast(entrada.readObject());
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
