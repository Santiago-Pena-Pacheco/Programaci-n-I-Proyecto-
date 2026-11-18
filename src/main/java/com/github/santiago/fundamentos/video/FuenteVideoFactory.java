package com.github.santiago.fundamentos.video;

import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/** FACTORY (patron creacional) */
public final class FuenteVideoFactory {

    private FuenteVideoFactory() {
    }

    public static FuenteVideo crear(String fuente) throws FuenteVideoException {
        if (fuente == null || fuente.isBlank()) {
            throw new FuenteVideoException("Escriba un número de cámara (0, 1...) o una dirección http://");
        }
        String texto = fuente.trim();
        String minusculas = texto.toLowerCase();

        if (minusculas.startsWith("http://") || minusculas.startsWith("https://")) {
            return new FuenteMjpeg(texto);
        }

        Integer indice = null;
        try {
            indice = Integer.valueOf(texto);
        } catch (NumberFormatException e) {
            // no es un numero: se trata como la ruta de un archivo de video
        }

        VideoCapture camara;
        if (indice != null) {
            camara = new VideoCapture(indice);
            if (!camara.isOpened()) {
                // Las camaras virtuales (Camo, OBS...) muchas veces solo abren por DirectShow
                camara.release();
                camara = new VideoCapture(indice, Videoio.CAP_DSHOW);
            }
        } else {
            camara = new VideoCapture(texto);
        }

        if (!camara.isOpened()) {
            camara.release();
            throw new FuenteVideoException(indice != null
                    ? "No se pudo abrir la cámara (índice " + indice + "). Con Camo: abra Camo Studio, "
                    + "conecte el celular y pruebe otro índice (0, 1, 2, 3)."
                    : "No se pudo abrir la fuente de video: " + texto);
        }
        return new FuenteOpenCV(camara);
    }
}
