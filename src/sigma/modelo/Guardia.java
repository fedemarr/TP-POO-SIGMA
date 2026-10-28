package sigma.modelo;

import java.time.LocalDate;
import java.util.Objects;

public class Guardia extends Empleado {
    private static final long serialVersionUID = 1L;
    private final String turno;
    private final PuestoGuardia puesto;

    public Guardia(String legajo, String nombre, LocalDate ingreso, String turno, PuestoGuardia puesto) {
        super(legajo, nombre, ingreso);
        if (turno == null || turno.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el turno.");
        }
        this.turno = turno.trim();
        this.puesto = Objects.requireNonNull(puesto, "puesto");
    }

    @Override
    public String describirRol() { return "Guardia de " + puesto + " - turno " + turno; }

    public boolean estaEnPorteria() { return puesto == PuestoGuardia.PORTERIA; }
}
