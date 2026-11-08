package sigma.modelo;

import java.time.LocalDate;

public class Administrador extends Empleado {
    public Administrador(String legajo, String nombre, LocalDate ingreso) {
        super(legajo, nombre, ingreso);
    }

    @Override
    public String describirRol() {
        return "Administrador";
    }
}
