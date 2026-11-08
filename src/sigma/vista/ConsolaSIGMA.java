package sigma.vista;

import sigma.AplicacionSIGMA;

import java.util.Scanner;

/** Interfaz por consola con menu. */
public class ConsolaSIGMA implements Interaccion {
    private final Scanner teclado = new Scanner(System.in);
    private final OperacionesSIGMA operaciones;

    public ConsolaSIGMA(AplicacionSIGMA aplicacion) {
        this.operaciones = new OperacionesSIGMA(aplicacion, this);
    }

    public void ejecutarMenu() {
        System.out.println("=== SIGMA - Barrio Los Aromos ===");
        try {
            operaciones.revisarPlazos();
            int opcion;
            do {
                System.out.println();
                System.out.println("MENU PRINCIPAL");
                System.out.println("1. Barrio y vecinos");
                System.out.println("2. Reclamos");
                System.out.println("3. Expensas y pagos");
                System.out.println("4. Equipos y mantenimiento");
                System.out.println("5. Visitas");
                System.out.println("6. Reportes y notificaciones");
                System.out.println("0. Salir");
                opcion = leerOpcion(6);
                switch (opcion) {
                    case 1:
                        submenu("BARRIO Y VECINOS", OperacionesSIGMA.MENU_BARRIO);
                        break;
                    case 2:
                        submenu("RECLAMOS", OperacionesSIGMA.MENU_RECLAMOS);
                        break;
                    case 3:
                        submenu("EXPENSAS Y PAGOS", OperacionesSIGMA.MENU_EXPENSAS);
                        break;
                    case 4:
                        submenu("EQUIPOS Y MANTENIMIENTO", OperacionesSIGMA.MENU_MANTENIMIENTO);
                        break;
                    case 5:
                        submenu("VISITAS", OperacionesSIGMA.MENU_VISITAS);
                        break;
                    case 6:
                        submenu("REPORTES", OperacionesSIGMA.MENU_REPORTES);
                        break;
                    default:
                        break;
                }
            } while (opcion != 0);
        } finally {
            teclado.close();
            System.out.println("Fin del programa. Los datos quedan guardados en " + AplicacionSIGMA.ARCHIVO_DATOS);
        }
    }

    private void submenu(String titulo, String[] opciones) {
        int opcion;
        do {
            System.out.println();
            System.out.println(titulo);
            for (int i = 0; i < opciones.length; i++) {
                System.out.println((i + 1) + ". " + opciones[i]);
            }
            System.out.println("0. Volver");
            opcion = leerOpcion(opciones.length);
            if (opcion > 0) {
                operaciones.ejecutar(opciones[opcion - 1]);
            }
        } while (opcion != 0);
    }

    private int leerOpcion(int maximo) {
        while (true) {
            System.out.print("Opcion: ");
            String texto = teclado.nextLine().trim();
            try {
                int opcion = Integer.parseInt(texto);
                if (opcion >= 0 && opcion <= maximo) {
                    return opcion;
                }
                System.out.println("Elegi un numero entre 0 y " + maximo);
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero");
            }
        }
    }

    @Override
    public String pedirTexto(String pregunta) {
        System.out.print(pregunta + ": ");
        return teclado.nextLine();
    }

    @Override
    public int elegir(String titulo, String[] opciones) {
        System.out.println(titulo);
        for (int i = 0; i < opciones.length; i++) {
            System.out.println("  " + (i + 1) + ". " + opciones[i]);
        }
        while (true) {
            System.out.print("Elegi una opcion: ");
            String texto = teclado.nextLine().trim();
            try {
                int opcion = Integer.parseInt(texto);
                if (opcion >= 1 && opcion <= opciones.length) {
                    return opcion - 1;
                }
                System.out.println("Elegi un numero entre 1 y " + opciones.length);
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un numero");
            }
        }
    }

    @Override
    public void mostrar(String texto) {
        System.out.println(texto);
    }

    @Override
    public void mostrarError(String texto) {
        System.out.println("ERROR: " + texto);
    }
}
