package com.github.santiago.fundamentos;

/**
 * Representa un apartamento del conjunto residencial.
 * Cada apartamento tiene un unico parqueadero fijo asignado (composicion),
 * tal como se definio en el modelo de base de datos (relacion 1:1).
 
 */
public class Apartamento {

    private int numero;
    private int torre;
    private Parqueadero parqueadero;

    public Apartamento(int numero, int torre, Parqueadero parqueadero) {
        this.numero = numero;
        this.torre = torre;
        this.parqueadero = parqueadero;
    }

    public int getNumero() {
        return numero;
    }

    public int getTorre() {
        return torre;
    }

    public Parqueadero getParqueadero() {
        return parqueadero;
    }

    @Override
    public String toString() {
        return "Apto " + numero + " - Torre " + torre;
    }
}
