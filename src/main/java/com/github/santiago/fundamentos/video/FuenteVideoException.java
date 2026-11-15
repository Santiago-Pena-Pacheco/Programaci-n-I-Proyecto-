package com.github.santiago.fundamentos.video;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;

/** No se pudo abrir la camara, el archivo o la direccion de video indicada. */
public class FuenteVideoException extends ControlAccesoException {

    public FuenteVideoException(String mensaje) {
        super(mensaje);
    }
}
