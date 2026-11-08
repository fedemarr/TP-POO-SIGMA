# Implementación: del diseño al código

Este documento registra cómo se implementó el modelo final de la etapa 1 y qué se agregó al programarlo. Los diagramas del modelo final ([completo](uml/clases-final-completo.puml), vistas por módulo y [arquitectura](uml/06-arquitectura.puml)) ya incluyen estos agregados.

## Correspondencia con el diagrama final

Todas las clases, enumerados, interfaces, excepciones y operaciones del modelo final están implementados con los mismos nombres. Los paquetes del código siguen los grupos del diagrama:

| Grupo del diagrama | Paquete |
| --- | --- |
| Barrio y vecinos, Personal, Reclamos, Expensas y pagos, Equipos, Visitas y avisos | `sigma.modelo` |
| Coordinación de casos de uso, Evaluación de plazos | `sigma.gestores` |
| Contratos de persistencia | `sigma.repositorios` |
| Persistencia en archivos | `sigma.persistencia` |
| Errores de negocio | `sigma.excepciones` |
| Interfaz de usuario | `sigma.vista` y `sigma` (`Main`, `AplicacionSIGMA`) |
| Pruebas | `test/` (repositorios en memoria) |

`CalculadoraMora` quedó en `sigma.modelo` porque la usan `Expensa` y `Pago`.

## Dónde se ven SOLID y GRASP

| Principio o patrón | En el código |
| --- | --- |
| SRP | `Barrio` solo guarda los datos del barrio; los gestores coordinan; `CalculadoraMora` calcula intereses; los repositorios guardan. |
| OCP y Polimorfismo | `Equipo.prioridadDeFalla` es abstracto: `Camara` y `Luminaria` lo sobrescriben y `GestorMantenimiento` usa solo `Equipo`. |
| DIP | Los gestores reciben interfaces de repositorio en el constructor. Las pruebas usan repositorios en memoria y el programa usa los de archivo. |
| Experto en información | `Expensa` calcula su capital pendiente, sus intereses y su saldo. |
| Creador | `Reclamo` crea sus `CambioEstado`; `Pago` crea sus `Imputacion`; `Barrio` crea sus `Sector`. |
| Controlador | `GestorReclamos`, `GestorExpensas`, `GestorAccesos` y `GestorMantenimiento` reciben las operaciones de la interfaz. |

## Agregados al modelo

El diagrama de la etapa 1 mostraba las operaciones relevantes para el diseño. Para que el programa funcione se agregaron, y ya figuran en los diagramas:

- **Constructores y consultas** que faltaban, por ejemplo los de `Reclamo`, `Expensa`, `Residencia`, `CalculadoraMora` y los gestores.
- **`Expensa` conoce su `Lote`.** Implementa la asociación Lote–Expensa en los dos sentidos, para que `RepositorioExpensas.guardar(expensa)` sepa a qué lote pertenece.
- **`Barrio`** guarda sus sectores y notificaciones (composiciones que ya estaban en el diagrama) y tiene operaciones para agregar lotes, empleados, equipos y notificaciones y para buscar empleados y sectores.
- **`Lote`:** `agregarResidencia` (controla que haya un único titular vigente), `obtenerTitular`, `buscarResidente` y `agregarExpensa`.
- **Consultas de repositorio:** `RepositorioExpensas.listarLotes`, `RepositorioEquipos.listarOrdenes` y `RepositorioNotificaciones.listar`. Las usan la revisión de morosidad, los reportes y la pantalla de notificaciones.
- **Gestores con `RepositorioNotificaciones`.** El diagrama ya mostraba esa asociación; ahora se recibe en el constructor.
- **Mantenimiento:**
  - `GestorMantenimiento.registrarEquipo`, `asignarTecnico`, `iniciarReparacion` y `cerrarOrdenSinReparacion`, y en `OrdenMantenimiento` los métodos `asignarTecnico` y `cerrarSinReparacion`. Cubren dos criterios ya documentados: una orden puede esperar un técnico, y un equipo que no se puede reparar queda fuera de servicio (CU-19 A2).
  - `Equipo.registrarFalla(descripcion, fecha, autor)` es una sobrecarga para guardar quién informó la falla. `fallaAbierta` permite sumar avisos repetidos a la falla existente (SUP-05).
- **`Autorizacion.esIgualA`:** evita duplicar una autorización (CU-21 A3).
- **`CalculadoraMora.interesPorDias`:** calcula el interés de un tramo; la usa `Expensa.interesPendiente`. Por eso la secuencia de pago muestra ese mensaje.

## Clases nuevas

| Clase | Para qué |
| --- | --- |
| `ConsolaSIGMA` | Menú por consola, la clase que ya figuraba en el diagrama. |
| `VentanaSIGMA` | Interfaz gráfica con Swing (la consigna la cuenta como mejora). |
| `Interaccion` | Lo que una vista sabe hacer: preguntar y mostrar. La implementan la consola y la ventana. |
| `OperacionesSIGMA` | Las operaciones del menú, escritas una sola vez para las dos vistas. |
| `AplicacionSIGMA` | Crea el almacenamiento, los repositorios y los gestores, y carga los datos de prueba. |
| `AlmacenArchivos` y `DatosSIGMA` | Almacenamiento compartido por los repositorios de archivo. |
| Repositorios `...Memoria` | Implementaciones para las pruebas, sin archivos. |

## Decisiones de implementación

- **Excepciones verificadas.** `ReglaNegocioException` hereda de `Exception`, como en el diagrama. Los métodos que aplican reglas de negocio la declaran con `throws`, y la interfaz la captura y muestra el mensaje. Los datos mal ingresados (números o fechas inválidas) usan `IllegalArgumentException`.
- **Persistencia en un solo archivo.** Todos los repositorios de archivo comparten `AlmacenArchivos`, que guarda el estado completo (`DatosSIGMA`) por serialización en `data/sigma.dat`. Así un pago, sus imputaciones, las expensas y el lote se confirman juntos, como pide `confirmarPago`. Se escribe primero un archivo temporal y después reemplaza al real. Si la escritura falla, se vuelve al último estado guardado, y el `finally` borra el temporal que haya quedado.
- **Plazos.** `PlanificadorPlazos` se ejecuta al abrir el programa y antes de cada operación. En la ventana, además, un hilo lo ejecuta una vez por minuto mediante `SwingUtilities.invokeLater`, para que no corra al mismo tiempo que un botón.
- **Visitas.** Una autorización común es para un solo día. Las de varios días son las recurrentes de proveedores habituales, de hasta 30 días.
- **Ordenamientos.**
  - `Comparable`: `Reclamo`, por prioridad y fecha, y las filas del reporte de morosidad, por deuda de mayor a menor.
  - `Comparator`: las expensas por vencimiento al imputar un pago, los pagos por fecha al aplicar saldos a favor, los lotes por número y los equipos por sector y código.

## Casos de uso: detalles resueltos en el código

| Caso de uso | Cómo se resolvió |
| --- | --- |
| CU-04 | Cambia el estado el responsable o el administrador. El límite de 5 se controla sobre el responsable al pasar a en curso. |
| CU-06 | Fuera de plazo, el reclamo se cierra y se rechaza la reapertura. Si el responsable ya tiene 5 en curso, se avisa al administrador. |
| CU-07 | Lista los reclamos no cerrados y marca los urgentes sin atender por más de 24 horas. |
| CU-11 | La morosidad se revisa al emitir expensas, al registrar pagos y en cada revisión de plazos. |
| CU-16 | Para ver la cuenta se pide el DNI de un titular o residente del lote. |
| CU-20 | Listado con filtro por sector, órdenes abiertas y cámaras sin funcionar hace más de 48 horas. |
| CU-25 | Morosidad ordenada por deuda, con capital, intereses, períodos impagos y condición de moroso. |
| CU-26 | Equipos no operativos agrupados por sector, con sus órdenes abiertas. |
| CU-27 | Comentan el vecino que presentó el reclamo (por DNI) o el personal del barrio (por legajo). |
