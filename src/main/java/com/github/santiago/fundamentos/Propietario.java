package com.github.santiago.fundamentos;

/**
 * Representa al propietario/residente de un apartamento.
 * Tiene un Apartamento (que a su vez tiene su Parqueadero fijo)
 * y un Vehiculo registrado.
 */
public class Propietario {

    private String nombre;
    private String cedula;
    private Apartamento apartamento;
    private Vehiculo vehiculo;

    public Propietario(String nombre, String cedula, Apartamento apartamento, Vehiculo vehiculo) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.apartamento = apartamento;
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

    public Apartamento getApartamento() {
        return apartamento;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void mostrarInformacion() {
        System.out.println(" ====================== ");
        System.out.println("nombre = " + nombre);
        System.out.println("cedula = " + cedula);
        System.out.println("apartamento = " + apartamento.getNumero());
        System.out.println("torre = " + apartamento.getTorre());
        System.out.println("parqueadero = " + apartamento.getParqueadero().getNumero());
        System.out.println("placa vehiculo = " + vehiculo.getPlaca());
    }
}
