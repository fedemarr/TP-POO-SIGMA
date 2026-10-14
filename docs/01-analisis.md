# SIGMA — Análisis del enunciado

Borrador de la etapa 1, integrado a partir del trabajo previo del equipo. Los supuestos son decisiones propuestas; deben contrastarse con la consigna original antes de la entrega.


## 1.1 Actores

| Actor | Descripción | Rol en el sistema |
| --- | --- | --- |
| Vecino | Titular de un lote o residente autorizado por él | Presenta reclamos, autoriza visitas, consulta su estado de cuenta, registra fallas |
| Administrador | Responsable de la administración del barrio | Asigna reclamos, emite expensas, registra pagos, genera órdenes y reportes |
| Guardia de seguridad | Personal con turno y puesto asignado | Controla ingresos y egresos de visitas, registra fallas de equipos |
| Empleado de mantenimiento | Personal con una especialidad | Atiende reclamos asignados y cierra órdenes de mantenimiento |

## 1.2 Requerimientos funcionales

| Código | Requerimiento | Actor principal |
| --- | --- | --- |
| RF-01 | Registrar un reclamo o solicitud indicando lote, tipo y descripción | Vecino |
| RF-02 | Asignar un reclamo a un empleado responsable | Administrador |
| RF-03 | Actualizar el estado de un reclamo dejando registro de la evolución | Empleado de mantenimiento |
| RF-04 | Agregar comentarios a un reclamo | Vecino, Empleado |
| RF-05 | Consultar el seguimiento completo de un reclamo | Vecino |
| RF-06 | Reabrir un reclamo resuelto dentro del plazo | Vecino |
| RF-07 | Escalar al administrador los reclamos de prioridad alta no atendidos | Administrador |
| RF-08 | Emitir las expensas de un período para todos los lotes | Administrador |
| RF-09 | Registrar pagos totales o parciales de expensas | Administrador |
| RF-10 | Calcular intereses punitorios por mora | Administrador |
| RF-11 | Imputar los pagos a intereses y capital por antigüedad | Administrador |
| RF-12 | Registrar ajustes sobre pagos erróneos | Administrador |
| RF-13 | Consultar el estado de cuenta de un lote | Vecino |
| RF-14 | Registrar la falla de una luminaria o cámara | Vecino, Guardia |
| RF-15 | Generar órdenes de mantenimiento con su prioridad | Administrador |
| RF-16 | Cerrar una orden registrando fecha, técnico y descripción | Empleado de mantenimiento |
| RF-17 | Consultar el estado de cada equipo | Administrador |
| RF-18 | Autorizar el ingreso de una visita o proveedor | Vecino |
| RF-19 | Registrar el ingreso de una visita verificando su autorización | Guardia |
| RF-20 | Registrar el egreso de una visita | Guardia |
| RF-21 | Emitir reportes de reclamos pendientes, morosidad y equipos fuera de servicio | Administrador |

## 1.3 Reglas de negocio

| Código | Regla |
| --- | --- |
| RN-01 | La prioridad del reclamo depende del tipo: seguridad alta, mantenimiento media, convivencia y administrativo bajas |
| RN-02 | Los estados son abierto, asignado, en curso, resuelto y cerrado, y solo se avanza al estado inmediato siguiente |
| RN-03 | Un reclamo resuelto puede reabrirse dentro de los 5 días; pasado ese plazo se cierra automáticamente |
| RN-04 | Cada reclamo tiene un único responsable asignado |
| RN-05 | Ningún empleado puede tener más de 5 reclamos en curso simultáneos |
| RN-06 | Un reclamo de prioridad alta sin atender durante 24 horas se escala al administrador |
| RN-07 | Solo el titular del lote o una persona autorizada por él puede presentar reclamos |
| RN-08 | La expensa mensual es el presupuesto del mes por el coeficiente de participación del lote |
| RN-09 | El vencimiento es el día 10; desde esa fecha se aplica un 3 % mensual de interés punitorio sobre el saldo impago |
| RN-10 | Los pagos parciales se imputan primero a intereses y después a capital, empezando por el período más antiguo |
| RN-11 | Un lote con más de tres períodos impagos se marca como moroso y se notifica al titular; la condición se quita al regularizar |
| RN-12 | Un pago registrado no se elimina: los errores se corrigen con un asiento de ajuste |
| RN-13 | Los estados de un equipo son operativo, con falla, en reparación y fuera de servicio |
| RN-14 | Una falla en cámara genera una orden de prioridad alta; en luminaria genera una normal, que pasa a alta si fallan más de 3 luminarias del mismo sector |
| RN-15 | Una cámara sin funcionar más de 48 horas se informa al administrador |
| RN-16 | Una orden de mantenimiento no se cierra sin fecha, técnico y descripción de la reparación |
| RN-17 | Toda visita requiere autorización previa del vecino con fecha y franja horaria; sin autorización no se habilita el ingreso |
| RN-18 | Los proveedores habituales pueden tener autorización recurrente de hasta 30 días |
| RN-19 | Cada ingreso se registra con su egreso; una persona no puede registrar un segundo ingreso sin haber salido |
| RN-20 | Un lote tiene un único titular, pero una persona puede ser titular de varios lotes |

## 1.4 Supuestos adoptados

El enunciado pide documentar todo lo que no figure. Estos son los que tomamos:

| Código | Supuesto |
| --- | --- |
| SUP-01 | El coeficiente de participación del lote se calcula como su superficie sobre la superficie total del barrio, y se recalcula solo si cambia la superficie |
| SUP-02 | El presupuesto mensual del barrio es un dato de entrada que carga el administrador antes de emitir las expensas |
| SUP-03 | El administrador es un empleado del barrio con su propio legajo y fecha de ingreso |
| SUP-04 | Los 150 lotes son fijos: no se dan de alta ni de baja durante la operación del sistema |
| SUP-05 | El interés punitorio del 3 % mensual se calcula proporcional a los días de atraso sobre el saldo impago |
| SUP-06 | Las validaciones por tiempo (escalado a las 24 horas, cierre a los 5 días, aviso de cámara a las 48 horas) se evalúan cuando el administrador consulta los reclamos o los equipos, porque el sistema no tiene un proceso de ejecución programada |
| SUP-07 | La orden de mantenimiento se genera automáticamente al registrarse una falla, con la prioridad que define RN-14; el administrador luego le asigna el técnico |
| SUP-08 | Una persona autorizada por el titular se registra como residente del lote con tipo familiar o inquilino |
| SUP-09 | La notificación formal al titular moroso y los avisos al administrador se registran en el sistema y se muestran por pantalla, sin envío real de correo |
| SUP-10 | Los reportes se emiten por consola y se exportan a un archivo de texto |
| SUP-11 | Un reclamo cerrado no puede reabrirse bajo ninguna condición |
