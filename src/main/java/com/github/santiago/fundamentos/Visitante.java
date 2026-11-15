package com.github.santiago.fundamentos;

/**
 * Representa a un visitante externo. Su placa no esta registrada por
 * ningun apartamento; se dirige a un apartamento de destino y el
 * sistema le asigna automaticamente un parqueadero del pool de visitantes.
 */
public class Visitante {

    private String nombre;
    private String cedula;
    private Vehiculo vehiculo;
    private String apartamentoDestino;
    private Parqueadero parqueaderoAsignado;

    public Visitante(String nombre, String cedula, Vehiculo vehiculo, String apartamentoDestino) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.vehiculo = vehiculo;
        this.apartamentoDestino = apartamentoDestino;
        this.parqueaderoAsignado = null;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
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

    public void mostrarInformacion() {
        System.out.println(" ====================== ");
        System.out.println("nombre = " + nombre);
        System.out.println("cedula = " + cedula);
        System.out.println("placa vehiculo = " + vehiculo.getPlaca());
        System.out.println("apartamento destino = " + apartamentoDestino);
        System.out.println("parqueadero asignado = "
                + (parqueaderoAsignado == null ? "NO HAY DISPONIBLE" : "#" + parqueaderoAsignado.getNumero()));
    }
}
