package com.github.santiago.fundamentos.modelo;

import com.github.santiago.fundamentos.logica.GestorParqueaderos;

/**
 * Superclase abstracta que representa a cualquier persona que interactua
 * con el sistema de control de acceso: un usuario fijo (propietario,
 * empleado, personal de evento... segun el contexto) o un visitante
 * temporal.
 *
 * Centraliza los atributos comunes (nombre, cedula, vehiculo) y define
 * el contrato polimorfico que cada subclase debe resolver a su manera:
 * como se le asigna un parqueadero, como se libera, y como se etiqueta
 * y se muestra su informacion segun el TipoContexto activo del sistema.
 */
public abstract class Persona {

    protected String nombre;
    protected String cedula;
    protected Vehiculo vehiculo;

    public Persona(String nombre, String cedula, Vehiculo vehiculo) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.vehiculo = vehiculo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public abstract boolean asignarParqueadero(GestorParqueaderos gestor);

    public abstract void liberarParqueadero();

    public abstract Parqueadero getParqueaderoAsignado();

    public abstract String getEtiquetaTipo(TipoContexto contexto);

    public void mostrarInformacion(TipoContexto contexto) {
        System.out.println(" ====================== ");
        System.out.println("tipo = " + getEtiquetaTipo(contexto));
        System.out.println("nombre = " + nombre);
        System.out.println("cedula = " + cedula);
        System.out.println("placa vehiculo = " + vehiculo.getPlaca());
    }
}
