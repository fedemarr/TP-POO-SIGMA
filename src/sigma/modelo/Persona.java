package sigma.modelo;

import java.io.Serializable;

public class Persona implements Serializable {
    private String nombre;
    private String apellido;
    private String documento;
    private String telefono;
    private String correoElectronico;

    public Persona(String nombre, String apellido, String documento, String telefono, String correo) {
        this.nombre = obligatorio(nombre, "nombre");
        this.apellido = obligatorio(apellido, "apellido");
        this.documento = obligatorio(documento, "documento");
        this.telefono = telefono;
        this.correoElectronico = correo;
    }

    private static String obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    public void actualizarContacto(String telefono, String correo) {
        this.telefono = telefono;
        this.correoElectronico = correo;
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getCorreoElectronico() { return correoElectronico; }

    // Dos personas son la misma si tienen el mismo documento
    @Override
    public boolean equals(Object otro) {
        if (this == otro) return true;
        if (!(otro instanceof Persona)) return false;
        Persona persona = (Persona) otro;
        return documento.equals(persona.documento);
    }

    @Override
    public int hashCode() {
        return documento.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (DNI " + documento + ")";
    }
}
