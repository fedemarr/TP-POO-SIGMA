package sigma.excepciones;

public class LimiteReclamosException extends ReglaNegocioException {
    public LimiteReclamosException(String mensaje) {
        super(mensaje);
    }
}
