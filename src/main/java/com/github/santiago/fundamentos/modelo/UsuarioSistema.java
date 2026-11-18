package com.github.santiago.fundamentos.modelo;

/** Persona que inicia sesion en el programa (administrador o vigilante) */
public class UsuarioSistema {

    private final int idUsuarioSistema;
    private final String usuario;
    private final String nombre;
    private final Rol rol;

    public UsuarioSistema(int idUsuarioSistema, String usuario, String nombre, Rol rol) {
        this.idUsuarioSistema = idUsuarioSistema;
        this.usuario = usuario;
        this.nombre = nombre;
        this.rol = rol;
    }

    public int getIdUsuarioSistema() {
        return idUsuarioSistema;
    }

    public String getUsuario() {
        return usuario;
    }

    /** Nombre completo: es el que firma los reportes de incidentes. */
    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public String toString() {
        return usuario + " (" + rol + ")";
    }
}
