package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FallaEquipo implements Serializable {
    private LocalDateTime fecha;
    private String descripcion;
    private String registradaPor;
    private List<String> observaciones = new ArrayList<>();

    public FallaEquipo(LocalDateTime fecha, String descripcion, String registradaPor) {
        if (fecha == null || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Fecha y descripcion son obligatorias");
        }
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.registradaPor = registradaPor;
    }

    public void agregarObservacion(String autor, String texto, LocalDateTime fecha) {
        if (autor == null || autor.isBlank() || texto == null || texto.isBlank() || fecha == null) {
            throw new IllegalArgumentException("Autor, texto y fecha son obligatorios");
        }
        observaciones.add(fecha + " " + autor + ": " + texto);
    }

    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getRegistradaPor() { return registradaPor; }
    public List<String> getObservaciones() { return new ArrayList<>(observaciones); }
}
