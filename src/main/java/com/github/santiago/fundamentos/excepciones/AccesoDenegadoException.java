package com.github.santiago.fundamentos.excepciones;

/** Un usuario dado de baja (INACTIVO) intento ingresar o ser modificado. */
public class AccesoDenegadoException extends ReglaNegocioException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
