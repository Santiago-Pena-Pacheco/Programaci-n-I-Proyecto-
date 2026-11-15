package com.github.santiago.fundamentos.modelo;

/**
 * Abstraccion generica de un destino dentro de la zona controlada.
 * Reemplaza al antiguo "Apartamento": segun el contexto puede representar
 * un apartamento y torre (conjunto residencial), un local y piso (centro
 * comercial), una oficina y sector (edificio corporativo) o una zona VIP
 * y escenario (concierto/evento).
 *
 * Mantiene la misma composicion 1:1 con un Parqueadero fijo que tenia
 * Apartamento, solo que ahora ese parqueadero es opcional (no todo
 * contexto asigna parqueadero fijo por ubicacion).
 *
 * @author salas
 */
public class UbicacionDestino {

    private String identificador; // numero de apto/local/oficina, nombre de zona, etc.
    private String zona;          // torre/piso/sector/escenario, etc.
    private Parqueadero parqueaderoFijo; // puede ser null si el contexto no aplica

    public UbicacionDestino(String identificador, String zona, Parqueadero parqueaderoFijo) {
        this.identificador = identificador;
        this.zona = zona;
        this.parqueaderoFijo = parqueaderoFijo;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getZona() {
        return zona;
    }

    public Parqueadero getParqueaderoFijo() {
        return parqueaderoFijo;
    }

    @Override
    public String toString() {
        return identificador + " - " + zona;
    }
}
