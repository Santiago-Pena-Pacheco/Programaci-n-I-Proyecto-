package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.modelo.UsuarioSistema;
import java.sql.SQLException;

public interface UsuarioSistemaDAO {

    // Valida usuario y clave; retorna null si las credenciales no son correctas
    UsuarioSistema autenticar(String usuario, String clave) throws SQLException;
}
