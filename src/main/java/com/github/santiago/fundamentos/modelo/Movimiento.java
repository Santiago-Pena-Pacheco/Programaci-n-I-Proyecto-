package com.github.santiago.fundamentos.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Una fila del historial de ingresos y salidas. Guarda copia del nombre y la
 * placa del momento, de modo que el historial no cambia si luego se edita a
 * la persona.
 *
 * @author salas
 */
public class Movimiento {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final long idMovimiento;
    private final TipoMovimiento tipo;
    private final LocalDateTime fechaHora;
    private final String placa;
    private final TipoPersona tipoPersona;
    private final String nombre;
    private final int numeroParqueadero;
    private final Origen origen;
    private final String detalle;

    public Movimiento(long idMovimiento, TipoMovimiento tipo, LocalDateTime fechaHora, String placa,
            TipoPersona tipoPersona, String nombre, int numeroParqueadero, Origen origen, String detalle) {
        this.idMovimiento = idMovimiento;
        this.tipo = tipo;
        this.fechaHora = fechaHora;
        this.placa = placa;
        this.tipoPersona = tipoPersona;
        this.nombre = nombre;
        this.numeroParqueadero = numeroParqueadero;
        this.origen = origen;
        this.detalle = detalle;
    }

    public long getIdMovimiento() {
        return idMovimiento;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getFechaHoraTexto() {
        return fechaHora.format(FORMATO);
    }

    public String getPlaca() {
        return placa;
    }

    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNumeroParqueadero() {
        return numeroParqueadero;
    }

    public Origen getOrigen() {
        return origen;
    }

    public String getDetalle() {
        return detalle;
    }
}
