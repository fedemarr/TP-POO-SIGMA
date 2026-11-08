package sigma.vista;

/**
 * Lo que una vista sabe hacer: preguntar y mostrar.
 * La consola lo implementa con Scanner y la ventana con JOptionPane,
 * asi las mismas operaciones funcionan en las dos interfaces.
 */
public interface Interaccion {
    /** Devuelve el texto ingresado. Si el usuario cancela, lanza IllegalArgumentException. */
    String pedirTexto(String pregunta);

    /** Muestra las opciones y devuelve la posicion elegida (empezando en 0). */
    int elegir(String titulo, String[] opciones);

    void mostrar(String texto);

    void mostrarError(String texto);
}
