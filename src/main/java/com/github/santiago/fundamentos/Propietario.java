package com.github.santiago.fundamentos;

/**
 * Residente propietario de un apartamento. Hereda nombre, cedula y vehiculo
 * de Persona y agrega su Apartamento (que a su vez tiene su Parqueadero fijo).
 */
public class Propietario extends Persona {

    private Apartamento apartamento;

    public Propietario(String nombre, String cedula, Apartamento apartamento, Vehiculo vehiculo) {
        super(nombre, cedula, vehiculo);
        this.apartamento = apartamento;
    }

    public Apartamento getApartamento() {
        return apartamento;
    }

    @Override
    public String getEtiquetaTipo() {
        return "Propietario";
    }

    @Override
    public void mostrarInformacion() {
        super.mostrarInformacion();
        System.out.println("apartamento = " + apartamento.getNumero());
        System.out.println("torre = " + apartamento.getTorre());
        System.out.println("parqueadero = " + apartamento.getParqueadero().getNumero());
    }
}
