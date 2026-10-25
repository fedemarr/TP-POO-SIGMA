# Criterios de diseño y operación

Los seis supuestos cotidianos se conservan en el análisis. Esta sección precisa los contratos de las operaciones para que los diagramas, la futura implementación y las pruebas usen el mismo criterio. No cambia las tasas, plazos ni límites exigidos por la consigna.

## Importes y mora

Los importes se representan con BigDecimal. El capital y el interés se expresan en pesos con dos decimales y redondeo HALF_UP. Los cálculos intermedios conservan precisión y se redondean al confirmar cada imputación. El coeficiente de un lote es su superficie dividida por la suma de superficies de los lotes. Los centavos residuales de la emisión se distribuyen por número de lote para que el total coincida con el presupuesto.

El interés es simple y no capitalizable: capital pendiente × 0,03 × días / 30. Se consideran días completos entre el vencimiento y la fecha de consulta o pago. El día del vencimiento tiene cero días de atraso. Un pago parcial cambia la base de capital para el tramo siguiente. Por ejemplo, $10.000 de capital durante diez días generan $100 de interés. Si se pagan $5.100, se cancelan esos $100 y $5.000 de capital; diez días después, el capital restante de $5.000 genera $50 adicionales. Consultar varias veces no duplica intereses. No se admiten pagos futuros ni movimientos anteriores al último movimiento confirmado de la cuenta.

## Imputaciones, ajustes y saldo a favor

Cada pago aplica primero a intereses y luego a capital del período más antiguo, antes de avanzar al siguiente. Un excedente queda en el pago como importe sin imputar del lote. Al emitir futuras expensas, el gestor aplica esos saldos a favor desde el pago más antiguo. La nueva imputación lleva la fecha de aplicación, por eso Pago.imputar recibe la fecha como argumento.

La corrección elegida es una reversión total con asiento de ajuste: se conserva el pago original, se registra el motivo y un importe negativo por su total aún no revertido. Se conservan también las imputaciones originales y se agregan contraimputaciones identificadas por el ajuste. El ajuste surte efecto en su fecha, sin reescribir el pasado; si corresponde corregir el pago, se registra uno nuevo. Se permite una única reversión total por pago y no se revierte un pago que ya fue revertido. Se recalculan el saldo a favor, las deudas y la morosidad, y se confirma todo junto. Este criterio evita borrar evidencia de movimientos anteriores.

Un lote se marca como moroso al superar tres períodos vencidos con saldo pendiente. Si ya está marcado, conserva la condición mientras mantenga deuda vencida y la pierde al regularizarla. Una nueva expensa aún no vencida no impide quitar la marca.

## Plazos y notificaciones

PlanificadorPlazos ejecuta la revisión al iniciar la aplicación, antes de operaciones sensibles y una vez por minuto mientras el programa está abierto. Al iniciar recupera las situaciones vencidas durante el tiempo en que el programa estuvo cerrado. Los gestores verifican las fechas exactas, no un conteo de ejecuciones.

Un reclamo alto sigue sin atender mientras no haya pasado a en curso. Al completar 24 horas genera un aviso. Un reclamo resuelto admite reapertura hasta completar cinco días; después se cierra. Una cámara no operativa por más de 48 horas genera aviso al administrador. RepositorioNotificaciones guarda una clave única por evento para evitar repetir avisos al revisar varias veces. Las notificaciones formales se registran con destinatario, fecha, motivo y referencia, y se consultan en el sistema; no se presupone una integración con correo.

## Reclamos, equipos y permisos

La asignación permite varios reclamos pendientes por empleado. El máximo de cinco se controla sobre los que están EN_CURSO, antes de entrar a ese estado y antes de reabrir. Los cambios de estado y su registro en el historial se hacen como una operación indivisible. El vecino solicitante y el personal autorizado pueden agregar comentarios; el identificador de autor distingue documento de vecino y legajo de empleado.

El vecino o guardia registra una falla; el administrador genera su orden y asigna la especialidad adecuada. Si aún no hay técnico disponible, la orden permanece abierta sin asignar. No puede cerrarse sin técnico, fecha y descripción de reparación. Los reportes repetidos se agregan a la falla u orden abierta. Al superar tres luminarias no operativas de un sector, el gestor eleva las órdenes abiertas de luminarias afectadas a prioridad alta. Las cámaras siempre tienen prioridad alta. Un equipo reparado pasa a operativo; si no puede repararse, pasa a fuera de servicio.

## Persistencia y tipos

Las interfaces de repositorio aíslan a los gestores de los archivos. Sus implementaciones comparten el almacenamiento de la aplicación. La confirmación de pago guarda pago, imputaciones, ajustes aplicables, expensas y lote como una instantánea conjunta, mediante un archivo temporal y reemplazo atómico. Ante un error se conserva el estado vigente y se descartan las copias de trabajo. Los repositorios no confirman parcialmente una operación financiera. En la etapa 2 se manejarán IOException y excepciones de negocio con try, catch y finally.

El modelo final conserva al administrador dentro de la jerarquía Empleado para reutilizar legajo, nombre y fecha de ingreso. Los constructores y operaciones mostrados son los relevantes para el diseño; los getters necesarios no se repiten de forma exhaustiva. Las implementaciones Archivo realizan los contratos heredados de sus interfaces, aunque el diagrama omita las firmas repetidas. Las implementaciones Memoria se reservan para pruebas.
