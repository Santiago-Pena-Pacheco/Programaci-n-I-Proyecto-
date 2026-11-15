package com.github.santiago.fundamentos;

/**
 * Superclase abstracta de las personas que interactuan con el conjunto:
 * centraliza nombre, cedula y vehiculo, y obliga a cada subclase a decir
 * como se llama su tipo (polimorfismo).
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

    /** Cada subclase decide como se llama segun el contexto activo (Propietario, Empleado, Cliente...). */
    public abstract String getEtiquetaTipo(TipoContexto contexto);

    public void mostrarInformacion(TipoContexto contexto) {
        System.out.println(" ====================== ");
        System.out.println("tipo = " + getEtiquetaTipo(contexto));
        System.out.println("nombre = " + nombre);
        System.out.println("cedula = " + cedula);
        System.out.println("placa vehiculo = " + vehiculo.getPlaca());
    }
}
