package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.dao.EscenarioDAO;
import com.github.santiago.fundamentos.dao.EscenarioDAOImpl;
import com.github.santiago.fundamentos.logica.SistemaControlAcceso;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.EstadoIncidente;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.Movimiento;
import com.github.santiago.fundamentos.modelo.Presencia;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;
import com.github.santiago.fundamentos.modelo.UsuarioSistema;
import com.github.santiago.fundamentos.modelo.Visitante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Ventana del administrador */
public class VentanaAdministrador extends JFrame {

    private final EscenarioDAO escenarioDAO = new EscenarioDAOImpl();

    private final JComboBox<Escenario> comboEscenario = new JComboBox<>();
    private final JLabel etiquetaActivo = new JLabel(" ");
    private final JTabbedPane pestanas = new JTabbedPane();

    private final DefaultTableModel modeloUsuarios = modeloSoloLectura();
    private final DefaultTableModel modeloVisitantes = modeloSoloLectura();
    private final DefaultTableModel modeloIncidentes = modeloSoloLectura();
    private final DefaultTableModel modeloMovimientos = modeloSoloLectura();
    private final JTable tablaUsuarios = new JTable(modeloUsuarios);
    private final JTable tablaVisitantes = new JTable(modeloVisitantes);
    private final JTable tablaIncidentes = new JTable(modeloIncidentes);
    private final JTable tablaMovimientos = new JTable(modeloMovimientos);
    private final JCheckBox casillaInactivos = new JCheckBox("Mostrar inactivos");
    private final JButton botonRegistrar = new JButton();
    private final JComboBox<Object> filtroEstadoIncidente = new JComboBox<>(new Object[]{
        "Todos", EstadoIncidente.ABIERTO, EstadoIncidente.EN_REVISION, EstadoIncidente.CERRADO});

    private final PanelVehiculosAdentro panelAdentro = new PanelVehiculosAdentro(null);

    private SistemaControlAcceso sistema;
    private List<UsuarioFijo> usuariosMostrados = new ArrayList<>();
    private List<Visitante> visitantesMostrados = new ArrayList<>();
    private List<Incidente> incidentesMostrados = new ArrayList<>();
    private boolean cargando = false; // evita reaccionar al llenar el combo

    public VentanaAdministrador(UsuarioSistema sesion) {
        super("Control de Acceso - Panel del Administrador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        pestanas.addTab("Usuarios fijos", panelConBotones(tablaUsuarios, botonesUsuarios()));
        pestanas.addTab("Vehículos adentro", panelAdentro);
        pestanas.addTab("Visitantes", panelConBotones(tablaVisitantes, botonesVisitantes()));
        pestanas.addTab("Incidentes", panelConBotones(tablaIncidentes, botonesIncidentes()));
        pestanas.addTab("Ingresos y salidas", panelConBotones(tablaMovimientos, botonesMovimientos()));

        add(construirEncabezado(sesion), BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);

        comboEscenario.addActionListener(e -> {
            if (!cargando) {
                cambiarEscenario();
            }
        });

        cargarEscenarios(null);
    }

    // ------------------------------------------------------------------
    //  Construccion de la ventana

    private JPanel construirEncabezado(UsuarioSistema sesion) {
        JButton botonActivar = new JButton("Activar para el vigilante");
        botonActivar.addActionListener(e -> activarEscenario());
        JButton botonNuevo = new JButton("Nuevo escenario");
        botonNuevo.addActionListener(e -> crearEscenario());
        JButton botonEditar = new JButton("Modificar escenario");
        botonEditar.addActionListener(e -> modificarEscenario());
        JButton botonSalir = new JButton("Cerrar sesión");
        botonSalir.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fila.add(new JLabel("Escenario:"));
        fila.add(comboEscenario);
        fila.add(botonActivar);
        fila.add(botonNuevo);
        fila.add(botonEditar);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        derecha.add(new JLabel("Administrador: " + sesion.getUsuario()));
        derecha.add(botonSalir);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(fila, BorderLayout.WEST);
        superior.add(derecha, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        panel.add(superior, BorderLayout.NORTH);
        panel.add(etiquetaActivo, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel panelConBotones(JTable tabla, JPanel botones) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel botonesUsuarios() {
        botonRegistrar.addActionListener(e -> registrarUsuarioFijo());
        casillaInactivos.addActionListener(e -> {
            if (sistema != null) {
                actualizarUsuarios();
            }
        });
        JPanel panel = new JPanel();
        panel.add(botonRegistrar);
        panel.add(boton("Modificar", e -> modificarUsuarioFijo()));
        panel.add(boton("Dar de baja", e -> darDeBajaUsuarioFijo()));
        panel.add(boton("Reactivar", e -> reactivarUsuarioFijo()));
        panel.add(boton("Corregir presencia", e -> corregirPresenciaUsuario()));
        panel.add(casillaInactivos);
        return panel;
    }

    private JPanel botonesVisitantes() {
        JPanel panel = new JPanel();
        panel.add(boton("Modificar", e -> modificarVisitante()));
        panel.add(boton("Corregir presencia", e -> corregirPresenciaVisitante()));
        panel.add(boton("Actualizar", e -> actualizarVisitantes()));
        return panel;
    }

    private JPanel botonesIncidentes() {
        filtroEstadoIncidente.addActionListener(e -> {
            if (sistema != null) {
                actualizarIncidentes();
            }
        });
        JPanel panel = new JPanel();
        panel.add(new JLabel("Mostrar:"));
        panel.add(filtroEstadoIncidente);
        panel.add(boton("Ver detalle", e -> verIncidente()));
        panel.add(boton("Modificar / dar seguimiento", e -> modificarIncidente()));
        panel.add(boton("Actualizar", e -> actualizarIncidentes()));
        return panel;
    }

    private JPanel botonesMovimientos() {
        JPanel panel = new JPanel();
        panel.add(boton("Actualizar", e -> actualizarMovimientos()));
        return panel;
    }

    private JButton boton(String texto, java.awt.event.ActionListener accion) {
        JButton b = new JButton(texto);
        b.addActionListener(accion);
        return b;
    }

    private static DefaultTableModel modeloSoloLectura() {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    // ------------------------------------------------------------------
    //  Escenarios

    /** Llena el combo con los escenarios de la BD y deja seleccionado el indicado (o el activo). */
    private void cargarEscenarios(Escenario seleccionar) {
        cargando = true;
        try {
            comboEscenario.removeAllItems();
            for (Escenario e : escenarioDAO.listar()) {
                comboEscenario.addItem(e);
            }
            Escenario destino = seleccionar != null ? seleccionar : escenarioDAO.obtenerActivo();
            if (destino != null) {
                for (int i = 0; i < comboEscenario.getItemCount(); i++) {
                    if (comboEscenario.getItemAt(i).getIdEscenario() == destino.getIdEscenario()) {
                        comboEscenario.setSelectedIndex(i);
                    }
                }
            }
        } catch (SQLException e) {
            mostrarMensaje(e.getMessage());
        } finally {
            cargando = false;
        }
        cambiarEscenario();
    }

    private void cambiarEscenario() {
        Escenario elegido = (Escenario) comboEscenario.getSelectedItem();
        if (elegido == null) {
            sistema = null;
            panelAdentro.setSistema(null);
            pestanas.setEnabled(false);
            return;
        }
        pestanas.setEnabled(true);
        sistema = new SistemaControlAcceso(elegido);
        panelAdentro.setSistema(sistema);
        botonRegistrar.setText("Registrar " + elegido.getEtiquetaUsuarioFijo().toLowerCase());
        pestanas.setTitleAt(0, elegido.getEtiquetaUsuarioFijoPlural());
        actualizarEtiquetaActivo();
        actualizarTodo();
    }

    private void actualizarEtiquetaActivo() {
        try {
            Escenario activo = escenarioDAO.obtenerActivo();
            etiquetaActivo.setText(activo == null
                    ? "  Escenario activo para el vigilante: ninguno"
                    : "  Escenario activo para el vigilante: " + activo.getNombre());
        } catch (SQLException e) {
            etiquetaActivo.setText(" ");
        }
    }

    private void activarEscenario() {
        Escenario elegido = (Escenario) comboEscenario.getSelectedItem();
        if (elegido == null) {
            return;
        }
        try {
            escenarioDAO.establecerActivo(elegido.getIdEscenario());
            actualizarEtiquetaActivo();
            mostrarMensaje("El vigilante trabajará con: " + elegido.getNombre());
        } catch (SQLException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void crearEscenario() {
        DialogoEscenario dialogo = new DialogoEscenario(this, escenarioDAO);
        dialogo.setVisible(true);
        if (dialogo.getEscenarioCreado() != null) {
            cargarEscenarios(dialogo.getEscenarioCreado());
        }
    }

    private void modificarEscenario() {
        Escenario elegido = (Escenario) comboEscenario.getSelectedItem();
        if (elegido == null) {
            return;
        }
        DialogoEscenario dialogo = new DialogoEscenario(this, escenarioDAO, elegido);
        dialogo.setVisible(true);
        if (dialogo.getEscenarioCreado() != null) {
            cargarEscenarios(dialogo.getEscenarioCreado());
        }
    }

    // ------------------------------------------------------------------
    //  Tablas

    private void actualizarTodo() {
        panelAdentro.actualizar();
        actualizarUsuarios();
        actualizarVisitantes();
        actualizarIncidentes();
        actualizarMovimientos();
    }

    private void actualizarUsuarios() {
        Escenario escenario = sistema.getEscenario();
        modeloUsuarios.setRowCount(0);
        modeloUsuarios.setColumnIdentifiers(new Object[]{
            "Nombre", "Cédula", escenario.getEtiquetaUbicacion(), escenario.getEtiquetaZona(),
            "Parqueadero", "Placa", "Estado", "Presencia"});
        try {
            usuariosMostrados = sistema.getUsuariosFijos(casillaInactivos.isSelected());
            for (UsuarioFijo u : usuariosMostrados) {
                modeloUsuarios.addRow(new Object[]{
                    u.getNombre(), u.getCedula(),
                    u.getUbicacion().getCodigo(), u.getUbicacion().getZona(),
                    "#" + u.getUbicacion().getParqueadero().getNumero(),
                    u.getPlaca(),
                    u.isActivo() ? "ACTIVO" : "INACTIVO (" + u.getFechaEstadoTexto() + ")",
                    u.getPresencia()});
            }
        } catch (SQLException e) {
            usuariosMostrados = new ArrayList<>();
            mostrarMensaje(e.getMessage());
        }
    }

    private void actualizarVisitantes() {
        modeloVisitantes.setRowCount(0);
        modeloVisitantes.setColumnIdentifiers(new Object[]{
            "Nombre", "Cédula", "Placa", sistema.getEscenario().getEtiquetaUbicacion() + " destino",
            "Parqueadero", "Presencia", "Último cambio"});
        try {
            visitantesMostrados = sistema.getVisitantes();
            for (Visitante v : visitantesMostrados) {
                modeloVisitantes.addRow(new Object[]{
                    v.getNombre(), v.getCedula(), v.getPlaca(), v.getDestino(),
                    "#" + v.getParqueaderoAsignado().getNumero(), v.getPresencia(), v.getUltimoCambioTexto()});
            }
        } catch (SQLException e) {
            visitantesMostrados = new ArrayList<>();
            mostrarMensaje(e.getMessage());
        }
    }

    private void actualizarIncidentes() {
        modeloIncidentes.setRowCount(0);
        modeloIncidentes.setColumnIdentifiers(new Object[]{
            "Fecha y hora", "Tipo", "Gravedad", "Lugar", "Placa", "Persona", "Reportó", "Estado"});
        incidentesMostrados = new ArrayList<>();
        try {
            Object filtro = filtroEstadoIncidente.getSelectedItem();
            for (Incidente i : sistema.getIncidentes()) {
                if (filtro instanceof EstadoIncidente && i.getEstado() != filtro) {
                    continue;
                }
                incidentesMostrados.add(i);
                modeloIncidentes.addRow(new Object[]{
                    i.getFechaHoraTexto(), i.getTipo(), i.getGravedad(), i.getLugar(), i.getPlaca(),
                    i.getPersonaInvolucrada(), i.getReportadoPor(), i.getEstado()});
            }
        } catch (SQLException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void actualizarMovimientos() {
        modeloMovimientos.setRowCount(0);
        modeloMovimientos.setColumnIdentifiers(new Object[]{
            "Fecha y hora", "Movimiento", "Tipo", "Nombre", "Placa", "Parqueadero", "Origen", "Detalle"});
        try {
            for (Movimiento m : sistema.getMovimientos(500)) {
                modeloMovimientos.addRow(new Object[]{
                    m.getFechaHoraTexto(), m.getTipo(), m.getTipoPersona(), m.getNombre(), m.getPlaca(),
                    "#" + m.getNumeroParqueadero(), m.getOrigen(), m.getDetalle()});
            }
        } catch (SQLException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Usuarios fijos

    private void registrarUsuarioFijo() {
        FormularioUsuarioFijo formulario = new FormularioUsuarioFijo(this, sistema);
        formulario.setVisible(true);
        if (formulario.isRegistrado()) {
            actualizarTodo();
        }
    }

    private UsuarioFijo usuarioSeleccionado() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0 || fila >= usuariosMostrados.size()) {
            mostrarMensaje("Seleccione una fila de la tabla");
            return null;
        }
        return usuariosMostrados.get(fila);
    }

    private void modificarUsuarioFijo() {
        UsuarioFijo u = usuarioSeleccionado();
        if (u == null) {
            return;
        }
        if (!u.isActivo()) {
            mostrarMensaje(u.getNombre() + " está inactivo: reactívelo para poder modificarlo");
            return;
        }
        FormularioUsuarioFijo formulario = new FormularioUsuarioFijo(this, sistema, u);
        formulario.setVisible(true);
        if (formulario.isRegistrado()) {
            actualizarUsuarios();
        }
    }

    // Baja logica: no se borra nada, el usuario queda INACTIVO con todos sus datos
    private void darDeBajaUsuarioFijo() {
        UsuarioFijo u = usuarioSeleccionado();
        if (u == null) {
            return;
        }
        if (!u.isActivo()) {
            mostrarMensaje(u.getNombre() + " ya está inactivo");
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja a " + u.getNombre() + "?\n"
                + "No se borra: queda INACTIVO con todo su historial y no podrá ingresar.\n"
                + "Podrá reactivarlo cuando quiera.",
                "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            mostrarMensaje(sistema.darDeBajaUsuarioFijo(u.getId())
                    ? "Usuario dado de baja (INACTIVO)"
                    : "No se pudo dar de baja: el registro ya no está activo");
            actualizarTodo();
        } catch (SQLException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void reactivarUsuarioFijo() {
        UsuarioFijo u = usuarioSeleccionado();
        if (u == null) {
            return;
        }
        if (u.isActivo()) {
            mostrarMensaje(u.getNombre() + " ya está activo");
            return;
        }
        try {
            mostrarMensaje(sistema.reactivarUsuarioFijo(u.getId())
                    ? "Usuario reactivado (ACTIVO, FUERA)"
                    : "No se pudo reactivar: el registro ya está activo");
            actualizarUsuarios();
        } catch (SQLException e) {
            mostrarMensaje(e.getMessage());
        }
    }

    private void corregirPresenciaUsuario() {
        UsuarioFijo u = usuarioSeleccionado();
        if (u != null) {
            corregirPresencia(u.getNombre(), u.getPresencia(), u);
        }
    }

    // ------------------------------------------------------------------
    //  Visitantes

    private void modificarVisitante() {
        int fila = tablaVisitantes.getSelectedRow();
        if (fila < 0 || fila >= visitantesMostrados.size()) {
            mostrarMensaje("Seleccione un visitante de la tabla");
            return;
        }
        Visitante v = visitantesMostrados.get(fila);
        String[] r = pedirCampos("Modificar visitante",
                new String[]{"Nombre:", "Cédula:", "Placa:", sistema.getEscenario().getEtiquetaUbicacion() + " destino:"},
                new String[]{v.getNombre(), v.getCedula(), v.getPlaca(), v.getDestino()});
        if (r == null) {
            return;
        }
        try {
            sistema.actualizarVisitante(v.getId(), r[0], r[1], r[2], r[3]);
            mostrarMensaje("Visitante modificado");
            actualizarVisitantes();
        } catch (ControlAccesoException ex) {
            mostrarMensaje(ex.getMessage());
        } catch (SQLException ex) {
            mostrarMensaje(ex.getMessage());
        }
    }

    private void corregirPresenciaVisitante() {
        int fila = tablaVisitantes.getSelectedRow();
        if (fila < 0 || fila >= visitantesMostrados.size()) {
            mostrarMensaje("Seleccione un visitante de la tabla");
            return;
        }
        Visitante v = visitantesMostrados.get(fila);
        corregirPresencia(v.getNombre(), v.getPresencia(), v);
    }

    // Cambia DENTRO <-> FUERA a mano cuando la camara no alcanzo a leer la placa
    private void corregirPresencia(String nombre, Presencia actual, com.github.santiago.fundamentos.modelo.Persona p) {
        Presencia nueva = actual == Presencia.DENTRO ? Presencia.FUERA : Presencia.DENTRO;
        int opcion = JOptionPane.showConfirmDialog(this,
                nombre + " figura " + actual + ".\n¿Cambiar a " + nueva + "?\n"
                + "Quedará en el historial como movimiento MANUAL.",
                "Corregir presencia", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sistema.corregirPresencia(p, nueva);
            actualizarTodo();
        } catch (ControlAccesoException | SQLException ex) {
            mostrarMensaje(ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Incidentes

    private Incidente incidenteSeleccionado() {
        int fila = tablaIncidentes.getSelectedRow();
        if (fila < 0 || fila >= incidentesMostrados.size()) {
            mostrarMensaje("Seleccione un incidente de la tabla");
            return null;
        }
        return incidentesMostrados.get(fila);
    }

    private void verIncidente() {
        Incidente i = incidenteSeleccionado();
        if (i == null) {
            return;
        }
        JTextArea area = new JTextArea(i.resumen(), 18, 55);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Incidente #" + i.getIdIncidente(), JOptionPane.PLAIN_MESSAGE);
    }

    private void modificarIncidente() {
        Incidente i = incidenteSeleccionado();
        if (i == null) {
            return;
        }
        FormularioIncidente formulario = new FormularioIncidente(this, sistema, null, i);
        formulario.setVisible(true);
        actualizarIncidentes(); // si se cancelo, se vuelve a leer el original de la base de datos
    }

    // ------------------------------------------------------------------
    //  Auxiliares

    /** Dialogo sencillo con un campo de texto por etiqueta. Devuelve los valores o null si se cancela. */
    private String[] pedirCampos(String titulo, String[] etiquetas, String[] valores) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        JTextField[] campos = new JTextField[etiquetas.length];
        for (int i = 0; i < etiquetas.length; i++) {
            campos[i] = new JTextField(valores[i], 22);
            panel.add(new JLabel(etiquetas[i]));
            panel.add(campos[i]);
        }
        if (JOptionPane.showConfirmDialog(this, panel, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        String[] r = new String[campos.length];
        for (int i = 0; i < campos.length; i++) {
            r[i] = campos[i].getText();
        }
        return r;
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }
}
