package com.github.santiago.fundamentos.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa un incidente de seguridad reportado por el vigilante.
 * Solo se registra la descripcion; la fecha y hora se toman automaticamente.
 *
 * @author salas
 */
public class Incidente {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private String descripcion;
    private LocalDateTime fechaHora;

    public Incidente(String descripcion) {
        this.descripcion = descripcion;
        this.fechaHora = LocalDateTime.now();
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void mostrarInformacion() {
        System.out.println(" ====================== ");
        System.out.println("descripcion = " + descripcion);
        System.out.println("fecha y hora = " + fechaHora.format(FORMATO));
    }
}
