package sigma.modelo;

import java.time.LocalDate;

public class EmpleadoMantenimiento extends Empleado {
    private Especialidad especialidad;

    public EmpleadoMantenimiento(String legajo, String nombre, LocalDate ingreso, Especialidad especialidad) {
        super(legajo, nombre, ingreso);
        if (especialidad == null) throw new IllegalArgumentException("La especialidad es obligatoria");
        this.especialidad = especialidad;
    }

    @Override
    public String describirRol() {
        return "Mantenimiento - " + especialidad;
    }

    public Especialidad getEspecialidad() { return especialidad; }
}
