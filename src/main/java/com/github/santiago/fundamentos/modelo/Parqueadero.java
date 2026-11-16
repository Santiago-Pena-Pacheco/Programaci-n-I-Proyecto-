package com.github.santiago.fundamentos.modelo;

/**
 * Representa un espacio de parqueo. Puede ser el parqueadero fijo de una
 * ubicacion (de su dueno) o uno de los parqueaderos del pool de visitantes.
 * No guarda si esta ocupado: eso se deduce de quien esta DENTRO.
 *
 * @author salas
 */
public class Parqueadero {

    private int idParqueadero;
    private int numero;
    private TipoParqueadero tipo;

    public Parqueadero(int numero, TipoParqueadero tipo) {
        this(0, numero, tipo);
    }

    public Parqueadero(int idParqueadero, int numero, TipoParqueadero tipo) {
        this.idParqueadero = idParqueadero;
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getIdParqueadero() {
        return idParqueadero;
    }

    public void setIdParqueadero(int idParqueadero) {
        this.idParqueadero = idParqueadero;
    }

    public int getNumero() {
        return numero;
    }

    public TipoParqueadero getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return "Parqueadero #" + numero;
    }
}
