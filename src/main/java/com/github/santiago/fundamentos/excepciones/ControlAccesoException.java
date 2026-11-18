package com.github.santiago.fundamentos.excepciones;

/** Raiz de las excepciones del dominio del control de acceso */
public class ControlAccesoException extends Exception {

    public ControlAccesoException(String mensaje) {
        super(mensaje);
    }

    public ControlAccesoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
