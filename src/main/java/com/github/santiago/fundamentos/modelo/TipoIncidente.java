package com.github.santiago.fundamentos.modelo;

/** Clase de suceso que se reporta. La etiqueta es el texto que ve el usuario. */
public enum TipoIncidente {
    ACCESO_NO_AUTORIZADO("Acceso no autorizado"),
    ROBO_O_HURTO("Robo o hurto"),
    DANO_A_VEHICULO("Daño a vehículo"),
    VEHICULO_MAL_PARQUEADO("Vehículo mal parqueado"),
    DISTURBIO("Disturbio o riña"),
    PERSONA_SOSPECHOSA("Persona sospechosa"),
    EMERGENCIA("Emergencia (incendio, accidente, salud)"),
    OTRO("Otro");

    private final String etiqueta;

    TipoIncidente(String etiqueta) {
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
