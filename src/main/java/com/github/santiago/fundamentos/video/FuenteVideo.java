package com.github.santiago.fundamentos.video;

import org.opencv.core.Mat;

/**
 * Lo minimo que la aplicacion necesita de una fuente de video. Es la interfaz
 * "objetivo" del patron Adapter: FuenteOpenCV y FuenteMjpeg la implementan, y
 * PanelCamara trabaja solo con este tipo, sin saber de donde viene la imagen.
 */
public interface FuenteVideo {

    boolean isOpened();

    boolean read(Mat frame);

    void release();

    /** Mensaje de error si la fuente fallo; null si todo va bien. */
    String error();
}
