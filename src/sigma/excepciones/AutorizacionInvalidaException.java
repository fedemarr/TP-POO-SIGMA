package sigma.excepciones;

public class AutorizacionInvalidaException extends ReglaNegocioException {
    public AutorizacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
