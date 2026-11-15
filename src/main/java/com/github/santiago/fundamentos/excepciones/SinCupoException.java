package com.github.santiago.fundamentos.excepciones;

/** No hay parqueaderos de visitantes libres (o la placa ya esta ocupando uno). */
public class SinCupoException extends ReglaNegocioException {

    public SinCupoException(String mensaje) {
        super(mensaje);
    }
}
