package com.github.santiago.fundamentos.video;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** ADAPTER (patron estructural): adapta un flujo HTTP MJPEG a la interfaz FuenteVideo */
public final class FuenteMjpeg implements FuenteVideo {

    private static final int TIEMPO_SIN_IMAGEN_MS = 15000;

    private final long inicio = System.currentTimeMillis();
    private volatile boolean activo = true;
    private volatile byte[] ultimoJpeg = null;
    private volatile String error = null;
    private byte[] ultimoDecodificado = null;
    private HttpURLConnection conexion;

    public FuenteMjpeg(String direccion) {
        Thread hilo = new Thread(() -> leerFlujo(direccion), "lector-mjpeg");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void leerFlujo(String direccion) {
        try {
            URL url = new URL(direccion);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            synchronized (this) {
                conexion = con;
            }
            con.setConnectTimeout(5000);
            con.setReadTimeout(10000);
            if (url.getUserInfo() != null) { // http://usuario:clave@direccion
                String credenciales = Base64.getEncoder().encodeToString(
                        url.getUserInfo().getBytes(StandardCharsets.UTF_8));
                con.setRequestProperty("Authorization", "Basic " + credenciales);
            }

            int codigo = con.getResponseCode();
            if (codigo != 200) {
                error = "La direccion respondio con el codigo " + codigo
                        + ". Revise la direccion (suele terminar en /video) y la clave si la app la pide.";
                return;
            }

            try (InputStream entrada = new BufferedInputStream(con.getInputStream(), 65536)) {
                ByteArrayOutputStream cuadro = new ByteArrayOutputStream(65536);
                boolean dentro = false;
                int anterior = -1;
                int actual;
                while (activo && (actual = entrada.read()) != -1) {
                    if (!dentro) {
                        if (anterior == 0xFF && actual == 0xD8) { // inicio de un JPEG
                            dentro = true;
                            cuadro.reset();
                            cuadro.write(0xFF);
                            cuadro.write(0xD8);
                        }
                    } else {
                        cuadro.write(actual);
                        if (anterior == 0xFF && actual == 0xD9) { // fin del JPEG
                            ultimoJpeg = cuadro.toByteArray();
                            dentro = false;
                        }
                    }
                    anterior = actual;
                }
            }
            if (activo && error == null) {
                error = "Se cerro la transmision de video.";
            }
        } catch (IOException e) {
            if (activo) {
                error = "No se pudo leer el video de la direccion: " + e.getMessage()
                        + ". Verifique que el celular y el computador esten en la misma red Wi-Fi "
                        + "y que la app este transmitiendo.";
            }
        }
    }

    @Override
    public boolean isOpened() {
        return activo && error == null;
    }

    @Override
    public boolean read(Mat frame) {
        byte[] jpeg = ultimoJpeg;
        if (jpeg == null || jpeg == ultimoDecodificado) {
            return false; // todavia no llega un cuadro nuevo
        }
        ultimoDecodificado = jpeg;
        Mat decodificada = Imgcodecs.imdecode(new MatOfByte(jpeg), Imgcodecs.IMREAD_COLOR);
        if (decodificada.empty()) {
            return false;
        }
        decodificada.copyTo(frame);
        return true;
    }

    @Override
    public void release() {
        activo = false;
        synchronized (this) {
            if (conexion != null) {
                conexion.disconnect(); // desbloquea la lectura que este esperando
            }
        }
    }

    @Override
    public String error() {
        if (error != null) {
            return error;
        }
        if (activo && ultimoJpeg == null
                && System.currentTimeMillis() - inicio > TIEMPO_SIN_IMAGEN_MS) {
            return "La direccion responde pero no entrega imagenes. "
                    + "Use la direccion de video MJPEG (en IP Webcam termina en /video).";
        }
        return null;
    }
}
