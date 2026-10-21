# SIGMA — Plan y decisiones de diseño

## Alcance

Propuesta 1: gestión del barrio Los Aromos. Aplicación Java orientada a objetos, inicialmente con interfaz de consola y persistencia en archivos. Módulos: reclamos, expensas y pagos, equipos y mantenimiento, visitas y reportes.

## Decisiones propuestas en el análisis previo

- Persona se vincula a Lote mediante Residencia, que registra tipo (titular, familiar o inquilino) y vigencia. Un lote tiene un único titular vigente; una persona puede ser titular de varios lotes.
- Equipo es abstracta y tiene las especializaciones Camara y Luminaria. La prioridad de falla usa polimorfismo y el contexto del sector para contar luminarias falladas.
- Empleado es abstracta y tiene Guardia, EmpleadoMantenimiento y, como propuesta de diseño, Administrador.
- Reclamo contiene CambioEstado y Comentario como composición, para conservar su evolución.
- Pago corresponde a un lote y genera imputaciones a distintas expensas, aplicando intereses y luego capital desde el período más antiguo. Los errores se corrigen mediante ajustes.
- Autorizacion registra visitante, lote, otorgante, vigencia y franja horaria. RegistroIngreso contiene autorización, guardia, ingreso y egreso opcional.

## SOLID y GRASP seleccionados

| Principio | Aplicación propuesta |
| --- | --- |
| SRP | Separar datos de expensa, cálculo de mora y persistencia. |
| OCP | Extender las reglas de prioridad sin modificar las reglas existentes. |
| DIP | Los gestores dependen de interfaces de repositorio, con implementaciones en archivos y en memoria. |
| Experto | La expensa calcula su saldo a partir de sus importes e imputaciones. |
| Creador | Reclamo crea los cambios de estado que contiene. |
| Controlador | GestorDeReclamos y GestorDeAccesos reciben las operaciones de la consola. |
| Polimorfismo | Camara y Luminaria resuelven la prioridad de falla de acuerdo con su tipo. |

Las [justificaciones completas de 3 SOLID y 4 GRASP](04-solid-y-grasp.md) documentan el problema, la decisión, las clases involucradas y la ventaja. Queda reflejar estas decisiones en el diagrama de clases final y actualizar las secuencias.

## Secuencias previstas

1. Registrar ingreso de visita: autorización vigente, inexistente o vencida; rechazo mediante excepción propia cuando corresponda.
2. Registrar pago: cálculo de mora e imputación por período, con loop y alternativas. Los mensajes deben coincidir con los métodos del diagrama de clases.

## Revisión de inconsistencias del borrador

- El PlantUML original omitía las asociaciones Vecino → CU-06, Empleado → CU-19 y Administrador → CU-04. Se incorporaron según las descripciones textuales.
- RF-04 figuraba en requerimientos pero no tenía caso de uso explícito. Se agregó CU-27 y sus asociaciones.
- El límite de cinco aplica a reclamos en curso, no al total histórico. El diagrama de clases debe expresar una relación 0..* y una restricción sobre los reclamos en curso. Antes de asignar responsable, la multiplicidad debe admitir 0..1.
- La reapertura de resuelto a en curso es una excepción explícita a la progresión ordinaria de RN-02.
- CU-03 actualmente limita la asignación a empleados con menos de cinco reclamos en curso. Revisar si la consigna exige limitar la asignación o únicamente la transición a en curso.
- Precisar cómo se prorratea el 3 % mensual (base de días, redondeo, momento inicial y prevención de doble cálculo). Esta decisión técnica queda pendiente de definición.
- Definir quién aplica saldo a favor, cómo revierte imputaciones un ajuste y cuándo se actualiza la morosidad.
- Confirmar si CU-14 debe extender Registrar pago o formar parte del cálculo de deuda consultado también por CU-16 y CU-25.
- Precisar qué significa «sin atender» para escalar un reclamo y cómo se evita generar avisos repetidos.
- Definir cuándo se ejecutan los cierres y avisos automáticos exigidos por el enunciado; el mecanismo técnico queda pendiente y no forma parte de los supuestos cotidianos.
- El PDF original fue mencionado en el chat, pero no se incorporó a esta importación. Las fechas, la rúbrica y los requisitos deben validarse contra ese archivo.

## Hitos informados en el chat

| Hito | Fecha informada (2026) |
| --- | --- |
| Entrega etapa 1 | 25/10 |
| Entrega etapa 2 | 04/11 |
| Defensa | 11/11 |

## Pendientes de etapa 1

- [ ] Verificar la consigna original y resolver las observaciones anteriores.
- [ ] Registrar integrantes y docente colaborador.
- [ ] Dibujar el diagrama de clases inicial con atributos, visibilidades, métodos y multiplicidades.
- [ ] Dibujar las dos secuencias y verificar su correspondencia con los métodos.
- [x] Redactar las justificaciones SOLID y GRASP.
- [ ] Preparar el diagrama de clases final y explicar los cambios.
- [ ] Revisar y exportar el informe de entrega.

## Etapa 2

- Implementar colecciones, Comparable para ordenar reclamos y Comparator para el reporte por deuda.
- Usar excepciones propias para las reglas del dominio y manejo de recursos de archivos según la consigna.
- Implementar persistencia e interfaz de consola.
- Probar mora, imputación, transiciones, límite de reclamos, prioridad de equipos y autorizaciones de visitas.
- Preparar plan de pruebas funcionales y guía de ejecución.

Cada integrante debe realizar sus propios commits y comprender el diseño completo para la defensa. Este commit inicial importa el trabajo previo; no sustituye la participación individual.
