package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDate;

public abstract class Empleado implements Serializable {
    private String legajo;
    private String nombre;
    private LocalDate fechaIngreso;

    protected Empleado(String legajo, String nombre, LocalDate ingreso) {
        if (legajo == null || legajo.isBlank() || nombre == null || nombre.isBlank() || ingreso == null) {
            throw new IllegalArgumentException("Legajo, nombre y fecha de ingreso son obligatorios");
        }
        this.legajo = legajo;
        this.nombre = nombre;
        this.fechaIngreso = ingreso;
    }

    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }

    // Cada tipo de empleado describe su rol (se sobrescribe en cada subclase)
    public abstract String describirRol();

    @Override
    public String toString() {
        return legajo + " - " + nombre + " (" + describirRol() + ")";
    }
}
