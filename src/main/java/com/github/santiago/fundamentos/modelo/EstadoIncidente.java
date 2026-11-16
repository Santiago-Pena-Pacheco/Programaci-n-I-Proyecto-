package com.github.santiago.fundamentos.modelo;

/** Seguimiento del reporte: lo cambia el administrador. */
public enum EstadoIncidente {
    ABIERTO("Abierto"),
    EN_REVISION("En revisión"),
    CERRADO("Cerrado");

    private final String etiqueta;

    EstadoIncidente(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
