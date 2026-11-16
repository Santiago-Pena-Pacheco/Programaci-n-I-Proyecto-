package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.excepciones.ReglaNegocioException;
import com.github.santiago.fundamentos.conexion.Conexion;
import com.github.santiago.fundamentos.modelo.Escenario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EscenarioDAOImpl implements EscenarioDAO {

    private static final String SELECT
            = "SELECT id_escenario, nombre, etiqueta_usuario_fijo, etiqueta_usuario_fijo_plural, "
            + "etiqueta_ubicacion, etiqueta_zona, cantidad_parqueaderos_visitantes "
            + "FROM escenario ";

    private static final String CLAVE_ACTIVO = "escenario_activo";

    @Override
    public List<Escenario> listar() throws SQLException {
        List<Escenario> escenarios = new ArrayList<>();

        String sql = SELECT + "ORDER BY nombre";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                escenarios.add(armarEscenario(rs));
            }
        }

        return escenarios;
    }

    @Override
    public Escenario buscarPorId(int id) throws SQLException {
        String sql = SELECT + "WHERE id_escenario = ?";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarEscenario(rs);
                }
            }
        }

        return null;
    }

    @Override
    public boolean insertar(Escenario e) throws SQLException {
        String sqlEscenario = "INSERT INTO escenario (nombre, etiqueta_usuario_fijo, "
                + "etiqueta_usuario_fijo_plural, etiqueta_ubicacion, etiqueta_zona, "
                + "cantidad_parqueaderos_visitantes) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlParqueadero = "INSERT INTO parqueadero (id_escenario, numero, tipo) "
                + "VALUES (?, ?, 'VISITANTE')";

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                int idNuevo;
                try (PreparedStatement ps = con.prepareStatement(sqlEscenario,
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, e.getNombre());
                    ps.setString(2, e.getEtiquetaUsuarioFijo());
                    ps.setString(3, e.getEtiquetaUsuarioFijoPlural());
                    ps.setString(4, e.getEtiquetaUbicacion());
                    ps.setString(5, e.getEtiquetaZona());
                    ps.setInt(6, e.getCantidadParqueaderosVisitantes());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        idNuevo = claves.getInt(1);
                    }
                }

                // El pool de parqueaderos de visitantes nace con el escenario
                try (PreparedStatement ps = con.prepareStatement(sqlParqueadero)) {
                    for (int numero = 1; numero <= e.getCantidadParqueaderosVisitantes(); numero++) {
                        ps.setInt(1, idNuevo);
                        ps.setInt(2, numero);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                con.commit();
                e.setIdEscenario(idNuevo);
                return true;
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    @Override
    public Escenario obtenerActivo() throws SQLException {
        String sql = "SELECT valor FROM configuracion WHERE clave = ?";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, CLAVE_ACTIVO);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    try {
                        return buscarPorId(Integer.parseInt(rs.getString("valor")));
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void establecerActivo(int idEscenario) throws SQLException {
        String sqlUpdate = "UPDATE configuracion SET valor = ? WHERE clave = ?";
        String sqlInsert = "INSERT INTO configuracion (clave, valor) VALUES (?, ?)";

        try (Connection con = Conexion.getConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
                ps.setString(1, String.valueOf(idEscenario));
                ps.setString(2, CLAVE_ACTIVO);
                if (ps.executeUpdate() > 0) {
                    return;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setString(1, CLAVE_ACTIVO);
                ps.setString(2, String.valueOf(idEscenario));
                ps.executeUpdate();
            }
        }
    }

    private Escenario armarEscenario(ResultSet rs) throws SQLException {
        return new Escenario(
                rs.getInt("id_escenario"),
                rs.getString("nombre"),
                rs.getString("etiqueta_usuario_fijo"),
                rs.getString("etiqueta_usuario_fijo_plural"),
                rs.getString("etiqueta_ubicacion"),
                rs.getString("etiqueta_zona"),
                rs.getInt("cantidad_parqueaderos_visitantes")
        );
    }

    @Override
    public boolean actualizar(Escenario e) throws SQLException, ControlAccesoException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE escenario SET nombre = ?, etiqueta_usuario_fijo = ?, "
                        + "etiqueta_usuario_fijo_plural = ?, etiqueta_ubicacion = ?, etiqueta_zona = ?, "
                        + "cantidad_parqueaderos_visitantes = ? WHERE id_escenario = ?")) {
                    ps.setString(1, e.getNombre());
                    ps.setString(2, e.getEtiquetaUsuarioFijo());
                    ps.setString(3, e.getEtiquetaUsuarioFijoPlural());
                    ps.setString(4, e.getEtiquetaUbicacion());
                    ps.setString(5, e.getEtiquetaZona());
                    ps.setInt(6, e.getCantidadParqueaderosVisitantes());
                    ps.setInt(7, e.getIdEscenario());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }

                int actual = 0;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(MAX(numero), 0) FROM parqueadero "
                        + "WHERE id_escenario = ? AND tipo = 'VISITANTE' FOR UPDATE")) {
                    ps.setInt(1, e.getIdEscenario());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        actual = rs.getInt(1);
                    }
                }
                int nueva = e.getCantidadParqueaderosVisitantes();

                if (nueva > actual) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (?, ?, 'VISITANTE')")) {
                        for (int n = actual + 1; n <= nueva; n++) {
                            ps.setInt(1, e.getIdEscenario());
                            ps.setInt(2, n);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                } else if (nueva < actual) {
                    // Solo se quitan los sobrantes si nunca han sido usados (el historial los referencia)
                    try (PreparedStatement ps = con.prepareStatement(
                            "DELETE FROM parqueadero WHERE id_escenario = ? AND tipo = 'VISITANTE' AND numero > ? "
                            + "AND NOT EXISTS (SELECT 1 FROM visitante v WHERE v.id_parqueadero = parqueadero.id_parqueadero) "
                            + "AND NOT EXISTS (SELECT 1 FROM movimiento m WHERE m.id_parqueadero = parqueadero.id_parqueadero)")) {
                        ps.setInt(1, e.getIdEscenario());
                        ps.setInt(2, nueva);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT COUNT(*) FROM parqueadero WHERE id_escenario = ? AND tipo = 'VISITANTE' AND numero > ?")) {
                        ps.setInt(1, e.getIdEscenario());
                        ps.setInt(2, nueva);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            if (rs.getInt(1) > 0) {
                                throw new ReglaNegocioException("No se pueden quitar parqueaderos de visitantes "
                                        + "que ya se han usado. Suba la cantidad o déjela como está.");
                            }
                        }
                    }
                }

                con.commit();
                return true;
            } catch (SQLException | ControlAccesoException | RuntimeException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
}
