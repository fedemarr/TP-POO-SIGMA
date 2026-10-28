# Pruebas del primer incremento de etapa 2

Ejecución: 28/10/2026, Windows, JDK local Microsoft 21.0.12. Se compiló con avisos activados y tratados como errores (`-Xlint:all -Werror`). Resultado: **30 pruebas de dominio e integración y 8 funcionales aprobadas**.

Reproducción desde la raíz del repositorio:

```powershell
powershell -ExecutionPolicy Bypass -File .\sigma.ps1 -Accion probar -Jdk "C:\ruta\al\jdk"
```

## Dominio e integración

Pruebas automatizadas en test/sigma/pruebas/PruebasAccesos.java. Usan verificaciones explícitas y repositorios en memoria para las reglas. Los casos de persistencia usan archivos temporales bajo build/pruebas/.

| Grupo | Comprobaciones ejecutadas | Resultado |
| --- | --- | --- |
| Fechas y recurrencias | Límites horarios incluidos, fuera de horario, fechas invertidas, un día no recurrente, treinta días inclusivos y rechazo de treinta y uno, franja vacía o cruce de medianoche. | Aprobado |
| Otorgante | Lote ajeno rechazado, residente requiere permiso y vigencia durante todo el intervalo, duplicado exacto no crea otro registro. | Aprobado |
| Ingresos | Portería requerida, sin autorización, primera candidata vencida y segunda vigente, autorización de otro lote, normalización de espacios del documento. | Aprobado |
| Duplicados | Segundo ingreso rechazado tanto para el mismo lote como para otro. | Aprobado |
| Egresos | Rechazo sin ingreso o anterior a la entrada; salida válida permite volver; reingreso anterior al último egreso rechazado; un registro cerrado no admite otro egreso. | Aprobado |
| Escritura | Excepción al guardar ingreso no confirma; excepción al guardar egreso conserva la entrada abierta; una ruta bloqueada propaga el error. | Aprobado |
| Archivos | Apertura de nuevas instancias recupera autorización, entrada y salida; archivo corrupto no se sobrescribe; cambios sin guardar no alteran lo confirmado. | Aprobado |
| Java | Comparable por fecha, Comparator por lote, llamada polimórfica de describirRol mediante Empleado. | Aprobado |

## Pruebas funcionales

Pruebas automatizadas en test/sigma/pruebas/PruebasConsola.java. Ejecutan procesos reales de sigma.Main con entradas por consola. Cada proceso tiene un límite de espera y cada prueba verifica las respuestas del menú; las transcripciones completas quedan en build/pruebas/consola-*/.

Datos comunes: Ana, titular del lote 1; visitante ficticio documento 123; autorización 28/10/2026 de 09:00 a 18:00. Las fechas son fijas para que las pruebas puedan repetirse en cualquier día.

| Caso | Entrada | Resultado esperado | Resultado obtenido |
| --- | --- | --- | --- |
| PF-01 | Opción 99; letra x como número de lote; consulta de autorizaciones en archivo nuevo. | Informar errores, volver al menú y mostrar lista vacía. | Coincide. |
| PF-02 | Autorizar; ingresar 10:00; reingresar 10:15; egreso 09:00; egreso 11:00; ingresar 12:00. | Dos ingresos confirmados; rechazar duplicado y salida anterior; conservar historial con una visita abierta. | Coincide. |
| PF-03 | Nuevo proceso sobre el mismo archivo; listar autorizaciones e historial; intentar ingreso 12:30. | Recuperar datos y rechazar ingreso por visita abierta. | Coincide. |
| PF-04 | Registrar egreso 13:00; abrir otro proceso y consultar historial. | Mantener la salida y no mostrar visitas pendientes. | Coincide. |
| PF-05 | Ingresar documento 404 sin autorización. | Rechazar y no confirmar ingreso. | Coincide. |
| PF-06 | Intentar ingreso del documento 123 con guardia de ronda. | Rechazar por no estar en portería. | Coincide. |
| PF-07 | Abrir un archivo con bytes inválidos. | Salir con error, informar que el archivo se conserva y no modificar los bytes. | Coincide. |
| PF-08 | Guardar autorización en una ruta cuyo padre es un archivo. | Informar operación no confirmada y conservar el archivo que bloquea la ruta. | Coincide. |

## Pendiente para la entrega completa

Estas pruebas cubren únicamente accesos. Faltan las pruebas de expensas, reclamos, equipos, reportes y plazos, además de la comprobación integral de todos los módulos. No se consideran aprobadas funcionalidades todavía no implementadas.
