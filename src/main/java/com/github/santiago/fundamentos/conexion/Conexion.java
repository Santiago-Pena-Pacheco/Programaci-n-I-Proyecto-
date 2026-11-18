package com.github.santiago.fundamentos.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** SINGLETON (patron creacional) */
public final class Conexion {

    private static final String URL
            = "jdbc:mysql://localhost:3306/control_acceso"
            + "?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Conexion instancia; // la unica instancia

    private Conexion() {
        // constructor privado: nadie mas puede crear conexiones "paralelas"
    }

    /** Devuelve la unica instancia, creandola la primera vez (hilo seguro). */
    public static synchronized Conexion getInstancia() {
        if (instancia == null) {
            instancia = new Conexion();
        }
        return instancia;
    }

    /** Atajo que usan los DAO: abre una conexion con la instancia unica. */
    public static Connection getConexion() throws SQLException {
        return getInstancia().abrir();
    }

    public Connection abrir() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Se captura la excepcion original y se lanza una con un mensaje claro
            throw new SQLException("No se pudo conectar a la base de datos 'control_acceso'.\n"
                    + "Verifique que MySQL este encendido en XAMPP, que haya importado "
                    + "sql/01_inicializacion.sql y que el usuario y la clave sean correctos.\n"
                    + "Detalle: " + e.getMessage(), e);
        }
    }
}
