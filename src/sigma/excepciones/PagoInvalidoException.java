package sigma.excepciones;

public class PagoInvalidoException extends ReglaNegocioException {
    public PagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
