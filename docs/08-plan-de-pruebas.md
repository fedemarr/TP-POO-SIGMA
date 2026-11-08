# Plan de pruebas

## Pruebas unitarias

Se ejecutan con `bash probar-linux-mac.sh` (o `probar-windows.bat`). Resultado: las 8 pruebas terminan en `OK`.

| Prueba | Qué verifica |
| --- | --- |
| `PruebaModeloInicial` | Barrio, lotes, coeficiente, titular único, familiar autorizado y polimorfismo de empleados |
| `PruebaExpensas` | Ejemplo de mora de los criterios de diseño ($100 y $50) e imputación: primero intereses, desde el período más antiguo |
| `PruebaGestorExpensas` | Emisión con centavos residuales, pago sin deuda, saldo a favor, ajuste, morosidad con más de 3 períodos y notificación única |
| `PruebaReclamos` | Estados en orden, reapertura dentro de 5 días, cierre por plazo, escalamiento y orden natural |
| `PruebaGestorReclamos` | Quién puede reclamar, límite de 5 en curso, aviso de escalamiento, cambio de estado por el administrador y reapertura fuera de plazo |
| `PruebaMantenimiento` | Prioridades de cámara y luminaria, más de 3 luminarias falladas, fallas repetidas, especialidad, cierre y aviso de 48 horas |
| `PruebaAccesos` | Caminos de la secuencia de ingreso: sin autorización, guardia fuera de portería, ingreso duplicado, franja horaria, autorización repetida y recurrente de más de 30 días |
| `PruebaPersistencia` | Los datos y las referencias entre objetos se conservan al volver a abrir el programa |

## Pruebas funcionales

Se ejecutaron sobre el programa por consola (`bash ejecutar-linux-mac.sh consola`), partiendo de los datos de prueba iniciales. Fecha de ejecución: 07/11/2026.

| N.º | Caso de uso | Dato de entrada | Resultado esperado | Resultado obtenido |
| --- | --- | --- | --- | --- |
| F01 | CU-01 Registrar reclamo | Lote 1, DNI 30111222 (titular), tipo SEGURIDAD, "Porton abierto" | Reclamo #1 con prioridad ALTA en estado ABIERTO | Reclamo #1, SEGURIDAD, prioridad ALTA, ABIERTO |
| F02 | CU-01 A1 Solicitante no autorizado | Lote 1, DNI 99999999 | Se rechaza el reclamo | "El DNI 99999999 no vive en el lote 1" |
| F03 | CU-03 Asignar reclamo | Reclamo 1, legajo GUA-01 | Reclamo ASIGNADO a Mario Ruiz | Reclamo #1 ASIGNADO, responsable Mario Ruiz |
| F04 | CU-04 A1 Salto de estados | Reclamo 1, GUA-01, nuevo estado RESUELTO | Transición inválida | "No se puede pasar de ASIGNADO a RESUELTO" |
| F05 | CU-09 Emitir expensas | Período 2026-07, presupuesto 1500000 | Expensas emitidas para los 150 lotes | "Se emitieron las expensas de 2026-07 para los 150 lotes" |
| F06 | CU-09 A1 Período ya emitido | Período 2026-07, presupuesto 1500000 | Se rechaza la emisión | "Las expensas de 2026-07 ya fueron emitidas" |
| F07 | CU-12 Registrar pago | Lote 2, $5000, EFECTIVO, 2026-08-01 | Pago 1 registrado e imputado | Pago 1, lote 2, $5000, EFECTIVO |
| F08 | CU-12 A1 Fecha futura | Lote 2, $100, EFECTIVO, 2027-01-01 | Se rechaza el pago | "No se admiten pagos con fecha futura" |
| F09 | CU-15 Registrar ajuste | Lote 2, pago 1, motivo "Error de carga" | Ajuste por -$5000 y pago revertido, sin borrarlo | Ajuste 1 por $-5000; Pago 1 REVERTIDO |
| F10 | CU-17 Registrar falla | Equipo CAM-01, informa Guardia, "Sin imagen" | Cámara en estado CON_FALLA | Camara CAM-01, CON_FALLA |
| F11 | CU-18 Especialidad incorrecta | ADM-01, CAM-01, técnico MAN-02 (plomería) | Se rechaza la orden | "Las camaras y luminarias las repara un tecnico de ELECTRICIDAD" |
| F12 | CU-18 Generar orden | ADM-01, CAM-01, técnico MAN-01 (electricidad) | Orden de prioridad ALTA | Orden 1, Camara CAM-01, prioridad ALTA, ABIERTA, Jorge Paz |
| F13 | CU-21 Autorizar visita | Lote 1, DNI 30111222, visitante Luis Gomez DNI 999, hoy, 00:00 a 23:59 | Autorización guardada | "Visita autorizada: Luis Gomez (DNI 999) -> lote 1" |
| F14 | CU-22 Registrar ingreso | GUA-01, DNI 999, lote 1 | Ingreso registrado | "Ingreso registrado para el DNI 999" |
| F15 | CU-22 A2 Ingreso duplicado | GUA-01, DNI 999, lote 1 | Se rechaza el ingreso | "La persona ya esta dentro del barrio y no registro su egreso" |
| F16 | CU-24 Registrar egreso | DNI 999 | Egreso registrado | "Egreso registrado para el DNI 999" |
| F17 | CU-22 Guardia fuera de portería | GUA-02 (ronda), DNI 999, lote 1 | Se rechaza el ingreso | "Solo el guardia de porteria registra ingresos" |
| F18 | CU-25 Reporte de morosidad | Reportes > Morosidad por lote | Lotes con deuda vencida, con capital e intereses | Lote 2: 1 período impago, capital $10000.01, intereses $731.56, total $10731.57 |

Todas las pruebas funcionales dieron el resultado esperado.
