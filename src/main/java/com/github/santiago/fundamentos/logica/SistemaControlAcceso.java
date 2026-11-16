package com.github.santiago.fundamentos.logica;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import com.github.santiago.fundamentos.excepciones.ReglaNegocioException;
import com.github.santiago.fundamentos.dao.AccesoDAO;
import com.github.santiago.fundamentos.dao.AccesoDAOImpl;
import com.github.santiago.fundamentos.dao.EscenarioDAO;
import com.github.santiago.fundamentos.dao.EscenarioDAOImpl;
import com.github.santiago.fundamentos.dao.IncidenteDAO;
import com.github.santiago.fundamentos.dao.IncidenteDAOImpl;
import com.github.santiago.fundamentos.dao.UsuarioFijoDAO;
import com.github.santiago.fundamentos.dao.UsuarioFijoDAOImpl;
import com.github.santiago.fundamentos.dao.VisitanteDAO;
import com.github.santiago.fundamentos.dao.VisitanteDAOImpl;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.EstadoIncidente;
import com.github.santiago.fundamentos.modelo.GravedadIncidente;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.Movimiento;
import com.github.santiago.fundamentos.modelo.Origen;
import com.github.santiago.fundamentos.modelo.Parqueadero;
import com.github.santiago.fundamentos.modelo.Persona;
import com.github.santiago.fundamentos.modelo.Presencia;
import com.github.santiago.fundamentos.modelo.ResultadoAcceso;
import com.github.santiago.fundamentos.modelo.ResultadoAcceso.Tipo;
import com.github.santiago.fundamentos.modelo.TipoMovimiento;
import com.github.santiago.fundamentos.modelo.TipoIncidente;
import com.github.santiago.fundamentos.modelo.TipoParqueadero;
import com.github.santiago.fundamentos.modelo.Ubicacion;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;
import com.github.santiago.fundamentos.modelo.UsuarioSistema;
import com.github.santiago.fundamentos.modelo.Vehiculo;
import com.github.santiago.fundamentos.modelo.VehiculoAdentro;
import com.github.santiago.fundamentos.modelo.Visitante;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de UN escenario. El corazon es procesarPlaca(): cada vez
 * que la camara lee una placa se consulta la PRESENCIA de su duenio y se
 * alterna (FUERA -> DENTRO es un ingreso, DENTRO -> FUERA es una salida).
 * Las salidas las detecta siempre la camara; el vigilante solo interviene
 * para registrar a un visitante que llega.
 *
 * Todos los datos son obligatorios y se validan aqui (y la base de datos lo
 * vuelve a exigir con NOT NULL y CHECK). Los errores de validacion se avisan
 * con DatoInvalidoException; las reglas de estado (sin cupo, ya adentro)
 * con ReglaNegocioException; ambos con mensaje listo para mostrar.
 *
 * @author salas
 */
public class SistemaControlAcceso {

    /** Tiempo durante el cual se ignora una placa recien leida (el carro sigue frente a la camara). */
    public static final int SEGUNDOS_ESPERA = 20;


    private final Escenario escenario;
    private final UsuarioFijoDAO usuarioFijoDAO;
    private final VisitanteDAO visitanteDAO;
    private final IncidenteDAO incidenteDAO;
    private final AccesoDAO accesoDAO;
    private final EscenarioDAO escenarioDAO;

    public SistemaControlAcceso(Escenario escenario) {
        this(escenario, new UsuarioFijoDAOImpl(), new VisitanteDAOImpl(), new IncidenteDAOImpl(),
                new AccesoDAOImpl(), new EscenarioDAOImpl());
    }

    // Constructor con los DAO a la vista: permite probar la logica sin base de datos
    public SistemaControlAcceso(Escenario escenario, UsuarioFijoDAO usuarioFijoDAO, VisitanteDAO visitanteDAO,
            IncidenteDAO incidenteDAO, AccesoDAO accesoDAO, EscenarioDAO escenarioDAO) {
        this.escenario = escenario;
        this.usuarioFijoDAO = usuarioFijoDAO;
        this.visitanteDAO = visitanteDAO;
        this.incidenteDAO = incidenteDAO;
        this.accesoDAO = accesoDAO;
        this.escenarioDAO = escenarioDAO;
    }

    public Escenario getEscenario() {
        return escenario;
    }

    // ------------------------------------------------------------------
    //  CONTROL DE ACCESO POR PLACA (lo llama la camara)
    // ------------------------------------------------------------------

    /**
     * Decide que pasa con una placa leida por la camara:
     *  - usuario fijo activo: alterna su presencia (ingreso o salida automatica)
     *  - usuario fijo dado de baja: DENEGADO
     *  - visitante que esta DENTRO: salida automatica
     *  - visitante conocido que esta FUERA: VISITANTE_REGRESA (solo se confirma su destino)
     *  - placa desconocida: VISITANTE_NUEVO (el vigilante registra sus datos)
     *  - la misma placa dentro del tiempo de espera, o un texto que no es una placa: IGNORADA
     */
    public ResultadoAcceso procesarPlaca(String placaLeida) throws SQLException, ControlAccesoException {
        String placa = Vehiculo.normalizarPlaca(placaLeida);
        int id = escenario.getIdEscenario();

        if (!Vehiculo.esPlacaValida(placa)) {
            return ResultadoAcceso.sinPersona(Tipo.IGNORADA, placa == null ? "" : placa);
        }

        UsuarioFijo usuario = usuarioFijoDAO.buscarPorPlaca(id, placa);
        if (usuario != null) {
            TipoMovimiento movimiento = accesoDAO.alternarUsuarioFijo(id, usuario.getId(), SEGUNDOS_ESPERA);
            if (movimiento == null) {
                return ResultadoAcceso.sinPersona(Tipo.IGNORADA, placa);
            }
            usuario.setPresencia(movimiento == TipoMovimiento.INGRESO ? Presencia.DENTRO : Presencia.FUERA);
            return ResultadoAcceso.deUsuarioFijo(movimiento == TipoMovimiento.INGRESO
                    ? Tipo.INGRESO_USUARIO_FIJO : Tipo.SALIDA_USUARIO_FIJO, placa, usuario);
        }

        UsuarioFijo dadoDeBaja = usuarioFijoDAO.buscarInactivoPorPlaca(id, placa);
        if (dadoDeBaja != null) {
            return ResultadoAcceso.deUsuarioFijo(Tipo.DENEGADO, placa, dadoDeBaja);
        }

        Visitante visitante = visitanteDAO.buscarPorPlaca(id, placa);
        if (visitante == null) {
            return ResultadoAcceso.sinPersona(Tipo.VISITANTE_NUEVO, placa);
        }
        if (visitante.isDentro()) {
            TipoMovimiento movimiento = accesoDAO.salidaVisitante(id, visitante.getId(), SEGUNDOS_ESPERA);
            if (movimiento == null) {
                return ResultadoAcceso.sinPersona(Tipo.IGNORADA, placa);
            }
            visitante.setPresencia(Presencia.FUERA);
            return ResultadoAcceso.deVisitante(Tipo.SALIDA_VISITANTE, placa, visitante);
        }
        // Esta FUERA: si acaba de salir y su carro sigue frente a la camara, no es un regreso
        if (accesoDAO.enTiempoDeEspera(id, placa, SEGUNDOS_ESPERA)) {
            return ResultadoAcceso.sinPersona(Tipo.IGNORADA, placa);
        }
        return ResultadoAcceso.deVisitante(Tipo.VISITANTE_REGRESA, placa, visitante);
    }

    /**
     * Ingreso de un visitante NUEVO (placa desconocida): queda guardado con todos
     * sus datos y se le asigna un parqueadero libre del pool.
     */
    public Visitante registrarIngresoVisitante(String placa, String nombre, String cedula, String destino)
            throws SQLException, ControlAccesoException {
        return registrarIngresoVisitante(placa, nombre, cedula, destino, Origen.CAMARA);
    }

    /** Igual, indicando si la placa la leyo la camara o la escribio el vigilante a mano. */
    public Visitante registrarIngresoVisitante(String placa, String nombre, String cedula, String destino,
            Origen origen) throws SQLException, ControlAccesoException {
        String placaNormalizada = validarPlaca(placa);
        validarNombre(nombre);
        validarCedula(cedula);
        validarTexto(destino, escenario.getEtiquetaUbicacion() + " de destino");

        int id = escenario.getIdEscenario();
        if (usuarioFijoDAO.buscarPorPlaca(id, placaNormalizada) != null) {
            throw new DatoInvalidoException("La placa " + placaNormalizada + " pertenece a un "
                    + escenario.getEtiquetaUsuarioFijo().toLowerCase() + " registrado, no a un visitante.");
        }
        Visitante visitante = new Visitante(nombre.trim(), cedula.trim(),
                new Vehiculo(placaNormalizada), destino.trim());
        return accesoDAO.ingresoVisitante(id, visitante, origen, "Visitante registrado por el vigilante");
    }

    /** Ingreso de un visitante CONOCIDO que estaba FUERA: se conservan sus datos y solo se confirma el destino. */
    public Visitante confirmarRegresoVisitante(Visitante existente, String destino) throws SQLException, ControlAccesoException {
        validarTexto(destino, escenario.getEtiquetaUbicacion() + " de destino");
        existente.setDestino(destino.trim());
        return accesoDAO.ingresoVisitante(escenario.getIdEscenario(), existente, Origen.CAMARA,
                "Regreso de visitante confirmado por el vigilante");
    }

    // ------------------------------------------------------------------
    //  USUARIOS FIJOS (administrador)
    // ------------------------------------------------------------------

    public UsuarioFijo registrarUsuarioFijo(String nombre, String cedula, String codigoUbicacion,
            String zona, int numeroParqueadero, String placa) throws SQLException, ControlAccesoException {
        UsuarioFijo usuario = armarUsuarioFijo(0, nombre, cedula, codigoUbicacion, zona, numeroParqueadero, placa);
        try {
            usuarioFijoDAO.insertar(escenario.getIdEscenario(), usuario);
        } catch (SQLException e) {
            throw traducirDuplicado(e);
        }
        return usuario;
    }

    public UsuarioFijo actualizarUsuarioFijo(int idUsuarioFijo, String nombre, String cedula,
            String codigoUbicacion, String zona, int numeroParqueadero, String placa) throws SQLException, ControlAccesoException {
        UsuarioFijo usuario = armarUsuarioFijo(idUsuarioFijo, nombre, cedula, codigoUbicacion, zona,
                numeroParqueadero, placa);
        try {
            usuarioFijoDAO.actualizar(escenario.getIdEscenario(), usuario);
        } catch (SQLException e) {
            throw traducirDuplicado(e);
        }
        return usuario;
    }

    private UsuarioFijo armarUsuarioFijo(int id, String nombre, String cedula, String codigoUbicacion,
            String zona, int numeroParqueadero, String placa) throws ControlAccesoException {
        String placaNormalizada = validarPlaca(placa);
        validarNombre(nombre);
        validarCedula(cedula);
        validarTexto(codigoUbicacion, escenario.getEtiquetaUbicacion());
        validarTexto(zona, escenario.getEtiquetaZona());
        if (numeroParqueadero <= 0) {
            throw new DatoInvalidoException("El número de parqueadero debe ser mayor que cero.");
        }
        Ubicacion ubicacion = new Ubicacion(codigoUbicacion.trim(), zona.trim(),
                new Parqueadero(numeroParqueadero, TipoParqueadero.FIJO));
        UsuarioFijo usuario = new UsuarioFijo(nombre.trim(), cedula.trim(),
                new Vehiculo(placaNormalizada), ubicacion);
        usuario.setId(id);
        return usuario;
    }

    /** Baja logica: no se borra, pasa a INACTIVO. false si ya estaba inactivo. */
    public boolean darDeBajaUsuarioFijo(int idUsuarioFijo) throws SQLException {
        return usuarioFijoDAO.darDeBaja(escenario.getIdEscenario(), idUsuarioFijo);
    }

    /** Deja ACTIVO (y FUERA) de nuevo a un usuario dado de baja; false si ya estaba activo. */
    public boolean reactivarUsuarioFijo(int idUsuarioFijo) throws SQLException {
        try {
            return usuarioFijoDAO.reactivar(idUsuarioFijo);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("No se puede reactivar: ya hay otro "
                        + escenario.getEtiquetaUsuarioFijo().toLowerCase()
                        + " activo con la misma cédula, placa o "
                        + escenario.getEtiquetaUbicacion().toLowerCase() + ".", e);
            }
            throw e;
        }
    }

    // ------------------------------------------------------------------
    //  VISITANTES, INCIDENTES Y ESCENARIO (administrador)
    // ------------------------------------------------------------------

    public Visitante actualizarVisitante(int idVisitante, String nombre, String cedula, String placa,
            String destino) throws SQLException, ControlAccesoException {
        String placaNormalizada = validarPlaca(placa);
        validarNombre(nombre);
        validarCedula(cedula);
        validarTexto(destino, escenario.getEtiquetaUbicacion() + " de destino");

        Visitante v = new Visitante(nombre.trim(), cedula.trim(), new Vehiculo(placaNormalizada), destino.trim());
        v.setId(idVisitante);
        try {
            visitanteDAO.actualizar(escenario.getIdEscenario(), v);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("Ya existe un visitante con esa placa.", e);
            }
            throw e;
        }
        return v;
    }

    /**
     * Registra un incidente. La fecha y hora y el nombre de quien reporta no se digitan:
     * la hora la pone la base de datos y el nombre sale de la sesion del vigilante.
     */
    public Incidente reportarIncidente(UsuarioSistema vigilante, TipoIncidente tipo, GravedadIncidente gravedad,
            String lugar, String placa, String persona, String descripcion, String acciones)
            throws SQLException, ControlAccesoException {
        Incidente incidente = new IncidenteBuilder(vigilante).tipo(tipo).gravedad(gravedad)
                .lugar(lugar).placa(placa).persona(persona).descripcion(descripcion).acciones(acciones)
                .build();
        incidenteDAO.insertar(escenario.getIdEscenario(), incidente);
        return incidente;
    }

    /** Modificacion del administrador: corrige datos y da seguimiento (estado y observacion). */
    public void actualizarIncidente(Incidente original, TipoIncidente tipo, GravedadIncidente gravedad,
            String lugar, String placa, String persona, String descripcion, String acciones,
            EstadoIncidente estado, String observacion) throws SQLException, ControlAccesoException {
        if (tipo == null || gravedad == null || estado == null) {
            throw new DatoInvalidoException("Faltan el tipo, la gravedad o el estado.");
        }
        original.setTipo(tipo);
        original.setGravedad(gravedad);
        original.setLugar(ReglasIncidente.lugar(lugar));
        original.setPlaca(ReglasIncidente.placa(placa));
        original.setPersonaInvolucrada(ReglasIncidente.persona(persona));
        original.setDescripcion(ReglasIncidente.descripcion(descripcion));
        original.setAccionesTomadas(ReglasIncidente.acciones(acciones));
        original.setEstado(estado);
        original.setObservacionAdmin(ReglasIncidente.observacion(observacion, estado));
        incidenteDAO.actualizar(escenario.getIdEscenario(), original);
    }

    /**
     * Ayuda del formulario: dada la placa del vehiculo afectado, dice de quien es
     * (usuario fijo o visitante registrado) para sugerir la persona involucrada.
     */
    public Optional<String> buscarDuenoDePlaca(String placa) throws SQLException {
        String p = Vehiculo.normalizarPlaca(placa);
        if (!Vehiculo.esPlacaValida(p)) {
            return Optional.empty();
        }
        int id = escenario.getIdEscenario();
        UsuarioFijo fijo = usuarioFijoDAO.buscarPorPlaca(id, p);
        if (fijo != null) {
            return Optional.of(fijo.getNombre());
        }
        Visitante visitante = visitanteDAO.buscarPorPlaca(id, p);
        return visitante == null ? Optional.empty() : Optional.of(visitante.getNombre());
    }

    public void actualizarEscenario(String nombre, String etiquetaFijo, String etiquetaFijoPlural,
            String etiquetaUbicacion, String etiquetaZona, int cantidadParqueaderosVisitantes)
            throws SQLException, ControlAccesoException {
        validarTexto(nombre, "El nombre del escenario");
        validarTexto(etiquetaFijo, "La etiqueta del usuario fijo");
        validarTexto(etiquetaFijoPlural, "La etiqueta plural");
        validarTexto(etiquetaUbicacion, "La etiqueta de ubicación");
        validarTexto(etiquetaZona, "La etiqueta de zona");
        if (cantidadParqueaderosVisitantes <= 0) {
            throw new DatoInvalidoException("Debe haber al menos un parqueadero de visitantes.");
        }
        escenario.setNombre(nombre.trim());
        escenario.setEtiquetaUsuarioFijo(etiquetaFijo.trim());
        escenario.setEtiquetaUsuarioFijoPlural(etiquetaFijoPlural.trim());
        escenario.setEtiquetaUbicacion(etiquetaUbicacion.trim());
        escenario.setEtiquetaZona(etiquetaZona.trim());
        escenario.setCantidadParqueaderosVisitantes(cantidadParqueaderosVisitantes);
        escenarioDAO.actualizar(escenario);
    }

    /**
     * Correccion del administrador cuando la camara no alcanzo a leer una placa
     * y el estado quedo desfasado. Queda en el historial con origen MANUAL.
     */
    public boolean corregirPresencia(Persona persona, Presencia nueva) throws SQLException, ControlAccesoException {
        String detalle = "Correccion manual del administrador";
        if (persona instanceof UsuarioFijo) {
            return accesoDAO.corregirPresenciaUsuarioFijo(escenario.getIdEscenario(), persona.getId(), nueva, detalle);
        }
        return accesoDAO.corregirPresenciaVisitante(escenario.getIdEscenario(), persona.getId(), nueva, detalle);
    }

    // ------------------------------------------------------------------
    //  CONSULTAS
    // ------------------------------------------------------------------

    public List<UsuarioFijo> getUsuariosFijos() throws SQLException {
        return getUsuariosFijos(false);
    }

    public List<UsuarioFijo> getUsuariosFijos(boolean incluirInactivos) throws SQLException {
        return usuarioFijoDAO.listar(escenario.getIdEscenario(), incluirInactivos);
    }

    public List<Visitante> getVisitantes() throws SQLException {
        return visitanteDAO.listar(escenario.getIdEscenario());
    }

    public List<Incidente> getIncidentes() throws SQLException {
        return incidenteDAO.listar(escenario.getIdEscenario());
    }

    public List<Movimiento> getMovimientos(int limite) throws SQLException {
        return accesoDAO.listarMovimientos(escenario.getIdEscenario(), limite);
    }

    /** Los vehiculos que estan dentro ahora mismo (usuarios fijos y visitantes). */
    public List<VehiculoAdentro> getVehiculosAdentro() throws SQLException {
        return accesoDAO.listarVehiculosAdentro(escenario.getIdEscenario());
    }

    public int getCantidadParqueaderosVisitantesDisponibles() throws SQLException {
        return visitanteDAO.contarParqueaderosDisponibles(escenario.getIdEscenario());
    }

    // ------------------------------------------------------------------
    //  VALIDACIONES
    // ------------------------------------------------------------------

    private String validarPlaca(String placa) throws DatoInvalidoException {
        return Vehiculo.validarPlaca(placa);
    }

    private void validarNombre(String nombre) throws DatoInvalidoException {
        Persona.validarNombre(nombre);
    }

    private void validarCedula(String cedula) throws DatoInvalidoException {
        Persona.validarCedula(cedula);
    }

    private void validarTexto(String texto, String campo) throws ControlAccesoException {
        if (texto == null || texto.isBlank()) {
            throw new DatoInvalidoException(campo + " es obligatorio.");
        }
    }

    // Convierte el error 1062 (dato repetido) de MySQL en un mensaje entendible
    private SQLException traducirDuplicado(SQLException e) {
        if (e.getErrorCode() != 1062) {
            return e;
        }
        String detalle = e.getMessage() == null ? "" : e.getMessage();
        String etiquetaFijo = escenario.getEtiquetaUsuarioFijo().toLowerCase();
        String mensaje;
        if (detalle.contains("uq_usuario_fijo_cedula")) {
            mensaje = "Ya existe un " + etiquetaFijo + " con esa cédula.";
        } else if (detalle.contains("uq_usuario_fijo_placa")) {
            mensaje = "Ya existe un " + etiquetaFijo + " con esa placa.";
        } else if (detalle.contains("uq_ubicacion_codigo") || detalle.contains("uq_usuario_fijo_ubicacion")) {
            mensaje = "Ya hay un " + etiquetaFijo + " registrado en esa "
                    + escenario.getEtiquetaUbicacion().toLowerCase() + ".";
        } else if (detalle.contains("uq_parqueadero_numero")) {
            mensaje = "Ese número de parqueadero fijo ya está asignado.";
        } else {
            return e;
        }
        return new SQLException(mensaje, e);
    }
}
