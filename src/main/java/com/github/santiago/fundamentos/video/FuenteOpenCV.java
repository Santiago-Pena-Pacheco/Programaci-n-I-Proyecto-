package com.github.santiago.fundamentos.video;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/** ADAPTER (patron estructural) */
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
