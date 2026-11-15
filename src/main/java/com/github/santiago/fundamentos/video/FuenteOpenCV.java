package com.github.santiago.fundamentos.video;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/**
 * ADAPTER (patron estructural): adapta la clase VideoCapture de OpenCV, cuya
 * interfaz no controlamos, a la interfaz FuenteVideo que espera la aplicacion.
 * Sirve para una camara numerada o un archivo de video.
 */
public final class FuenteOpenCV implements FuenteVideo {

    private final VideoCapture camara;

    public FuenteOpenCV(VideoCapture camara) {
        this.camara = camara;
    }

    @Override
    public boolean isOpened() {
        return camara.isOpened();
    }

    @Override
    public boolean read(Mat frame) {
        return camara.read(frame);
    }

    @Override
    public void release() {
        camara.release();
    }

    @Override
    public String error() {
        return null;
    }
}
