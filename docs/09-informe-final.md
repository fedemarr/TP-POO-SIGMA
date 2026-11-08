# Informe final

## Cambios respecto del diseño original

En general pudimos programar lo que habíamos diseñado en la etapa 1: están todas las clases del diagrama final, con sus métodos, las reglas de negocio y los supuestos. Al pasar al código igual tuvimos que agregar algunas cosas que el diagrama no mostraba, y actualizamos los diagramas para que coincidan con lo que quedó programado. El detalle está en [Implementación: del diseño al código](07-implementacion.md).

Los cambios más importantes fueron:

- Además del menú por consola que habíamos planeado (`ConsolaSIGMA`), hicimos una interfaz gráfica con Swing (`VentanaSIGMA`). Para no escribir todo dos veces, las dos pantallas usan las mismas operaciones a través de la interfaz `Interaccion`.
- Todos los repositorios guardan en un solo archivo (`data/sigma.dat`). Así, cuando se registra un pago, se guardan juntos el pago, sus imputaciones, las expensas y el lote, como habíamos puesto en el diseño.
- Agregamos algunas operaciones que hacían falta para usar el sistema: cerrar una orden cuando el equipo no se puede reparar, asignar el técnico después de crear la orden y no duplicar una autorización de visita que ya existe.

## Dificultades encontradas

- **Guardar los datos.** Al principio cada repositorio tenía su propio archivo, y cuando volvíamos a abrir el programa los objetos quedaban repetidos: por ejemplo, el lote de un pago ya no era el mismo lote del barrio. Lo solucionamos guardando todo junto en un único archivo.
- **Los intereses por mora.** Nos costó porque un pago parcial cambia el capital sobre el que se calcula el interés de los días siguientes. Terminamos calculando el interés por tramos, entre un pago y el siguiente, y lo probamos con el ejemplo de los criterios de diseño ($100 y después $50).
- **Corregir un pago sin borrarlo.** La consigna pide que un pago no se elimine, así que hubo que registrar contraimputaciones y revisar las fechas antes de tocar las expensas, para no dejar nada a medias.
- **Que el código coincida con el diseño.** Cuando comparamos el código con el diagrama y los casos de uso, nos dimos cuenta de que faltaban algunas reglas (la morosidad con más de 3 períodos, los avisos de 24 y 48 horas, el límite de 5 reclamos en curso) y las completamos antes de entregar.

## Distribución de tareas

| Integrante | Tareas |
| --- | --- |
| Federico Martinez | Desarrollo del programa en Java: modelo, gestores, persistencia, interfaz y pruebas. |
| Joaquin Rodrigues | Diagramas UML: modelo de clases, arquitectura y secuencias, y su actualización para que coincidan con el código. |
| Lautaro Lugaro | Documentación: análisis, casos de uso, criterios de diseño, README, plan de pruebas e informe. |

## Reflexión del grupo

Lo que más nos sirvió fue haber hecho bien el diseño en la etapa 1. Como ya teníamos definidas las clases, las reglas y los casos de uso, al programar sabíamos qué tenía que hacer cada parte, y los patrones que habíamos justificado (los gestores como controladores, los repositorios como interfaces) nos facilitaron las pruebas, porque pudimos probar la lógica sin usar archivos.

También aprendimos que el diagrama y el código se tienen que revisar juntos todo el tiempo. Cuando los comparamos al final encontramos varias diferencias, y si lo hubiéramos hecho mientras programábamos nos habríamos ahorrado trabajo.

Lo que haríamos distinto es organizarnos mejor con el repositorio: subir los avances de a poco, cada uno con sus propios commits, en lugar de juntar todo para el final. Al dividirnos las tareas por área (código, UML y documentación) avanzamos rápido, pero nos faltó revisar más seguido el trabajo de los otros.
