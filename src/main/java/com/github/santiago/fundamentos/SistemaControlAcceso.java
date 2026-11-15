package com.github.santiago.fundamentos;

import java.util.ArrayList;

/**
 * Centraliza la logica de negocio: recibe los registros de propietarios y
 * visitantes desde la interfaz del vigilante, administra el pool de
 * parqueaderos de visitantes y guarda los incidentes reportados.
 */
public class SistemaControlAcceso {

    private ArrayList<Propietario> propietarios;
    private ArrayList<Visitante> visitantes;
    private ArrayList<Incidente> incidentes;
    private GestorParqueaderos gestorParqueaderos;

    // Cantidad de parqueaderos de visitantes que se crean al iniciar el sistema
    private static final int CANTIDAD_PARQUEADEROS_VISITANTES = 10;

    public SistemaControlAcceso() {
        this.propietarios = new ArrayList<>();
        this.visitantes = new ArrayList<>();
        this.incidentes = new ArrayList<>();
        this.gestorParqueaderos = new GestorParqueaderos(CANTIDAD_PARQUEADEROS_VISITANTES);
    }

    /**
     * Registra un nuevo propietario: crea su Vehiculo, el Parqueadero fijo
     * de su apartamento y el Apartamento, y los une en un objeto Propietario.
     */
    public Propietario registrarPropietario(String nombre, String cedula, int numeroApartamento,
            int numeroTorre, int numeroParqueadero, String placa) {

        Vehiculo vehiculo = new Vehiculo(placa);
        Parqueadero parqueaderoFijo = new Parqueadero(numeroParqueadero);
        parqueaderoFijo.asignarVehiculo(placa); // el parqueadero fijo queda ocupado por el vehiculo del propietario

        Apartamento apartamento = new Apartamento(numeroApartamento, numeroTorre, parqueaderoFijo);
        Propietario propietario = new Propietario(nombre, cedula, apartamento, vehiculo);

        this.propietarios.add(propietario);
        return propietario;
    }

    /**
     * Registra un visitante y le asigna automaticamente un parqueadero
     * disponible del pool de visitantes (si hay).
     */
    public Visitante registrarVisitante(String placa, String nombre, String cedula, String apartamentoDestino) {
        Vehiculo vehiculo = new Vehiculo(placa);
        Visitante visitante = new Visitante(nombre, cedula, vehiculo, apartamentoDestino);

        Parqueadero disponible = gestorParqueaderos.buscarParqueaderoVisitanteDisponible();
        if (disponible != null) {
            disponible.asignarVehiculo(placa);
            visitante.setParqueaderoAsignado(disponible);
        }

        this.visitantes.add(visitante);
        return visitante;
    }

    /**
     * Reporta un incidente. Solo se pide la descripcion; la fecha y hora
     * quedan registradas automaticamente dentro de Incidente.
     */
    public Incidente reportarIncidente(String descripcion) {
        Incidente incidente = new Incidente(descripcion);
        this.incidentes.add(incidente);
        return incidente;
    }

    public void mostrarPropietarios() {
        if (this.propietarios.isEmpty()) {
            System.out.println("No existen propietarios registrados.");
            return;
        }
        for (Propietario p : this.propietarios) {
            p.mostrarInformacion();
        }
    }

    public void mostrarVisitantes() {
        if (this.visitantes.isEmpty()) {
            System.out.println("No existen visitantes registrados.");
            return;
        }
        for (Visitante v : this.visitantes) {
            v.mostrarInformacion();
        }
    }

    public void mostrarIncidentes() {
        if (this.incidentes.isEmpty()) {
            System.out.println("No existen incidentes reportados.");
            return;
        }
        for (Incidente i : this.incidentes) {
            i.mostrarInformacion();
        }
    }

}
