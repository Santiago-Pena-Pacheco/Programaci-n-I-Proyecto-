package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.excepciones.ReglaNegocioException;
import com.github.santiago.fundamentos.excepciones.SinCupoException;
import com.github.santiago.fundamentos.excepciones.AccesoDenegadoException;
import com.github.santiago.fundamentos.conexion.Conexion;
import com.github.santiago.fundamentos.modelo.Movimiento;
import com.github.santiago.fundamentos.modelo.Origen;
import com.github.santiago.fundamentos.modelo.Parqueadero;
import com.github.santiago.fundamentos.modelo.Presencia;
import com.github.santiago.fundamentos.modelo.TipoMovimiento;
import com.github.santiago.fundamentos.modelo.TipoParqueadero;
import com.github.santiago.fundamentos.modelo.TipoPersona;
import com.github.santiago.fundamentos.modelo.VehiculoAdentro;
import com.github.santiago.fundamentos.modelo.Visitante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Todas las horas las pone MySQL con NOW() y se leen como LocalDateTime: asi no
// intervienen las zonas horarias de Java ni del conector.
public class AccesoDAOImpl implements AccesoDAO {

    private static final String DETALLE_CAMARA = "Lectura de cámara";

    @Override
    public TipoMovimiento alternarUsuarioFijo(int idEscenario, int idUsuarioFijo, int segundosEspera)
            throws SQLException, ControlAccesoException {
        String sql = "SELECT uf.nombre, uf.placa, uf.estado, uf.presencia, ub.id_parqueadero "
                + "FROM usuario_fijo uf JOIN ubicacion ub ON uf.id_ubicacion = ub.id_ubicacion "
                + "WHERE uf.id_usuario_fijo = ? AND uf.id_escenario = ? FOR UPDATE";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                String nombre = null;
                String placa = null;
                Presencia actual = null;
                int idParqueadero = 0;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idUsuarioFijo);
                    ps.setInt(2, idEscenario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("El usuario ya no existe.");
                        }
                        if (!"ACTIVO".equals(rs.getString("estado"))) {
                            throw new AccesoDenegadoException("El usuario está dado de baja.");
                        }
                        nombre = rs.getString("nombre");
                        placa = rs.getString("placa");
                        actual = Presencia.valueOf(rs.getString("presencia"));
                        idParqueadero = rs.getInt("id_parqueadero");
                    }
                }

                if (enTiempoDeEspera(con, idEscenario, placa, segundosEspera)) {
                    con.rollback();
                    return null;
                }

                TipoMovimiento movimiento = actual == Presencia.FUERA
                        ? TipoMovimiento.INGRESO : TipoMovimiento.SALIDA;
                Presencia nueva = movimiento == TipoMovimiento.INGRESO ? Presencia.DENTRO : Presencia.FUERA;
                fijarPresenciaUsuarioFijo(con, idUsuarioFijo, nueva);
                registrarMovimiento(con, idEscenario, movimiento, placa, TipoPersona.USUARIO_FIJO,
                        idUsuarioFijo, nombre, idParqueadero, Origen.CAMARA, DETALLE_CAMARA);

                con.commit();
                return movimiento;
            } catch (SQLException | ControlAccesoException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public TipoMovimiento salidaVisitante(int idEscenario, int idVisitante, int segundosEspera)
            throws SQLException, ControlAccesoException {
        String sql = "SELECT nombre, placa, presencia, id_parqueadero FROM visitante "
                + "WHERE id_visitante = ? AND id_escenario = ? FOR UPDATE";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                String nombre = null;
                String placa = null;
                int idParqueadero = 0;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idVisitante);
                    ps.setInt(2, idEscenario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("El visitante ya no existe.");
                        }
                        if (!"DENTRO".equals(rs.getString("presencia"))) {
                            con.rollback();
                            return null; // ya estaba fuera
                        }
                        nombre = rs.getString("nombre");
                        placa = rs.getString("placa");
                        idParqueadero = rs.getInt("id_parqueadero");
                    }
                }

                if (enTiempoDeEspera(con, idEscenario, placa, segundosEspera)) {
                    con.rollback();
                    return null;
                }

                fijarPresenciaVisitante(con, idVisitante, Presencia.FUERA);
                registrarMovimiento(con, idEscenario, TipoMovimiento.SALIDA, placa, TipoPersona.VISITANTE,
                        idVisitante, nombre, idParqueadero, Origen.CAMARA, DETALLE_CAMARA);

                con.commit();
                return TipoMovimiento.SALIDA;
            } catch (SQLException | ControlAccesoException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public Visitante ingresoVisitante(int idEscenario, Visitante v, Origen origen, String detalle)
            throws SQLException, ControlAccesoException {
        String sqlExistente = "SELECT id_visitante, presencia FROM visitante "
                + "WHERE id_escenario = ? AND placa = ? FOR UPDATE";
        String sqlInsert = "INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, "
                + "id_parqueadero, presencia, ultimo_cambio) VALUES (?, ?, ?, ?, ?, ?, 'DENTRO', NOW())";
        String sqlUpdate = "UPDATE visitante SET nombre = ?, cedula = ?, destino = ?, id_parqueadero = ?, "
                + "presencia = 'DENTRO', ultimo_cambio = NOW() WHERE id_visitante = ?";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                boolean existe = false;
                int idVisitante = 0;
                try (PreparedStatement ps = con.prepareStatement(sqlExistente)) {
                    ps.setInt(1, idEscenario);
                    ps.setString(2, v.getPlaca());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            existe = true;
                            idVisitante = rs.getInt("id_visitante");
                            if ("DENTRO".equals(rs.getString("presencia"))) {
                                throw new ReglaNegocioException("La placa " + v.getPlaca()
                                        + " ya está adentro. Su salida la registra la cámara.");
                            }
                        }
                    }
                }

                Parqueadero libre = buscarParqueaderoVisitanteLibre(con, idEscenario);
                if (libre == null) {
                    throw new SinCupoException("No hay parqueaderos de visitantes disponibles: no se puede registrar el ingreso.");
                }

                if (existe) {
                    try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                        ps.setString(1, v.getNombre());
                        ps.setString(2, v.getCedula());
                        ps.setString(3, v.getDestino());
                        ps.setInt(4, libre.getIdParqueadero());
                        ps.setInt(5, idVisitante);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idEscenario);
                        ps.setString(2, v.getNombre());
                        ps.setString(3, v.getCedula());
                        ps.setString(4, v.getPlaca());
                        ps.setString(5, v.getDestino());
                        ps.setInt(6, libre.getIdParqueadero());
                        ps.executeUpdate();
                        try (ResultSet claves = ps.getGeneratedKeys()) {
                            claves.next();
                            idVisitante = claves.getInt(1);
                        }
                    }
                }

                registrarMovimiento(con, idEscenario, TipoMovimiento.INGRESO, v.getPlaca(),
                        TipoPersona.VISITANTE, idVisitante, v.getNombre(), libre.getIdParqueadero(),
                        origen, detalle);

                con.commit();
                v.setId(idVisitante);
                v.setParqueaderoAsignado(libre);
                v.setPresencia(Presencia.DENTRO);
                v.setUltimoCambio(LocalDateTime.now());
                return v;
            } catch (SQLException | ControlAccesoException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean corregirPresenciaUsuarioFijo(int idEscenario, int idUsuarioFijo, Presencia nueva,
            String detalle) throws SQLException, ControlAccesoException {
        String sql = "SELECT uf.nombre, uf.placa, uf.estado, uf.presencia, ub.id_parqueadero "
                + "FROM usuario_fijo uf JOIN ubicacion ub ON uf.id_ubicacion = ub.id_ubicacion "
                + "WHERE uf.id_usuario_fijo = ? AND uf.id_escenario = ? FOR UPDATE";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                String nombre = null;
                String placa = null;
                int idParqueadero = 0;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idUsuarioFijo);
                    ps.setInt(2, idEscenario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("El usuario ya no existe.");
                        }
                        if (!"ACTIVO".equals(rs.getString("estado"))) {
                            throw new AccesoDenegadoException("El usuario está dado de baja: reactívelo primero.");
                        }
                        if (Presencia.valueOf(rs.getString("presencia")) == nueva) {
                            con.rollback();
                            return false;
                        }
                        nombre = rs.getString("nombre");
                        placa = rs.getString("placa");
                        idParqueadero = rs.getInt("id_parqueadero");
                    }
                }

                fijarPresenciaUsuarioFijo(con, idUsuarioFijo, nueva);
                registrarMovimiento(con, idEscenario,
                        nueva == Presencia.DENTRO ? TipoMovimiento.INGRESO : TipoMovimiento.SALIDA,
                        placa, TipoPersona.USUARIO_FIJO, idUsuarioFijo, nombre, idParqueadero,
                        Origen.MANUAL, detalle);

                con.commit();
                return true;
            } catch (SQLException | ControlAccesoException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean corregirPresenciaVisitante(int idEscenario, int idVisitante, Presencia nueva, String detalle)
            throws SQLException, ControlAccesoException {
        String sql = "SELECT nombre, placa, presencia, id_parqueadero FROM visitante "
                + "WHERE id_visitante = ? AND id_escenario = ? FOR UPDATE";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                String nombre = null;
                String placa = null;
                int idParqueadero = 0;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, idVisitante);
                    ps.setInt(2, idEscenario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("El visitante ya no existe.");
                        }
                        if (Presencia.valueOf(rs.getString("presencia")) == nueva) {
                            con.rollback();
                            return false;
                        }
                        nombre = rs.getString("nombre");
                        placa = rs.getString("placa");
                        idParqueadero = rs.getInt("id_parqueadero");
                    }
                }

                if (nueva == Presencia.DENTRO) {
                    // volver a ponerlo DENTRO ocupa un parqueadero libre
                    Parqueadero libre = buscarParqueaderoVisitanteLibre(con, idEscenario);
                    if (libre == null) {
                        throw new SinCupoException("No hay parqueaderos de visitantes disponibles.");
                    }
                    idParqueadero = libre.getIdParqueadero();
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE visitante SET presencia = 'DENTRO', id_parqueadero = ?, "
                            + "ultimo_cambio = NOW() WHERE id_visitante = ?")) {
                        ps.setInt(1, idParqueadero);
                        ps.setInt(2, idVisitante);
                        ps.executeUpdate();
                    }
                } else {
                    fijarPresenciaVisitante(con, idVisitante, Presencia.FUERA);
                }

                registrarMovimiento(con, idEscenario,
                        nueva == Presencia.DENTRO ? TipoMovimiento.INGRESO : TipoMovimiento.SALIDA,
                        placa, TipoPersona.VISITANTE, idVisitante, nombre, idParqueadero,
                        Origen.MANUAL, detalle);

                con.commit();
                return true;
            } catch (SQLException | ControlAccesoException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean enTiempoDeEspera(int idEscenario, String placa, int segundos) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            return enTiempoDeEspera(con, idEscenario, placa, segundos);
        }
    }

    @Override
    public List<Movimiento> listarMovimientos(int idEscenario, int limite) throws SQLException {
        List<Movimiento> movimientos = new ArrayList<>();

        String sql = "SELECT m.id_movimiento, m.tipo, m.fecha_hora, m.placa, m.tipo_persona, m.nombre, "
                + "p.numero, m.origen, m.detalle "
                + "FROM movimiento m JOIN parqueadero p ON m.id_parqueadero = p.id_parqueadero "
                + "WHERE m.id_escenario = ? ORDER BY m.fecha_hora DESC, m.id_movimiento DESC LIMIT ?";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEscenario);
            ps.setInt(2, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(new Movimiento(
                            rs.getLong("id_movimiento"),
                            TipoMovimiento.valueOf(rs.getString("tipo")),
                            rs.getObject("fecha_hora", LocalDateTime.class),
                            rs.getString("placa"),
                            TipoPersona.valueOf(rs.getString("tipo_persona")),
                            rs.getString("nombre"),
                            rs.getInt("numero"),
                            Origen.valueOf(rs.getString("origen")),
                            rs.getString("detalle")
                    ));
                }
            }
        }

        return movimientos;
    }

    @Override
    public List<VehiculoAdentro> listarVehiculosAdentro(int idEscenario) throws SQLException {
        // "desde" es el ultimo INGRESO de esa placa; si no hubiera movimiento, la fecha del ultimo cambio.
        // Los minutos los calcula MySQL (misma hora que uso para anotar el ingreso).
        String sql = "SELECT t.tipo_persona, t.nombre, t.placa, t.destino, t.numero, t.desde, "
                + "TIMESTAMPDIFF(MINUTE, t.desde, NOW()) AS minutos FROM ("
                + "SELECT 'USUARIO_FIJO' AS tipo_persona, uf.nombre, uf.placa, "
                + "CONCAT(ub.codigo, ' - ', ub.zona) AS destino, p.numero, "
                + "COALESCE((SELECT MAX(m.fecha_hora) FROM movimiento m WHERE m.id_escenario = uf.id_escenario "
                + "AND m.placa = uf.placa AND m.tipo = 'INGRESO'), uf.fecha_estado) AS desde "
                + "FROM usuario_fijo uf JOIN ubicacion ub ON uf.id_ubicacion = ub.id_ubicacion "
                + "JOIN parqueadero p ON ub.id_parqueadero = p.id_parqueadero "
                + "WHERE uf.id_escenario = ? AND uf.estado = 'ACTIVO' AND uf.presencia = 'DENTRO' "
                + "UNION ALL "
                + "SELECT 'VISITANTE', v.nombre, v.placa, v.destino, p.numero, "
                + "COALESCE((SELECT MAX(m.fecha_hora) FROM movimiento m WHERE m.id_escenario = v.id_escenario "
                + "AND m.placa = v.placa AND m.tipo = 'INGRESO'), v.ultimo_cambio) "
                + "FROM visitante v JOIN parqueadero p ON v.id_parqueadero = p.id_parqueadero "
                + "WHERE v.id_escenario = ? AND v.presencia = 'DENTRO') t "
                + "ORDER BY t.desde, t.placa";

        List<VehiculoAdentro> adentro = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            ps.setInt(2, idEscenario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    adentro.add(new VehiculoAdentro(
                            TipoPersona.valueOf(rs.getString("tipo_persona")),
                            rs.getString("nombre"),
                            rs.getString("placa"),
                            rs.getString("destino"),
                            rs.getInt("numero"),
                            rs.getObject("desde", LocalDateTime.class),
                            rs.getLong("minutos")));
                }
            }
        }
        return adentro;
    }

    // ------------------------------------------------------------------
    //  Auxiliares (reutilizan la conexion/transaccion en curso)

    /** Anota un movimiento en el historial. Tambien lo usa UsuarioFijoDAOImpl al dar de baja. */
    static void registrarMovimiento(Connection con, int idEscenario, TipoMovimiento tipo, String placa,
            TipoPersona tipoPersona, int idPersona, String nombre, int idParqueadero, Origen origen,
            String detalle) throws SQLException {
        String sql = "INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, "
                + "id_persona, nombre, id_parqueadero, origen, detalle) VALUES (?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            ps.setString(2, tipo.name());
            ps.setString(3, placa);
            ps.setString(4, tipoPersona.name());
            ps.setInt(5, idPersona);
            ps.setString(6, nombre);
            ps.setInt(7, idParqueadero);
            ps.setString(8, origen.name());
            ps.setString(9, detalle);
            ps.executeUpdate();
        }
    }

    private static boolean enTiempoDeEspera(Connection con, int idEscenario, String placa, int segundos)
            throws SQLException {
        // La diferencia la calcula MySQL (NULL si esa placa nunca ha tenido movimientos)
        String sql = "SELECT TIMESTAMPDIFF(SECOND, MAX(fecha_hora), NOW()) FROM movimiento "
                + "WHERE id_escenario = ? AND placa = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            ps.setString(2, placa);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long transcurridos = rs.getLong(1);
                return !rs.wasNull() && transcurridos < segundos;
            }
        }
    }

    private static void fijarPresenciaUsuarioFijo(Connection con, int idUsuarioFijo, Presencia nueva)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE usuario_fijo SET presencia = ? WHERE id_usuario_fijo = ?")) {
            ps.setString(1, nueva.name());
            ps.setInt(2, idUsuarioFijo);
            ps.executeUpdate();
        }
    }

    private static void fijarPresenciaVisitante(Connection con, int idVisitante, Presencia nueva)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE visitante SET presencia = ?, ultimo_cambio = NOW() WHERE id_visitante = ?")) {
            ps.setString(1, nueva.name());
            ps.setInt(2, idVisitante);
            ps.executeUpdate();
        }
    }

    // Un parqueadero de visitantes esta libre si ningun visitante DENTRO lo tiene asignado.
    // Primero se bloquean todos los parqueaderos de visitantes del escenario para que dos
    private static Parqueadero buscarParqueaderoVisitanteLibre(Connection con, int idEscenario)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_parqueadero FROM parqueadero "
                + "WHERE id_escenario = ? AND tipo = 'VISITANTE' ORDER BY numero FOR UPDATE")) {
            ps.setInt(1, idEscenario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // solo interesa el bloqueo
                }
            }
        }

        String sql = "SELECT p.id_parqueadero, p.numero FROM parqueadero p "
                + "WHERE p.id_escenario = ? AND p.tipo = 'VISITANTE' "
                + "AND NOT EXISTS (SELECT 1 FROM visitante v "
                + "WHERE v.id_parqueadero = p.id_parqueadero AND v.presencia = 'DENTRO') "
                + "ORDER BY p.numero LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Parqueadero(rs.getInt("id_parqueadero"), rs.getInt("numero"),
                            TipoParqueadero.VISITANTE);
                }
            }
        }
        return null;
    }
}
