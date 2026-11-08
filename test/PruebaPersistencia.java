import sigma.AplicacionSIGMA;
import sigma.modelo.Lote;
import sigma.modelo.MedioPago;
import sigma.modelo.Pago;
import sigma.modelo.Reclamo;
import sigma.modelo.TipoReclamo;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/** Los datos se conservan entre ejecuciones: se guarda, se vuelve a abrir y se verifica. */
public class PruebaPersistencia {
    public static void main(String[] args) throws Exception {
        Path archivo = Paths.get("build", "prueba-sigma.dat");
        borrarSiExiste(archivo);
        try {
            AplicacionSIGMA primera = new AplicacionSIGMA(archivo);
            Verificar.verdadero(primera.getBarrio().getLotes().size() == 150, "datos iniciales: 150 lotes");
            Lote lote1 = primera.getBarrio().obtenerLote(1);
            Reclamo reclamo = primera.getGestorReclamos().registrarReclamo(lote1, lote1.obtenerTitular(LocalDate.now()),
                    TipoReclamo.MANTENIMIENTO, "Perdida de agua", LocalDateTime.now());
            primera.getGestorExpensas().emitirExpensas(primera.getBarrio(), YearMonth.of(2026, 9), new BigDecimal("900000"));
            primera.getGestorExpensas().registrarPago(lote1, new BigDecimal("1000"), MedioPago.EFECTIVO, LocalDate.of(2026, 9, 5));

            // Se vuelve a abrir el mismo archivo, como si se reiniciara el programa
            AplicacionSIGMA segunda = new AplicacionSIGMA(archivo);
            Lote recargado = segunda.getBarrio().obtenerLote(1);
            Verificar.verdadero(segunda.getGestorReclamos().buscarReclamo(reclamo.getNumero()) != null, "reclamo guardado");
            Verificar.verdadero(recargado.getExpensas().size() == 1, "expensa guardada");
            Pago pago = segunda.getGestorExpensas().consultarPagos(recargado).get(0);
            Verificar.verdadero(pago.getLote() == recargado, "el pago apunta al mismo lote del barrio");
            Verificar.igual("1000", pago.getImporte(), "importe del pago");
            BigDecimal capital = recargado.getExpensas().get(0).capitalPendiente();
            Verificar.verdadero(capital.compareTo(recargado.getExpensas().get(0).getImporte()) < 0, "el pago quedo imputado");

            Verificar.verdadero(!Files.exists(Paths.get(archivo + ".tmp")), "no queda el archivo temporal");
            System.out.println("PruebaPersistencia: OK");
        } finally {
            borrarSiExiste(archivo);
        }
    }

    private static void borrarSiExiste(Path archivo) throws Exception {
        if (Files.exists(archivo)) {
            Files.delete(archivo);
        }
    }
}
