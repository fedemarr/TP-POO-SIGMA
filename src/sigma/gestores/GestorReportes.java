package sigma.gestores;

import sigma.modelo.CalculadoraMora;
import sigma.modelo.EstadoEquipo;
import sigma.modelo.Equipo;
import sigma.modelo.Expensa;
import sigma.modelo.Lote;
import sigma.modelo.OrdenMantenimiento;
import sigma.modelo.Reclamo;
import sigma.repositorios.RepositorioEquipos;
import sigma.repositorios.RepositorioExpensas;
import sigma.repositorios.RepositorioReclamos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GestorReportes {
    private final RepositorioReclamos reclamos;
    private final RepositorioExpensas expensas;
    private final RepositorioEquipos equipos;
    private final CalculadoraMora mora;

    public GestorReportes(RepositorioReclamos reclamos, RepositorioExpensas expensas,
                          RepositorioEquipos equipos, CalculadoraMora mora) {
        if (reclamos == null || expensas == null || equipos == null || mora == null) {
            throw new IllegalArgumentException("Dependencias obligatorias");
        }
        this.reclamos = reclamos;
        this.expensas = expensas;
        this.equipos = equipos;
        this.mora = mora;
    }

    /** CU-07: reclamos no cerrados por prioridad y fecha; marca los urgentes sin atender por 24 horas. */
    public String reclamosPendientes(LocalDateTime ahora) {
        List<Reclamo> pendientes = new ArrayList<>();
        for (Reclamo reclamo : reclamos.listar()) {
            if (reclamo.estaPendiente()) pendientes.add(reclamo);
        }
        Collections.sort(pendientes);
        String texto = "RECLAMOS PENDIENTES al " + ahora.toLocalDate() + "\n";
        for (Reclamo reclamo : pendientes) {
            texto = texto + reclamo;
            if (reclamo.requiereEscalamiento(ahora)) {
                texto = texto + " | SIN ATENDER HACE MAS DE 24 HORAS";
            }
            texto = texto + "\n";
        }
        if (pendientes.isEmpty()) {
            texto = texto + "No hay reclamos pendientes\n";
        }
        return texto;
    }

    /** CU-25: lotes con deuda vencida, del que mas debe al que menos. */
    public String morosidad(LocalDate fecha) {
        List<FilaMorosidad> filas = new ArrayList<>();
        for (Lote lote : expensas.listarLotes()) {
            FilaMorosidad fila = new FilaMorosidad(lote);
            for (Expensa expensa : expensas.buscarPorLote(lote)) {
                if (expensa.estaVencida(fecha) && expensa.capitalPendiente().signum() > 0) {
                    fila.agregar(expensa.capitalPendiente(), expensa.interesPendiente(fecha, mora));
                }
            }
            if (fila.tieneDeuda()) {
                filas.add(fila);
            }
        }
        Collections.sort(filas);
        String texto = "MOROSIDAD POR LOTE al " + fecha + "\n";
        for (FilaMorosidad fila : filas) {
            texto = texto + fila + "\n";
        }
        if (filas.isEmpty()) {
            texto = texto + "No hay lotes con deuda vencida\n";
        }
        return texto;
    }

    /** CU-26: equipos con falla, en reparacion o fuera de servicio, agrupados por sector, con sus ordenes abiertas. */
    public String equiposFueraDeServicio(LocalDateTime ahora) {
        List<Equipo> lista = equipos.listar();
        lista.sort(Comparator.comparing(Equipo::getNombreSector).thenComparing(Equipo::getCodigo));
        String texto = "EQUIPOS QUE NO ESTAN OPERATIVOS al " + ahora.toLocalDate() + "\n";
        String sectorActual = "";
        int cantidad = 0;
        for (Equipo equipo : lista) {
            if (equipo.getEstado() != EstadoEquipo.OPERATIVO) {
                if (!equipo.getNombreSector().equals(sectorActual)) {
                    sectorActual = equipo.getNombreSector();
                    texto = texto + "Sector " + sectorActual + ":\n";
                }
                texto = texto + "  " + equipo + "\n";
                for (OrdenMantenimiento orden : equipos.listarOrdenes()) {
                    if (orden.estaAbierta() && orden.getEquipo() == equipo) {
                        texto = texto + "     " + orden + "\n";
                    }
                }
                cantidad++;
            }
        }
        if (cantidad == 0) {
            texto = texto + "Todos los equipos estan operativos\n";
        }
        return texto;
    }

    public void exportar(String texto, String ruta) throws IOException {
        if (texto == null || ruta == null || ruta.isBlank()) throw new IllegalArgumentException("Texto y ruta obligatorios");
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ruta))) {
            escritor.write(texto);
        }
    }

    /** Una linea del reporte de morosidad. Su orden natural es por deuda, de mayor a menor. */
    private static class FilaMorosidad implements Comparable<FilaMorosidad> {
        private final Lote lote;
        private BigDecimal capital = BigDecimal.ZERO;
        private BigDecimal intereses = BigDecimal.ZERO;
        private int periodosImpagos = 0;

        FilaMorosidad(Lote lote) {
            this.lote = lote;
        }

        void agregar(BigDecimal capitalPendiente, BigDecimal interesPendiente) {
            capital = capital.add(capitalPendiente);
            intereses = intereses.add(interesPendiente);
            periodosImpagos++;
        }

        boolean tieneDeuda() {
            return periodosImpagos > 0;
        }

        BigDecimal total() {
            return capital.add(intereses);
        }

        @Override
        public int compareTo(FilaMorosidad otra) {
            return otra.total().compareTo(total());
        }

        @Override
        public String toString() {
            String texto = "Lote " + lote.getNumero() + " | periodos impagos: " + periodosImpagos
                    + " | capital: $" + capital + " | intereses: $" + intereses + " | total: $" + total();
            if (lote.isMoroso()) {
                texto = texto + " | MOROSO";
            }
            return texto;
        }
    }
}
