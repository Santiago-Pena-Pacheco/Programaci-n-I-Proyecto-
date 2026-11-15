package com.github.santiago.fundamentos;

/**
 * Representa un vehiculo, ya sea de un propietario o de un visitante.
 */
public class Vehiculo {

    private String placa;

    public Vehiculo(String placa) {
        this.placa = placa;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    @Override
    public String toString() {
        return placa;
    }
}
