package sigma.gestores;

import sigma.excepciones.AutorizacionInvalidaException;
import sigma.excepciones.IngresoDuplicadoException;
import sigma.excepciones.ReglaNegocioException;
import sigma.modelo.Autorizacion;
import sigma.modelo.Guardia;
import sigma.modelo.Lote;
import sigma.modelo.Persona;
import sigma.modelo.RegistroIngreso;
import sigma.modelo.Visitante;
import sigma.repositorios.RepositorioAutorizaciones;
import sigma.repositorios.RepositorioIngresos;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Controlador del ingreso de visitas. Sigue el diagrama de secuencia CU-22. */
public class GestorAccesos {
    private final RepositorioAutorizaciones autorizaciones;
    private final RepositorioIngresos ingresos;

    public GestorAccesos(RepositorioAutorizaciones autorizaciones, RepositorioIngresos ingresos) {
        if (autorizaciones == null || ingresos == null) throw new IllegalArgumentException("Repositorios obligatorios");
        this.autorizaciones = autorizaciones;
        this.ingresos = ingresos;
    }

    public Autorizacion autorizarVisita(Persona otorgante, Lote lote, Visitante visitante, LocalDate desde, LocalDate hasta,
                                        LocalTime horaDesde, LocalTime horaHasta, boolean recurrente)
            throws AutorizacionInvalidaException, IOException {
        if (otorgante == null || lote == null || desde == null) throw new IllegalArgumentException("Datos obligatorios");
        if (!lote.estaAutorizada(otorgante, desde)) {
            throw new AutorizacionInvalidaException("La persona no puede autorizar visitas para el lote " + lote.getNumero());
        }
        Autorizacion autorizacion = new Autorizacion(otorgante, lote, visitante, desde, hasta, horaDesde, horaHasta, recurrente);
        // CU-21 A3: si ya existe la misma autorizacion, se devuelve la existente en lugar de duplicarla
        for (Autorizacion existente : autorizaciones.buscar(visitante.getDocumento(), lote)) {
            if (existente.esIgualA(autorizacion)) {
                return existente;
            }
        }
        autorizaciones.guardar(autorizacion);
        return autorizacion;
    }

    public RegistroIngreso registrarIngreso(String documento, Lote lote, Guardia guardia, LocalDateTime fecha)
            throws ReglaNegocioException, IOException {
        if (documento == null || lote == null || guardia == null || fecha == null) {
            throw new IllegalArgumentException("Datos obligatorios");
        }
        if (!guardia.estaEnPorteria()) {
            throw new ReglaNegocioException("Solo el guardia de porteria registra ingresos");
        }
        // Se busca entre todas las autorizaciones del visitante una que este vigente
        Autorizacion vigente = null;
        List<Autorizacion> candidatas = autorizaciones.buscar(documento, lote);
        for (Autorizacion candidata : candidatas) {
            if (candidata.estaVigente(fecha)) {
                vigente = candidata;
                break;
            }
        }
        if (vigente == null) {
            throw new AutorizacionInvalidaException("No hay una autorizacion vigente para " + documento);
        }
        if (ingresos.buscarAbierto(documento) != null) {
            throw new IngresoDuplicadoException("La persona ya esta dentro del barrio y no registro su egreso");
        }
        RegistroIngreso registro = new RegistroIngreso(vigente, guardia, fecha);
        ingresos.guardar(registro);
        return registro;
    }

    public void registrarEgreso(String documento, LocalDateTime fecha) throws ReglaNegocioException, IOException {
        if (documento == null || fecha == null) throw new IllegalArgumentException("Datos obligatorios");
        RegistroIngreso abierto = ingresos.buscarAbierto(documento);
        if (abierto == null) {
            throw new ReglaNegocioException("No hay un ingreso abierto para " + documento);
        }
        abierto.registrarEgreso(fecha);
        ingresos.guardar(abierto);
    }
}
