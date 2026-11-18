package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.dao.EscenarioDAO;
import com.github.santiago.fundamentos.modelo.Escenario;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Dialogo modal para crear un escenario nuevo */
public class DialogoEscenario extends JDialog {

    private final EscenarioDAO escenarioDAO;

    private final JTextField campoNombre = new JTextField();
    private final JTextField campoUsuarioFijo = new JTextField();
    private final JTextField campoUsuarioFijoPlural = new JTextField();
    private final JTextField campoUbicacion = new JTextField();
    private final JTextField campoZona = new JTextField();
    private final JTextField campoCantidad = new JTextField("10");

    private Escenario escenarioCreado = null;

    private final Escenario existente; // null = crear uno nuevo

    public DialogoEscenario(Frame ventanaPadre, EscenarioDAO escenarioDAO) {
        this(ventanaPadre, escenarioDAO, null);
    }

    public DialogoEscenario(Frame ventanaPadre, EscenarioDAO escenarioDAO, Escenario existente) {
        super(ventanaPadre, existente == null ? "Nuevo escenario" : "Modificar escenario", true);
        this.escenarioDAO = escenarioDAO;
        this.existente = existente;
        setLayout(new BorderLayout(10, 10));
        setSize(500, 380);
        setLocationRelativeTo(ventanaPadre);

        JPanel panelCampos = new JPanel(new GridLayout(0, 2, 8, 8));
        panelCampos.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelCampos.add(new JLabel("Nombre del escenario:"));
        panelCampos.add(campoNombre);
        panelCampos.add(new JLabel("Usuario fijo (singular):"));
        panelCampos.add(campoUsuarioFijo);
        panelCampos.add(new JLabel("Usuario fijo (plural):"));
        panelCampos.add(campoUsuarioFijoPlural);
        panelCampos.add(new JLabel("Destino (ej. Apartamento, Local):"));
        panelCampos.add(campoUbicacion);
        panelCampos.add(new JLabel("Zona (ej. Torre, Piso):"));
        panelCampos.add(campoZona);
        panelCampos.add(new JLabel("Parqueaderos de visitantes:"));
        panelCampos.add(campoCantidad);

        JLabel ayuda = new JLabel("<html>Ejemplo: Concierto / Personal / Personal / Puesto / Zona / 30</html>");
        ayuda.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        if (existente != null) {
            campoNombre.setText(existente.getNombre());
            campoUsuarioFijo.setText(existente.getEtiquetaUsuarioFijo());
            campoUsuarioFijoPlural.setText(existente.getEtiquetaUsuarioFijoPlural());
            campoUbicacion.setText(existente.getEtiquetaUbicacion());
            campoZona.setText(existente.getEtiquetaZona());
            campoCantidad.setText(String.valueOf(existente.getCantidadParqueaderosVisitantes()));
        }

        JButton botonGuardar = new JButton(existente == null ? "Crear escenario" : "Guardar cambios");
        botonGuardar.addActionListener(e -> guardar());
        JPanel panelBoton = new JPanel();
        panelBoton.add(botonGuardar);

        add(ayuda, BorderLayout.NORTH);
        add(panelCampos, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);
    }

    /** El escenario que se creo o modifico, o null si el administrador cerro el dialogo sin guardar. */
    public Escenario getEscenarioCreado() {
        return escenarioCreado;
    }

    private void guardar() {
        String nombre = campoNombre.getText().trim();
        String usuarioFijo = campoUsuarioFijo.getText().trim();
        String plural = campoUsuarioFijoPlural.getText().trim();
        String ubicacion = campoUbicacion.getText().trim();
        String zona = campoZona.getText().trim();

        if (nombre.isEmpty() || usuarioFijo.isEmpty() || plural.isEmpty()
                || ubicacion.isEmpty() || zona.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(campoCantidad.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "La cantidad de parqueaderos debe ser un numero.",
                    "Dato invalido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cantidad < 1 || cantidad > 500) {
            JOptionPane.showMessageDialog(this,
                    "La cantidad de parqueaderos debe estar entre 1 y 500.",
                    "Dato invalido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Escenario nuevo = new Escenario(existente == null ? 0 : existente.getIdEscenario(),
                nombre, usuarioFijo, plural, ubicacion, zona, cantidad);
        try {
            if (existente == null) {
                escenarioDAO.insertar(nuevo);
            } else {
                escenarioDAO.actualizar(nuevo);
            }
            escenarioCreado = nuevo;
            JOptionPane.showMessageDialog(this, existente == null
                    ? "Escenario creado correctamente." : "Escenario modificado correctamente.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ControlAccesoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(this, "Ya existe un escenario con ese nombre.");
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}
