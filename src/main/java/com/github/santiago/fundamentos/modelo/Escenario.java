package com.github.santiago.fundamentos.modelo;

/** Contexto de uso del sistema (conjunto residencial, centro comercial, concierto, oficinas...) */
public class Escenario {

    private int idEscenario;
    private String nombre;
    private String etiquetaUsuarioFijo;
    private String etiquetaUsuarioFijoPlural;
    private String etiquetaUbicacion;
    private String etiquetaZona;
    private int cantidadParqueaderosVisitantes;

    public Escenario() {
    }

    public Escenario(int idEscenario, String nombre, String etiquetaUsuarioFijo,
            String etiquetaUsuarioFijoPlural, String etiquetaUbicacion,
            String etiquetaZona, int cantidadParqueaderosVisitantes) {
        this.idEscenario = idEscenario;
        this.nombre = nombre;
        this.etiquetaUsuarioFijo = etiquetaUsuarioFijo;
        this.etiquetaUsuarioFijoPlural = etiquetaUsuarioFijoPlural;
        this.etiquetaUbicacion = etiquetaUbicacion;
        this.etiquetaZona = etiquetaZona;
        this.cantidadParqueaderosVisitantes = cantidadParqueaderosVisitantes;
    }

    public int getIdEscenario() {
        return idEscenario;
    }

    public void setIdEscenario(int idEscenario) {
        this.idEscenario = idEscenario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEtiquetaUsuarioFijo() {
        return etiquetaUsuarioFijo;
    }

    public void setEtiquetaUsuarioFijo(String etiquetaUsuarioFijo) {
        this.etiquetaUsuarioFijo = etiquetaUsuarioFijo;
    }

    public String getEtiquetaUsuarioFijoPlural() {
        return etiquetaUsuarioFijoPlural;
    }

    public void setEtiquetaUsuarioFijoPlural(String etiquetaUsuarioFijoPlural) {
        this.etiquetaUsuarioFijoPlural = etiquetaUsuarioFijoPlural;
    }

    public String getEtiquetaUbicacion() {
        return etiquetaUbicacion;
    }

    public void setEtiquetaUbicacion(String etiquetaUbicacion) {
        this.etiquetaUbicacion = etiquetaUbicacion;
    }

    public String getEtiquetaZona() {
        return etiquetaZona;
    }

    public void setEtiquetaZona(String etiquetaZona) {
        this.etiquetaZona = etiquetaZona;
    }

    public int getCantidadParqueaderosVisitantes() {
        return cantidadParqueaderosVisitantes;
    }

    public void setCantidadParqueaderosVisitantes(int cantidadParqueaderosVisitantes) {
        this.cantidadParqueaderosVisitantes = cantidadParqueaderosVisitantes;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
