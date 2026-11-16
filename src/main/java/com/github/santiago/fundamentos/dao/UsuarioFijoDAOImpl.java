package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import com.github.santiago.fundamentos.excepciones.ReglaNegocioException;
import com.github.santiago.fundamentos.excepciones.AccesoDenegadoException;
import com.github.santiago.fundamentos.conexion.Conexion;
import com.github.santiago.fundamentos.modelo.Origen;
import com.github.santiago.fundamentos.modelo.Parqueadero;
import com.github.santiago.fundamentos.modelo.Presencia;
import com.github.santiago.fundamentos.modelo.TipoMovimiento;
import com.github.santiago.fundamentos.modelo.TipoParqueadero;
import com.github.santiago.fundamentos.modelo.TipoPersona;
import com.github.santiago.fundamentos.modelo.Ubicacion;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;
import com.github.santiago.fundamentos.modelo.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioFijoDAOImpl implements UsuarioFijoDAO {

    // Todas las columnas son NOT NULL: los JOIN son normales (sin LEFT)
    private static final String SELECT
            = "SELECT uf.id_usuario_fijo, uf.nombre, uf.cedula, uf.placa, uf.estado, uf.presencia, "
            + "uf.fecha_estado, ub.id_ubicacion, ub.codigo, ub.zona, p.id_parqueadero, p.numero "
            + "FROM usuario_fijo uf "
            + "JOIN ubicacion ub ON uf.id_ubicacion = ub.id_ubicacion "
            + "JOIN parqueadero p ON ub.id_parqueadero = p.id_parqueadero ";

    @Override
    public boolean insertar(int idEscenario, UsuarioFijo u) throws SQLException, ControlAccesoException {
        Ubicacion ubicacion = u.getUbicacion();
        Parqueadero pedido = ubicacion.getParqueadero();

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idUbicacion;
                int idParqueadero;
                int numeroFinal;

                // Como los usuarios no se borran, la ubicacion puede existir de un usuario dado
                // de baja: se reutiliza junto con su parqueadero fijo
                int[] existente = buscarUbicacion(con, idEscenario, ubicacion.getCodigo(), ubicacion.getZona());
                if (existente != null) {
                    idUbicacion = existente[0];
                    idParqueadero = existente[1];
                    numeroFinal = existente[2];
                    if (numeroFinal != pedido.getNumero()) {
                        throw new DatoInvalidoException("Esa ubicación ya tiene asignado el parqueadero fijo #"
                                + numeroFinal + ". Use ese número.");
                    }
                } else {
                    numeroFinal = pedido.getNumero();
                    idParqueadero = crearParqueaderoFijo(con, idEscenario, numeroFinal);
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, idEscenario);
                        ps.setString(2, ubicacion.getCodigo());
                        ps.setString(3, ubicacion.getZona());
                        ps.setInt(4, idParqueadero);
                        ps.executeUpdate();
                        try (ResultSet claves = ps.getGeneratedKeys()) {
                            claves.next();
                            idUbicacion = claves.getInt(1);
                        }
                    }
                }

                int idUsuario;
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion) "
                        + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idEscenario);
                    ps.setString(2, u.getNombre());
                    ps.setString(3, u.getCedula());
                    ps.setString(4, u.getPlaca());
                    ps.setInt(5, idUbicacion);
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        idUsuario = claves.getInt(1);
                    }
                }

                con.commit();
                ubicacion.setIdUbicacion(idUbicacion);
                ubicacion.setParqueadero(new Parqueadero(idParqueadero, numeroFinal, TipoParqueadero.FIJO));
                u.setId(idUsuario);
                u.setPresencia(Presencia.FUERA);
                return true;
            } catch (SQLException | ControlAccesoException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean actualizar(int idEscenario, UsuarioFijo u) throws SQLException, ControlAccesoException {
        Ubicacion ubicacion = u.getUbicacion();
        Parqueadero pedido = ubicacion.getParqueadero();

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idUbicacion = 0;
                int idParqueadero = 0;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT uf.estado, uf.id_ubicacion, ub.id_parqueadero FROM usuario_fijo uf "
                        + "JOIN ubicacion ub ON uf.id_ubicacion = ub.id_ubicacion "
                        + "WHERE uf.id_usuario_fijo = ? AND uf.id_escenario = ? FOR UPDATE")) {
                    ps.setInt(1, u.getId());
                    ps.setInt(2, idEscenario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new ReglaNegocioException("El usuario ya no existe.");
                        }
                        if (!"ACTIVO".equals(rs.getString("estado"))) {
                            throw new AccesoDenegadoException("El usuario está dado de baja: reactívelo para editarlo.");
                        }
                        idUbicacion = rs.getInt("id_ubicacion");
                        idParqueadero = rs.getInt("id_parqueadero");
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE parqueadero SET numero = ? WHERE id_parqueadero = ?")) {
                    ps.setInt(1, pedido.getNumero());
                    ps.setInt(2, idParqueadero);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE ubicacion SET codigo = ?, zona = ? WHERE id_ubicacion = ?")) {
                    ps.setString(1, ubicacion.getCodigo());
                    ps.setString(2, ubicacion.getZona());
                    ps.setInt(3, idUbicacion);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario_fijo SET nombre = ?, cedula = ?, placa = ? WHERE id_usuario_fijo = ?")) {
                    ps.setString(1, u.getNombre());
                    ps.setString(2, u.getCedula());
                    ps.setString(3, u.getPlaca());
                    ps.setInt(4, u.getId());
                    ps.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException | ControlAccesoException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<UsuarioFijo> listar(int idEscenario, boolean incluirInactivos) throws SQLException {
        List<UsuarioFijo> usuarios = new ArrayList<>();

        String sql = SELECT + "WHERE uf.id_escenario = ? "
                + (incluirInactivos ? "" : "AND uf.estado = 'ACTIVO' ")
                + "ORDER BY uf.estado, uf.nombre";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(armarUsuarioFijo(rs));
                }
            }
        }
        return usuarios;
    }

    @Override
    public UsuarioFijo buscarPorPlaca(int idEscenario, String placa) throws SQLException {
        return buscar(idEscenario, placa, "ACTIVO");
    }

    @Override
    public UsuarioFijo buscarInactivoPorPlaca(int idEscenario, String placa) throws SQLException {
        return buscar(idEscenario, placa, "INACTIVO");
    }

    private UsuarioFijo buscar(int idEscenario, String placa, String estado) throws SQLException {
        String sql = SELECT + "WHERE uf.id_escenario = ? AND uf.placa = ? AND uf.estado = ? "
                + "ORDER BY uf.id_usuario_fijo DESC LIMIT 1";
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            ps.setString(2, placa);
            ps.setString(3, estado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarUsuarioFijo(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean darDeBaja(int idEscenario, int idUsuarioFijo) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                String nombre;
                String placa;
                Presencia presencia;
                int idParqueadero;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT uf.nombre, uf.placa, uf.estado, uf.presencia, ub.id_parqueadero "
                        + "FROM usuario_fijo uf JOIN ubicacion ub ON uf.id_ubicacion = ub.id_ubicacion "
                        + "WHERE uf.id_usuario_fijo = ? AND uf.id_escenario = ? FOR UPDATE")) {
                    ps.setInt(1, idUsuarioFijo);
                    ps.setInt(2, idEscenario);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !"ACTIVO".equals(rs.getString("estado"))) {
                            con.rollback();
                            return false; // no existe o ya estaba inactivo
                        }
                        nombre = rs.getString("nombre");
                        placa = rs.getString("placa");
                        presencia = Presencia.valueOf(rs.getString("presencia"));
                        idParqueadero = rs.getInt("id_parqueadero");
                    }
                }

                // baja_id = id propio: deja libres la cedula, placa y ubicacion para otro usuario activo.
                // Queda FUERA para no dejar un parqueadero ocupado por alguien sin acceso.
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE usuario_fijo SET estado = 'INACTIVO', baja_id = id_usuario_fijo, "
                        + "presencia = 'FUERA', fecha_estado = NOW() WHERE id_usuario_fijo = ?")) {
                    ps.setInt(1, idUsuarioFijo);
                    ps.executeUpdate();
                }
                if (presencia == Presencia.DENTRO) {
                    AccesoDAOImpl.registrarMovimiento(con, idEscenario, TipoMovimiento.SALIDA, placa,
                            TipoPersona.USUARIO_FIJO, idUsuarioFijo, nombre, idParqueadero,
                            Origen.MANUAL, "Salida por baja del usuario");
                }

                con.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean reactivar(int idUsuarioFijo) throws SQLException {
        // Si otro usuario activo ya usa la misma cedula, placa o ubicacion, MySQL
        // rechaza el cambio (error 1062) y no se reactiva
        String sql = "UPDATE usuario_fijo SET estado = 'ACTIVO', baja_id = 0, presencia = 'FUERA', "
                + "fecha_estado = NOW() WHERE id_usuario_fijo = ? AND estado = 'INACTIVO'";
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuarioFijo);
            return ps.executeUpdate() > 0;
        }
    }

    // {id_ubicacion, id_parqueadero, numero} o null si la ubicacion no existe
    private int[] buscarUbicacion(Connection con, int idEscenario, String codigo, String zona)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT ub.id_ubicacion, p.id_parqueadero, p.numero FROM ubicacion ub "
                + "JOIN parqueadero p ON ub.id_parqueadero = p.id_parqueadero "
                + "WHERE ub.id_escenario = ? AND ub.codigo = ? AND ub.zona = ? FOR UPDATE")) {
            ps.setInt(1, idEscenario);
            ps.setString(2, codigo);
            ps.setString(3, zona);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3)};
                }
            }
        }
        return null;
    }

    private int crearParqueaderoFijo(Connection con, int idEscenario, int numero) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (?, ?, 'FIJO')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idEscenario);
            ps.setInt(2, numero);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    // Construye un UsuarioFijo (con Ubicacion y Parqueadero) a partir de una fila del JOIN
    private UsuarioFijo armarUsuarioFijo(ResultSet rs) throws SQLException {
        try {
            return armarUsuarioFijoValidado(rs);
        } catch (DatoInvalidoException e) {
            throw new SQLException("Hay un dato inválido guardado en la base de datos: " + e.getMessage(), e);
        }
    }

    private UsuarioFijo armarUsuarioFijoValidado(ResultSet rs) throws SQLException, DatoInvalidoException {
        Parqueadero parqueadero = new Parqueadero(
                rs.getInt("id_parqueadero"), rs.getInt("numero"), TipoParqueadero.FIJO);
        Ubicacion ubicacion = new Ubicacion(
                rs.getInt("id_ubicacion"), rs.getString("codigo"), rs.getString("zona"), parqueadero);
        UsuarioFijo usuario = new UsuarioFijo(
                rs.getString("nombre"), rs.getString("cedula"), new Vehiculo(rs.getString("placa")), ubicacion);
        usuario.setId(rs.getInt("id_usuario_fijo"));
        usuario.setActivo("ACTIVO".equals(rs.getString("estado")));
        usuario.setPresencia(Presencia.valueOf(rs.getString("presencia")));
        usuario.setFechaEstado(rs.getObject("fecha_estado", java.time.LocalDateTime.class));
        return usuario;
    }
}
