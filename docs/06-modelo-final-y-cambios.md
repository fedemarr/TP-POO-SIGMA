# Modelo final y justificación de cambios

El modelo final aplica las decisiones justificadas en [SOLID y GRASP](04-solid-y-grasp.md). El documento de diseño contiene el desarrollo completo y las láminas de detalle.

## Comparación con el modelo inicial

| Aspecto | Modelo inicial | Modelo final | Motivo |
| --- | --- | --- | --- |
| Barrio | Coordina los módulos y carga y guarda archivos. | Conserva la información propia del barrio y el cálculo de su superficie total. | Reducir los motivos de cambio de una misma clase: SRP. |
| Coordinación | Las operaciones de ingreso y pago comienzan en Barrio. | Comienzan en GestorAccesos y GestorExpensas. Reclamos y mantenimiento tienen sus propios gestores. | Aplicar Controlador de GRASP por módulo. |
| Persistencia | Barrio contiene guardar y cargar. | Las interfaces de repositorio definen contratos y las clases Archivo los implementan. | Separar el almacenamiento y aplicar DIP. |
| Construcción de gestores | No aparecen dependencias explícitas de repositorios. | Los constructores de los gestores de accesos y expensas reciben sus dependencias. | Permitir implementaciones de archivos o de prueba sin cambiar la coordinación. |
| Intereses | Expensa concentra el cálculo de intereses y saldo. | CalculadoraMora resuelve el interés y Expensa conserva la responsabilidad de calcular su saldo con las imputaciones recibidas. | SRP junto con Experto en información. |
| Prioridad de equipos | Equipo declara prioridadDeFalla y sus subclases la sobrescriben. | Se conserva esta jerarquía y GestorMantenimiento usa la abstracción Equipo. | Hacer visible OCP y Polimorfismo sin inventar un cambio de jerarquía que no ocurrió. |
| Historial | Reclamo contiene CambioEstado. | Se conserva la composición y se exige crear el registro al validar y aplicar cada cambio de estado. | Hacer explícito Creador y mantener el historial consistente. |
| Pago | La secuencia no detalla la confirmación de todos los datos relacionados. | confirmarPago recibe el pago, las expensas modificadas y el lote. Un error descarta las copias de trabajo. | Evitar un pago confirmado con imputaciones o saldos incompletos. |
| Autorizaciones | La consulta no detalla varias autorizaciones candidatas. | El gestor busca por documento y lote y elige una vigente antes de registrar el ingreso. | No rechazar una visita por examinar únicamente una autorización vencida. |
| Errores | Las excepciones figuran como propuestas en las secuencias. | El modelo incorpora una jerarquía de errores de negocio. Las secuencias también muestran errores de persistencia. | Precisar los resultados alternativos que debe manejar la futura consola. |

## Precisiones de la revisión final

PlanificadorPlazos y RepositorioNotificaciones permiten evaluar las reglas temporales y evitar avisos repetidos. Pago.imputar recibe la fecha de aplicación, para distinguir el pago original de la aplicación posterior de un saldo a favor. Las órdenes pueden esperar un técnico mientras están abiertas, pero no pueden cerrarse sin ese dato. AjustePago identifica las contraimputaciones, conservando el pago y sus imputaciones originales.

Los [criterios de diseño y operación](05-criterios-diseno.md) precisan mora, ajustes, saldo a favor, plazos y persistencia sin modificar los seis supuestos cotidianos adoptados.

## Fuentes principales

- [Modelo inicial completo](uml/clases-inicial.puml)
- [Modelo final completo](uml/clases-final-completo.puml)
- [Arquitectura final](uml/06-arquitectura.svg)
- [Secuencia: ingreso de visita](uml/secuencia-ingreso-final.svg)
- [Secuencia: registro de pago](uml/secuencia-pago-final.svg)
