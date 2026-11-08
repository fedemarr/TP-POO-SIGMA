package sigma.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Barrio implements Serializable {
    public static final int CANTIDAD_LOTES = 150;

    private String nombre;
    private List<Lote> lotes = new ArrayList<>();
    private List<Empleado> personal = new ArrayList<>();
    private List<Equipo> equipos = new ArrayList<>();
    private List<Sector> sectores = new ArrayList<>();
    private List<Notificacion> notificaciones = new ArrayList<>();

    public Barrio(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
        this.nombre = nombre;
    }

    public double calcularSuperficieTotal() {
        double total = 0;
        for (Lote lote : lotes) {
            total += lote.getSuperficie();
        }
        return total;
    }

    public Lote obtenerLote(int numero) {
        for (Lote lote : lotes) {
            if (lote.getNumero() == numero) return lote;
        }
        return null;
    }

    public void agregarLote(Lote lote) {
        if (lote == null || obtenerLote(lote.getNumero()) != null) {
            throw new IllegalArgumentException("Lote invalido o repetido");
        }
        if (lotes.size() >= CANTIDAD_LOTES) {
            throw new IllegalArgumentException("El barrio ya tiene sus " + CANTIDAD_LOTES + " lotes");
        }
        lotes.add(lote);
    }

    public void agregarEmpleado(Empleado empleado) {
        if (empleado == null || buscarEmpleado(empleado.getLegajo()) != null) {
            throw new IllegalArgumentException("Empleado invalido o legajo repetido");
        }
        personal.add(empleado);
    }

    public Empleado buscarEmpleado(String legajo) {
        for (Empleado empleado : personal) {
            if (empleado.getLegajo().equals(legajo)) return empleado;
        }
        return null;
    }

    /** Devuelve el sector con ese nombre; si no existe lo crea (el barrio se compone de sectores). */
    public Sector obtenerSector(String nombreSector) {
        for (Sector sector : sectores) {
            if (sector.getNombre().equalsIgnoreCase(nombreSector)) return sector;
        }
        Sector nuevo = new Sector(nombreSector);
        sectores.add(nuevo);
        return nuevo;
    }

    public void agregarEquipo(Equipo equipo) {
        if (equipo == null) throw new IllegalArgumentException("El equipo es obligatorio");
        if (!equipos.contains(equipo)) {
            equipos.add(equipo);
        }
    }

    public void agregarNotificacion(Notificacion notificacion) {
        if (notificacion == null) throw new IllegalArgumentException("La notificacion es obligatoria");
        notificaciones.add(notificacion);
    }

    public String getNombre() { return nombre; }
    public List<Lote> getLotes() { return new ArrayList<>(lotes); }
    public List<Empleado> getPersonal() { return new ArrayList<>(personal); }
    public List<Equipo> getEquipos() { return new ArrayList<>(equipos); }
    public List<Sector> getSectores() { return new ArrayList<>(sectores); }
    public List<Notificacion> getNotificaciones() { return new ArrayList<>(notificaciones); }
}
