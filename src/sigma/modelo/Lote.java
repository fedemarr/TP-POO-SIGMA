package sigma.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lote implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final double superficie;
    private final ArrayList<Residencia> residencias = new ArrayList<Residencia>();

    public Lote(int numero, double superficie) {
        if (numero <= 0 || !Double.isFinite(superficie) || superficie <= 0) {
            throw new IllegalArgumentException("Numero y superficie deben ser positivos.");
        }
        this.numero = numero;
        this.superficie = superficie;
    }

    public void agregarResidencia(Residencia residencia) {
        if (residencia == null) { throw new IllegalArgumentException("Debe indicar la residencia."); }
        // El alta y los cambios de titularidad se implementaran en el modulo de vecinos.
        // Por ahora se impide cargar dos titulares en el mismo lote de demostracion.
        for (Residencia actual : residencias) {
            if (actual == residencia || (actual.getTipo() == TipoResidente.TITULAR
                    && residencia.getTipo() == TipoResidente.TITULAR)) {
                throw new IllegalArgumentException("La residencia o el titular ya estan registrados.");
            }
        }
        residencias.add(residencia);
    }

    public boolean estaAutorizada(Persona persona, LocalDate fecha) {
        if (persona == null || fecha == null) { return false; }
        for (Residencia residencia : residencias) {
            if (residencia.getPersona().equals(persona) && residencia.estaVigente(fecha)
                    && residencia.isAutorizadaPorTitular()) {
                return true;
            }
        }
        return false;
    }

    public double calcularCoeficiente(double superficieTotal) {
        if (!Double.isFinite(superficieTotal) || superficieTotal < superficie) {
            throw new IllegalArgumentException("La superficie total debe incluir la del lote.");
        }
        return superficie / superficieTotal;
    }

    public int getNumero() { return numero; }
    public double getSuperficie() { return superficie; }
    public List<Residencia> getResidencias() { return Collections.unmodifiableList(residencias); }
}
