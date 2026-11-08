package sigma.vista;

import sigma.AplicacionSIGMA;
import sigma.excepciones.ReglaNegocioException;
import sigma.gestores.GestorExpensas;
import sigma.gestores.GestorMantenimiento;
import sigma.gestores.GestorReclamos;
import sigma.gestores.GestorReportes;
import sigma.modelo.AjustePago;
import sigma.modelo.Administrador;
import sigma.modelo.Autorizacion;
import sigma.modelo.Barrio;
import sigma.modelo.Camara;
import sigma.modelo.CambioEstado;
import sigma.modelo.Comentario;
import sigma.modelo.Empleado;
import sigma.modelo.EmpleadoMantenimiento;
import sigma.modelo.Equipo;
import sigma.modelo.Especialidad;
import sigma.modelo.EstadoReclamo;
import sigma.modelo.Expensa;
import sigma.modelo.FallaEquipo;
import sigma.modelo.Guardia;
import sigma.modelo.Lote;
import sigma.modelo.Luminaria;
import sigma.modelo.MedioPago;
import sigma.modelo.Notificacion;
import sigma.modelo.OrdenMantenimiento;
import sigma.modelo.Pago;
import sigma.modelo.Persona;
import sigma.modelo.PuestoGuardia;
import sigma.modelo.Reclamo;
import sigma.modelo.Residencia;
import sigma.modelo.TipoReclamo;
import sigma.modelo.TipoResidente;
import sigma.modelo.Visitante;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Operaciones que ofrecen las dos interfaces (consola y ventana).
 * Pide los datos a traves de Interaccion, llama a los gestores (controladores)
 * y muestra el resultado. No contiene reglas de negocio.
 */
public class OperacionesSIGMA {
    // Opciones de cada menu. La consola y la ventana muestran estos mismos textos.
    public static final String[] MENU_BARRIO = {"Ver un lote", "Ver lotes con residentes", "Agregar residente",
            "Ver personal", "Registrar empleado"};
    public static final String[] MENU_RECLAMOS = {"Registrar reclamo", "Asignar responsable", "Cambiar estado",
            "Reabrir reclamo", "Agregar comentario", "Ver seguimiento", "Ver reclamos pendientes"};
    public static final String[] MENU_EXPENSAS = {"Emitir expensas", "Registrar pago", "Consultar cuenta", "Revertir pago"};
    public static final String[] MENU_MANTENIMIENTO = {"Registrar camara", "Registrar luminaria", "Registrar falla",
            "Generar orden", "Asignar tecnico", "Iniciar reparacion", "Cerrar orden", "Ver equipos", "Ver ordenes"};
    public static final String[] MENU_VISITAS = {"Autorizar visita", "Registrar ingreso", "Registrar egreso"};
    public static final String[] MENU_REPORTES = {"Ver reclamos pendientes", "Morosidad por lote", "Equipos no operativos",
            "Ver notificaciones", "Exportar reporte"};

    private final AplicacionSIGMA aplicacion;
    private final Interaccion vista;
    private String ultimoReporte = "";

    public OperacionesSIGMA(AplicacionSIGMA aplicacion, Interaccion vista) {
        this.aplicacion = aplicacion;
        this.vista = vista;
    }

    /** Ejecuta la opcion elegida y traduce los errores a mensajes para el usuario. */
    public void ejecutar(String opcion) {
        try {
            aplicacion.revisarPlazos();
            realizar(opcion);
        } catch (ReglaNegocioException e) {
            vista.mostrarError("Operacion no permitida: " + e.getMessage());
        } catch (IOException e) {
            vista.mostrarError("No se pudieron guardar los datos. La operacion no se confirmo. " + e.getMessage());
        } catch (IllegalArgumentException | DateTimeException e) {
            vista.mostrarError("Dato invalido: " + e.getMessage());
        }
    }

    /** Revision periodica de plazos (la ventana la llama una vez por minuto). */
    public void revisarPlazos() {
        try {
            aplicacion.revisarPlazos();
        } catch (ReglaNegocioException | IOException e) {
            vista.mostrarError("Error al revisar los plazos: " + e.getMessage());
        }
    }

    private void realizar(String opcion) throws ReglaNegocioException, IOException {
        switch (opcion) {
            // Barrio
            case "Ver un lote": verLote(); break;
            case "Ver lotes con residentes": verLotesConResidentes(); break;
            case "Agregar residente": agregarResidente(); break;
            case "Ver personal": verPersonal(); break;
            case "Registrar empleado": registrarEmpleado(); break;
            // Reclamos
            case "Registrar reclamo": registrarReclamo(); break;
            case "Asignar responsable": asignarReclamo(); break;
            case "Cambiar estado": cambiarEstadoReclamo(); break;
            case "Reabrir reclamo": reabrirReclamo(); break;
            case "Agregar comentario": comentarReclamo(); break;
            case "Ver seguimiento": verSeguimiento(); break;
            case "Ver reclamos pendientes": verReclamosPendientes(); break;
            // Expensas
            case "Emitir expensas": emitirExpensas(); break;
            case "Registrar pago": registrarPago(); break;
            case "Consultar cuenta": consultarCuenta(); break;
            case "Revertir pago": registrarAjuste(); break;
            // Mantenimiento
            case "Registrar camara": registrarCamara(); break;
            case "Registrar luminaria": registrarLuminaria(); break;
            case "Registrar falla": registrarFalla(); break;
            case "Generar orden": generarOrden(); break;
            case "Asignar tecnico": asignarTecnico(); break;
            case "Iniciar reparacion": iniciarReparacion(); break;
            case "Cerrar orden": cerrarOrden(); break;
            case "Ver equipos": verEquipos(); break;
            case "Ver ordenes": verOrdenes(); break;
            // Visitas
            case "Autorizar visita": autorizarVisita(); break;
            case "Registrar ingreso": registrarIngreso(); break;
            case "Registrar egreso": registrarEgreso(); break;
            // Reportes
            case "Morosidad por lote": reporteMorosidad(); break;
            case "Equipos no operativos": reporteEquipos(); break;
            case "Ver notificaciones": verNotificaciones(); break;
            case "Exportar reporte": exportarReporte(); break;
            default:
                throw new IllegalArgumentException("Opcion desconocida: " + opcion);
        }
    }

    // ---------------------------------------------------------------- Barrio

    private void verLote() {
        Lote lote = pedirLote();
        String texto = lote + "\n";
        for (Residencia residencia : lote.getResidencias()) {
            texto = texto + "  " + residencia + "\n";
        }
        texto = texto + "Expensas:\n";
        for (Expensa expensa : lote.getExpensas()) {
            texto = texto + "  " + expensa + "\n";
        }
        vista.mostrar(texto);
    }

    private void verLotesConResidentes() {
        String texto = "LOTES CON RESIDENTES (" + barrio().getLotes().size() + " lotes en total)\n";
        for (Lote lote : barrio().getLotes()) {
            if (!lote.getResidencias().isEmpty()) {
                texto = texto + lote + "\n";
                for (Residencia residencia : lote.getResidencias()) {
                    texto = texto + "   " + residencia + "\n";
                }
            }
        }
        vista.mostrar(texto);
    }

    private void agregarResidente() throws ReglaNegocioException, IOException {
        Lote lote = pedirLote();
        Persona persona = new Persona(pedir("Nombre"), pedir("Apellido"), pedir("DNI"), pedir("Telefono"), pedir("Correo electronico"));
        TipoResidente tipo = TipoResidente.values()[vista.elegir("Tipo de residente", nombres(TipoResidente.values()))];
        boolean autorizado = tipo == TipoResidente.TITULAR || pedirSiNo("El titular lo autoriza a presentar reclamos y autorizar visitas?");
        lote.agregarResidencia(new Residencia(persona, tipo, LocalDate.now(), null, autorizado));
        aplicacion.guardarCambios();
        vista.mostrar("Se agrego a " + persona + " al lote " + lote.getNumero());
    }

    private void verPersonal() {
        String texto = "PERSONAL\n";
        for (Empleado empleado : barrio().getPersonal()) {
            texto = texto + empleado + "\n";
        }
        vista.mostrar(texto);
    }

    private void registrarEmpleado() throws IOException {
        String[] tipos = {"Administrador", "Guardia", "Mantenimiento"};
        int tipo = vista.elegir("Tipo de empleado", tipos);
        String legajo = pedir("Legajo");
        String nombre = pedir("Nombre y apellido");
        LocalDate ingreso = LocalDate.now();
        Empleado empleado;
        if (tipo == 0) {
            empleado = new Administrador(legajo, nombre, ingreso);
        } else if (tipo == 1) {
            PuestoGuardia puesto = PuestoGuardia.values()[vista.elegir("Puesto", nombres(PuestoGuardia.values()))];
            empleado = new Guardia(legajo, nombre, ingreso, pedir("Turno"), puesto);
        } else {
            Especialidad especialidad = Especialidad.values()[vista.elegir("Especialidad", nombres(Especialidad.values()))];
            empleado = new EmpleadoMantenimiento(legajo, nombre, ingreso, especialidad);
        }
        barrio().agregarEmpleado(empleado);
        aplicacion.guardarCambios();
        vista.mostrar("Empleado registrado: " + empleado);
    }

    // ---------------------------------------------------------------- Reclamos

    private void registrarReclamo() throws ReglaNegocioException, IOException {
        Lote lote = pedirLote();
        Persona solicitante = pedirResidente(lote);
        TipoReclamo tipo = TipoReclamo.values()[vista.elegir("Tipo de reclamo", nombres(TipoReclamo.values()))];
        Reclamo reclamo = reclamos().registrarReclamo(lote, solicitante, tipo, pedir("Descripcion"), LocalDateTime.now());
        vista.mostrar("Reclamo registrado:\n" + reclamo);
    }

    private void asignarReclamo() throws ReglaNegocioException, IOException {
        Reclamo reclamo = pedirReclamo();
        Empleado empleado = pedirEmpleado();
        reclamos().asignarReclamo(reclamo, empleado, LocalDateTime.now());
        vista.mostrar("Reclamo asignado:\n" + reclamo);
    }

    private void cambiarEstadoReclamo() throws ReglaNegocioException, IOException {
        Reclamo reclamo = pedirReclamo();
        Empleado empleado = pedirEmpleado();
        EstadoReclamo[] estados = {EstadoReclamo.EN_CURSO, EstadoReclamo.RESUELTO};
        EstadoReclamo nuevo = estados[vista.elegir("Nuevo estado", nombres(estados))];
        reclamos().cambiarEstado(reclamo, empleado, nuevo, pedir("Observacion"), LocalDateTime.now());
        vista.mostrar("Estado actualizado:\n" + reclamo);
    }

    private void reabrirReclamo() throws ReglaNegocioException, IOException {
        Reclamo reclamo = pedirReclamo();
        Persona vecino = pedirResidente(reclamo.getLote());
        reclamos().reabrir(reclamo, vecino, pedir("Motivo"), LocalDateTime.now());
        vista.mostrar("Reclamo reabierto:\n" + reclamo);
    }

    // CU-27: comentan el vecino que hizo el reclamo (por DNI) o el personal del barrio (por legajo)
    private void comentarReclamo() throws ReglaNegocioException, IOException {
        Reclamo reclamo = pedirReclamo();
        String autor = pedir("Autor (DNI del vecino o legajo del empleado)");
        boolean esElVecino = reclamo.getSolicitante().getDocumento().equals(autor);
        boolean esPersonal = barrio().buscarEmpleado(autor) != null;
        if (!esElVecino && !esPersonal) {
            throw new ReglaNegocioException("Solo pueden comentar el vecino que hizo el reclamo o el personal del barrio");
        }
        reclamos().agregarComentario(reclamo, autor, pedir("Comentario"), LocalDateTime.now());
        vista.mostrar("Comentario agregado al reclamo #" + reclamo.getNumero());
    }

    private void verSeguimiento() throws ReglaNegocioException {
        Reclamo reclamo = pedirReclamo();
        Persona vecino = pedirResidente(reclamo.getLote());
        reclamos().consultarSeguimiento(reclamo, vecino);
        String texto = reclamo + "\nHistorial:\n";
        for (CambioEstado cambio : reclamo.getHistorial()) {
            texto = texto + "  " + cambio + "\n";
        }
        texto = texto + "Comentarios:\n";
        for (Comentario comentario : reclamo.getComentarios()) {
            texto = texto + "  " + comentario + "\n";
        }
        vista.mostrar(texto);
    }

    private void verReclamosPendientes() {
        mostrarReporte(reportes().reclamosPendientes(LocalDateTime.now()));
    }

    // ---------------------------------------------------------------- Expensas

    private void emitirExpensas() throws ReglaNegocioException, IOException {
        YearMonth periodo = YearMonth.parse(pedir("Periodo (AAAA-MM)"));
        BigDecimal presupuesto = new BigDecimal(pedir("Presupuesto total del mes"));
        expensas().emitirExpensas(barrio(), periodo, presupuesto);
        vista.mostrar("Se emitieron las expensas de " + periodo + " para los " + barrio().getLotes().size() + " lotes");
    }

    private void registrarPago() throws ReglaNegocioException, IOException {
        Lote lote = pedirLote();
        BigDecimal importe = new BigDecimal(pedir("Importe"));
        MedioPago medio = MedioPago.values()[vista.elegir("Medio de pago", nombres(MedioPago.values()))];
        LocalDate fecha = pedirFecha("Fecha del pago (AAAA-MM-DD, vacio = hoy)");
        Pago pago = expensas().registrarPago(lote, importe, medio, fecha);
        vista.mostrar("Pago registrado:\n" + pago + "\nSaldo de la cuenta: $" + expensas().consultarCuenta(lote, fecha));
    }

    // CU-16: solo un titular o residente del lote puede ver su cuenta
    private void consultarCuenta() {
        Lote lote = pedirLote();
        pedirResidente(lote);
        LocalDate hoy = LocalDate.now();
        String texto = "CUENTA DEL " + lote + " al " + hoy + "\n";
        for (Expensa expensa : expensas().consultarExpensas(lote)) {
            texto = texto + "  " + expensa + "\n";
        }
        for (Pago pago : expensas().consultarPagos(lote)) {
            texto = texto + "  " + pago + "\n";
        }
        texto = texto + "Saldo (deuda - saldo a favor): $" + expensas().consultarCuenta(lote, hoy);
        vista.mostrar(texto);
    }

    private void registrarAjuste() throws ReglaNegocioException, IOException {
        Lote lote = pedirLote();
        Pago pago = null;
        int numero = pedirEntero("Numero de pago a revertir");
        for (Pago candidato : expensas().consultarPagos(lote)) {
            if (candidato.getNumero() == numero) pago = candidato;
        }
        if (pago == null) throw new IllegalArgumentException("El lote no tiene el pago " + numero);
        AjustePago ajuste = expensas().registrarAjuste(pago, pago.getImporte().negate(), pedir("Motivo del ajuste"), LocalDate.now());
        vista.mostrar("Ajuste registrado: " + ajuste + "\n" + pago);
    }

    // ---------------------------------------------------------------- Mantenimiento

    private void registrarCamara() throws ReglaNegocioException, IOException {
        String codigo = pedirCodigoNuevo();
        Camara camara = new Camara(codigo, barrio().obtenerSector(pedir("Sector")), LocalDate.now(),
                pedir("Resolucion"), pedirEntero("Dias de grabacion"));
        mantenimiento().registrarEquipo(camara);
        vista.mostrar("Equipo registrado: " + camara);
    }

    private void registrarLuminaria() throws ReglaNegocioException, IOException {
        String codigo = pedirCodigoNuevo();
        Luminaria luminaria = new Luminaria(codigo, barrio().obtenerSector(pedir("Sector")), LocalDate.now(),
                pedir("Tipo de lampara"), Double.parseDouble(pedir("Potencia (W)")));
        mantenimiento().registrarEquipo(luminaria);
        vista.mostrar("Equipo registrado: " + luminaria);
    }

    private void registrarFalla() throws IOException {
        Equipo equipo = pedirEquipo();
        String autor = pedir("Quien informa (vecino o guardia)");
        FallaEquipo falla = mantenimiento().registrarFalla(equipo, autor, pedir("Descripcion de la falla"), LocalDateTime.now());
        vista.mostrar("Falla registrada del " + falla.getFecha() + ": " + falla.getDescripcion() + "\n" + equipo);
    }

    private void generarOrden() throws ReglaNegocioException, IOException {
        Administrador administrador = pedirAdministrador();
        Equipo equipo = pedirEquipo();
        FallaEquipo falla = equipo.fallaAbierta();
        if (falla == null) throw new IllegalArgumentException("El equipo no tiene una falla abierta");
        EmpleadoMantenimiento tecnico = null;
        if (pedirSiNo("Hay un tecnico disponible para asignar ahora?")) {
            tecnico = pedirTecnico();
        }
        OrdenMantenimiento orden = mantenimiento().generarOrden(administrador, falla, tecnico, LocalDateTime.now());
        vista.mostrar("Orden generada:\n" + orden);
    }

    private void asignarTecnico() throws ReglaNegocioException, IOException {
        OrdenMantenimiento orden = pedirOrden();
        mantenimiento().asignarTecnico(orden, pedirTecnico());
        vista.mostrar("Tecnico asignado:\n" + orden);
    }

    private void iniciarReparacion() throws ReglaNegocioException, IOException {
        OrdenMantenimiento orden = pedirOrden();
        mantenimiento().iniciarReparacion(orden);
        vista.mostrar("Reparacion iniciada:\n" + orden.getEquipo());
    }

    private void cerrarOrden() throws ReglaNegocioException, IOException {
        OrdenMantenimiento orden = pedirOrden();
        EmpleadoMantenimiento tecnico = pedirTecnico();
        String descripcion = pedir("Descripcion de la reparacion");
        if (pedirSiNo("Se pudo reparar el equipo?")) {
            mantenimiento().cerrarOrden(orden, tecnico, descripcion, LocalDateTime.now());
        } else {
            mantenimiento().cerrarOrdenSinReparacion(orden, tecnico, descripcion, LocalDateTime.now());
        }
        vista.mostrar("Orden cerrada:\n" + orden + "\n" + orden.getEquipo());
    }

    // CU-20: listado con filtro opcional por sector, ordenes abiertas y camaras caidas hace mas de 48 horas
    private void verEquipos() {
        String filtro = vista.pedirTexto("Sector (vacio = todos)");
        LocalDateTime ahora = LocalDateTime.now();
        String texto = "EQUIPOS\n";
        int cantidad = 0;
        for (Equipo equipo : mantenimiento().consultarEquipos(ahora)) {
            boolean coincide = filtro == null || filtro.isBlank() || equipo.getNombreSector().equalsIgnoreCase(filtro.trim());
            if (coincide) {
                texto = texto + equipo;
                if (equipo instanceof Camara && ((Camara) equipo).requiereAviso(ahora)) {
                    texto = texto + " | SIN FUNCIONAR HACE MAS DE 48 HORAS";
                }
                texto = texto + "\n";
                for (OrdenMantenimiento orden : mantenimiento().consultarOrdenesAbiertas(equipo)) {
                    texto = texto + "     " + orden + "\n";
                }
                cantidad++;
            }
        }
        if (cantidad == 0) {
            texto = texto + "No hay equipos para ese filtro\n";
        }
        vista.mostrar(texto);
    }

    private void verOrdenes() {
        String texto = "ORDENES DE MANTENIMIENTO\n";
        for (OrdenMantenimiento orden : mantenimiento().consultarOrdenes()) {
            texto = texto + orden + "\n";
        }
        vista.mostrar(texto);
    }

    // ---------------------------------------------------------------- Accesos

    private void autorizarVisita() throws ReglaNegocioException, IOException {
        Lote lote = pedirLote();
        Persona otorgante = pedirResidente(lote);
        Visitante visitante = new Visitante(pedir("Nombre del visitante"), pedir("DNI del visitante"));
        boolean recurrente = pedirSiNo("Es un proveedor habitual (autorizacion recurrente de hasta 30 dias)?");
        LocalDate desde = pedirFecha("Fecha (AAAA-MM-DD, vacio = hoy)");
        LocalDate hasta = desde;
        if (recurrente) {
            hasta = LocalDate.parse(pedir("Hasta (AAAA-MM-DD)"));
        }
        LocalTime horaDesde = LocalTime.parse(pedir("Hora desde (HH:MM)"));
        LocalTime horaHasta = LocalTime.parse(pedir("Hora hasta (HH:MM)"));
        Autorizacion autorizacion = aplicacion.getGestorAccesos().autorizarVisita(otorgante, lote, visitante,
                desde, hasta, horaDesde, horaHasta, recurrente);
        vista.mostrar("Visita autorizada: " + autorizacion);
    }

    private void registrarIngreso() throws ReglaNegocioException, IOException {
        Guardia guardia = pedirGuardia();
        String documento = pedir("DNI del visitante");
        Lote lote = pedirLote();
        aplicacion.getGestorAccesos().registrarIngreso(documento, lote, guardia, LocalDateTime.now());
        vista.mostrar("Ingreso registrado para el DNI " + documento);
    }

    private void registrarEgreso() throws ReglaNegocioException, IOException {
        String documento = pedir("DNI del visitante");
        aplicacion.getGestorAccesos().registrarEgreso(documento, LocalDateTime.now());
        vista.mostrar("Egreso registrado para el DNI " + documento);
    }

    // ---------------------------------------------------------------- Reportes

    private void reporteMorosidad() {
        mostrarReporte(reportes().morosidad(LocalDate.now()));
    }

    private void reporteEquipos() {
        mostrarReporte(reportes().equiposFueraDeServicio(LocalDateTime.now()));
    }

    private void verNotificaciones() {
        String texto = "NOTIFICACIONES\n";
        List<Notificacion> notificaciones = aplicacion.getNotificaciones();
        for (Notificacion notificacion : notificaciones) {
            texto = texto + notificacion + "\n";
        }
        if (notificaciones.isEmpty()) texto = texto + "No hay notificaciones\n";
        mostrarReporte(texto);
    }

    private void exportarReporte() throws IOException {
        if (ultimoReporte.isEmpty()) throw new IllegalArgumentException("Primero genera un reporte");
        String ruta = pedir("Nombre del archivo (por ejemplo reporte.txt)");
        reportes().exportar(ultimoReporte, ruta);
        vista.mostrar("Reporte exportado a " + ruta);
    }

    private void mostrarReporte(String texto) {
        ultimoReporte = texto;
        vista.mostrar(texto);
    }

    // ---------------------------------------------------------------- Ayudas para pedir datos

    private String pedir(String pregunta) {
        String texto = vista.pedirTexto(pregunta);
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Falta completar: " + pregunta);
        return texto.trim();
    }

    private int pedirEntero(String pregunta) {
        return Integer.parseInt(pedir(pregunta));
    }

    private boolean pedirSiNo(String pregunta) {
        String[] opciones = {"Si", "No"};
        return vista.elegir(pregunta, opciones) == 0;
    }

    private LocalDate pedirFecha(String pregunta) {
        String texto = vista.pedirTexto(pregunta);
        if (texto == null || texto.isBlank()) return LocalDate.now();
        return LocalDate.parse(texto.trim());
    }

    private Lote pedirLote() {
        int numero = pedirEntero("Numero de lote");
        Lote lote = barrio().obtenerLote(numero);
        if (lote == null) throw new IllegalArgumentException("No existe el lote " + numero);
        return lote;
    }

    private Persona pedirResidente(Lote lote) {
        String documento = pedir("DNI del vecino");
        Persona persona = lote.buscarResidente(documento, LocalDate.now());
        if (persona == null) throw new IllegalArgumentException("El DNI " + documento + " no vive en el lote " + lote.getNumero());
        return persona;
    }

    private Empleado pedirEmpleado() {
        String legajo = pedir("Legajo del empleado");
        Empleado empleado = barrio().buscarEmpleado(legajo);
        if (empleado == null) throw new IllegalArgumentException("No existe el legajo " + legajo);
        return empleado;
    }

    private Administrador pedirAdministrador() {
        Empleado empleado = pedirEmpleado();
        if (!(empleado instanceof Administrador)) throw new IllegalArgumentException(empleado.getNombre() + " no es administrador");
        return (Administrador) empleado;
    }

    private Guardia pedirGuardia() {
        Empleado empleado = pedirEmpleado();
        if (!(empleado instanceof Guardia)) throw new IllegalArgumentException(empleado.getNombre() + " no es guardia");
        return (Guardia) empleado;
    }

    private EmpleadoMantenimiento pedirTecnico() {
        Empleado empleado = pedirEmpleado();
        if (!(empleado instanceof EmpleadoMantenimiento)) {
            throw new IllegalArgumentException(empleado.getNombre() + " no es empleado de mantenimiento");
        }
        return (EmpleadoMantenimiento) empleado;
    }

    private Reclamo pedirReclamo() {
        int numero = pedirEntero("Numero de reclamo");
        Reclamo reclamo = reclamos().buscarReclamo(numero);
        if (reclamo == null) throw new IllegalArgumentException("No existe el reclamo " + numero);
        return reclamo;
    }

    private Equipo pedirEquipo() {
        String codigo = pedir("Codigo del equipo");
        Equipo equipo = mantenimiento().buscarEquipo(codigo);
        if (equipo == null) throw new IllegalArgumentException("No existe el equipo " + codigo);
        return equipo;
    }

    private String pedirCodigoNuevo() {
        String codigo = pedir("Codigo del equipo");
        if (mantenimiento().buscarEquipo(codigo) != null) throw new IllegalArgumentException("Ya existe el equipo " + codigo);
        return codigo;
    }

    private OrdenMantenimiento pedirOrden() {
        int numero = pedirEntero("Numero de orden");
        OrdenMantenimiento orden = mantenimiento().buscarOrden(numero);
        if (orden == null) throw new IllegalArgumentException("No existe la orden " + numero);
        return orden;
    }

    private static String[] nombres(Object[] valores) {
        String[] nombres = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            nombres[i] = valores[i].toString();
        }
        return nombres;
    }

    private Barrio barrio() { return aplicacion.getBarrio(); }
    private GestorReclamos reclamos() { return aplicacion.getGestorReclamos(); }
    private GestorExpensas expensas() { return aplicacion.getGestorExpensas(); }
    private GestorMantenimiento mantenimiento() { return aplicacion.getGestorMantenimiento(); }
    private GestorReportes reportes() { return aplicacion.getGestorReportes(); }
}
