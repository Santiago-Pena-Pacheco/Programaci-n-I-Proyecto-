package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.conexion.Conexion;
import com.github.santiago.fundamentos.modelo.EstadoIncidente;
import com.github.santiago.fundamentos.modelo.GravedadIncidente;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.TipoIncidente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// La fecha y hora las pone MySQL con NOW() y se leen como LocalDateTime
public class IncidenteDAOImpl implements IncidenteDAO {

    @Override
    public boolean insertar(int idEscenario, Incidente i) throws SQLException {
        String sql = "INSERT INTO incidente (id_escenario, fecha_hora, id_usuario_sistema, reportado_por, "
                + "tipo, gravedad, lugar, placa, persona_involucrada, descripcion, acciones_tomadas, "
                + "estado, observacion_admin, fecha_estado) "
                + "VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ABIERTO', 'Sin revisar', NOW())";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idEscenario);
            ps.setInt(2, i.getIdUsuarioSistema());
            ps.setString(3, i.getReportadoPor());
            ps.setString(4, i.getTipo().name());
            ps.setString(5, i.getGravedad().name());
            ps.setString(6, i.getLugar());
            ps.setString(7, i.getPlaca());
            ps.setString(8, i.getPersonaInvolucrada());
            ps.setString(9, i.getDescripcion());
            ps.setString(10, i.getAccionesTomadas());

            if (ps.executeUpdate() == 0) {
                return false;
            }
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    i.setIdIncidente(claves.getInt(1));
                }
            }
            return true;
        }
    }

    @Override
    public List<Incidente> listar(int idEscenario) throws SQLException {
        List<Incidente> incidentes = new ArrayList<>();

        String sql = "SELECT id_incidente, fecha_hora, id_usuario_sistema, reportado_por, tipo, gravedad, "
                + "lugar, placa, persona_involucrada, descripcion, acciones_tomadas, estado, "
                + "observacion_admin, fecha_estado FROM incidente "
                + "WHERE id_escenario = ? ORDER BY fecha_hora DESC, id_incidente DESC";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEscenario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    incidentes.add(Incidente.cargado(
                            rs.getInt("id_incidente"),
                            rs.getObject("fecha_hora", LocalDateTime.class),
                            rs.getInt("id_usuario_sistema"),
                            rs.getString("reportado_por"),
                            TipoIncidente.valueOf(rs.getString("tipo")),
                            GravedadIncidente.valueOf(rs.getString("gravedad")),
                            rs.getString("lugar"),
                            rs.getString("placa"),
                            rs.getString("persona_involucrada"),
                            rs.getString("descripcion"),
                            rs.getString("acciones_tomadas"),
                            EstadoIncidente.valueOf(rs.getString("estado")),
                            rs.getString("observacion_admin"),
                            rs.getObject("fecha_estado", LocalDateTime.class)));
                }
            }
        }

        return incidentes;
    }

    @Override
    public boolean actualizar(int idEscenario, Incidente i) throws SQLException {
        // fecha_estado solo se renueva si el estado cambio de verdad
        String sql = "UPDATE incidente SET tipo = ?, gravedad = ?, lugar = ?, placa = ?, "
                + "persona_involucrada = ?, descripcion = ?, acciones_tomadas = ?, observacion_admin = ?, "
                + "fecha_estado = IF(estado <> ?, NOW(), fecha_estado), estado = ? "
                + "WHERE id_incidente = ? AND id_escenario = ?";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, i.getTipo().name());
            ps.setString(2, i.getGravedad().name());
            ps.setString(3, i.getLugar());
            ps.setString(4, i.getPlaca());
            ps.setString(5, i.getPersonaInvolucrada());
            ps.setString(6, i.getDescripcion());
            ps.setString(7, i.getAccionesTomadas());
            ps.setString(8, i.getObservacionAdmin());
            ps.setString(9, i.getEstado().name());
            ps.setString(10, i.getEstado().name());
            ps.setInt(11, i.getIdIncidente());
            ps.setInt(12, idEscenario);
            return ps.executeUpdate() > 0;
        }
    }
}
