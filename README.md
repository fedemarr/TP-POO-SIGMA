# SIGMA

**Sistema Integral de Gestión y Mantenimiento de Aromos**

Trabajo práctico de Programación Orientada a Objetos. Propuesta A: gestión del barrio cerrado Los Aromos.

## Entrega de etapa 1

### [Abrir el documento de análisis y diseño (PDF)](docs/SIGMA-Documento-de-Diseno-Etapa-1.pdf)

El documento de 43 páginas incluye propuesta e integrantes, requisitos y supuestos, 27 casos de uso y su diagrama, modelo de clases inicial, dos secuencias, justificación de tres SOLID y cuatro GRASP, modelo de clases final y explicación de los cambios.

Los modelos finales se encuentran en las páginas 32 a 41 del PDF y las secuencias en las páginas 42 y 43. Las láminas son vectoriales y se pueden ampliar sin perder nitidez.

## Equipo

| Integrante | Legajo |
| --- | --- |
| Lautaro Lugaro | 1222389 |
| Federico Martinez | 1181299 |
| Joaquin Rodrigues | 1193419 |

Materia: Programación Orientada a Objetos. Docente: Leon Maria Angela.

## Documentación y diagramas

- [Análisis, requisitos, reglas y supuestos](docs/01-analisis.md)
- [Descripción de los casos de uso](docs/02-casos-de-uso.md)
- [Justificación de 3 SOLID y 4 GRASP](docs/04-solid-y-grasp.md)
- [Criterios de diseño y operación](docs/05-criterios-diseno.md)
- [Modelo final y nota de cambios](docs/06-modelo-final-y-cambios.md)
- [Índice de diagramas y fuentes editables](docs/uml/README.md)
- [Estado de entrega y continuación](docs/03-plan-y-decisiones.md)

## Etapa 2: programa en Java

### Requisitos

- Java 17 o superior (JDK). No usa bibliotecas externas.

### Ejecutar

Linux / macOS:

```bash
bash ejecutar-linux-mac.sh            # interfaz gráfica (Swing)
bash ejecutar-linux-mac.sh consola    # menú por consola
```

Windows: doble clic en `ejecutar-windows.bat` (ventana) o, desde una terminal, `ejecutar-windows.bat consola`.

Los datos se guardan en `data/sigma.dat` y se recuperan en la siguiente ejecución. Para empezar de cero, borrar esa carpeta.

### Pruebas

```bash
bash probar-linux-mac.sh
```

En Windows: `probar-windows.bat`. Hay ocho pruebas en `test/`; cada una imprime `OK` si todas sus comprobaciones se cumplen.

### Datos de prueba

La primera vez que se ejecuta, el programa crea los 150 lotes del barrio y estos datos:

| Dato | Valores |
| --- | --- |
| Titulares | Lote 1: Ana Perez (DNI 30111222). Lote 2: Bruno Gomez (28999888). Lote 3: Carla Diaz (33444555) |
| Familiar autorizado | Lote 3: Diego Diaz (40111000) |
| Personal | ADM-01 administradora, GUA-01 guardia de portería, GUA-02 guardia de ronda, MAN-01 electricista, MAN-02 plomera |
| Equipos | CAM-01 y CAM-02 (cámaras), LUM-01 a LUM-05 (luminarias del sector Calle interna 1) |

Ejemplo: Reclamos > Registrar reclamo, lote 1, DNI 30111222. Después, Asignar responsable con el legajo GUA-01.

### Organización del código

| Paquete | Contenido |
| --- | --- |
| `sigma.modelo` | Entidades y enumerados del diagrama de clases final |
| `sigma.excepciones` | `ReglaNegocioException` y sus subclases |
| `sigma.gestores` | Controladores de cada módulo, reportes y `PlanificadorPlazos` |
| `sigma.repositorios` | Interfaces de persistencia |
| `sigma.persistencia` | Implementaciones en archivo y `AlmacenArchivos` |
| `sigma.vista` | `ConsolaSIGMA`, `VentanaSIGMA` y las operaciones que comparten |
| `test/` | Pruebas y repositorios en memoria para probar sin archivos |

### Documentación de la etapa 2

- [Implementación: del diseño al código](docs/07-implementacion.md)
- [Plan de pruebas (unitarias y funcionales)](docs/08-plan-de-pruebas.md)
- [Informe final](docs/09-informe-final.md)
