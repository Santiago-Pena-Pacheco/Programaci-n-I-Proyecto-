package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import com.github.santiago.fundamentos.conexion.Conexion;
import com.github.santiago.fundamentos.modelo.Parqueadero;
import com.github.santiago.fundamentos.modelo.Presencia;
import com.github.santiago.fundamentos.modelo.TipoParqueadero;
import com.github.santiago.fundamentos.modelo.Vehiculo;
import com.github.santiago.fundamentos.modelo.Visitante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VisitanteDAOImpl implements VisitanteDAO {

    private static final String SELECT
            = "SELECT v.id_visitante, v.nombre, v.cedula, v.placa, v.destino, v.presencia, v.ultimo_cambio, "
            + "p.id_parqueadero, p.numero "
            + "FROM visitante v JOIN parqueadero p ON v.id_parqueadero = p.id_parqueadero ";

    @Override
    public List<Visitante> listar(int idEscenario) throws SQLException {
        List<Visitante> visitantes = new ArrayList<>();
        String sql = SELECT + "WHERE v.id_escenario = ? ORDER BY v.ultimo_cambio DESC, v.id_visitante DESC";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    visitantes.add(armarVisitante(rs));
                }
            }
        }
        return visitantes;
    }

    @Override
    public Visitante buscarPorPlaca(int idEscenario, String placa) throws SQLException {
        String sql = SELECT + "WHERE v.id_escenario = ? AND v.placa = ?";
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            ps.setString(2, placa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return armarVisitante(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean actualizar(int idEscenario, Visitante v) throws SQLException {
        String sql = "UPDATE visitante SET nombre = ?, cedula = ?, placa = ?, destino = ? "
                + "WHERE id_visitante = ? AND id_escenario = ?";
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getNombre());
            ps.setString(2, v.getCedula());
            ps.setString(3, v.getPlaca());
            ps.setString(4, v.getDestino());
            ps.setInt(5, v.getId());
            ps.setInt(6, idEscenario);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int contarParqueaderosDisponibles(int idEscenario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parqueadero p "
                + "WHERE p.id_escenario = ? AND p.tipo = 'VISITANTE' "
                + "AND NOT EXISTS (SELECT 1 FROM visitante v "
                + "WHERE v.id_parqueadero = p.id_parqueadero AND v.presencia = 'DENTRO')";
        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEscenario);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private Visitante armarVisitante(ResultSet rs) throws SQLException {
        try {
            return armarVisitanteValidado(rs);
        } catch (DatoInvalidoException e) {
            throw new SQLException("Hay un dato inválido guardado en la base de datos: " + e.getMessage(), e);
        }
    }

    private Visitante armarVisitanteValidado(ResultSet rs) throws SQLException, DatoInvalidoException {
        Visitante visitante = new Visitante(rs.getString("nombre"), rs.getString("cedula"),
                new Vehiculo(rs.getString("placa")), rs.getString("destino"));
        visitante.setId(rs.getInt("id_visitante"));
        visitante.setPresencia(Presencia.valueOf(rs.getString("presencia")));
        visitante.setUltimoCambio(rs.getObject("ultimo_cambio", LocalDateTime.class));
        visitante.setParqueaderoAsignado(new Parqueadero(
                rs.getInt("id_parqueadero"), rs.getInt("numero"), TipoParqueadero.VISITANTE));
        return visitante;
    }
}
