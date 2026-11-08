package sigma.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Camara extends Equipo {
    private static final int HORAS_PARA_AVISAR = 48;

    private String resolucion;
    private int diasGrabacion;

    public Camara(String codigo, Sector sector, LocalDate instalacion, String resolucion, int diasGrabacion) {
        super(codigo, sector, instalacion);
        if (resolucion == null || resolucion.isBlank() || diasGrabacion <= 0) {
            throw new IllegalArgumentException("Resolucion y dias de grabacion son obligatorios");
        }
        this.resolucion = resolucion;
        this.diasGrabacion = diasGrabacion;
    }

    // Una falla en una camara siempre genera una orden de prioridad alta
    @Override
    public PrioridadOrden prioridadDeFalla(int falladasEnSector) {
        return PrioridadOrden.ALTA;
    }

    /** Una camara sin funcionar por mas de 48 horas se informa al administrador. */
    public boolean requiereAviso(LocalDateTime ahora) {
        if (ahora == null) throw new IllegalArgumentException("La fecha es obligatoria");
        FallaEquipo falla = fallaAbierta();
        if (falla == null) return false;
        return ahora.isAfter(falla.getFecha().plusHours(HORAS_PARA_AVISAR));
    }

    @Override
    public String getTipo() {
        return "Camara";
    }

    public String getResolucion() { return resolucion; }
    public int getDiasGrabacion() { return diasGrabacion; }
}
