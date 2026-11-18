package com.github.santiago.fundamentos.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Un vehiculo que esta DENTRO ahora mismo (de un usuario fijo o de un visitante), con la hora en que entro y cuanto lleva adentro */
public class VehiculoAdentro {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TipoPersona tipoPersona;
    private final String nombre;
    private final String placa;
    private final String destino;
    private final int numeroParqueadero;
    private final LocalDateTime desde;
    private final long minutos;

    public VehiculoAdentro(TipoPersona tipoPersona, String nombre, String placa, String destino,
            int numeroParqueadero, LocalDateTime desde, long minutos) {
        this.tipoPersona = tipoPersona;
        this.nombre = nombre;
        this.placa = placa;
        this.destino = destino;
        this.numeroParqueadero = numeroParqueadero;
        this.desde = desde;
        this.minutos = Math.max(0, minutos);
    }

    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPlaca() {
        return placa;
    }

    public String getDestino() {
        return destino;
    }

    public int getNumeroParqueadero() {
        return numeroParqueadero;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public String getDesdeTexto() {
        return desde.format(FORMATO);
    }

    public long getMinutos() {
        return minutos;
    }

    /** Tiempo adentro en texto corto: "35 min", "2 h 10 min", "1 d 3 h". */
    public String getTiempoTexto() {
        if (minutos < 60) {
            return minutos + " min";
        }
        long horas = minutos / 60;
        if (horas < 24) {
            return horas + " h " + (minutos % 60) + " min";
        }
        return (horas / 24) + " d " + (horas % 24) + " h";
    }
}
