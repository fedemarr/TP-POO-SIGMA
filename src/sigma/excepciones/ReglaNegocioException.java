package sigma.excepciones;

/**
 * Excepcion base de las reglas de negocio de SIGMA.
 * Es verificada (hereda de Exception): quien llama esta obligado a tratarla.
 */
public class ReglaNegocioException extends Exception {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
