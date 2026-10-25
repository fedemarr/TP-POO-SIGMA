# SIGMA - Casos de uso

## Relaciones

| Caso base o extensión | Caso relacionado | Tipo | Motivo |
| --- | --- | --- | --- |
| CU-01 | CU-02 | include | Validar la autorización del solicitante es obligatorio. |
| CU-09 | CU-10 | include | La emisión calcula el importe de cada lote. |
| CU-12 | CU-13 | include | Registrar un pago requiere imputarlo. |
| CU-22 | CU-23 | include | Todo ingreso exige verificar autorización vigente. |
| CU-06 | CU-05 | extend | Al consultar un reclamo resuelto puede solicitarse su reapertura. |
| CU-08 | CU-07 | extend | La consulta puede detectar un reclamo alto no atendido y generar el aviso. |
| CU-11 | CU-09 | extend | La emisión puede detectar la condición de morosidad. |
| CU-14 | CU-12 | extend | Si existe mora, el pago considera sus intereses pendientes. |

Registrar una falla y generar una orden son operaciones separadas: el vecino o guardia informa y el administrador genera la orden. La relación temporal entre ambas no se dibuja como include. Los avisos también se revisan automáticamente mediante el planificador.

## CU-01 Registrar reclamo

- Actor: Vecino
- Precondición: El vecino está registrado como titular o residente autorizado de un lote.
- Flujo principal: 1) Selecciona el lote. 2) El sistema valida la autorización (CU-02). 3) Ingresa tipo, descripción y fecha. 4) El sistema asigna la prioridad según el tipo (RN-01). 5) Crea el reclamo en estado abierto y registra el primer cambio de estado. 6) Devuelve el número de reclamo.
- Alternativos: A1) El solicitante no está autorizado: se rechaza y se informa el motivo. A2) El tipo es inválido: se solicita de nuevo.

## CU-02 Validar autorización del solicitante

- Actor: ninguno (invocado por CU-01)
- Precondición: Existe el lote indicado.
- Flujo principal: 1) Busca la residencia vigente de la persona en el lote. 2) Verifica que sea titular o residente autorizado. 3) Devuelve el resultado.
- Alternativos: A1) No hay residencia vigente: devuelve no autorizado.

## CU-03 Asignar reclamo a empleado

- Actor: Administrador
- Precondición: Existe un reclamo abierto y un empleado registrado.
- Flujo principal: 1) El administrador selecciona el reclamo y un empleado. 2) El sistema asigna el responsable. 3) Pasa el reclamo a asignado y registra el cambio en el historial.
- Alternativos: A1) Reclamo no abierto: rechaza la asignación. A2) Empleado inexistente: solicita otro. El límite de cinco se comprueba al pasar a en curso, no al acumular reclamos asignados.

## CU-04 Actualizar estado del reclamo

- Actor: Personal responsable del reclamo, Administrador.
- Precondición: El reclamo existe. El actor es su responsable o el administrador.
- Flujo principal: 1) Selecciona el reclamo. 2) Solicita el estado siguiente. 3) Si pasa a en curso, el gestor comprueba que el responsable tenga menos de cinco reclamos en curso. 4) Reclamo valida la transición y guarda estado, fecha y observación.
- Alternativos: A1) Salto de estados o reclamo cerrado: transición inválida. A2) Límite alcanzado: no cambia el estado. A3) Actor sin permiso: acceso denegado.

## CU-05 Consultar seguimiento de reclamo

- Actor: Vecino
- Precondición: El vecino presentó al menos un reclamo.
- Flujo principal: 1) Consulta sus reclamos. 2) Selecciona uno. 3) El sistema muestra los datos, el historial de cambios de estado y los comentarios.
- Alternativos: A1) No tiene reclamos: se informa la lista vacía.

## CU-06 Reabrir reclamo resuelto

- Actor: Vecino
- Precondición: El reclamo está en estado resuelto y no pasaron 5 días.
- Flujo principal: 1) El vecino selecciona un reclamo resuelto e indica el motivo. 2) El sistema comprueba el plazo de cinco días y la capacidad del responsable. 3) Lo devuelve a en curso y registra el cambio.
- Alternativos: A1) Plazo vencido: se cierra y rechaza la reapertura. A2) El responsable ya tiene cinco reclamos en curso: no se reabre y se informa al administrador para resolver la carga.

## CU-07 Consultar reclamos pendientes

- Actor: Administrador
- Precondición: Ninguna.
- Flujo principal: 1) Solicita el listado. 2) El sistema devuelve los reclamos no cerrados ordenados por prioridad y fecha. 3) Marca los que superan las 24 horas sin atención.
- Alternativos: A1) No hay pendientes: se informa la lista vacía.

## CU-08 Escalar reclamo de prioridad alta

- Actor: Administrador
- Precondición: Existe un reclamo de prioridad alta sin atender hace más de 24 horas.
- Flujo principal: 1) La revisión de plazos detecta un reclamo de prioridad alta sin pasar a en curso durante 24 horas. 2) Genera una notificación al administrador con una referencia al reclamo. 3) Evita repetir el mismo aviso.
- Alternativos: A1) Reclamo ya atendido: no escala. A2) Aviso existente: no lo duplica.

## CU-09 Emitir expensas del período

- Actor: Administrador
- Precondición: Está cargado el presupuesto del mes y no se emitieron las expensas de ese período.
- Flujo principal: 1) Indica período y presupuesto. 2) Para cada lote, el sistema calcula la expensa (CU-10). 3) Asigna el vencimiento el día 10. 4) Genera las expensas en estado impaga.
- Alternativos: A1) El período ya fue emitido: se rechaza la operación.

## CU-10 Calcular expensa del lote

- Actor: ninguno (invocado por CU-09)
- Precondición: Existe un lote del barrio y un presupuesto válido para el período.
- Flujo principal: 1) Divide la superficie del lote por la suma de superficies de los 150 lotes. 2) Multiplica el presupuesto por ese coeficiente. 3) Redondea a centavos. 4) Distribuye los centavos residuales para que la suma emitida coincida con el presupuesto.
- Alternativos: A1) Superficie total o presupuesto inválidos: se rechaza la emisión.

## CU-11 Notificar morosidad

- Actor: Administrador
- Precondición: El lote supera los tres períodos impagos.
- Flujo principal: 1) Al emitir expensas, registrar pagos o revisar plazos, el sistema cuenta los períodos vencidos con saldo. 2) Si son más de tres, marca al lote como moroso y registra una notificación al titular. 3) Evita repetir avisos de la misma situación.
- Alternativos: A1) No alcanza el umbral: no marca una nueva morosidad. A2) Al cancelar toda la deuda vencida, elimina la condición de moroso.

## CU-12 Registrar pago

- Actor: Administrador
- Precondición: El lote tiene al menos una expensa impaga.
- Flujo principal: 1) Selecciona el lote e ingresa fecha, importe y medio. 2) El gestor consulta y ordena las expensas. 3) Calcula los saldos a esa fecha. 4) Crea Pago e imputa mediante CU-13. 5) Actualiza la morosidad. 6) Confirma juntos pago, imputaciones y saldos.
- Alternativos: A1) Importe no positivo, medio inválido o fecha futura: rechazo. A2) Lote sin deuda: no registra otro pago. A3) Error de archivo: no confirma cambios parciales.

## CU-13 Imputar pago a períodos adeudados

- Actor: ninguno (invocado por CU-12)
- Precondición: Existe un pago válido y la lista de expensas del lote.
- Flujo principal: 1) Ordena las expensas adeudadas por período. 2) En cada una aplica primero a intereses pendientes y después a capital. 3) Crea las imputaciones y actualiza la expensa. 4) Continúa mientras quede importe. 5) Conserva el remanente como saldo a favor.
- Alternativos: A1) El pago no alcanza: la expensa queda parcialmente pagada. A2) El pago supera la deuda: conserva el excedente para futuras expensas.

## CU-14 Aplicar interés por mora

- Actor: Administrador
- Precondición: El pago se registra después del vencimiento de alguna expensa.
- Flujo principal: 1) Calcula el interés simple del 3 % mensual, con tasa diaria 0,03/30, sobre el capital impago durante cada tramo. 2) Considera pagos parciales y ajustes según su fecha. 3) Descuenta el interés ya pagado. 4) Devuelve el interés pendiente sin registrarlo otra vez como un cargo.
- Alternativos: A1) Sin atraso: interés cero. A2) Repetición de consulta: devuelve el mismo resultado, sin acumular cargos duplicados.

## CU-15 Registrar ajuste de pago

- Actor: Administrador
- Precondición: Existe un pago registrado con error.
- Flujo principal: 1) Selecciona el pago y registra el motivo. 2) Crea un asiento por el importe negativo del pago aún no revertido. 3) Registra contraimputaciones que revierten su aplicación y su saldo a favor en la fecha del ajuste. 4) Recalcula la deuda y la morosidad. 5) Confirma el conjunto sin borrar el pago original. 6) Si corresponde, registra luego un pago nuevo con los datos correctos.
- Alternativos: A1) Pago ya revertido: no permite otro ajuste de reversión. A2) Motivo vacío o importe distinto del total a revertir: rechazo. A3) Error de archivo: no confirma la operación.

## CU-16 Consultar estado de cuenta del lote

- Actor: Vecino
- Precondición: El vecino es titular o residente del lote.
- Flujo principal: 1) Selecciona el lote. 2) El sistema muestra las expensas con su estado, los pagos imputados, los intereses y el saldo total.
- Alternativos: A1) No es titular ni residente del lote: se deniega el acceso.

## CU-17 Registrar falla de equipo

- Actor: Vecino, Guardia de seguridad
- Precondición: El equipo está registrado y quien informa es vecino o guardia.
- Flujo principal: 1) Selecciona el equipo e indica la descripción. 2) El sistema registra la falla y pasa el equipo a con falla. 3) La falla queda pendiente de gestión por el administrador mediante CU-18.
- Alternativos: A1) Existe una falla u orden abierta: agrega la información a la existente, según SUP-05. A2) Equipo inexistente: rechaza el reporte.

## CU-18 Generar orden de mantenimiento

- Actor: Administrador
- Precondición: Existe una falla reportada. Quien genera la orden es el administrador.
- Flujo principal: 1) El administrador selecciona la falla. 2) El sistema determina la prioridad mediante Equipo.prioridadDeFalla. 3) Si hay más de tres luminarias falladas en el sector, también eleva las órdenes abiertas afectadas. 4) Crea la orden y asigna un técnico de la especialidad correspondiente.
- Alternativos: A1) Sin técnico disponible: la orden queda abierta sin asignar. A2) Ya existe orden abierta para esa falla: muestra la existente y no duplica.

## CU-19 Cerrar orden de mantenimiento

- Actor: Empleado de mantenimiento
- Precondición: La orden está abierta y asignada al empleado.
- Flujo principal: 1) Selecciona la orden. 2) Ingresa fecha de cierre, técnico y descripción de la reparación. 3) El sistema cierra la orden y pasa el equipo a operativo.
- Alternativos: A1) Falta alguno de los tres datos: el sistema impide el cierre (RN-16). A2) El equipo no se pudo reparar: pasa a fuera de servicio.

## CU-20 Consultar estado de equipos

- Actor: Administrador
- Precondición: El administrador tiene acceso al módulo de equipos.
- Flujo principal: 1) Solicita el listado, con filtro opcional por sector o estado. 2) El sistema muestra cada equipo con su estado y sus órdenes abiertas. 3) Marca las cámaras sin funcionar hace más de 48 horas (RN-15).
- Alternativos: A1) No hay equipos para el filtro elegido: lista vacía. A2) Cámara no operativa por más de 48 horas: registra el aviso si todavía no existe.

## CU-21 Autorizar visita

- Actor: Vecino
- Precondición: El vecino es titular o residente del lote.
- Flujo principal: 1) El vecino identifica al visitante y el lote. 2) Indica fechas y franja horaria diaria. 3) Si es una autorización recurrente de proveedor habitual, su duración no supera treinta días. 4) El sistema valida los datos y guarda la autorización.
- Alternativos: A1) Recurrencia superior a treinta días o fechas invertidas: rechazo. A2) La franja cruza medianoche: se solicitan franjas separadas. A3) Duplicado de autorización: muestra la existente.

## CU-22 Registrar ingreso de visita

- Actor: Guardia de seguridad
- Precondición: El guardia está en el puesto de portería.
- Flujo principal: 1) El guardia ingresa documento, lote y fecha y hora. 2) GestorAccesos valida portería. 3) Busca una autorización vigente mediante CU-23. 4) Verifica que el documento no tenga ingreso abierto. 5) Crea y guarda RegistroIngreso. 6) Confirma el ingreso al terminar el guardado.
- Alternativos: A1) Sin autorización vigente: AutorizacionInvalidaException. A2) Ingreso previo sin egreso: IngresoDuplicadoException. A3) Guardado fallido: no habilita el ingreso.

## CU-23 Verificar autorización vigente

- Actor: ninguno (invocado por CU-22)
- Precondición: Se conocen el documento, el lote y el momento del ingreso.
- Flujo principal: 1) Busca todas las autorizaciones del documento y lote. 2) Comprueba fechas y franja horaria de cada candidata. 3) Selecciona una vigente.
- Alternativos: A1) No hay candidatas o todas están vencidas o fuera de horario: devuelve autorización inválida.

## CU-24 Registrar egreso de visita

- Actor: Guardia de seguridad
- Precondición: Existe un registro de ingreso sin egreso para esa persona.
- Flujo principal: 1) Busca el ingreso abierto por documento. 2) Registra la hora de egreso.
- Alternativos: A1) No hay ingreso abierto: se informa el error.

## CU-25 Emitir reporte de morosidad

- Actor: Administrador
- Precondición: El administrador tiene acceso a las cuentas de los lotes.
- Flujo principal: 1) Solicita el reporte. 2) El sistema lista los lotes con deuda, ordenados por importe adeudado, indicando períodos impagos, intereses y condición de moroso. 3) Lo exporta a archivo.
- Alternativos: A1) No hay deuda: informa reporte vacío. A2) No se puede exportar el archivo: informa el error y conserva la consulta en pantalla.

## CU-26 Emitir reporte de equipos fuera de servicio

- Actor: Administrador
- Precondición: El administrador tiene acceso al inventario de equipos.
- Flujo principal: 1) Solicita el reporte. 2) El sistema lista los equipos en estado con falla, en reparación o fuera de servicio, agrupados por sector, con sus órdenes abiertas. 3) Lo exporta a archivo.
- Alternativos: A1) Todos están operativos: reporte vacío. A2) Error de exportación: informa el error sin modificar el inventario.

## CU-27 Agregar comentario a reclamo

- Actor: Vecino solicitante y personal autorizado, incluidos guardias, mantenimiento y administrador.
- Precondición: El reclamo existe y el actor está autorizado para intervenir.
- Flujo principal: 1) Selecciona el reclamo. 2) Ingresa texto no vacío. 3) El sistema registra el comentario con un identificador estable de autor y la fecha. 4) Lo muestra en el seguimiento.
- Alternativos: A1) Texto vacío: rechazo. A2) Actor sin permiso: acceso denegado.
