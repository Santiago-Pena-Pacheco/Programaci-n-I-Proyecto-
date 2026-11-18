package com.github.santiago.fundamentos.gui;

import com.github.santiago.fundamentos.vision.ConversorImagenes;
import com.github.santiago.fundamentos.vision.DetectorPlacas;
import com.github.santiago.fundamentos.vision.ResultadoDeteccion;
import com.github.santiago.fundamentos.video.FuenteVideo;
import com.github.santiago.fundamentos.video.FuenteVideoException;
import com.github.santiago.fundamentos.video.FuenteVideoFactory;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Panel central de la GUI: muestra en tiempo real el feed de la camara de seguridad, dibuja un recuadro sobre la placa cuando se */
public class PanelCamara extends JPanel {

    /** Interfaz del OBSERVER. Se invoca en el hilo de Swing (EDT) cada vez que se confirma una placa nueva. */
    public interface OnPlacaDetectadaListener {
        void onPlacaConfirmada(String placa);
    }

    private static final int INTERVALO_MS = 60; // ~16 fps, suficiente para OCR + dibujo

    private final JLabel etiquetaVideo = new JLabel();
    private final JLabel etiquetaEstado = new JLabel("Camara detenida", SwingConstants.CENTER);

    private FuenteVideo captura;
    private DetectorPlacas detector;
    private Timer timer;
    private final List<OnPlacaDetectadaListener> observadores = new ArrayList<>();
    private String ultimaPlacaNotificada = "";

    public PanelCamara() {
        setLayout(new BorderLayout());
        etiquetaVideo.setHorizontalAlignment(SwingConstants.CENTER);
        etiquetaVideo.setBackground(Color.BLACK);
        etiquetaVideo.setOpaque(true);
        etiquetaEstado.setForeground(new Color(60, 60, 60));
        etiquetaEstado.setFont(etiquetaEstado.getFont().deriveFont(Font.BOLD, 13f));

        add(etiquetaVideo, BorderLayout.CENTER);
        add(etiquetaEstado, BorderLayout.SOUTH);
    }

    /** OBSERVER: cada ventana interesada se suscribe y se entera cuando se confirma una placa. */
    public void addListener(OnPlacaDetectadaListener observador) {
        observadores.add(observador);
    }

    public void removeListener(OnPlacaDetectadaListener observador) {
        observadores.remove(observador);
    }

    private void notificarObservadores(String placa) {
        for (OnPlacaDetectadaListener observador : new ArrayList<>(observadores)) {
            observador.onPlacaConfirmada(placa);
        }
    }

    /** Inicia la captura de la camara (indice 0 = camara por defecto) y el detector de placas apuntando a la carpeta tessdata indicada */
    public void iniciar(int indiceCamara, String rutaTessdata) {
        iniciar(String.valueOf(indiceCamara), rutaTessdata);
    }

    /** Igual que el anterior, pero la fuente puede ser un numero de camara ("0", "1"...), la ruta de un video, o la direccion de una transmision por red ("http */
    public void iniciar(String fuente, String rutaTessdata) {
        detener();

        try {
            captura = FuenteVideoFactory.crear(fuente); // Factory: decide si es camara, archivo o celular por red
        } catch (FuenteVideoException e) {
            etiquetaEstado.setText(e.getMessage());
            return;
        }
        etiquetaEstado.setText(fuente.trim().toLowerCase().startsWith("http")
                ? "Conectando con " + fuente.trim() + " ..."
                : "Camara activa - buscando placas...");

        detector = new DetectorPlacas(rutaTessdata);

        timer = new Timer(INTERVALO_MS, e -> procesarSiguienteFrame());
        timer.start();
    }

    private void procesarSiguienteFrame() {
        if (captura == null) {
            return;
        }
        String error = captura.error();
        if (error != null) {
            detener();
            etiquetaEstado.setText(error);
            return;
        }
        if (!captura.isOpened()) {
            return;
        }
        Mat frame = new Mat();
        if (!captura.read(frame) || frame.empty()) {
            return;
        }
        if (etiquetaEstado.getText().startsWith("Conectando")) {
            etiquetaEstado.setText("Camara activa - buscando placas...");
        }

        ResultadoDeteccion resultado = detector.procesarFrame(frame);

        if (resultado.hayRegionDetectada()) {
            DetectorPlacas.dibujarRegion(frame, resultado.getRegionPlaca());
        }

        BufferedImage imagen = ConversorImagenes.matABufferedImage(ConversorImagenes.aTresCanales(frame));
        mostrarImagen(imagen);

        if (resultado.hayPlacaConfirmada()) {
            String placa = resultado.getPlacaConfirmada();
            if (!placa.equals(ultimaPlacaNotificada)) {
                ultimaPlacaNotificada = placa;
                etiquetaEstado.setText("Placa detectada: " + placa);
                notificarObservadores(placa);
            }
        }
    }

    private void mostrarImagen(BufferedImage imagen) {
        int anchoDisponible = Math.max(getWidth(), 640);
        int altoDisponible = Math.max(getHeight() - 30, 480);
        Image escalada = imagen.getScaledInstance(anchoDisponible, altoDisponible, Image.SCALE_FAST);
        etiquetaVideo.setIcon(new ImageIcon(escalada));
    }

    /** Permite que la ventana principal vuelva a aceptar la siguiente placa (tras cerrar un dialogo). */
    public void limpiarUltimaPlacaNotificada() {
        ultimaPlacaNotificada = "";
    }

    public void detener() {
        if (timer != null) {
            timer.stop();
        }
        if (captura != null) {
            captura.release();
            captura = null;
        }
        if (detector != null) {
            detector.close();
        }
        etiquetaEstado.setText("Camara detenida");
        etiquetaVideo.setIcon(null);
    }

    // ------------------------------------------------------------------
    //  Fuentes de video
}
