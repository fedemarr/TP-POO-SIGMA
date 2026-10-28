package sigma.excepciones;

public class IngresoDuplicadoException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public IngresoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
