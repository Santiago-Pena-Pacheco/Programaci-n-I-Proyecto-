package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.logica.SistemaControlAcceso;
import com.github.santiago.fundamentos.modelo.Escenario;
import com.github.santiago.fundamentos.modelo.Persona;
import com.github.santiago.fundamentos.modelo.ResultadoAcceso;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;
import com.github.santiago.fundamentos.modelo.UsuarioSistema;
import com.github.santiago.fundamentos.modelo.Visitante;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal del vigilante. Trabaja siempre sobre el escenario que el
 * administrador dejo activo, con:
 *   - Un menu lateral con las acciones del vigilante (visitantes, incidentes,
 *     registros). El registro de usuarios fijos es del administrador.
 *   - Un panel central que muestra el feed de la camara en tiempo real
 *     y reconoce placas automaticamente.
 *
 * Cuando la camara confirma una placa, el sistema alterna su estado
 * DENTRO/FUERA: la primera lectura es un ingreso y la siguiente una salida,
 * ambas automaticas (el vigilante NO reporta salidas). El vigilante solo
 * interviene si la placa es de un visitante nuevo (llena el formulario) o
 * de un visitante conocido que regresa (confirma su destino).
 *
 * @author salas
 */
public class InterfazVigilante extends JFrame {

    private final SistemaControlAcceso sistema;
    private final Escenario escenario;
    private final UsuarioSistema sesion;
    private JDialog ventanaAdentro;
    private PanelVehiculosAdentro panelAdentro;
    private final PanelCamara panelCamara = new PanelCamara();

    public InterfazVigilante(UsuarioSistema sesion, Escenario escenario) {
        super("Control de Acceso - Panel del Vigilante");
        this.escenario = escenario;
        this.sesion = sesion;
        this.sistema = new SistemaControlAcceso(escenario);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(construirMenuLateral(), BorderLayout.WEST);
        add(construirEncabezado(sesion), BorderLayout.NORTH);
        add(panelCamara, BorderLayout.CENTER);

        panelCamara.addListener(this::onPlacaConfirmada);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panelCamara.detener(); // libera la camara al cerrar con la X
            }
        });
    }

    private JPanel construirEncabezado(UsuarioSistema sesion) {
        JLabel titulo = new JLabel("  Cámara de seguridad - " + escenario.getNombre());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 16f));
        JLabel usuario = new JLabel("Vigilante: " + sesion.getNombre() + "  ");
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.add(titulo, BorderLayout.WEST);
        panel.add(usuario, BorderLayout.EAST);
        return panel;
    }

    private JPanel construirMenuLateral() {
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        menu.setPreferredSize(new Dimension(230, 0));
        menu.setBackground(new Color(38, 45, 58));

        menu.add(crearTitulo("CONTROL DE ACCESO"));
        menu.add(Box.createVerticalStrut(20));

        menu.add(crearBoton("Iniciar cámara", e -> iniciarCamara()));
        menu.add(Box.createVerticalStrut(10));
        menu.add(crearBoton("Registrar visitante", e ->
                new FormularioVisitante(this, sistema, null).setVisible(true)));
        menu.add(crearBoton("Reportar incidente", e -> reportarIncidente()));
        menu.add(Box.createVerticalStrut(10));
        menu.add(crearBoton("Vehículos adentro", e -> abrirVehiculosAdentro()));
        menu.add(crearBoton("Ver registros", e ->
                new PanelRegistros(this, sistema).setVisible(true)));
        menu.add(Box.createVerticalGlue());
        menu.add(crearBoton("Cerrar sesión", e -> {
            panelCamara.detener();
            dispose();
            new LoginFrame().setVisible(true);
        }));
        menu.add(crearBoton("Salir", e -> {
            panelCamara.detener();
            dispose();
            System.exit(0);
        }));

        return menu;
    }

    private JLabel crearTitulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 14f));
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        return etiqueta;
    }

    private JButton crearBoton(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setMaximumSize(new Dimension(210, 36));
        boton.addActionListener(accion);
        return boton;
    }

    private void iniciarCamara() {
        String fuente = JOptionPane.showInputDialog(this,
                "<html>Cámara a usar:<br>"
                + "&bull; un número (<b>0</b> = cámara por defecto, 1, 2... = cámara virtual como Camo)<br>"
                + "&bull; o la dirección <b>http://...</b> de la transmisión del celular por Wi-Fi</html>",
                "0");
        if (fuente == null || fuente.isBlank()) {
            return;
        }
        // Primero busca solita la carpeta de Tesseract dentro del proyecto; solo si no
        // la encuentra pide la ruta como antes
        File tessdata = buscarTessdata();
        String rutaTessdata;
        if (tessdata != null) {
            rutaTessdata = tessdata.getAbsolutePath();
        } else {
            rutaTessdata = JOptionPane.showInputDialog(this,
                    "No se encontró Tesseract dentro del proyecto.\n"
                    + "Ruta de la carpeta 'tessdata' de Tesseract:",
                    "C:/Program Files/Tesseract-OCR/tessdata");
            if (rutaTessdata == null || rutaTessdata.isBlank()) {
                return;
            }
        }
        panelCamara.iniciar(fuente.trim(), rutaTessdata.trim());
    }

    /**
     * Busca la carpeta 'tessdata' (la que contiene eng.traineddata) junto al
     * programa, sin importar si la carpeta de Tesseract se llama "Tesseract-OCR",
     * "tesseract", etc. Mira en la carpeta desde donde se ejecuta el programa,
     * en su carpeta padre y en la del propio codigo/jar. Retorna null si no hay.
     */
    private static File buscarTessdata() {
        List<File> raices = new ArrayList<>();
        File actual = new File(System.getProperty("user.dir"));
        raices.add(actual);
        if (actual.getParentFile() != null) {
            raices.add(actual.getParentFile());
        }
        try {
            File codigo = new File(InterfazVigilante.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File carpeta = codigo.isFile() ? codigo.getParentFile() : codigo;
            for (int i = 0; i < 3 && carpeta != null; i++) { // target/classes -> proyecto
                raices.add(carpeta);
                carpeta = carpeta.getParentFile();
            }
        } catch (Exception e) {
            // si no se puede saber donde esta el codigo, se siguen usando las otras rutas
        }

        for (File raiz : raices) {
            File encontrada = buscarTessdataEn(raiz);
            if (encontrada != null) {
                return encontrada;
            }
        }
        return null;
    }

    private static File buscarTessdataEn(File raiz) {
        File directa = new File(raiz, "tessdata");
        if (tieneIdioma(directa)) {
            return directa;
        }
        File[] hijos = raiz.listFiles(File::isDirectory);
        if (hijos == null) {
            return null;
        }
        for (File hijo : hijos) {
            if (!hijo.getName().toLowerCase().contains("tess")) {
                continue;
            }
            File tessdata = new File(hijo, "tessdata");
            if (tieneIdioma(tessdata)) {
                return tessdata;
            }
            if (tieneIdioma(hijo)) { // la carpeta copiada ya es tessdata
                return hijo;
            }
        }
        return null;
    }

    private static boolean tieneIdioma(File carpeta) {
        return new File(carpeta, "eng.traineddata").isFile();
    }

    // Ventana sin bloqueo: queda abierta al lado de la camara y se actualiza sola
    private void abrirVehiculosAdentro() {
        if (ventanaAdentro != null && ventanaAdentro.isDisplayable()) {
            ventanaAdentro.toFront();
            return;
        }
        ventanaAdentro = new JDialog(this, "Vehículos adentro - " + escenario.getNombre(), false);
        panelAdentro = new PanelVehiculosAdentro(sistema);
        ventanaAdentro.setContentPane(panelAdentro);
        ventanaAdentro.setSize(820, 460);
        ventanaAdentro.setLocationRelativeTo(this);
        ventanaAdentro.setVisible(true);
    }

    private void reportarIncidente() {
        new FormularioIncidente(this, sistema, sesion).setVisible(true);
    }

    /**
     * Callback invocado por PanelCamara cada vez que se confirma una placa.
     * La decision (ingreso, salida, denegado...) la toma SistemaControlAcceso
     * segun el estado DENTRO/FUERA; aqui solo se muestra el resultado y, si
     * hace falta, se piden al vigilante los datos de un visitante.
     */
    private void onPlacaConfirmada(String placa) {
        try {
            ResultadoAcceso r = sistema.procesarPlaca(placa);
            switch (r.getTipo()) {
                case INGRESO_USUARIO_FIJO:
                case SALIDA_USUARIO_FIJO:
                case SALIDA_VISITANTE: {
                    // Polimorfismo: se trata igual a cualquier Persona; cada subclase aporta su propio detalle
                    Persona persona = r.getPersona();
                    boolean ingreso = r.getTipo() == ResultadoAcceso.Tipo.INGRESO_USUARIO_FIJO;
                    JOptionPane.showMessageDialog(this,
                            (ingreso ? "INGRESO AUTORIZADO" : "SALIDA REGISTRADA") + "\n\n"
                                    + persona.getTipoTexto(escenario) + ": " + persona.getNombre()
                                    + " (" + persona.getPlaca() + ")\n"
                                    + persona.getDetalleAcceso(escenario)
                                    + (ingreso ? "" : "\nSu parqueadero quedó libre."),
                            ingreso ? "Ingreso" : "Salida", JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
                case VISITANTE_REGRESA:
                    confirmarRegreso(r.getVisitante());
                    break;
                case VISITANTE_NUEVO:
                    JOptionPane.showMessageDialog(this,
                            "Placa " + r.getPlaca() + " no registrada.\nSe abrirá el formulario de visitante.",
                            "Visitante nuevo", JOptionPane.WARNING_MESSAGE);
                    new FormularioVisitante(this, sistema, r.getPlaca()).setVisible(true);
                    break;
                case DENEGADO:
                    JOptionPane.showMessageDialog(this,
                            "ACCESO DENEGADO\n\nLa placa " + r.getPlaca() + " pertenece a "
                                    + r.getUsuarioFijo().getNombre() + ", dado de baja.\nAvise al administrador.",
                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                    break;
                case IGNORADA:
                default:
                    break; // lectura repetida o texto que no es una placa: no se molesta al vigilante
            }
        } catch (SQLException | ControlAccesoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        if (panelAdentro != null && ventanaAdentro != null && ventanaAdentro.isDisplayable()) {
            panelAdentro.actualizar(); // refleja de inmediato el ingreso o la salida que acaba de leer la camara
        }
        panelCamara.limpiarUltimaPlacaNotificada();
    }

    // Visitante que ya esta guardado y vuelve: sus datos no se piden de nuevo, solo el destino
    private void confirmarRegreso(Visitante v) throws SQLException {
        String destino = (String) JOptionPane.showInputDialog(this,
                "Visitante conocido: " + v.getNombre() + " (" + v.getPlaca() + ")\n"
                        + "Confirme el " + escenario.getEtiquetaUbicacion().toLowerCase() + " al que se dirige:",
                "Regreso de visitante", JOptionPane.QUESTION_MESSAGE, null, null, v.getDestino());
        if (destino == null) {
            return; // el vigilante cancelo: no entra
        }
        try {
            Visitante dentro = sistema.confirmarRegresoVisitante(v, destino);
            JOptionPane.showMessageDialog(this,
                    "Ingreso registrado.\nParqueadero de visitante asignado: #"
                            + dentro.getParqueaderoAsignado().getNumero(),
                    "Ingreso", JOptionPane.INFORMATION_MESSAGE);
        } catch (ControlAccesoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Dato obligatorio", JOptionPane.WARNING_MESSAGE);
        }
    }
}
