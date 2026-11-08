package sigma.modelo;

import java.time.LocalDate;

public class Guardia extends Empleado {
    private String turno;
    private PuestoGuardia puesto;

    public Guardia(String legajo, String nombre, LocalDate ingreso, String turno, PuestoGuardia puesto) {
        super(legajo, nombre, ingreso);
        if (puesto == null) throw new IllegalArgumentException("El puesto es obligatorio");
        this.turno = turno;
        this.puesto = puesto;
    }

    @Override
    public String describirRol() {
        return "Guardia de " + puesto + ", turno " + turno;
    }

    public boolean estaEnPorteria() {
        return puesto == PuestoGuardia.PORTERIA;
    }

    public String getTurno() { return turno; }
    public PuestoGuardia getPuesto() { return puesto; }
}
