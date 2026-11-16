package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.excepciones.ReglaNegocioException;
import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import com.github.santiago.fundamentos.logica.SistemaControlAcceso;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.Origen;
import com.github.santiago.fundamentos.modelo.Visitante;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Dialogo modal para registrar un visitante. Puede abrirse de dos formas:
 *  - Manualmente desde el menu lateral (placa vacia, por si la camara no leyo).
 *  - Automaticamente cuando la camara detecta una placa que NO pertenece
 *    a ningun usuario fijo ni a un visitante que siga adentro: en ese caso
 *    llega con el campo de placa ya prellenado.
 * La etiqueta del destino (Apartamento, Local, Oficina...) sale del Escenario.
 *
 * @author salas
 */
public class FormularioVisitante extends JDialog {

    private final JTextField campoPlaca = new JTextField();
    private final JTextField campoNombre = new JTextField();
    private final JTextField campoCedula = new JTextField();
    private final JTextField campoDestino = new JTextField();

    private final boolean desdeCamara;

    public FormularioVisitante(Frame ventanaPadre, SistemaControlAcceso sistema, String placaPrellenada) {
        super(ventanaPadre, "Registrar visitante", true);
        Escenario escenario = sistema.getEscenario();
        this.desdeCamara = placaPrellenada != null && !placaPrellenada.isBlank();
        setLayout(new BorderLayout(10, 10));
        setSize(430, 320);
        setLocationRelativeTo(ventanaPadre);

        if (placaPrellenada != null && !placaPrellenada.isBlank()) {
            campoPlaca.setText(placaPrellenada);
        }

        JPanel panelCampos = new JPanel(new GridLayout(0, 2, 8, 8));
        panelCampos.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelCampos.add(new JLabel("Placa del vehículo:"));
        panelCampos.add(campoPlaca);
        panelCampos.add(new JLabel("Nombre del visitante:"));
        panelCampos.add(campoNombre);
        panelCampos.add(new JLabel("Cédula:"));
        panelCampos.add(campoCedula);
        panelCampos.add(new JLabel(escenario.getEtiquetaUbicacion() + " al que se dirige:"));
        panelCampos.add(campoDestino);

        JButton botonGuardar = new JButton("Guardar");
        botonGuardar.addActionListener(e -> guardar(sistema));

        JPanel panelBoton = new JPanel();
        panelBoton.add(botonGuardar);

        add(panelCampos, BorderLayout.CENTER);
        add(panelBoton, BorderLayout.SOUTH);
    }

    private void guardar(SistemaControlAcceso sistema) {
        Escenario escenario = sistema.getEscenario();
        try {
            Visitante visitante = sistema.registrarIngresoVisitante(campoPlaca.getText(),
                    campoNombre.getText(), campoCedula.getText(), campoDestino.getText(),
                    desdeCamara ? Origen.CAMARA : Origen.MANUAL);
            JOptionPane.showMessageDialog(this,
                    "Visitante registrado correctamente.\n"
                            + "Se dirige a: " + escenario.getEtiquetaUbicacion() + " "
                            + visitante.getDestino() + "\n"
                            + "Parqueadero de visitante asignado: #"
                            + visitante.getParqueaderoAsignado().getNumero()
                            + "\n\nSu salida la detectará la cámara.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (DatoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        } catch (ReglaNegocioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se puede ingresar", JOptionPane.WARNING_MESSAGE);
        } catch (ControlAccesoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
