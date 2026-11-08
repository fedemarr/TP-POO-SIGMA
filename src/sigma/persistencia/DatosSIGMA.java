package sigma.persistencia;

import sigma.modelo.Autorizacion;
import sigma.modelo.Barrio;
import sigma.modelo.OrdenMantenimiento;
import sigma.modelo.Pago;
import sigma.modelo.Reclamo;
import sigma.modelo.RegistroIngreso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Todo el estado de la aplicacion. Se guarda como un unico objeto serializado,
 * asi las referencias entre objetos (un pago y su lote, una orden y su equipo)
 * se conservan al volver a abrir el programa.
 * El barrio contiene los lotes (con sus residentes y expensas), el personal, los equipos y las notificaciones.
 */
public class DatosSIGMA implements Serializable {
    private Barrio barrio;
    private List<Reclamo> reclamos = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();
    private List<OrdenMantenimiento> ordenes = new ArrayList<>();
    private List<Autorizacion> autorizaciones = new ArrayList<>();
    private List<RegistroIngreso> ingresos = new ArrayList<>();

    public DatosSIGMA(Barrio barrio) {
        if (barrio == null) throw new IllegalArgumentException("El barrio es obligatorio");
        this.barrio = barrio;
    }

    // Los repositorios de archivo trabajan directamente sobre estas listas
    public Barrio getBarrio() { return barrio; }
    List<Reclamo> getReclamos() { return reclamos; }
    List<Pago> getPagos() { return pagos; }
    List<OrdenMantenimiento> getOrdenes() { return ordenes; }
    List<Autorizacion> getAutorizaciones() { return autorizaciones; }
    List<RegistroIngreso> getIngresos() { return ingresos; }
}
