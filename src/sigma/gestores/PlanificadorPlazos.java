package sigma.gestores;

import sigma.excepciones.ReglaNegocioException;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Revisa las reglas que dependen del paso del tiempo: escalamiento y cierre de reclamos,
 * camaras sin funcionar y morosidad. Se ejecuta al iniciar, antes de cada operacion
 * y (en la ventana) una vez por minuto. Los gestores comparan fechas exactas,
 * por eso ejecutarlo varias veces no repite avisos.
 */
public class PlanificadorPlazos {
    private final GestorReclamos reclamos;
    private final GestorMantenimiento mantenimiento;
    private final GestorExpensas expensas;

    public PlanificadorPlazos(GestorReclamos reclamos, GestorMantenimiento mantenimiento, GestorExpensas expensas) {
        if (reclamos == null || mantenimiento == null || expensas == null) {
            throw new IllegalArgumentException("Gestores obligatorios");
        }
        this.reclamos = reclamos;
        this.mantenimiento = mantenimiento;
        this.expensas = expensas;
    }

    public void ejecutar(LocalDateTime ahora) throws ReglaNegocioException, IOException {
        reclamos.verificarPlazos(ahora);
        mantenimiento.verificarPlazos(ahora);
        expensas.verificarMorosidad(ahora.toLocalDate());
    }
}
