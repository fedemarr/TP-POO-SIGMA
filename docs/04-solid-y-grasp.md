# SIGMA — Justificación de 3 principios SOLID y 4 patrones GRASP

## Alcance de la revisión

Esta sección describe las decisiones de diseño para pasar del diagrama de clases inicial al final. Algunas responsabilidades ya aparecen en el modelo inicial y se conservarán; otras requieren incorporar gestores y repositorios. La aplicación Java todavía no está implementada. El diagrama final y las dos secuencias deberán reflejar estas decisiones antes de cerrar la etapa 1.

## 1. SOLID: responsabilidad única (SRP)

**Problema detectado.** En el modelo inicial, `Barrio` reúne operaciones de reclamos, pagos, visitas, mantenimiento y lectura y escritura de archivos. Esto le da distintos motivos para cambiar: una modificación del formato de almacenamiento puede afectar la misma clase que coordina las reglas del negocio.

**Decisión de diseño.** Mantener en `Barrio` la información y las operaciones propias del barrio, y distribuir la coordinación de casos de uso entre `GestorReclamos`, `GestorExpensas`, `GestorAccesos` y `GestorMantenimiento`. Extraer el cálculo de intereses a `CalculadoraMora` y la lectura y escritura de archivos a repositorios. Las entidades conservan sus reglas: por ejemplo, `Reclamo` controla sus transiciones y `Expensa` calcula su saldo. No se busca que cada clase tenga un solo método, sino una responsabilidad coherente.

**Clases involucradas.** `Barrio`, los cuatro gestores, `CalculadoraMora`, los repositorios, `Reclamo` y `Expensa`.

**Ventaja.** Se puede modificar el almacenamiento o la fórmula de intereses sin concentrar todos los cambios en `Barrio`. Las responsabilidades separadas también facilitan probar y explicar cada parte del sistema.

## 2. SOLID: abierto/cerrado (OCP)

**Problema detectado.** Si el gestor determina la prioridad con condiciones sobre el tipo concreto de equipo, incorporar un nuevo tipo obliga a modificar esas condiciones. El conocimiento de las reglas de cada equipo quedaría repartido entre el gestor y las entidades.

**Decisión de diseño.** Conservar la operación abstracta `Equipo.prioridadDeFalla(falladasEnSector)`, ya presente en el modelo inicial. `Camara` devuelve prioridad alta; `Luminaria` devuelve normal, o alta si hay más de tres luminarias falladas en el sector. `GestorMantenimiento` llama a esa operación mediante una referencia de tipo `Equipo`, sin preguntar si el objeto es una cámara o una luminaria. El contexto del sector proporciona el conteo necesario.

**Clases involucradas.** `Equipo`, `Camara`, `Luminaria`, `Sector` y `GestorMantenimiento`.

**Ventaja.** Un nuevo tipo de equipo puede aportar su propia regla implementando la operación. Mientras respete el contrato y use el mismo contexto de entrada, el gestor no necesita modificar su selección de prioridad. Esto no implica que cualquier cambio futuro pueda hacerse sin modificar ninguna clase.

## 3. SOLID: inversión de dependencias (DIP)

**Problema detectado.** Si `GestorExpensas` abre archivos directamente o crea una implementación concreta de repositorio, la coordinación de los pagos queda ligada a ese mecanismo de almacenamiento. Las pruebas también necesitarían archivos reales.

**Decisión de diseño.** Definir interfaces como `RepositorioExpensas` y `RepositorioPagos`, con las operaciones que necesita el gestor. Sus implementaciones en archivos realizan la persistencia requerida por la consigna. El gestor recibe los repositorios por constructor y trabaja con sus interfaces; no crea las implementaciones concretas. El punto de inicio de la aplicación elige e inyecta las implementaciones. Para las pruebas se pueden proporcionar repositorios en memoria con los mismos contratos.

**Clases e interfaces involucradas.** `GestorExpensas`, `RepositorioExpensas`, `RepositorioPagos`, sus implementaciones en archivos y sus implementaciones de prueba en memoria.

**Ventaja.** La lógica de pagos puede probarse sin acceder al disco. Cambiar el formato de los archivos afecta a los repositorios, sin obligar a reescribir el gestor. Las implementaciones en memoria son auxiliares de prueba; la aplicación debe conservar su persistencia en archivos.

## 4. GRASP: Experto en información

**Problema detectado.** Calcular el saldo de una expensa desde la consola o desde un gestor obligaría a esos objetos a conocer y manipular sus importes e imputaciones. La misma fórmula podría terminar repetida en pagos y reportes.

**Decisión de diseño.** Conservar `Expensa.calcularSaldo(fecha)`, porque `Expensa` conoce su importe y las imputaciones que recibió. La expensa obtiene el componente de mora mediante la colaboración con `CalculadoraMora`, prevista por SRP, y calcula el saldo pendiente sin volver a sumar intereses ya incluidos. Los gestores y los reportes solicitan el resultado a la expensa.

**Clases involucradas.** `Expensa`, `Imputacion` y `CalculadoraMora`.

**Ventaja.** El cálculo queda cerca de los datos que necesita, se evita duplicarlo y se protege el estado interno de la entidad. La regla de mora mantiene su responsabilidad específica, sin convertir a los gestores en calculadoras de saldos.

## 5. GRASP: Creador

**Problema detectado.** Si la consola o el gestor crean por separado un cambio de estado y lo agregan después, podría modificarse el estado de un reclamo sin registrar su historial, o registrarse un cambio que nunca ocurrió.

**Decisión de diseño.** Asignar a `Reclamo` la creación de sus objetos `CambioEstado`. La operación `cambiarEstado(nuevo, observacion, fecha)` valida la transición y, si es válida, actualiza el estado e incorpora el registro a su historial como una única operación de dominio. Las demás operaciones que cambian el estado también deben conservar esa garantía. Si una validación falla, no se modifica ni el estado ni el historial.

**Clases involucradas.** `Reclamo` y `CambioEstado`.

**Ventaja.** El objeto que contiene el historial y dispone de los datos necesarios es quien crea sus registros. Se mantiene la correspondencia entre el estado actual y la evolución del reclamo. Este patrón ya tiene una base en la composición del diagrama inicial y se hace explícito en el final.

## 6. GRASP: Controlador

**Problema detectado.** Concentrar la coordinación en `Barrio` o trasladarla a la consola mezcla la interacción con el usuario con la ejecución de los casos de uso. Además, un controlador único para todos los módulos acumularía demasiadas tareas.

**Decisión de diseño.** Introducir controladores por módulo. `GestorAccesos` recibe las solicitudes de ingreso y egreso, consulta las autorizaciones y verifica ingresos abiertos. `GestorExpensas` coordina la emisión y el registro de pagos. `GestorReclamos` y `GestorMantenimiento` coordinan los casos de sus módulos. Los gestores delegan las reglas específicas a las entidades y el almacenamiento a los repositorios. La consola recoge los datos, invoca al gestor y muestra el resultado o error.

**Clases involucradas.** Los cuatro gestores, la futura interfaz de consola, las entidades y las interfaces de repositorio correspondientes.

**Ventaja.** Se define un punto de entrada claro para cada caso de uso, se pueden probar las operaciones sin recorrer el menú y se evita repartir su coordinación por la interfaz. Las secuencias de ingreso y pago deberán comenzar en los gestores correspondientes en lugar de `Barrio`.

## 7. GRASP: Polimorfismo

**Problema detectado.** La prioridad de una falla depende del tipo de equipo. Resolverla con comprobaciones repetidas del tipo concreto acoplaría el gestor a cada variante y mezclaría reglas diferentes.

**Decisión de diseño.** Asignar el comportamiento variable a las subclases de `Equipo`. `Camara` y `Luminaria` sobrescriben `prioridadDeFalla(falladasEnSector)`. El gestor envía el mismo mensaje y cada objeto responde según su regla. La abstracción y las sobrescrituras ya aparecen en el modelo inicial y se mantienen en el final.

**Clases involucradas.** `Equipo`, `Camara`, `Luminaria` y `GestorMantenimiento`.

**Ventaja.** Cada variante conserva su comportamiento y el gestor trabaja de manera uniforme con los equipos. La misma decisión respalda OCP y Polimorfismo: OCP explica cómo facilita extender el diseño; GRASP Polimorfismo explica a qué objetos se asigna la responsabilidad variable.

## Correspondencia requerida en el modelo final

| Decisión | Cambio o evidencia que debe mostrar el diagrama final |
| --- | --- |
| SRP | `Barrio` deja de coordinar todos los módulos y de leer y escribir archivos. Se incorporan gestores, repositorios y `CalculadoraMora`. |
| OCP y Polimorfismo | Operación abstracta de prioridad en `Equipo`, sobrescrituras en `Camara` y `Luminaria` y dependencia del gestor respecto de `Equipo`. |
| DIP | Interfaces de repositorio, implementaciones que las realizan y gestores que reciben y usan las interfaces. |
| Experto | `Expensa` mantiene el cálculo del saldo y colabora con `CalculadoraMora`. |
| Creador | `Reclamo` contiene y crea sus objetos `CambioEstado`. |
| Controlador | Las dos secuencias se actualizan para usar `GestorAccesos` y `GestorExpensas`, con mensajes que existan en el modelo final. |

Esta sección no da por terminado el diagrama final ni la implementación: establece las decisiones que ambos deberán reflejar. No se modifican las reglas de negocio ni los seis supuestos cotidianos adoptados.
