package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.dao.EscenarioDAO;
import com.github.santiago.fundamentos.dao.EscenarioDAOImpl;
import com.github.santiago.fundamentos.dao.UsuarioSistemaDAO;
import com.github.santiago.fundamentos.dao.UsuarioSistemaDAOImpl;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.Rol;
import com.github.santiago.fundamentos.modelo.UsuarioSistema;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Ventana de inicio de sesion */
public class LoginFrame extends JFrame {

    private final UsuarioSistemaDAO usuarioDAO = new UsuarioSistemaDAOImpl();
    private final EscenarioDAO escenarioDAO = new EscenarioDAOImpl();

    private final JTextField campoUsuario = new JTextField();
    private final JPasswordField campoClave = new JPasswordField();

    public LoginFrame() {
        super("Control de Acceso - Iniciar sesion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(380, 250);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("CONTROL DE ACCESO", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));

        JPanel campos = new JPanel(new GridLayout(0, 2, 8, 10));
        campos.setBorder(BorderFactory.createEmptyBorder(15, 25, 5, 25));
        campos.add(new JLabel("Usuario:"));
        campos.add(campoUsuario);
        campos.add(new JLabel("Clave:"));
        campos.add(campoClave);

        JButton botonIngresar = new JButton("Ingresar");
        botonIngresar.addActionListener(e -> ingresar());
        JPanel panelBoton = new JPanel();
        panelBoton.add(botonIngresar);

        add(titulo, BorderLayout.NORTH);
        add(campos, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(botonIngresar); // Enter tambien ingresa
    }

    private void ingresar() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese usuario y clave.");
            return;
        }

        try {
            UsuarioSistema sesion = usuarioDAO.autenticar(usuario, clave);
            if (sesion == null) {
                JOptionPane.showMessageDialog(this, "Usuario o clave incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                campoClave.setText("");
                return;
            }

            if (sesion.getRol() == Rol.ADMINISTRADOR) {
                dispose();
                new VentanaAdministrador(sesion).setVisible(true);
            } else {
                Escenario activo = escenarioDAO.obtenerActivo();
                if (activo == null) {
                    JOptionPane.showMessageDialog(this,
                            "Todavia no hay un escenario activo.\n"
                            + "Pidale al administrador que elija uno.",
                            "Sin escenario", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                dispose();
                new InterfazVigilante(sesion, activo).setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}
