package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Comentario implements Serializable {
    private String autor;
    private LocalDateTime fecha;
    private String texto;

    public Comentario(String autor, LocalDateTime fecha, String texto) {
        if (autor == null || autor.isBlank() || fecha == null || texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Autor, fecha y texto son obligatorios");
        }
        this.autor = autor;
        this.fecha = fecha;
        this.texto = texto;
    }

    public String getAutor() { return autor; }
    public LocalDateTime getFecha() { return fecha; }
    public String getTexto() { return texto; }

    @Override
    public String toString() {
        return fecha + " - " + autor + ": " + texto;
    }
}
