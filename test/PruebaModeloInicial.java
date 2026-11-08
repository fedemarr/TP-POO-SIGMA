import sigma.excepciones.ReglaNegocioException;
import sigma.modelo.Administrador;
import sigma.modelo.Barrio;
import sigma.modelo.Empleado;
import sigma.modelo.EmpleadoMantenimiento;
import sigma.modelo.Especialidad;
import sigma.modelo.Guardia;
import sigma.modelo.Lote;
import sigma.modelo.Persona;
import sigma.modelo.PuestoGuardia;
import sigma.modelo.Residencia;
import sigma.modelo.TipoResidente;

import java.time.LocalDate;

/** Barrio, lotes, residentes y personal. */
public class PruebaModeloInicial {
    public static void main(String[] args) throws Exception {
        LocalDate enero = LocalDate.of(2026, 1, 1);
        LocalDate noviembre = LocalDate.of(2026, 11, 7);

        Persona ana = new Persona("Ana", "Perez", "123", "111", "ana@mail.com");
        ana.actualizarContacto("222", "nuevo@mail.com");
        Verificar.verdadero(ana.getTelefono().equals("222"), "actualizar contacto");

        Barrio barrio = new Barrio("Los Aromos");
        Lote lote = new Lote(1, 500);
        barrio.agregarLote(lote);
        barrio.agregarLote(new Lote(2, 1500));
        Verificar.verdadero(barrio.obtenerLote(1) == lote, "buscar lote por numero");
        Verificar.verdadero(barrio.calcularSuperficieTotal() == 2000, "superficie total");
        Verificar.verdadero(lote.calcularCoeficiente(2000) == 0.25, "coeficiente del lote");

        // Titular y familiar: el familiar solo puede presentar reclamos si el titular lo autoriza
        Persona hijo = new Persona("Juan", "Perez", "456", "333", "juan@mail.com");
        lote.agregarResidencia(new Residencia(ana, TipoResidente.TITULAR, enero, null, true));
        Residencia residenciaHijo = new Residencia(hijo, TipoResidente.FAMILIAR, enero, null, false);
        lote.agregarResidencia(residenciaHijo);
        Verificar.verdadero(lote.estaAutorizada(ana, noviembre), "el titular esta autorizado");
        Verificar.verdadero(!lote.estaAutorizada(hijo, noviembre), "familiar sin autorizacion");
        residenciaHijo.autorizar();
        Verificar.verdadero(lote.estaAutorizada(hijo, noviembre), "familiar autorizado por el titular");
        Verificar.verdadero(lote.obtenerTitular(noviembre).equals(ana), "titular vigente");

        // Un lote tiene un unico titular vigente
        try {
            lote.agregarResidencia(new Residencia(hijo, TipoResidente.TITULAR, enero, null, true));
            throw new AssertionError("Se permitio un segundo titular");
        } catch (ReglaNegocioException esperada) {
            System.out.println("  Rechazo esperado: " + esperada.getMessage());
        }

        // Polimorfismo: cada empleado describe su rol
        Empleado administrador = new Administrador("A1", "Sol", enero);
        Guardia guardia = new Guardia("G1", "Luis", enero, "noche", PuestoGuardia.PORTERIA);
        Empleado tecnico = new EmpleadoMantenimiento("M1", "Rita", enero, Especialidad.ELECTRICIDAD);
        Verificar.verdadero(administrador.describirRol().equals("Administrador"), "rol administrador");
        Verificar.verdadero(guardia.estaEnPorteria(), "guardia en porteria");
        Verificar.verdadero(tecnico.describirRol().contains("ELECTRICIDAD"), "rol de mantenimiento");

        System.out.println("PruebaModeloInicial: OK");
    }
}
