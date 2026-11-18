package com.github.santiago.fundamentos.modelo;

import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import java.util.regex.Pattern;

/** Representa un vehiculo, ya sea de un usuario fijo o de un visitante */
public class Vehiculo {

    // Colombia: carro ABC123, moto ABC12D
    private static final Pattern PATRON_PLACA = Pattern.compile("[A-Z]{3}[0-9]{2}[0-9A-Z]");

    private String placa;

    public Vehiculo(String placa) throws DatoInvalidoException {
        setPlaca(placa);
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) throws DatoInvalidoException {
        this.placa = validarPlaca(placa);
    }

    public static String normalizarPlaca(String placa) {
        return placa == null ? null : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public static boolean esPlacaValida(String placa) {
        String normalizada = normalizarPlaca(placa);
        return normalizada != null && PATRON_PLACA.matcher(normalizada).matches();
    }

    /** Devuelve la placa normalizada o lanza DatoInvalidoException con el mensaje para el usuario. */
    public static String validarPlaca(String placa) throws DatoInvalidoException {
        if (!esPlacaValida(placa)) {
            throw new DatoInvalidoException(
                    "La placa no es válida. Use 3 letras y 3 números (ABC123) o 3 letras, 2 números y 1 letra (ABC12D).");
        }
        return normalizarPlaca(placa);
    }

    @Override
    public String toString() {
        return placa;
    }
}
