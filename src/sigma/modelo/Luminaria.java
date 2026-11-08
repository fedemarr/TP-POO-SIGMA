package sigma.modelo;

import java.time.LocalDate;

public class Luminaria extends Equipo {
    private static final int MAXIMO_FALLADAS_POR_SECTOR = 3;

    private String tipoLampara;
    private double potencia;

    public Luminaria(String codigo, Sector sector, LocalDate instalacion, String tipoLampara, double potencia) {
        super(codigo, sector, instalacion);
        if (tipoLampara == null || tipoLampara.isBlank() || potencia <= 0) {
            throw new IllegalArgumentException("Tipo de lampara y potencia son obligatorios");
        }
        this.tipoLampara = tipoLampara;
        this.potencia = potencia;
    }

    // Normal, salvo que fallen mas de 3 luminarias del mismo sector
    @Override
    public PrioridadOrden prioridadDeFalla(int falladasEnSector) {
        if (falladasEnSector > MAXIMO_FALLADAS_POR_SECTOR) {
            return PrioridadOrden.ALTA;
        }
        return PrioridadOrden.NORMAL;
    }

    @Override
    public String getTipo() {
        return "Luminaria";
    }

    public String getTipoLampara() { return tipoLampara; }
    public double getPotencia() { return potencia; }
}
