package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Equipo implements Serializable {
    private String codigo;
    private LocalDate fechaInstalacion;
    private EstadoEquipo estado = EstadoEquipo.OPERATIVO;
    private Sector sector;
    private List<FallaEquipo> fallas = new ArrayList<>();

    protected Equipo(String codigo, Sector sector, LocalDate instalacion) {
        if (codigo == null || codigo.isBlank() || sector == null || instalacion == null) {
            throw new IllegalArgumentException("Codigo, sector y fecha de instalacion son obligatorios");
        }
        this.codigo = codigo;
        this.sector = sector;
        this.fechaInstalacion = instalacion;
        sector.agregarEquipo(this);
    }

    public FallaEquipo registrarFalla(String descripcion, LocalDateTime fecha) {
        return registrarFalla(descripcion, fecha, "Sin informar");
    }

    /** Sobrecarga que guarda quien informo la falla (un vecino o un guardia). */
    public FallaEquipo registrarFalla(String descripcion, LocalDateTime fecha, String autor) {
        FallaEquipo falla = new FallaEquipo(fecha, descripcion, autor);
        fallas.add(falla);
        cambiarEstado(EstadoEquipo.CON_FALLA);
        return falla;
    }

    public void cambiarEstado(EstadoEquipo nuevo) {
        if (nuevo == null) throw new IllegalArgumentException("El estado es obligatorio");
        estado = nuevo;
    }

    /** Mientras el equipo no este operativo, su ultima falla sigue abierta. */
    public FallaEquipo fallaAbierta() {
        if (estado == EstadoEquipo.OPERATIVO) return null;
        return getUltimaFalla();
    }

    public FallaEquipo getUltimaFalla() {
        if (fallas.isEmpty()) return null;
        return fallas.get(fallas.size() - 1);
    }

    public boolean contieneFalla(FallaEquipo falla) {
        return fallas.contains(falla);
    }

    // Polimorfismo: cada tipo de equipo decide la prioridad de su orden
    public abstract PrioridadOrden prioridadDeFalla(int falladasEnSector);

    public abstract String getTipo();

    public String getCodigo() { return codigo; }
    public Sector getSector() { return sector; }
    public String getNombreSector() { return sector.getNombre(); }
    public EstadoEquipo getEstado() { return estado; }
    public LocalDate getFechaInstalacion() { return fechaInstalacion; }

    @Override
    public String toString() {
        return getTipo() + " " + codigo + " | sector " + sector.getNombre() + " | " + estado;
    }
}
