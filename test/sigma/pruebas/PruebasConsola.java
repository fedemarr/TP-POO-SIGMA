package sigma.pruebas;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/** Pruebas funcionales: cada llamada abre un proceso nuevo y utiliza el menu real. */
public class PruebasConsola {
    private static int aprobadas;

    public static void main(String[] argumentos) throws Exception {
        Path carpeta = Paths.get("build", "pruebas");
        Files.createDirectories(carpeta);
        Path sesion = Files.createTempDirectory(carpeta, "consola-");
        Path archivo = sesion.resolve("accesos.dat");

        Resultado vacio = ejecutar(archivo, "99\n2\n123\nx\n4\n0\n", sesion, "PF-01");
        verificar(vacio.codigo == 0 && vacio.salida.contains("Opcion invalida")
                && vacio.salida.contains("Dato invalido") && vacio.salida.contains("No hay autorizaciones"),
                "PF-01 Menu tolera opcion y numero invalidos, y muestra lista vacia");

        String alta = "1\n1\n1\nVisita de prueba\n123\n2026-10-28\nn\n09:00\n18:00\n";
        String ingreso = "2\n123\n1\n1\n2026-10-28 10:00\n";
        String duplicado = "2\n123\n1\n1\n2026-10-28 10:15\n";
        String egresoInvalido = "3\n123\n2026-10-28 09:00\n";
        String egreso = "3\n123\n2026-10-28 11:00\n";
        String reingreso = "2\n123\n1\n1\n2026-10-28 12:00\n";
        Resultado flujo = ejecutar(archivo,
                alta + ingreso + duplicado + egresoInvalido + egreso + reingreso + "5\n1\n0\n",
                sesion, "PF-02");
        verificar(flujo.codigo == 0 && contar(flujo.salida, "Ingreso confirmado:") == 2
                && flujo.salida.contains("ya tiene un ingreso sin egreso")
                && flujo.salida.contains("egreso no puede ser anterior")
                && flujo.salida.contains("Egreso confirmado") && flujo.salida.contains("PENDIENTE"),
                "PF-02 Alta, ingreso, duplicado, egreso invalido, egreso valido y reingreso");

        Resultado reinicio = ejecutar(archivo, "4\n5\n2\n2\n123\n1\n1\n2026-10-28 12:30\n0\n",
                sesion, "PF-03");
        verificar(reinicio.codigo == 0 && reinicio.salida.contains("Visita de prueba")
                && reinicio.salida.contains("2026-10-28T11:00") && reinicio.salida.contains("PENDIENTE")
                && reinicio.salida.contains("ya tiene un ingreso sin egreso"),
                "PF-03 Reinicio conserva autorizacion, historial e ingreso abierto");

        Resultado cierre = ejecutar(archivo, "3\n123\n2026-10-28 13:00\n0\n", sesion, "PF-04-egreso");
        Resultado consulta = ejecutar(archivo, "5\n1\n0\n", sesion, "PF-04-reinicio");
        verificar(cierre.codigo == 0 && cierre.salida.contains("Egreso confirmado")
                && consulta.codigo == 0 && consulta.salida.contains("2026-10-28T13:00")
                && !consulta.salida.contains("PENDIENTE"), "PF-04 Egreso persistido entre procesos");

        Resultado sinPermiso = ejecutar(archivo, "2\n404\n1\n1\n2026-10-28 14:00\n0\n",
                sesion, "PF-05");
        verificar(sinPermiso.codigo == 0 && sinPermiso.salida.contains("No hay autorizacion vigente")
                && !sinPermiso.salida.contains("Ingreso confirmado:"), "PF-05 Sin autorizacion no ingresa");

        Resultado ronda = ejecutar(archivo, "2\n123\n1\n2\n2026-10-28 14:00\n0\n", sesion, "PF-06");
        verificar(ronda.codigo == 0 && ronda.salida.contains("Solo un guardia de porteria")
                && !ronda.salida.contains("Ingreso confirmado:"), "PF-06 Guardia de ronda no habilita acceso");

        Path corrupto = sesion.resolve("corrupto.dat");
        byte[] bytes = new byte[] {1, 2, 3};
        Files.write(corrupto, bytes);
        Resultado error = ejecutar(corrupto, "0\n", sesion, "PF-07");
        verificar(error.codigo != 0 && error.salida.contains("El archivo se conserva")
                && Arrays.equals(bytes, Files.readAllBytes(corrupto)),
                "PF-07 Archivo corrupto informa error y conserva los bytes originales");

        Path bloqueante = sesion.resolve("bloqueante");
        Files.write(bloqueante, new byte[] {9});
        Resultado fallaGuardado = ejecutar(bloqueante.resolve("datos.dat"), alta + "0\n", sesion, "PF-08");
        verificar(fallaGuardado.codigo == 0 && fallaGuardado.salida.contains("Operacion no confirmada")
                && !fallaGuardado.salida.contains("Autorizacion confirmada")
                && Arrays.equals(new byte[] {9}, Files.readAllBytes(bloqueante)),
                "PF-08 Fallo real de escritura no confirma ni reemplaza el archivo bloqueante");

        System.out.println("RESULTADO: " + aprobadas + " pruebas funcionales aprobadas.");
        System.out.println("Transcripciones de consola: " + sesion.toAbsolutePath());
    }

    private static Resultado ejecutar(Path archivo, String entradas, Path sesion, String nombre)
            throws IOException, InterruptedException {
        Path java = Paths.get(System.getProperty("java.home"), "bin", "java.exe");
        Path log = sesion.resolve(nombre + ".txt");
        Process proceso = new ProcessBuilder(java.toString(), "-Dfile.encoding=UTF-8", "-cp",
                "build/clases", "sigma.Main", archivo.toString()).redirectErrorStream(true)
                .redirectOutput(log.toFile()).start();
        try (OutputStream entrada = proceso.getOutputStream()) {
            entrada.write(entradas.getBytes(StandardCharsets.UTF_8));
        }
        if (!proceso.waitFor(15, TimeUnit.SECONDS)) {
            proceso.destroyForcibly();
            throw new IOException("El programa no termino: " + nombre);
        }
        return new Resultado(proceso.exitValue(), new String(Files.readAllBytes(log), StandardCharsets.UTF_8));
    }

    private static int contar(String texto, String buscado) {
        int cantidad = 0;
        int indice = 0;
        while ((indice = texto.indexOf(buscado, indice)) >= 0) {
            cantidad++;
            indice += buscado.length();
        }
        return cantidad;
    }

    private static void verificar(boolean condicion, String descripcion) {
        if (!condicion) { throw new AssertionError(descripcion); }
        aprobadas++;
        System.out.println("OK " + descripcion);
    }

    private static class Resultado {
        final int codigo;
        final String salida;

        Resultado(int codigo, String salida) { this.codigo = codigo; this.salida = salida; }
    }
}
