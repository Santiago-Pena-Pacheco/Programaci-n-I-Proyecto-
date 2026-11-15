package com.github.santiago.fundamentos;

/**
 * Persona externa y temporal. Hereda de Persona y agrega el apartamento
 * al que se dirige y el parqueadero que el sistema le asigna del pool.
 */
public class Visitante extends Persona {

    private String apartamentoDestino;
    private Parqueadero parqueaderoAsignado;

    public Visitante(String nombre, String cedula, Vehiculo vehiculo, String apartamentoDestino) {
        super(nombre, cedula, vehiculo);
        this.apartamentoDestino = apartamentoDestino;
        this.parqueaderoAsignado = null;
    }

    public String getApartamentoDestino() {
        return apartamentoDestino;
    }

    public Parqueadero getParqueaderoAsignado() {
        return parqueaderoAsignado;
    }

    public void setParqueaderoAsignado(Parqueadero parqueaderoAsignado) {
        this.parqueaderoAsignado = parqueaderoAsignado;
    }

    @Override
    public String getEtiquetaTipo() {
        return "Visitante";
    }

    @Override
    public void mostrarInformacion() {
        super.mostrarInformacion();
        System.out.println("apartamento destino = " + apartamentoDestino);
        System.out.println("parqueadero asignado = "
                + (parqueaderoAsignado == null ? "NO HAY DISPONIBLE" : "#" + parqueaderoAsignado.getNumero()));
    }
}
