package com.github.santiago.fundamentos.modelo;

import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import java.util.regex.Pattern;

/** Clase padre abstracta que agrupa los atributos y comportamientos comunes a cualquier persona que interactua con el sistema (UsuarioFijo o Visitante) */
public abstract class Persona {

    private static final Pattern CEDULA = Pattern.compile("[0-9]{5,12}");

    private int id;
    private String nombre;
    private String cedula;
    private Vehiculo vehiculo;
    private Presencia presencia = Presencia.FUERA;

    protected Persona(String nombre, String cedula, Vehiculo vehiculo) throws DatoInvalidoException {
        setNombre(nombre);
        setCedula(cedula);
        setVehiculo(vehiculo);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) throws DatoInvalidoException {
        this.nombre = validarNombre(nombre);
    }

    /** Devuelve el nombre limpio o lanza DatoInvalidoException (la usan los setters y la logica). */
    public static String validarNombre(String nombre) throws DatoInvalidoException {
        if (nombre == null || nombre.trim().length() < 3) {
            throw new DatoInvalidoException("El nombre es obligatorio (mínimo 3 caracteres).");
        }
        return nombre.trim();
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) throws DatoInvalidoException {
        this.cedula = validarCedula(cedula);
    }

    public static String validarCedula(String cedula) throws DatoInvalidoException {
        if (cedula == null || !CEDULA.matcher(cedula.trim()).matches()) {
            throw new DatoInvalidoException("La cédula es obligatoria y debe tener solo números (5 a 12 dígitos).");
        }
        return cedula.trim();
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) throws DatoInvalidoException {
        if (vehiculo == null) {
            throw new DatoInvalidoException("El vehículo es obligatorio.");
        }
        this.vehiculo = vehiculo;
    }

    public String getPlaca() {
        return vehiculo.getPlaca();
    }

    public Presencia getPresencia() {
        return presencia;
    }

    public void setPresencia(Presencia presencia) {
        if (presencia == null) {
            throw new IllegalArgumentException("La presencia no puede ser nula."); // error de programacion, no del usuario
        }
        this.presencia = presencia;
    }

    public boolean isDentro() {
        return presencia == Presencia.DENTRO;
    }

    /** Nombre del tipo de persona para titulos y mensajes ("Propietario", "Visitante"...). */
    public abstract String getTipoTexto(Escenario escenario);

    /** Metodo abstracto: cada subclase decide COMO describe a donde va y donde parquea */
    public abstract String getDetalleAcceso(Escenario escenario);

    @Override
    public String toString() {
        return nombre + " (CC " + cedula + ") - Placa: " + vehiculo.getPlaca();
    }
}
