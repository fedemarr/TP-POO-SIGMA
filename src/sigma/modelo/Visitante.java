package sigma.modelo;

import java.io.Serializable;

public class Visitante implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombre;
    private final String documento;

    public Visitante(String nombre, String documento) {
        if (nombre == null || nombre.trim().isEmpty() || documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar nombre y documento del visitante.");
        }
        this.nombre = nombre.trim();
        this.documento = documento.trim();
    }

    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }
}
