package com.github.santiago.fundamentos.modelo;

/** Destino dentro de un escenario */
public class Ubicacion {

    private int idUbicacion;
    private String codigo;
    private String zona;
    private Parqueadero parqueadero;

    public Ubicacion(String codigo, String zona, Parqueadero parqueadero) {
        this(0, codigo, zona, parqueadero);
    }

    public Ubicacion(int idUbicacion, String codigo, String zona, Parqueadero parqueadero) {
        this.idUbicacion = idUbicacion;
        this.codigo = codigo;
        this.zona = zona == null ? "" : zona;
        this.parqueadero = parqueadero;
    }

    public int getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(int idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getZona() {
        return zona;
    }

    public Parqueadero getParqueadero() {
        return parqueadero;
    }

    public void setParqueadero(Parqueadero parqueadero) {
        this.parqueadero = parqueadero;
    }

    /** Texto para mostrar usando las etiquetas del escenario, p. ej. "Apartamento 101 - Torre 3". */
    public String descripcion(Escenario escenario) {
        String texto = escenario.getEtiquetaUbicacion() + " " + codigo;
        if (!zona.isEmpty()) {
            texto += " - " + escenario.getEtiquetaZona() + " " + zona;
        }
        return texto;
    }

    @Override
    public String toString() {
        return zona.isEmpty() ? codigo : codigo + " - " + zona;
    }
}
