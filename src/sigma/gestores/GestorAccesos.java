package sigma.gestores;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import sigma.excepciones.AutorizacionInvalidaException;
import sigma.excepciones.IngresoDuplicadoException;
import sigma.excepciones.ReglaNegocioException;
import sigma.modelo.*;
import sigma.repositorios.RepositorioAutorizaciones;
import sigma.repositorios.RepositorioIngresos;

public class GestorAccesos {
    private final RepositorioAutorizaciones autorizaciones;
    private final RepositorioIngresos ingresos;

    public GestorAccesos(RepositorioAutorizaciones autorizaciones, RepositorioIngresos ingresos) {
        this.autorizaciones = Objects.requireNonNull(autorizaciones, "autorizaciones");
        this.ingresos = Objects.requireNonNull(ingresos, "ingresos");
    }

    public Autorizacion autorizarVisita(Persona otorgante, Lote lote, Visitante visitante,
            LocalDate desde, LocalDate hasta, LocalTime horaDesde, LocalTime horaHasta,
            boolean recurrente) throws ReglaNegocioException, IOException {
        Autorizacion nueva = new Autorizacion(otorgante, lote, visitante, desde, hasta,
                horaDesde, horaHasta, recurrente);
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            if (!lote.estaAutorizada(otorgante, dia)) {
                throw new AutorizacionInvalidaException("El vecino no esta autorizado en el lote para esas fechas.");
            }
        }
        for (Autorizacion existente : autorizaciones.buscar(visitante.getDocumento(), lote)) {
            if (existente.mismaVigencia(nueva)) { return existente; }
        }
        autorizaciones.guardar(nueva);
        return nueva;
    }

    public RegistroIngreso registrarIngreso(String documento, Lote lote, Guardia guardia,
            LocalDateTime fecha) throws ReglaNegocioException, IOException {
        Objects.requireNonNull(guardia, "guardia");
        Objects.requireNonNull(lote, "lote");
        Objects.requireNonNull(fecha, "fecha");
        if (!guardia.estaEnPorteria()) {
            throw new ReglaNegocioException("Solo un guardia de porteria puede habilitar el ingreso.");
        }
        documento = validarDocumento(documento);
        Autorizacion vigente = null;
        for (Autorizacion candidata : autorizaciones.buscar(documento, lote)) {
            if (candidata.estaVigente(fecha)) {
                vigente = candidata;
                break;
            }
        }
        if (vigente == null) {
            throw new AutorizacionInvalidaException("No hay autorizacion vigente para ese documento y lote.");
        }
        if (ingresos.buscarAbierto(documento) != null) {
            throw new IngresoDuplicadoException("La persona ya tiene un ingreso sin egreso.");
        }
        // Se evita cargar movimientos anteriores a una visita ya finalizada.
        for (RegistroIngreso anterior : ingresos.listar()) {
            if (anterior.getAutorizacion().getVisitante().getDocumento().equals(documento)
                    && anterior.getEgreso() != null && fecha.isBefore(anterior.getEgreso())) {
                throw new ReglaNegocioException("El ingreso es anterior al ultimo egreso de la persona.");
            }
        }
        RegistroIngreso nuevo = new RegistroIngreso(vigente, guardia, fecha);
        ingresos.guardar(nuevo);
        return nuevo;
    }

    public void registrarEgreso(String documento, LocalDateTime fecha)
            throws ReglaNegocioException, IOException {
        RegistroIngreso abierto = ingresos.buscarAbierto(validarDocumento(documento));
        if (abierto == null) { throw new ReglaNegocioException("La persona no tiene un ingreso abierto."); }
        abierto.registrarEgreso(fecha);
        ingresos.guardar(abierto);
    }

    private String validarDocumento(String documento) throws ReglaNegocioException {
        if (documento == null || documento.trim().isEmpty()) {
            throw new ReglaNegocioException("Debe indicar un documento.");
        }
        return documento.trim();
    }
}
