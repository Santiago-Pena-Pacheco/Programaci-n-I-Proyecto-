package com.github.santiago.fundamentos.excepciones;

/**
 * Raiz de las excepciones del dominio del control de acceso. Son excepciones
 * verificadas (extends Exception): el compilador obliga a quien las puede
 * recibir a decidir que mostrarle al usuario, y asi la aplicacion no se cierra
 * ante una entrada invalida. El mensaje siempre esta listo para mostrarse.
 *
 * @author salas
 */
public class ControlAccesoException extends Exception {

    public ControlAccesoException(String mensaje) {
        super(mensaje);
    }

    public ControlAccesoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
