package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import sigma.excepciones.ReglaNegocioException;

public class RegistroIngreso implements Serializable, Comparable<RegistroIngreso> {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private final Autorizacion autorizacion;
    private final Guardia guardia;
    private final LocalDateTime ingreso;
    private LocalDateTime egreso;

    public RegistroIngreso(Autorizacion autorizacion, Guardia guardia, LocalDateTime ingreso)
            throws ReglaNegocioException {
        this.autorizacion = Objects.requireNonNull(autorizacion, "autorizacion");
        this.guardia = Objects.requireNonNull(guardia, "guardia");
        this.ingreso = Objects.requireNonNull(ingreso, "ingreso");
        if (!guardia.estaEnPorteria() || !autorizacion.estaVigente(ingreso)) {
            throw new ReglaNegocioException("El ingreso requiere porteria y autorizacion vigente.");
        }
    }

    public boolean estaAbierto() { return egreso == null; }

    public void registrarEgreso(LocalDateTime fecha) throws ReglaNegocioException {
        if (fecha == null || fecha.isBefore(ingreso)) {
            throw new ReglaNegocioException("El egreso no puede ser anterior al ingreso.");
        }
        if (!estaAbierto()) { throw new ReglaNegocioException("El ingreso ya tiene egreso."); }
        egreso = fecha;
    }

    @Override
    public int compareTo(RegistroIngreso otro) {
        int orden = ingreso.compareTo(otro.ingreso);
        return orden != 0 ? orden : id.compareTo(otro.id);
    }

    public String getId() { return id; }
    public Autorizacion getAutorizacion() { return autorizacion; }
    public Guardia getGuardia() { return guardia; }
    public LocalDateTime getIngreso() { return ingreso; }
    public LocalDateTime getEgreso() { return egreso; }
}
