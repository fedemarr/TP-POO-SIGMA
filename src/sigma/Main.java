package sigma;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import sigma.consola.MenuAccesos;
import sigma.gestores.GestorAccesos;
import sigma.persistencia.AlmacenAccesos;
import sigma.persistencia.RepositorioAutorizacionesArchivo;
import sigma.persistencia.RepositorioIngresosArchivo;
import sigma.repositorios.RepositorioAutorizaciones;
import sigma.repositorios.RepositorioIngresos;

public class Main {
    public static void main(String[] argumentos) {
        Path archivo = argumentos.length == 0 ? Paths.get("datos", "accesos.dat") : Paths.get(argumentos[0]);
        AlmacenAccesos almacen = new AlmacenAccesos(archivo);
        RepositorioAutorizaciones autorizaciones = new RepositorioAutorizacionesArchivo(almacen);
        RepositorioIngresos ingresos = new RepositorioIngresosArchivo(almacen);
        try (Scanner entrada = new Scanner(System.in, "UTF-8")) {
            // Se verifica el archivo antes de permitir nuevas operaciones: nunca se reinicia si esta corrupto.
            autorizaciones.listar();
            ingresos.listar();
            System.out.println("Archivo de datos: " + archivo.toAbsolutePath().normalize());
            GestorAccesos gestor = new GestorAccesos(autorizaciones, ingresos);
            new MenuAccesos(entrada, gestor, autorizaciones, ingresos).ejecutar();
        } catch (IOException e) {
            System.err.println("No se pudieron cargar los datos. El archivo se conserva: " + e.getMessage());
            System.exit(1);
        }
    }
}
