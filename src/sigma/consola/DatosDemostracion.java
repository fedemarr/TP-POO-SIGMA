package sigma.consola;

import java.time.LocalDate;
import sigma.modelo.*;

/** Datos ficticios de dos lotes para probar accesos. No representan el padron completo. */
public class DatosDemostracion {
    private final Persona[] vecinos;
    private final Lote[] lotes;
    private final Guardia[] guardias;

    public DatosDemostracion() {
        vecinos = new Persona[] {
            new Persona("Ana", "Perez", "30111222", "1100000001", "ana@example.com"),
            new Persona("Luis", "Gomez", "30222333", "1100000002", "luis@example.com")
        };
        lotes = new Lote[] { new Lote(1, 500), new Lote(2, 600) };
        LocalDate alta = LocalDate.of(2020, 1, 1);
        for (int i = 0; i < lotes.length; i++) {
            lotes[i].agregarResidencia(new Residencia(vecinos[i], TipoResidente.TITULAR, alta));
        }
        guardias = new Guardia[] {
            new Guardia("G001", "Guardia de prueba A", alta, "Manana", PuestoGuardia.PORTERIA),
            new Guardia("G002", "Guardia de prueba B", alta, "Manana", PuestoGuardia.RONDA)
        };
    }

    public Persona vecino(int numero) { return vecinos[indice(numero)]; }
    public Lote lote(int numero) { return lotes[indice(numero)]; }
    public Guardia guardia(int numero) { return guardias[indice(numero)]; }

    private int indice(int numero) {
        if (numero < 1 || numero > 2) { throw new IllegalArgumentException("Elija 1 o 2."); }
        return numero - 1;
    }
}
