package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Empleado implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String legajo;
    private final String nombre;
    private final LocalDate fechaIngreso;

    protected Empleado(String legajo, String nombre, LocalDate fechaIngreso) {
        if (legajo == null || legajo.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar legajo y nombre del empleado.");
        }
        this.legajo = legajo.trim();
        this.nombre = nombre.trim();
        this.fechaIngreso = Objects.requireNonNull(fechaIngreso, "fechaIngreso");
    }

    public String getLegajo() { return legajo; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public abstract String describirRol();
}
