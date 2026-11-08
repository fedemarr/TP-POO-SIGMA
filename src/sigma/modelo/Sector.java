package sigma.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Sector implements Serializable {
    private String nombre;
    private List<Equipo> equipos = new ArrayList<>();

    public Sector(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        this.nombre = nombre;
    }

    public int contarLuminariasFalladas() {
        int cantidad = 0;
        for (Equipo equipo : equipos) {
            if (equipo instanceof Luminaria && equipo.getEstado() != EstadoEquipo.OPERATIVO) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public void agregarEquipo(Equipo equipo) {
        if (equipo == null) throw new IllegalArgumentException("El equipo es obligatorio");
        equipos.add(equipo);
    }

    public String getNombre() { return nombre; }
}
