package com.github.santiago.fundamentos.modelo;

/**
 * Lo que paso cuando la camara leyo una placa. Le dice a la ventana que debe
 * mostrar: un aviso de ingreso o salida automatica, un acceso denegado o el
 * formulario de visitante.
 *
 * @author salas
 */
public class ResultadoAcceso {

    public enum Tipo {
        INGRESO_USUARIO_FIJO,  // un usuario fijo activo entro
        SALIDA_USUARIO_FIJO,   // un usuario fijo activo salio
        SALIDA_VISITANTE,      // un visitante que estaba adentro salio
        VISITANTE_NUEVO,       // placa desconocida: hay que registrar un visitante
        VISITANTE_REGRESA,     // visitante conocido que estaba fuera: confirmar su destino
        DENEGADO,              // placa de un usuario fijo dado de baja
        IGNORADA               // lectura repetida dentro del tiempo de espera
    }

    private final Tipo tipo;
    private final String placa;
    private final UsuarioFijo usuarioFijo;
    private final Visitante visitante;

    private ResultadoAcceso(Tipo tipo, String placa, UsuarioFijo usuarioFijo, Visitante visitante) {
        this.tipo = tipo;
        this.placa = placa;
        this.usuarioFijo = usuarioFijo;
        this.visitante = visitante;
    }

    public static ResultadoAcceso deUsuarioFijo(Tipo tipo, String placa, UsuarioFijo usuario) {
        return new ResultadoAcceso(tipo, placa, usuario, null);
    }

    public static ResultadoAcceso deVisitante(Tipo tipo, String placa, Visitante visitante) {
        return new ResultadoAcceso(tipo, placa, null, visitante);
    }

    public static ResultadoAcceso sinPersona(Tipo tipo, String placa) {
        return new ResultadoAcceso(tipo, placa, null, null);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getPlaca() {
        return placa;
    }

    /** La persona involucrada como tipo padre (UsuarioFijo o Visitante), o null si no hay ninguna. */
    public Persona getPersona() {
        return usuarioFijo != null ? usuarioFijo : visitante;
    }

    /** El usuario fijo involucrado (null si el resultado es de un visitante o de nadie). */
    public UsuarioFijo getUsuarioFijo() {
        return usuarioFijo;
    }

    /** El visitante involucrado (null si el resultado es de un usuario fijo o de nadie). */
    public Visitante getVisitante() {
        return visitante;
    }
}
