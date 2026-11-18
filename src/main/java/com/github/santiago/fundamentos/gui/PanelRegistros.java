package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.logica.SistemaControlAcceso;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.Movimiento;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;
import com.github.santiago.fundamentos.modelo.Visitante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/** Consulta (solo lectura) de lo que hay en el escenario */
public class PanelRegistros extends JDialog {

    public PanelRegistros(Frame ventanaPadre, SistemaControlAcceso sistema) {
        super(ventanaPadre, "Registros - " + sistema.getEscenario().getNombre(), true);
        setSize(860, 460);
        setLocationRelativeTo(ventanaPadre);
        setLayout(new BorderLayout());

        Escenario escenario = sistema.getEscenario();
        JTabbedPane pestanas = new JTabbedPane();
        try {
            pestanas.addTab(escenario.getEtiquetaUsuarioFijoPlural(), crearTablaUsuariosFijos(sistema));
            pestanas.addTab("Visitantes", crearTablaVisitantes(sistema));
            pestanas.addTab("Ingresos y salidas", crearTablaMovimientos(sistema));
            pestanas.addTab("Incidentes", crearTablaIncidentes(sistema));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        add(pestanas, BorderLayout.CENTER);
    }

    private JScrollPane crearTablaUsuariosFijos(SistemaControlAcceso sistema) throws SQLException {
        Escenario escenario = sistema.getEscenario();
        String[] columnas = {"Nombre", "Cédula", escenario.getEtiquetaUbicacion(),
            escenario.getEtiquetaZona(), "Parqueadero", "Placa", "Estado", "Presencia"};
        DefaultTableModel modelo = noEditable(columnas);
        for (UsuarioFijo u : sistema.getUsuariosFijos(true)) {
            modelo.addRow(new Object[]{
                u.getNombre(), u.getCedula(),
                u.getUbicacion().getCodigo(), u.getUbicacion().getZona(),
                "#" + u.getUbicacion().getParqueadero().getNumero(),
                u.getPlaca(),
                u.isActivo() ? "ACTIVO" : "INACTIVO (" + u.getFechaEstadoTexto() + ")",
                u.getPresencia()
            });
        }
        return new JScrollPane(new JTable(modelo));
    }

    private JScrollPane crearTablaVisitantes(SistemaControlAcceso sistema) throws SQLException {
        String[] columnas = {"Nombre", "Cédula", "Placa",
            sistema.getEscenario().getEtiquetaUbicacion() + " destino",
            "Parqueadero", "Presencia", "Último cambio"};
        DefaultTableModel modelo = noEditable(columnas);
        for (Visitante v : sistema.getVisitantes()) {
            modelo.addRow(new Object[]{
                v.getNombre(), v.getCedula(), v.getPlaca(), v.getDestino(),
                "#" + v.getParqueaderoAsignado().getNumero(),
                v.getPresencia(), v.getUltimoCambioTexto()
            });
        }
        return new JScrollPane(new JTable(modelo));
    }

    private JScrollPane crearTablaMovimientos(SistemaControlAcceso sistema) throws SQLException {
        String[] columnas = {"Fecha y hora", "Movimiento", "Tipo", "Nombre", "Placa", "Parqueadero", "Origen", "Detalle"};
        DefaultTableModel modelo = noEditable(columnas);
        for (Movimiento m : sistema.getMovimientos(500)) {
            modelo.addRow(new Object[]{
                m.getFechaHoraTexto(), m.getTipo(), m.getTipoPersona(), m.getNombre(), m.getPlaca(),
                "#" + m.getNumeroParqueadero(), m.getOrigen(), m.getDetalle()
            });
        }
        return new JScrollPane(new JTable(modelo));
    }

    private JScrollPane crearTablaIncidentes(SistemaControlAcceso sistema) throws SQLException {
        String[] columnas = {"Fecha y hora", "Tipo", "Gravedad", "Lugar", "Placa", "Persona",
            "Reportó", "Estado", "Descripción", "Acciones tomadas"};
        DefaultTableModel modelo = noEditable(columnas);
        for (Incidente i : sistema.getIncidentes()) {
            modelo.addRow(new Object[]{i.getFechaHoraTexto(), i.getTipo(), i.getGravedad(), i.getLugar(),
                i.getPlaca(), i.getPersonaInvolucrada(), i.getReportadoPor(), i.getEstado(),
                i.getDescripcion(), i.getAccionesTomadas()});
        }
        JTable tabla = new JTable(modelo);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int c = 0; c < columnas.length; c++) {
            tabla.getColumnModel().getColumn(c).setPreferredWidth(c >= 8 ? 300 : 110);
        }
        return new JScrollPane(tabla);
    }

    private DefaultTableModel noEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }
}
