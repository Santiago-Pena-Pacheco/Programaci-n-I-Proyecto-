package com.github.santiago.fundamentos.excepciones;

/** Un dato digitado esta vacio, mal formado o fuera de rango (placa, cedula, nombre, etc.). */
public class DatoInvalidoException extends ControlAccesoException {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
