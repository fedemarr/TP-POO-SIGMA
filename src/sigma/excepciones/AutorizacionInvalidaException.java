package sigma.excepciones;

public class AutorizacionInvalidaException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public AutorizacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
