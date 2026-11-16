package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.conexion.Conexion;
import com.github.santiago.fundamentos.modelo.Rol;
import com.github.santiago.fundamentos.modelo.UsuarioSistema;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioSistemaDAOImpl implements UsuarioSistemaDAO {

    @Override
    public UsuarioSistema autenticar(String usuario, String clave) throws SQLException {
        String sql = "SELECT id_usuario_sistema, usuario, nombre, rol FROM usuario_sistema "
                + "WHERE usuario = ? AND clave_hash = ? AND activo = 1";

        try (Connection con = Conexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, sha256(clave));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UsuarioSistema(
                            rs.getInt("id_usuario_sistema"),
                            rs.getString("usuario"),
                            rs.getString("nombre"),
                            Rol.valueOf(rs.getString("rol"))
                    );
                }
            }
        }

        return null;
    }

    // La clave nunca se guarda ni se compara en texto plano: solo su hash SHA-256
    private String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
