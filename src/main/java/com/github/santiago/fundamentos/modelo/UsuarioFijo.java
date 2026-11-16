package com.github.santiago.fundamentos.modelo;

import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Persona con acceso permanente a un escenario: propietario en un conjunto,
 * empleado en un centro comercial u oficina, personal en un concierto. El
 * nombre concreto lo da la etiqueta del Escenario, no la clase. Hereda de
 * Persona los atributos comunes y anade su Ubicacion (con su Parqueadero
 * fijo), su estado (ACTIVO / INACTIVO: nunca se borra) y su presencia
 * (DENTRO / FUERA).
 *
 * @author salas
 */
public class UsuarioFijo extends Persona {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Ubicacion ubicacion;
    private boolean activo = true;
    private LocalDateTime fechaEstado = LocalDateTime.now(); // ultimo cambio de estado

    public UsuarioFijo(String nombre, String cedula, Vehiculo vehiculo, Ubicacion ubicacion)
            throws DatoInvalidoException {
        super(nombre, cedula, vehiculo);
        setUbicacion(ubicacion);
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) throws DatoInvalidoException {
        if (ubicacion == null || ubicacion.getParqueadero() == null) {
            throw new DatoInvalidoException("La ubicación y su parqueadero fijo son obligatorios.");
        }
        this.ubicacion = ubicacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaEstado() {
        return fechaEstado;
    }

    public void setFechaEstado(LocalDateTime fechaEstado) {
        this.fechaEstado = fechaEstado;
    }

    public String getEstadoTexto() {
        return activo ? "ACTIVO" : "INACTIVO";
    }

    public String getFechaEstadoTexto() {
        return fechaEstado == null ? "" : fechaEstado.format(FORMATO);
    }

    @Override
    public String getTipoTexto(Escenario escenario) {
        return escenario.getEtiquetaUsuarioFijo();
    }

    /** Implementacion del usuario fijo: su ubicacion y su parqueadero FIJO. */
    @Override
    public String getDetalleAcceso(Escenario escenario) {
        return ubicacion.descripcion(escenario)
                + "\nParqueadero fijo #" + ubicacion.getParqueadero().getNumero();
    }
}
