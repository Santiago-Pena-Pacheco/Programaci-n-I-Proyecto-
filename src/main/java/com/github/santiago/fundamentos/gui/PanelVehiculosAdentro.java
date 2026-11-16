package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.logica.SistemaControlAcceso;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.TipoPersona;
import com.github.santiago.fundamentos.modelo.VehiculoAdentro;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista en vivo de los vehiculos que estan DENTRO del escenario: quien es, su
 * placa, a donde va, en que parqueadero esta, a que hora entro y cuanto lleva
 * adentro. Se puede buscar por placa o nombre y se actualiza sola cada pocos
 * segundos mientras esta visible, asi el vigilante y el administrador la pueden
 * dejar abierta mientras la camara registra ingresos y salidas.
 *
 * @author salas
 */
public class PanelVehiculosAdentro extends JPanel {

    private static final int SEGUNDOS_ACTUALIZACION = 10;

    private final DefaultTableModel modelo = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel resumen = new JLabel(" ");
    private final JTextField campoBuscar = new JTextField(14);
    private final JCheckBox casillaAuto = new JCheckBox("Actualizar solo cada " + SEGUNDOS_ACTUALIZACION + " s", true);
    private final Timer temporizador;

    private SistemaControlAcceso sistema;
    private List<VehiculoAdentro> todos = new ArrayList<>();

    public PanelVehiculosAdentro(SistemaControlAcceso sistema) {
        super(new BorderLayout(0, 8));
        this.sistema = sistema;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        resumen.setFont(resumen.getFont().deriveFont(Font.BOLD, 14f));

        JButton botonActualizar = new JButton("Actualizar ahora");
        botonActualizar.addActionListener(e -> actualizar());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        barra.add(new JLabel("Buscar (placa o nombre):"));
        barra.add(campoBuscar);
        barra.add(botonActualizar);
        barra.add(casillaAuto);

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(resumen, BorderLayout.NORTH);
        arriba.add(barra, BorderLayout.SOUTH);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        campoBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                mostrar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                mostrar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                mostrar();
            }
        });

        temporizador = new Timer(SEGUNDOS_ACTUALIZACION * 1000, e -> {
            if (casillaAuto.isSelected()) {
                actualizar();
            }
        });
        casillaAuto.addActionListener(e -> {
            if (casillaAuto.isSelected()) {
                actualizar();
            }
        });

        actualizar();
    }

    /** El administrador cambia de escenario: la lista pasa a ser la del nuevo (o queda vacia si es null). */
    public void setSistema(SistemaControlAcceso nuevo) {
        this.sistema = nuevo;
        campoBuscar.setText("");
        actualizar();
    }

    // El temporizador solo corre mientras el panel esta en una ventana visible
    @Override
    public void addNotify() {
        super.addNotify();
        temporizador.start();
        actualizar();
    }

    @Override
    public void removeNotify() {
        temporizador.stop();
        super.removeNotify();
    }

    /** Vuelve a leer de la base de datos quien esta adentro. */
    public void actualizar() {
        if (sistema == null) {
            todos = new ArrayList<>();
            mostrar();
            return;
        }
        try {
            todos = sistema.getVehiculosAdentro();
        } catch (SQLException e) {
            todos = new ArrayList<>();
            resumen.setText("No se pudo leer: " + e.getMessage());
            modelo.setRowCount(0);
            return;
        }
        mostrar();
    }

    // Aplica el filtro de busqueda y llena la tabla y el resumen
    private void mostrar() {
        modelo.setRowCount(0);
        if (sistema == null) {
            resumen.setText(" ");
            return;
        }
        Escenario escenario = sistema.getEscenario();
        modelo.setColumnIdentifiers(new Object[]{"Tipo", "Nombre", "Placa",
            escenario.getEtiquetaUbicacion() + " / destino", "Parqueadero", "Ingresó", "Tiempo adentro"});
        int[] anchos = {90, 220, 80, 150, 90, 130, 110};
        for (int c = 0; c < anchos.length; c++) {
            tabla.getColumnModel().getColumn(c).setPreferredWidth(anchos[c]);
        }

        String filtro = campoBuscar.getText().trim().toUpperCase().replace(" ", "");
        int fijos = 0;
        int visitantes = 0;
        for (VehiculoAdentro v : todos) {
            boolean esFijo = v.getTipoPersona() == TipoPersona.USUARIO_FIJO;
            if (esFijo) {
                fijos++;
            } else {
                visitantes++;
            }
            if (!filtro.isEmpty() && !v.getPlaca().contains(filtro)
                    && !v.getNombre().toUpperCase().replace(" ", "").contains(filtro)) {
                continue;
            }
            modelo.addRow(new Object[]{
                esFijo ? escenario.getEtiquetaUsuarioFijo() : "Visitante",
                v.getNombre(), v.getPlaca(), v.getDestino(), "#" + v.getNumeroParqueadero(),
                v.getDesdeTexto(), v.getTiempoTexto()});
        }

        String libres = "";
        try {
            libres = "  ·  Parqueaderos de visitantes libres: "
                    + sistema.getCantidadParqueaderosVisitantesDisponibles()
                    + " de " + escenario.getCantidadParqueaderosVisitantes();
        } catch (SQLException e) {
            // el resumen sigue siendo util sin este dato
        }
        resumen.setText(todos.size() + (todos.size() == 1 ? " vehículo adentro" : " vehículos adentro")
                + "  (" + fijos + " " + escenario.getEtiquetaUsuarioFijoPlural().toLowerCase()
                + ", " + visitantes + (visitantes == 1 ? " visitante)" : " visitantes)") + libres);
    }
}
