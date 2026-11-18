package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.logica.SistemaControlAcceso;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/** Dialogo modal para registrar o modificar un usuario fijo */
public class FormularioUsuarioFijo extends JDialog {

    private final JTextField campoNombre = new JTextField();
    private final JTextField campoCedula = new JTextField();
    private final JTextField campoUbicacion = new JTextField();
    private final JTextField campoZona = new JTextField();
    private final JTextField campoParqueadero = new JTextField();
    private final JTextField campoPlaca = new JTextField();

    private boolean registrado = false;

    private final UsuarioFijo existente; // null = registrar uno nuevo

    public FormularioUsuarioFijo(Frame ventanaPadre, SistemaControlAcceso sistema) {
        this(ventanaPadre, sistema, null);
    }

    public FormularioUsuarioFijo(Frame ventanaPadre, SistemaControlAcceso sistema, UsuarioFijo existente) {
        super(ventanaPadre, (existente == null ? "Registrar " : "Modificar ")
                + sistema.getEscenario().getEtiquetaUsuarioFijo().toLowerCase(), true);
        this.existente = existente;
        Escenario escenario = sistema.getEscenario();
        setLayout(new BorderLayout(10, 10));
        setSize(450, 380);
        setLocationRelativeTo(ventanaPadre);

        JPanel panelCampos = new JPanel(new GridLayout(0, 2, 8, 8));
        panelCampos.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        agregarCampo(panelCampos, "Nombre:", campoNombre);
        agregarCampo(panelCampos, "Cédula:", campoCedula);
        agregarCampo(panelCampos, escenario.getEtiquetaUbicacion() + ":", campoUbicacion);
        agregarCampo(panelCampos, escenario.getEtiquetaZona() + ":", campoZona);
        agregarCampo(panelCampos, "Parqueadero fijo (número):", campoParqueadero);
        agregarCampo(panelCampos, "Placa del vehículo:", campoPlaca);

        if (existente != null) {
            campoNombre.setText(existente.getNombre());
            campoCedula.setText(existente.getCedula());
            campoUbicacion.setText(existente.getUbicacion().getCodigo());
            campoZona.setText(existente.getUbicacion().getZona());
            campoParqueadero.setText(String.valueOf(existente.getUbicacion().getParqueadero().getNumero()));
            campoPlaca.setText(existente.getPlaca());
        }

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardar(sistema));

        JPanel panelBoton = new JPanel();
        panelBoton.add(botonGuardar);

        add(panelCampos, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);
    }

    public boolean isRegistrado() {
        return registrado;
    }

    private void agregarCampo(JPanel panel, String etiqueta, JTextField campo) {
        panel.add(new JLabel(etiqueta));
        panel.add(campo);
    }

    private void guardar(SistemaControlAcceso sistema) {
        Escenario escenario = sistema.getEscenario();

        int numeroParqueadero;
        try {
            numeroParqueadero = Integer.parseInt(campoParqueadero.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El parqueadero fijo es obligatorio y debe ser un número.",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            UsuarioFijo usuario = existente == null
                    ? sistema.registrarUsuarioFijo(campoNombre.getText(), campoCedula.getText(),
                            campoUbicacion.getText(), campoZona.getText(), numeroParqueadero, campoPlaca.getText())
                    : sistema.actualizarUsuarioFijo(existente.getId(), campoNombre.getText(), campoCedula.getText(),
                            campoUbicacion.getText(), campoZona.getText(), numeroParqueadero, campoPlaca.getText());

            JOptionPane.showMessageDialog(this,
                    escenario.getEtiquetaUsuarioFijo() + (existente == null ? " registrado" : " modificado")
                            + " correctamente.\n" + usuario.getUbicacion().descripcion(escenario)
                            + "\nParqueadero fijo: #" + usuario.getUbicacion().getParqueadero().getNumero(),
                    "Listo", JOptionPane.INFORMATION_MESSAGE);
            registrado = true;
            dispose();

        } catch (ControlAccesoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Datos incorrectos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
