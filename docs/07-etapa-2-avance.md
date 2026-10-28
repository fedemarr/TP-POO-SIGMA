# Etapa 2: avance de implementación

## Primer incremento: accesos (28/10/2026)

Se implementan CU-21 Autorizar visita, CU-22 Registrar ingreso, CU-23 Verificar autorización vigente y CU-24 Registrar egreso. El programa se ejecuta en Java, con menú de consola y persistencia local en archivos.

La secuencia de ingreso conserva el orden de las comprobaciones del diseño: portería, búsqueda de candidatas, elección de una autorización vigente, ausencia de otro ingreso abierto, creación y guardado. Una excepción de escritura impide confirmar la operación.

## Clases y decisiones

| Parte | Implementación | Relación con el diseño |
| --- | --- | --- |
| Vecinos y lotes | Persona, Lote, Residencia, TipoResidente | Información necesaria para validar al otorgante. Los métodos financieros de Lote se implementarán con expensas. |
| Personal | Empleado abstracto, Guardia y PuestoGuardia | Constructor protected y sobreescritura de describirRol. Administrador y mantenimiento se incorporarán con sus módulos. |
| Accesos | Visitante, Autorizacion y RegistroIngreso | Fechas, franjas, recurrencias, entrada y salida. |
| Coordinación | GestorAccesos | Controlador de GRASP: coordina y delega validaciones a las entidades. |
| Contratos | RepositorioAutorizaciones y RepositorioIngresos | DIP: el gestor recibe interfaces por constructor. |
| Archivos | Implementaciones Archivo y AlmacenAccesos | El almacenamiento queda fuera de las entidades y del gestor (SRP). |
| Pruebas | Implementaciones Memoria bajo test/ | Permiten ejercitar reglas y simular fallas sin tocar los datos reales. |

Este incremento muestra SRP, DIP y Controlador. Las decisiones Experto, Creador, OCP y Polimorfismo del modelo relacionadas con expensas, reclamos y equipos se implementarán en sus módulos; la sobreescritura de Guardia no reemplaza el ejemplo acordado de Camara y Luminaria.

## Precisiones y cambios respecto del modelo de etapa 1

Los diagramas de etapa 1 se conservan como referencia de lo entregado. El [diagrama del código actual](uml/etapa2-accesos.svg) y su [fuente](uml/etapa2-accesos.puml) complementan ese modelo; representan únicamente lo implementado. La [secuencia de ingreso actual](uml/etapa2-secuencia-ingreso.svg) documenta la validación cronológica adicional.

- Se incorporan identificadores internos UUID a Autorizacion y RegistroIngreso. Distinguen una actualización de egreso de una nueva entrada después de reiniciar, aunque coincidan visitante y fecha.
- Ambos repositorios agregan listar(), necesario para las consultas del menú y el control cronológico. No cambian las firmas de los tres casos de uso del gestor.
- RegistroIngreso implementa Comparable por fecha de entrada y, en empates, identificador. El historial también permite Comparator por lote y luego por el orden natural.
- Los objetos persistidos implementan Serializable. AlmacenAccesos guarda una instantánea mediante archivo temporal y reemplazo atómico, compartida por los dos repositorios. Se leen copias independientes: modificar un egreso y fallar al guardarlo no altera lo previamente confirmado.
- Se valida que un nuevo ingreso no sea anterior al último egreso registrado de esa persona. Complementa la prohibición de ingresos abiertos simultáneos y evita superponer visitas mediante fechas cargadas manualmente.
- Se explicitan extremos incluidos en las franjas y días: una recurrencia desde el día 1 hasta el día 30 incluye treinta días. La hora final debe ser posterior a la inicial y ambas corresponden al mismo día.
- GestorAccesos comprueba el permiso del otorgante para todas las fechas de la autorización. Una autorización duplicada exacta devuelve el registro existente, como prevé CU-21.
- Los constructores y getters auxiliares permiten cargar los datos ficticios y mostrarlos. Persona se identifica por documento. Lote.agregarResidencia permite preparar la demostración e impide un segundo titular; la gestión completa del padrón y los cambios de titularidad siguen pendientes.

## Persistencia y alcance

La serialización es una elección de implementación local, sin bibliotecas externas. El formato depende de las clases Java y aún no incluye migraciones entre versiones. Los archivos generados son de uso de esta aplicación y se guardan fuera del control de versiones. Por ahora se utiliza una sola instancia por archivo; no se ofrece trabajo concurrente desde varias consolas.

Los dos vecinos y guardias de demostración se crean al abrir el programa. Las autorizaciones, visitantes e ingresos sí se conservan entre ejecuciones. Aún no hay gestión editable del padrón, de empleados ni de los 150 lotes, ni identificación de usuarios mediante contraseña. Los roles del menú se seleccionan para demostrar los casos de uso.

## Próximos incrementos

1. Expensas, mora, pagos parciales, imputaciones, ajustes y morosidad; completar la segunda secuencia entregada.
2. Reclamos: estados, historial, comentarios, permisos, límites de trabajo y plazos.
3. Equipos y mantenimiento: Camara/Luminaria, prioridades, técnicos, órdenes y plazos.
4. Padrón completo, reportes exportados y planificador de notificaciones.
5. Pruebas integrales, informe final, revisión del equipo y tag etapa-2.

La fecha prevista por la consigna para etapa 2 es el 04/11/2026. No se marca etapa-2 como completa mientras esos módulos y entregables estén pendientes. La distribución real de tareas y las reflexiones de los integrantes deberán completarse con sus aportes, sin atribuirles código o revisiones que no realizaron.
