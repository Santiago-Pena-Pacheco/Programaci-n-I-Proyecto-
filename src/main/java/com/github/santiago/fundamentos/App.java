package com.github.santiago.fundamentos;

import com.github.santiago.fundamentos.conexion.Conexion;
import com.github.santiago.fundamentos.gui.LoginFrame;

import nu.pattern.OpenCV;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;

/** Punto de entrada del programa: carga OpenCV, comprueba la conexion con MySQL antes de abrir nada y muestra la ventana de login */
public class App {

    public static void main(String[] args) {
        // Carga la libreria nativa de OpenCV empaquetada por org.openpnp:opencv.
        // No requiere instalar OpenCV manualmente en el sistema operativo.
        OpenCV.loadLocally();

        // Prueba de la conexion antes de abrir la ventana
        try (Connection con = Conexion.getConexion()) {
            System.out.println("Conexion exitosa");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Error de conexion", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
