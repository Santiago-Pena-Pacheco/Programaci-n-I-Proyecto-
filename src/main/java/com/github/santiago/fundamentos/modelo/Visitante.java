package com.github.santiago.fundamentos.modelo;

import com.github.santiago.fundamentos.logica.GestorParqueaderos;

/**
 * Representa a un visitante externo (temporal). Segun el TipoContexto
 * activo del sistema, esta MISMA clase se muestra en pantalla como
 * "Visitante" (residencial/oficina), "Cliente" (centro comercial) o
 * "Asistente" (concierto/evento).
 *
 * Su placa no esta registrada por ninguna ubicacion fija; se dirige a un
 * destino y el sistema le asigna automaticamente un parqueadero del pool
 * de visitantes.
 *
 * @author salas
 */
public class Visitante extends Persona {

    private String destino;
    private Parqueadero parqueaderoAsignado;

    public Visitante(String nombre, String cedula, Vehiculo vehiculo, String destino) {
        super(nombre, cedula, vehiculo);
        this.destino = destino;
        this.parqueaderoAsignado = null;
    }

    public String getDestino() {
        return destino;
    }

    /**
     * A diferencia del usuario fijo, el visitante SI compite por un
     * parqueadero libre del pool administrado por el gestor.
     */
    @Override
    public boolean asignarParqueadero(GestorParqueaderos gestor) {
        Parqueadero disponible = gestor.buscarParqueaderoVisitanteDisponible();
        if (disponible == null) {
            return false;
        }
        disponible.asignarVehiculo(vehiculo.getPlaca());
        this.parqueaderoAsignado = disponible;
        return true;
    }

    @Override
    public void liberarParqueadero() {
        if (parqueaderoAsignado != null) {
            parqueaderoAsignado.liberar();
            parqueaderoAsignado = null;
        }
    }

    @Override
    public Parqueadero getParqueaderoAsignado() {
        return parqueaderoAsignado;
    }

    @Override
    public String getEtiquetaTipo(TipoContexto contexto) {
        return contexto.getEtiquetaVisitante();
    }

    @Override
    public void mostrarInformacion(TipoContexto contexto) {
        super.mostrarInformacion(contexto);
        System.out.println("destino = " + destino);
        System.out.println("parqueadero asignado = "
                + (parqueaderoAsignado == null ? "NO HAY DISPONIBLE" : "#" + parqueaderoAsignado.getNumero()));
    }
}
