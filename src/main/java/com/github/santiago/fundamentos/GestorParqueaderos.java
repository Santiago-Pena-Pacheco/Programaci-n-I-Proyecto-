package com.github.santiago.fundamentos;

import java.util.ArrayList;

/**
 * Administra el pool de parqueaderos disponibles para visitantes.
 * Se extrajo de SistemaControlAcceso para que las clases Persona puedan
 * pedir un parqueadero de forma polimorfica (cada una decide como)
 * sin depender directamente de la clase de logica de negocio completa.
 *
 * @author salas
 */
public class GestorParqueaderos {

    private ArrayList<Parqueadero> poolVisitantes;

    public GestorParqueaderos(int cantidadParqueaderosVisitantes) {
        this.poolVisitantes = new ArrayList<>();
        for (int i = 1; i <= cantidadParqueaderosVisitantes; i++) {
            this.poolVisitantes.add(new Parqueadero(i));
        }
    }

    public Parqueadero buscarParqueaderoVisitanteDisponible() {
        for (Parqueadero p : poolVisitantes) {
            if (p.estaDisponible()) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Parqueadero> getPoolVisitantes() {
        return poolVisitantes;
    }
}
