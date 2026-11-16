package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.logica.ReglasIncidente;
import com.github.santiago.fundamentos.logica.SistemaControlAcceso;
import com.github.santiago.fundamentos.modelo.EstadoIncidente;
import com.github.santiago.fundamentos.modelo.GravedadIncidente;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.TipoIncidente;
import com.github.santiago.fundamentos.modelo.UsuarioSistema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reporte de incidente paso a paso. El orden de captura es siempre el mismo:
 *   1. Que ocurrio        (tipo y gravedad)
 *   2. Donde y cuando     (lugar; la hora y quien reporta se ponen solos)
 *   3. Vehiculo y persona (placa afectada y persona involucrada; "N/A" si no hay)
 *   4. Detalle            (que paso y que acciones se tomaron)
 *   5. Seguimiento        (solo al modificar, lo hace el administrador)
 *   6. Revision           (resumen completo antes de guardar)
 * Cada paso se valida antes de pasar al siguiente. Ningun dato es opcional.
 *
 * Se abre en dos modos: vigilante que reporta uno nuevo, o administrador que
 * modifica uno existente (la fecha y quien lo reporto no se pueden cambiar).
 *
 * @author salas
 */
public class FormularioIncidente extends JDialog {

    private final SistemaControlAcceso sistema;
    private final UsuarioSistema vigilante; // quien reporta (modo nuevo)
    private final Incidente existente;      // null = reporte nuevo

    private final List<String> titulos = new ArrayList<>();
    private final CardLayout cartas = new CardLayout();
    private final JPanel panelPasos = new JPanel(cartas);
    private final JLabel etiquetaPaso = new JLabel();
    private final JButton botonAtras = new JButton("« Atrás");
    private final JButton botonSiguiente = new JButton("Siguiente »");
    private final JButton botonGuardar = new JButton("Guardar reporte");

    private final JComboBox<TipoIncidente> comboTipo = new JComboBox<>(TipoIncidente.values());
    private final JComboBox<GravedadIncidente> comboGravedad = new JComboBox<>(GravedadIncidente.values());
    private final JLabel ayudaGravedad = new JLabel(" ");
    private final JTextField campoLugar = new JTextField();
    private final JCheckBox casillaSinVehiculo = new JCheckBox("No involucra vehículo");
    private final JTextField campoPlaca = new JTextField();
    private final JLabel etiquetaDueno = new JLabel(" ");
    private final JCheckBox casillaSinPersona = new JCheckBox("No hay persona identificada");
    private final JTextField campoPersona = new JTextField();
    private final JTextArea areaDescripcion = areaTexto();
    private final JTextArea areaAcciones = areaTexto();
    private final JComboBox<EstadoIncidente> comboEstado = new JComboBox<>(EstadoIncidente.values());
    private final JTextArea areaObservacion = areaTexto();
    private final JTextArea areaResumen = new JTextArea();

    private int paso = 0;
    private boolean guardado = false;

    public FormularioIncidente(Frame padre, SistemaControlAcceso sistema, UsuarioSistema vigilante) {
        this(padre, sistema, vigilante, null);
    }

    public FormularioIncidente(Frame padre, SistemaControlAcceso sistema, UsuarioSistema vigilante,
            Incidente existente) {
        super(padre, existente == null ? "Reportar incidente" : "Modificar incidente", true);
        this.sistema = sistema;
        this.vigilante = vigilante;
        this.existente = existente;

        setSize(640, 560);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(8, 8));

        agregarPaso("Qué ocurrió", construirPasoQueOcurrio());
        agregarPaso("Dónde y cuándo", construirPasoDondeYCuando());
        agregarPaso("Vehículo y persona", construirPasoVehiculoYPersona());
        agregarPaso("Detalle del suceso", construirPasoDetalle());
        if (existente != null) {
            agregarPaso("Seguimiento", construirPasoSeguimiento());
        }
        agregarPaso("Revisión final", construirPasoResumen());

        etiquetaPaso.setFont(etiquetaPaso.getFont().deriveFont(Font.BOLD, 15f));
        etiquetaPaso.setBorder(BorderFactory.createEmptyBorder(12, 16, 0, 16));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton botonCancelar = new JButton("Cancelar");
        botonCancelar.addActionListener(e -> dispose());
        botonAtras.addActionListener(e -> irA(paso - 1));
        botonSiguiente.addActionListener(e -> siguiente());
        botonGuardar.addActionListener(e -> guardar());
        botones.add(botonCancelar);
        botones.add(botonAtras);
        botones.add(botonSiguiente);
        botones.add(botonGuardar);

        add(etiquetaPaso, BorderLayout.NORTH);
        add(panelPasos, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        configurarComportamiento();
        if (existente != null) {
            cargarDatos(existente);
        }
        irA(0);
    }

    public boolean isGuardado() {
        return guardado;
    }

    // ------------------------------------------------------------------
    //  Pasos
    // ------------------------------------------------------------------

    private void agregarPaso(String titulo, JComponent contenido) {
        titulos.add(titulo);
        // BoxLayout alinea cada componente por su cuenta: todos a la izquierda, para que queden en columna
        for (Component c : contenido.getComponents()) {
            if (c instanceof JComponent) {
                ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        envoltorio.add(contenido, BorderLayout.NORTH);
        panelPasos.add(envoltorio, "p" + (titulos.size() - 1));
    }

    private JComponent construirPasoQueOcurrio() {
        JPanel p = formulario();
        p.add(nota("Elija la clase de suceso y qué tan grave es. Si duda, elija la gravedad más alta."));
        p.add(etiqueta("Tipo de incidente:"));
        p.add(comboTipo);
        p.add(etiqueta("Gravedad:"));
        p.add(comboGravedad);
        p.add(ayudaGravedad);
        return p;
    }

    private JComponent construirPasoDondeYCuando() {
        JPanel p = formulario();
        p.add(nota("Indique dónde ocurrió. La fecha, la hora y su nombre se registran automáticamente."));
        p.add(etiqueta("Lugar exacto (ej. Parqueadero 4, entrada, torre 2):"));
        p.add(campoLugar);
        p.add(etiqueta("Fecha y hora:"));
        p.add(new JLabel(existente == null
                ? "Automática: se registrará al guardar el reporte"
                : existente.getFechaHoraTexto()));
        p.add(etiqueta("Reportado por:"));
        p.add(new JLabel(existente == null ? vigilante.getNombre() : existente.getReportadoPor()));
        return p;
    }

    private JComponent construirPasoVehiculoYPersona() {
        JPanel p = formulario();
        p.add(nota("Escriba la placa del vehículo afectado y la persona involucrada. "
                + "Si no hay ninguno, marque la casilla: se guarda como N/A."));
        p.add(casillaSinVehiculo);
        p.add(etiqueta("Placa del vehículo afectado:"));
        p.add(campoPlaca);
        p.add(etiquetaDueno);
        p.add(Box.createVerticalStrut(8));
        p.add(casillaSinPersona);
        p.add(etiqueta("Persona involucrada (nombre completo):"));
        p.add(campoPersona);
        return p;
    }

    private JComponent construirPasoDetalle() {
        JPanel p = formulario();
        p.add(etiqueta("Qué pasó (describa los hechos, mínimo 10 caracteres):"));
        p.add(new JScrollPane(areaDescripcion));
        p.add(Box.createVerticalStrut(8));
        p.add(etiqueta("Acciones tomadas por el vigilante:"));
        p.add(new JScrollPane(areaAcciones));
        return p;
    }

    private JComponent construirPasoSeguimiento() {
        JPanel p = formulario();
        p.add(nota("Seguimiento del administrador. Al pasar a \"En revisión\" o \"Cerrado\" "
                + "la observación es obligatoria."));
        p.add(etiqueta("Estado del reporte:"));
        p.add(comboEstado);
        p.add(Box.createVerticalStrut(8));
        p.add(etiqueta("Observación (qué se revisó o cómo se resolvió):"));
        p.add(new JScrollPane(areaObservacion));
        return p;
    }

    private JComponent construirPasoResumen() {
        areaResumen.setEditable(false);
        areaResumen.setLineWrap(true);
        areaResumen.setWrapStyleWord(true);
        areaResumen.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(areaResumen);
        scroll.setPreferredSize(new Dimension(560, 380));
        return scroll;
    }

    // ------------------------------------------------------------------
    //  Comportamiento
    // ------------------------------------------------------------------

    private void configurarComportamiento() {
        comboGravedad.addActionListener(e -> mostrarAyudaGravedad());
        mostrarAyudaGravedad();

        casillaSinVehiculo.addActionListener(e -> {
            boolean sin = casillaSinVehiculo.isSelected();
            campoPlaca.setEnabled(!sin);
            if (sin) {
                campoPlaca.setText("");
                etiquetaDueno.setText(" ");
            }
        });
        casillaSinPersona.addActionListener(e -> {
            boolean sin = casillaSinPersona.isSelected();
            campoPersona.setEnabled(!sin);
            if (sin) {
                campoPersona.setText("");
            }
        });
        // Al salir del campo de la placa se busca de quien es, para sugerir la persona
        campoPlaca.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                buscarDueno();
            }
        });
    }

    private void mostrarAyudaGravedad() {
        GravedadIncidente g = (GravedadIncidente) comboGravedad.getSelectedItem();
        String texto = " ";
        if (g != null) {
            switch (g) {
                case BAJA: texto = "Molestia menor, sin daños ni riesgo."; break;
                case MEDIA: texto = "Daño o problema que requiere atención, sin riesgo para las personas."; break;
                case ALTA: texto = "Riesgo para personas o bienes, o pérdida importante."; break;
                default: texto = "Peligro inmediato: avise también a las autoridades."; break;
            }
        }
        ayudaGravedad.setText("<html><i>" + texto + "</i></html>");
    }

    private void buscarDueno() {
        if (casillaSinVehiculo.isSelected() || campoPlaca.getText().isBlank()) {
            etiquetaDueno.setText(" ");
            return;
        }
        try {
            Optional<String> dueno = sistema.buscarDuenoDePlaca(campoPlaca.getText());
            if (dueno.isPresent()) {
                etiquetaDueno.setText("Vehículo registrado a nombre de: " + dueno.get());
                if (campoPersona.isEnabled() && campoPersona.getText().isBlank()) {
                    campoPersona.setText(dueno.get()); // sugerencia; se puede cambiar
                }
            } else {
                etiquetaDueno.setText("Placa no registrada en este escenario (puede continuar).");
            }
        } catch (SQLException e) {
            etiquetaDueno.setText(" ");
        }
    }

    private void cargarDatos(Incidente i) {
        comboTipo.setSelectedItem(i.getTipo());
        comboGravedad.setSelectedItem(i.getGravedad());
        campoLugar.setText(i.getLugar());
        if (Incidente.SIN_DATO.equals(i.getPlaca())) {
            casillaSinVehiculo.setSelected(true);
            campoPlaca.setEnabled(false);
        } else {
            campoPlaca.setText(i.getPlaca());
        }
        if (Incidente.SIN_DATO.equals(i.getPersonaInvolucrada())) {
            casillaSinPersona.setSelected(true);
            campoPersona.setEnabled(false);
        } else {
            campoPersona.setText(i.getPersonaInvolucrada());
        }
        areaDescripcion.setText(i.getDescripcion());
        areaAcciones.setText(i.getAccionesTomadas());
        comboEstado.setSelectedItem(i.getEstado());
        areaObservacion.setText(Incidente.SIN_REVISAR.equals(i.getObservacionAdmin()) ? "" : i.getObservacionAdmin());
    }

    private void irA(int nuevo) {
        paso = Math.max(0, Math.min(nuevo, titulos.size() - 1));
        cartas.show(panelPasos, "p" + paso);
        etiquetaPaso.setText("Paso " + (paso + 1) + " de " + titulos.size() + " — " + titulos.get(paso));
        boolean ultimo = paso == titulos.size() - 1;
        botonAtras.setEnabled(paso > 0);
        botonSiguiente.setVisible(!ultimo);
        botonGuardar.setVisible(ultimo);
        if (ultimo) {
            areaResumen.setText(construirResumen());
            areaResumen.setCaretPosition(0);
        }
    }

    private void siguiente() {
        if (validarPaso(paso)) {
            irA(paso + 1);
        }
    }

    // Valida solo los datos del paso actual y avisa el error en el momento
    private boolean validarPaso(int numero) {
        try {
            String titulo = titulos.get(numero);
            switch (titulo) {
                case "Dónde y cuándo":
                    ReglasIncidente.lugar(campoLugar.getText());
                    break;
                case "Vehículo y persona":
                    ReglasIncidente.placa(textoPlaca());
                    ReglasIncidente.persona(textoPersona());
                    break;
                case "Detalle del suceso":
                    ReglasIncidente.descripcion(areaDescripcion.getText());
                    ReglasIncidente.acciones(areaAcciones.getText());
                    break;
                case "Seguimiento":
                    ReglasIncidente.observacion(areaObservacion.getText(), (EstadoIncidente) comboEstado.getSelectedItem());
                    break;
                default:
                    break;
            }
            return true;
        } catch (ControlAccesoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Revise este paso", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    private String textoPlaca() {
        return casillaSinVehiculo.isSelected() ? Incidente.SIN_DATO : campoPlaca.getText();
    }

    private String textoPersona() {
        return casillaSinPersona.isSelected() ? Incidente.SIN_DATO : campoPersona.getText();
    }

    private String construirResumen() {
        try {
            EstadoIncidente estado = existente == null ? EstadoIncidente.ABIERTO
                    : (EstadoIncidente) comboEstado.getSelectedItem();
            Incidente temporal = Incidente.cargado(
                    existente == null ? 0 : existente.getIdIncidente(),
                    existente == null ? LocalDateTime.now() : existente.getFechaHora(),
                    existente == null ? vigilante.getIdUsuarioSistema() : existente.getIdUsuarioSistema(),
                    existente == null ? vigilante.getNombre() : existente.getReportadoPor(),
                    (TipoIncidente) comboTipo.getSelectedItem(),
                    (GravedadIncidente) comboGravedad.getSelectedItem(),
                    ReglasIncidente.lugar(campoLugar.getText()),
                    ReglasIncidente.placa(textoPlaca()),
                    ReglasIncidente.persona(textoPersona()),
                    ReglasIncidente.descripcion(areaDescripcion.getText()),
                    ReglasIncidente.acciones(areaAcciones.getText()),
                    estado,
                    existente == null ? Incidente.SIN_REVISAR
                            : ReglasIncidente.observacion(areaObservacion.getText(), estado),
                    LocalDateTime.now());
            return (existente == null ? "Revise el reporte antes de guardarlo. "
                    + "La hora real se toma al guardar.\n\n" : "") + temporal.resumen();
        } catch (ControlAccesoException ex) {
            return "Falta información: " + ex.getMessage() + "\nVuelva a los pasos anteriores.";
        }
    }

    private void guardar() {
        // Se valida de nuevo todo, por si se volvio atras y se borro algo
        for (int i = 0; i < titulos.size() - 1; i++) {
            if (!validarPaso(i)) {
                irA(i);
                return;
            }
        }
        try {
            if (existente == null) {
                sistema.reportarIncidente(vigilante, (TipoIncidente) comboTipo.getSelectedItem(),
                        (GravedadIncidente) comboGravedad.getSelectedItem(), campoLugar.getText(),
                        textoPlaca(), textoPersona(), areaDescripcion.getText(), areaAcciones.getText());
                JOptionPane.showMessageDialog(this, "Incidente reportado correctamente.",
                        "Reporte guardado", JOptionPane.INFORMATION_MESSAGE);
            } else {
                sistema.actualizarIncidente(existente, (TipoIncidente) comboTipo.getSelectedItem(),
                        (GravedadIncidente) comboGravedad.getSelectedItem(), campoLugar.getText(),
                        textoPlaca(), textoPersona(), areaDescripcion.getText(), areaAcciones.getText(),
                        (EstadoIncidente) comboEstado.getSelectedItem(), areaObservacion.getText());
                JOptionPane.showMessageDialog(this, "Incidente modificado correctamente.",
                        "Reporte guardado", JOptionPane.INFORMATION_MESSAGE);
            }
            guardado = true;
            dispose();
        } catch (ControlAccesoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incorrectos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Auxiliares de dibujo
    // ------------------------------------------------------------------

    private static JPanel formulario() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 3, 0));
        return l;
    }

    private static JLabel nota(String texto) {
        JLabel l = new JLabel("<html><i>" + texto + "</i></html>");
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return l;
    }

    private static JTextArea areaTexto() {
        JTextArea a = new JTextArea(6, 40);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }
}
