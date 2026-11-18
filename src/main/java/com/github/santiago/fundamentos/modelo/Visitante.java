package com.github.santiago.fundamentos.modelo;

import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Representa a un visitante externo */
public class Visitante extends Persona {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String destino;
    private Parqueadero parqueaderoAsignado;
    private LocalDateTime ultimoCambio = LocalDateTime.now();

    public Visitante(String nombre, String cedula, Vehiculo vehiculo, String destino)
            throws DatoInvalidoException {
        super(nombre, cedula, vehiculo);
        setDestino(destino);
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) throws DatoInvalidoException {
        if (destino == null || destino.isBlank()) {
            throw new DatoInvalidoException("El destino del visitante es obligatorio.");
        }
        this.destino = destino.trim();
    }

    public Parqueadero getParqueaderoAsignado() {
        return parqueaderoAsignado;
    }

    public void setParqueaderoAsignado(Parqueadero parqueaderoAsignado) {
        this.parqueaderoAsignado = parqueaderoAsignado;
    }

    public LocalDateTime getUltimoCambio() {
        return ultimoCambio;
    }

    public void setUltimoCambio(LocalDateTime ultimoCambio) {
        this.ultimoCambio = ultimoCambio;
    }

    public String getUltimoCambioTexto() {
        return ultimoCambio == null ? "" : ultimoCambio.format(FORMATO);
    }

    @Override
    public String getTipoTexto(Escenario escenario) {
        return "Visitante";
    }

    /** Implementacion del visitante: a donde va y su parqueadero TEMPORAL. */
    @Override
    public String getDetalleAcceso(Escenario escenario) {
        return "Va a: " + escenario.getEtiquetaUbicacion() + " " + destino
                + "\nParqueadero de visitante #" + parqueaderoAsignado.getNumero();
    }
}
