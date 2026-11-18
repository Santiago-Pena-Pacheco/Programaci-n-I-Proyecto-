package com.github.santiago.fundamentos.video;

import org.opencv.core.Mat;

/** Lo minimo que la aplicacion necesita de una fuente de video */
public interface FuenteVideo {

    boolean isOpened();

    boolean read(Mat frame);

    void release();

    /** Mensaje de error si la fuente fallo; null si todo va bien. */
    String error();
}
