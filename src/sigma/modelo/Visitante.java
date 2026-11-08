package sigma.modelo;

import java.io.Serializable;

public class Visitante implements Serializable {
    private String nombre;
    private String documento;

    public Visitante(String nombre, String documento) {
        if (nombre == null || nombre.isBlank() || documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("Nombre y documento del visitante son obligatorios");
        }
        this.nombre = nombre;
        this.documento = documento;
    }

    public String getNombre() { return nombre; }
    public String getDocumento() { return documento; }

    @Override
    public String toString() {
        return nombre + " (DNI " + documento + ")";
    }
}
