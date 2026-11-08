package sigma.modelo;

import sigma.excepciones.ReglaNegocioException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Lote implements Serializable {
    // Un lote es moroso cuando supera esta cantidad de periodos vencidos e impagos
    private static final int PERIODOS_PARA_MOROSIDAD = 3;

    private int numero;
    private double superficie;
    private boolean moroso;
    private List<Residencia> residencias = new ArrayList<>();
    private List<Expensa> expensas = new ArrayList<>();

    public Lote(int numero, double superficie) {
        if (numero <= 0 || superficie <= 0) {
            throw new IllegalArgumentException("Numero o superficie invalida");
        }
        this.numero = numero;
        this.superficie = superficie;
    }

    public double calcularCoeficiente(double superficieTotal) {
        if (superficieTotal <= 0) throw new IllegalArgumentException("Superficie total invalida");
        return superficie / superficieTotal;
    }

    // Puede presentar reclamos o autorizar visitas el titular, o un residente autorizado por el titular
    public boolean estaAutorizada(Persona persona, LocalDate fecha) {
        if (persona == null || fecha == null) throw new IllegalArgumentException("Datos obligatorios");
        for (Residencia residencia : residencias) {
            boolean esLaPersona = residencia.getPersona().equals(persona);
            boolean tienePermiso = residencia.getTipo() == TipoResidente.TITULAR || residencia.isAutorizadaPorTitular();
            if (esLaPersona && residencia.estaVigente(fecha) && tienePermiso) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal calcularDeuda(LocalDate fecha, CalculadoraMora mora) {
        if (fecha == null || mora == null) throw new IllegalArgumentException("Datos obligatorios");
        BigDecimal total = BigDecimal.ZERO;
        for (Expensa expensa : expensas) {
            total = total.add(expensa.calcularSaldo(fecha, mora));
        }
        return total;
    }

    /**
     * Mas de tres periodos vencidos con saldo marcan el lote como moroso.
     * Si ya estaba marcado, conserva la condicion mientras tenga deuda vencida,
     * y la pierde cuando la regulariza.
     */
    public boolean actualizarMorosidad(List<Expensa> expensas, LocalDate fecha) {
        if (expensas == null || fecha == null) throw new IllegalArgumentException("Datos obligatorios");
        int vencidasImpagas = 0;
        for (Expensa expensa : expensas) {
            if (expensa.estaVencida(fecha) && expensa.capitalPendiente().signum() > 0) {
                vencidasImpagas++;
            }
        }
        if (vencidasImpagas > PERIODOS_PARA_MOROSIDAD) {
            moroso = true;
        } else if (vencidasImpagas == 0) {
            moroso = false;
        }
        return moroso;
    }

    public void agregarResidencia(Residencia residencia) throws ReglaNegocioException {
        if (residencia == null) throw new IllegalArgumentException("La residencia es obligatoria");
        if (residencia.getTipo() == TipoResidente.TITULAR && obtenerTitular(residencia.getDesde()) != null) {
            throw new ReglaNegocioException("El lote " + numero + " ya tiene un titular vigente");
        }
        residencias.add(residencia);
    }

    public Persona obtenerTitular(LocalDate fecha) {
        for (Residencia residencia : residencias) {
            if (residencia.getTipo() == TipoResidente.TITULAR && residencia.estaVigente(fecha)) {
                return residencia.getPersona();
            }
        }
        return null;
    }

    public Persona buscarResidente(String documento, LocalDate fecha) {
        for (Residencia residencia : residencias) {
            if (residencia.getPersona().getDocumento().equals(documento) && residencia.estaVigente(fecha)) {
                return residencia.getPersona();
            }
        }
        return null;
    }

    public void agregarExpensa(Expensa expensa) {
        if (expensa == null || expensa.getLote() != this) {
            throw new IllegalArgumentException("La expensa no corresponde a este lote");
        }
        expensas.add(expensa);
    }

    public List<Expensa> getExpensas() { return new ArrayList<>(expensas); }
    public List<Residencia> getResidencias() { return new ArrayList<>(residencias); }
    public int getNumero() { return numero; }
    public double getSuperficie() { return superficie; }
    public boolean isMoroso() { return moroso; }

    @Override
    public String toString() {
        String texto = "Lote " + numero + " (" + superficie + " m2)";
        if (moroso) {
            texto = texto + " - MOROSO";
        }
        return texto;
    }
}
