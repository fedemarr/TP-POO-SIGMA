# SIGMA

**Sistema Integral de Gestión y Mantenimiento de Aromos**

Trabajo práctico de Programación Orientada a Objetos. Propuesta A: gestión del barrio cerrado Los Aromos.

## Etapa 2 en desarrollo

Primer incremento ejecutable: módulo de accesos, con autorización de visitas y proveedores, ingreso, egreso e historial. Implementa los CU-21 a CU-24 y la secuencia de ingreso, con sus alternativas de error. Expensas, reclamos y mantenimiento todavía están pendientes.

Se utiliza Java con menú de consola y archivos. No se requieren frameworks, base de datos ni dependencias externas. Los paquetes separan modelo, gestores, repositorios, persistencia y consola.

### Ejecutar en Windows

Se necesita un **JDK**, que incluye `java` y `javac`. Abrir PowerShell en la carpeta del repositorio y ejecutar:

```powershell
powershell -ExecutionPolicy Bypass -File .\sigma.ps1 -Accion ejecutar
```

El script utiliza `JAVA_HOME` o busca `javac` en el PATH. Si no lo encuentra, indicar la carpeta del JDK:

```powershell
powershell -ExecutionPolicy Bypass -File .\sigma.ps1 -Accion ejecutar -Jdk "C:\ruta\al\jdk"
```

En la computadora donde se inició esta implementación, el JDK disponible está en `C:\Users\fede\.jdks\ms-21.0.12`. Esa ruta es un ejemplo local, no un requisito del proyecto.

Para compilar sin abrir el menú, cambiar la acción por `compilar`. En IntelliJ se puede abrir el repositorio, configurar un JDK, marcar `src` como carpeta de fuentes y ejecutar `sigma.Main`, con el repositorio como directorio de trabajo.

### Probar

```powershell
powershell -ExecutionPolicy Bypass -File .\sigma.ps1 -Accion probar -Jdk "C:\ruta\al\jdk"
```

Ejecuta 30 pruebas de dominio e integración y 8 pruebas funcionales del menú. Las verificaciones lanzan errores explícitos y no dependen de habilitar `assert`. Las pruebas usan archivos separados bajo `build/pruebas/`; no modifican los datos del programa. Las transcripciones funcionales quedan en esa carpeta.

### Datos ficticios y recorrido de prueba

- Vecino 1: Ana Perez, documento 30111222, titular del lote 1.
- Vecino 2: Luis Gomez, documento 30222333, titular del lote 2.
- Guardia 1: portería. Guardia 2: ronda, para comprobar el rechazo de ingreso.

El padrón inicial es una demostración de dos lotes; todavía no es la carga completa de los 150 lotes. Cada autorización conserva los datos del vecino, lote y visitante en el archivo.

1. Opción 1: elegir vecino 1 y lote 1; visitante de prueba con documento 123; fecha de hoy, sin recurrencia, de 09:00 a 18:00.
2. Opción 2: documento 123, lote 1, guardia 1; escribir la fecha de hoy a las 10:00 con formato `AAAA-MM-DD HH:MM`.
3. Repetir el ingreso: debe rechazarlo por existir una entrada sin salida.
4. Opción 3: registrar el egreso del documento 123 a las 11:00.
5. Cerrar con 0, volver a abrir y consultar las opciones 4 y 5: deben mantenerse la autorización y el historial.

El programa guarda después de cada operación confirmada en `datos/accesos.dat`. La carpeta de datos no se sube a GitHub. Se puede elegir otro archivo con `-ArchivoDatos "datos/otra-prueba.dat"`. Usar una sola instancia del programa por archivo. Si un archivo está dañado, se informa el error y se conserva; no se reemplaza por datos vacíos.

- [Avance, decisiones de implementación y pendientes](docs/07-etapa-2-avance.md)
- [Plan y resultados de pruebas del primer incremento](docs/08-pruebas-accesos.md)
- [Diagrama del código de accesos](docs/uml/etapa2-accesos.svg)

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

## Estructura y estado

- docs/: documento de diseño, PDF y fuentes Markdown.
- docs/uml/: diagramas PlantUML, SVG y PNG.
- src/: código Java del incremento de accesos.
- test/: pruebas de dominio, integración y consola.
- sigma.ps1: compilación, ejecución y pruebas en Windows.

La etapa 1 corresponde al diseño y se conserva en el tag `etapa-1`. La etapa 2 está en desarrollo; este incremento no constituye su entrega final ni tiene todavía el tag `etapa-2`.
