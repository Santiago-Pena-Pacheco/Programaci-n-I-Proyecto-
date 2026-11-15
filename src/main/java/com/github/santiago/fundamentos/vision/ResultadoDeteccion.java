package com.github.santiago.fundamentos.vision;

import org.opencv.core.Rect;

/**
 * Resultado de procesar un frame de la camara: puede traer la region
 * donde se detecto un posible rectangulo de placa (para dibujar el
 * recuadro en la GUI) y, opcionalmente, una placa ya CONFIRMADA (validada
 * durante varias lecturas consecutivas identicas, igual que el buffer
 * "lecturas_placa" del script original en Python).
 *
 * @author salas
 */
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
