import java.math.BigDecimal;

/** Comprobaciones simples para las pruebas: si algo no se cumple, la prueba se corta con un error. */
public class Verificar {
    public static void verdadero(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError("Fallo: " + mensaje);
        }
    }

    public static void igual(String esperado, BigDecimal obtenido, String mensaje) {
        if (new BigDecimal(esperado).compareTo(obtenido) != 0) {
            throw new AssertionError("Fallo: " + mensaje + ". Esperado " + esperado + ", obtenido " + obtenido);
        }
    }
}
