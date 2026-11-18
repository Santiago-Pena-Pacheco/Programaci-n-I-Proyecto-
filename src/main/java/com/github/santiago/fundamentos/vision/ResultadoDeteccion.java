package com.github.santiago.fundamentos.vision;

import org.opencv.core.Rect;

/** Resultado de procesar un frame de la camara */
public class ResultadoDeteccion {

    private final Rect regionPlaca;      // puede ser null si no se detecto contorno candidato
    private final String placaConfirmada; // puede ser null si aun no hay suficiente confianza

    public ResultadoDeteccion(Rect regionPlaca, String placaConfirmada) {
        this.regionPlaca = regionPlaca;
        this.placaConfirmada = placaConfirmada;
    }

    public Rect getRegionPlaca() {
        return regionPlaca;
    }

    public boolean hayRegionDetectada() {
        return regionPlaca != null;
    }

    public String getPlacaConfirmada() {
        return placaConfirmada;
    }

    public boolean hayPlacaConfirmada() {
        return placaConfirmada != null && !placaConfirmada.isBlank();
    }
}
