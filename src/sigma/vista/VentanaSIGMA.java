package sigma.vista;

import sigma.AplicacionSIGMA;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

/** Interfaz grafica (Swing). Cada boton ejecuta una operacion; la logica esta en los gestores. */
public class VentanaSIGMA extends JFrame implements Interaccion {
    private static final int UN_MINUTO = 60000;

    private final OperacionesSIGMA operaciones;
    private final JTextArea salida = new JTextArea(24, 80);

    public VentanaSIGMA(AplicacionSIGMA aplicacion) {
        super("SIGMA - Barrio Los Aromos");
        operaciones = new OperacionesSIGMA(aplicacion, this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("SIGMA - Sistema Integral de Gestion y Mantenimiento de Aromos");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        add(titulo, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Barrio", panel(OperacionesSIGMA.MENU_BARRIO));
        pestanas.addTab("Reclamos", panel(OperacionesSIGMA.MENU_RECLAMOS));
        pestanas.addTab("Expensas", panel(OperacionesSIGMA.MENU_EXPENSAS));
        pestanas.addTab("Mantenimiento", panel(OperacionesSIGMA.MENU_MANTENIMIENTO));
        pestanas.addTab("Visitas", panel(OperacionesSIGMA.MENU_VISITAS));
        pestanas.addTab("Reportes", panel(OperacionesSIGMA.MENU_REPORTES));
        add(pestanas, BorderLayout.WEST);

        salida.setEditable(false);
        salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(salida), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);

        operaciones.revisarPlazos();
        operaciones.ejecutar("Ver lotes con residentes");
        Thread revision = new Thread(new RevisionPeriodica());
        revision.start();
    }

    private JPanel panel(String[] opciones) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (int i = 0; i < opciones.length; i++) {
            String opcion = opciones[i];
            JButton boton = new JButton(opcion);
            boton.addActionListener(e -> operaciones.ejecutar(opcion));
            panel.add(boton);
        }
        return panel;
    }

    /**
     * Tarea del hilo que, una vez por minuto, pide revisar los plazos.
     * La revision se hace con invokeLater para que corra en el hilo de Swing,
     * igual que los botones, y no al mismo tiempo que otra operacion.
     */
    private class RevisionPeriodica implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(UN_MINUTO);
                } catch (InterruptedException e) {
                    return;
                }
                SwingUtilities.invokeLater(() -> operaciones.revisarPlazos());
            }
        }
    }

    @Override
    public String pedirTexto(String pregunta) {
        String respuesta = JOptionPane.showInputDialog(this, pregunta);
        if (respuesta == null) throw new IllegalArgumentException("Operacion cancelada");
        return respuesta;
    }

    @Override
    public int elegir(String titulo, String[] opciones) {
        Object elegido = JOptionPane.showInputDialog(this, titulo, "SIGMA", JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[0]);
        if (elegido == null) throw new IllegalArgumentException("Operacion cancelada");
        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i].equals(elegido)) return i;
        }
        throw new IllegalArgumentException("Opcion desconocida");
    }

    @Override
    public void mostrar(String texto) {
        salida.setText(texto);
        salida.setCaretPosition(0); // vuelve al principio del texto
    }

    @Override
    public void mostrarError(String texto) {
        JOptionPane.showMessageDialog(this, texto, "SIGMA", JOptionPane.ERROR_MESSAGE);
    }
}
