package com.github.santiago.fundamentos;

/**
 * Representa un espacio de parqueo. Puede ser el parqueadero fijo de un
 * apartamento (asignado a un propietario) o uno de los parqueaderos del
 * pool de visitantes.
 * El estado "ocupado" solo se modifica desde asignarVehiculo() y liberar().
 */
public class Parqueadero {

    private int numero;
    private boolean ocupado;
    private String placaVehiculoActual;

    public Parqueadero(int numero) {
        this.numero = numero;
        this.ocupado = false;
        this.placaVehiculoActual = null;
    }

    public int getNumero() {
        return numero;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public String getPlacaVehiculoActual() {
        return placaVehiculoActual;
    }

    public boolean asignarVehiculo(String placa) {
        if (ocupado) {
            return false;
        }
        this.ocupado = true;
        this.placaVehiculoActual = placa;
        return true;
    }

    public void liberar() {
        this.ocupado = false;
        this.placaVehiculoActual = null;
    }

    public boolean estaDisponible() {
        return !ocupado;
    }

    @Override
    public String toString() {
        return "Parqueadero #" + numero;
    }
}
