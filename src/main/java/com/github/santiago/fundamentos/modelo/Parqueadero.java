package com.github.santiago.fundamentos.modelo;

import com.github.santiago.fundamentos.logica.GestorParqueaderos;

/**
 * Representa un espacio de parqueo. Puede ser el parqueadero fijo asociado
 * a una UbicacionDestino (asignado a un UsuarioFijo) o uno de los
 * parqueaderos del pool de visitantes administrado por GestorParqueaderos.
 * El estado "ocupado" solo se modifica desde asignarVehiculo() y liberar().
 *
 * @author salas
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
