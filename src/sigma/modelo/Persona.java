package sigma.modelo;

import java.io.Serializable;
import java.util.Objects;

public class Persona implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombre;
    private final String apellido;
    private final String documento;
    private String telefono;
    private String correoElectronico;

    public Persona(String nombre, String apellido, String documento, String telefono, String correo) {
        this.nombre = texto(nombre, "nombre");
        this.apellido = texto(apellido, "apellido");
        this.documento = texto(documento, "documento");
        this.telefono = texto(telefono, "telefono");
        this.correoElectronico = texto(correo, "correo");
    }

    private static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar " + campo + ".");
        }
        return valor.trim();
    }

    public String getDocumento() { return documento; }
    public String getNombreCompleto() { return nombre + " " + apellido; }
    public String getTelefono() { return telefono; }
    public String getCorreoElectronico() { return correoElectronico; }

    public void actualizarContacto(String telefono, String correo) {
        String nuevoTelefono = texto(telefono, "telefono");
        String nuevoCorreo = texto(correo, "correo");
        this.telefono = nuevoTelefono;
        this.correoElectronico = nuevoCorreo;
    }

    @Override
    public boolean equals(Object otro) {
        if (!(otro instanceof Persona)) { return false; }
        return documento.equals(((Persona) otro).documento);
    }

    @Override
    public int hashCode() { return Objects.hash(documento); }
}
