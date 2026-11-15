package com.github.santiago.fundamentos.excepciones;

/** La operacion es valida como dato pero la regla del negocio no la permite en este momento. */
public class ReglaNegocioException extends ControlAccesoException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
