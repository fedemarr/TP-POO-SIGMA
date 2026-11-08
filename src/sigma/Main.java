package sigma;

import sigma.excepciones.ReglaNegocioException;
import sigma.vista.ConsolaSIGMA;
import sigma.vista.VentanaSIGMA;

import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Punto de entrada.
 *   java -cp build sigma.Main            abre la ventana (Swing)
 *   java -cp build sigma.Main consola    abre el menu por consola
 */
public class Main {
    public static void main(String[] args) {
        try {
            AplicacionSIGMA aplicacion = new AplicacionSIGMA(AplicacionSIGMA.ARCHIVO_DATOS);
            if (args.length > 0 && args[0].equalsIgnoreCase("consola")) {
                new ConsolaSIGMA(aplicacion).ejecutarMenu();
            } else {
                SwingUtilities.invokeLater(() -> new VentanaSIGMA(aplicacion).setVisible(true));
            }
        } catch (IOException e) {
            System.out.println("No se pudieron leer los datos guardados: " + e.getMessage());
        } catch (ReglaNegocioException e) {
            System.out.println("Error al cargar los datos iniciales: " + e.getMessage());
        }
    }
}
