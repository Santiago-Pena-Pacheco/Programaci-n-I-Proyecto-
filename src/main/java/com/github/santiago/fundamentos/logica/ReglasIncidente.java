package com.github.santiago.fundamentos.logica;

import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import com.github.santiago.fundamentos.modelo.EstadoIncidente;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.Vehiculo;

/** Reglas de cada dato de un reporte de incidente */
public final class ReglasIncidente {

    private ReglasIncidente() {
    }

    public static String lugar(String texto) throws DatoInvalidoException {
        return largo(texto, 3, 80, "El lugar del incidente (mínimo 3 caracteres)");
    }

    /** Placa valida, o "N/A" cuando el incidente no involucra ningun vehiculo. */
    public static String placa(String texto) throws DatoInvalidoException {
        String t = texto == null ? "" : texto.trim();
        if (t.equalsIgnoreCase(Incidente.SIN_DATO)) {
            return Incidente.SIN_DATO;
        }
        String normalizada = Vehiculo.normalizarPlaca(t);
        if (!Vehiculo.esPlacaValida(t)) {
            throw new DatoInvalidoException("La placa del vehículo afectado no es válida (ABC123 o ABC12D). "
                    + "Si no hay vehículo, marque \"No involucra vehículo\".");
        }
        return normalizada;
    }

    /** Nombre de la persona involucrada, o "N/A" si no hay ninguna. */
    public static String persona(String texto) throws DatoInvalidoException {
        String t = texto == null ? "" : texto.trim();
        if (t.equalsIgnoreCase(Incidente.SIN_DATO)) {
            return Incidente.SIN_DATO;
        }
        return largo(t, 3, 80, "La persona involucrada (mínimo 3 caracteres, o marque \"No hay persona identificada\")");
    }

    public static String descripcion(String texto) throws DatoInvalidoException {
        return largo(texto, 10, 500, "La descripción de lo que pasó (mínimo 10 caracteres)");
    }

    public static String acciones(String texto) throws DatoInvalidoException {
        return largo(texto, 5, 300, "Las acciones tomadas (mínimo 5 caracteres)");
    }

    /** Observacion del administrador: obligatoria en cuanto el reporte deja de estar ABIERTO. */
    public static String observacion(String texto, EstadoIncidente estado) throws DatoInvalidoException {
        String t = texto == null ? "" : texto.trim();
        if (estado == EstadoIncidente.ABIERTO) {
            return t.isEmpty() ? Incidente.SIN_REVISAR : limitar(t, 300, "La observación");
        }
        if (t.isEmpty() || t.equalsIgnoreCase(Incidente.SIN_REVISAR)) {
            throw new DatoInvalidoException(
                    "Escriba la observación: qué se revisó o cómo se resolvió el incidente.");
        }
        return limitar(t, 300, "La observación");
    }

    private static String largo(String texto, int min, int max, String campo) throws DatoInvalidoException {
        String t = texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
        if (t.length() < min) {
            throw new DatoInvalidoException("Dato obligatorio: " + campo + ".");
        }
        return limitar(t, max, campo);
    }

    private static String limitar(String t, int max, String campo) throws DatoInvalidoException {
        if (t.length() > max) {
            throw new DatoInvalidoException(campo + " no puede pasar de " + max + " caracteres.");
        }
        return t;
    }
}
