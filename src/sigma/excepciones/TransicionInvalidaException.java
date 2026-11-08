package sigma.excepciones;

public class TransicionInvalidaException extends ReglaNegocioException {
    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
