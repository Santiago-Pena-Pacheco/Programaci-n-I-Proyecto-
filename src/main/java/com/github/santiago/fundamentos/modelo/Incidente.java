package com.github.santiago.fundamentos.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reporte de seguridad hecho por un vigilante. Ningun dato es opcional: si no
 * hay vehiculo o persona involucrada se escribe "N/A" (SIN_DATO). La fecha y
 * hora y el nombre de quien reporta los pone el sistema, no se digitan.
 *
 * @author salas
 */
public class Incidente {

    /** Valor de placa o persona cuando el incidente no involucra vehiculo o persona. */
    public static final String SIN_DATO = "N/A";
    public static final String SIN_REVISAR = "Sin revisar";

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private int idIncidente;
    private LocalDateTime fechaHora;
    private int idUsuarioSistema;
    private String reportadoPor;
    private TipoIncidente tipo;
    private GravedadIncidente gravedad;
    private String lugar;
    private String placa;
    private String personaInvolucrada;
    private String descripcion;
    private String accionesTomadas;
    private EstadoIncidente estado = EstadoIncidente.ABIERTO;
    private String observacionAdmin = SIN_REVISAR;
    private LocalDateTime fechaEstado;

    public Incidente(int idUsuarioSistema, String reportadoPor, TipoIncidente tipo, GravedadIncidente gravedad,
            String lugar, String placa, String personaInvolucrada, String descripcion, String accionesTomadas) {
        this.idUsuarioSistema = idUsuarioSistema;
        this.reportadoPor = reportadoPor;
        this.tipo = tipo;
        this.gravedad = gravedad;
        this.lugar = lugar;
        this.placa = placa;
        this.personaInvolucrada = personaInvolucrada;
        this.descripcion = descripcion;
        this.accionesTomadas = accionesTomadas;
        this.fechaHora = LocalDateTime.now();
        this.fechaEstado = this.fechaHora;
    }

    public int getIdIncidente() {
        return idIncidente;
    }

    public void setIdIncidente(int idIncidente) {
        this.idIncidente = idIncidente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getFechaHoraTexto() {
        return fechaHora.format(FORMATO);
    }

    public int getIdUsuarioSistema() {
        return idUsuarioSistema;
    }

    public String getReportadoPor() {
        return reportadoPor;
    }

    public TipoIncidente getTipo() {
        return tipo;
    }

    public void setTipo(TipoIncidente tipo) {
        this.tipo = tipo;
    }

    public GravedadIncidente getGravedad() {
        return gravedad;
    }

    public void setGravedad(GravedadIncidente gravedad) {
        this.gravedad = gravedad;
    }

    public String getLugar() {
        return lugar;
    }

    public void setLugar(String lugar) {
        this.lugar = lugar;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getPersonaInvolucrada() {
        return personaInvolucrada;
    }

    public void setPersonaInvolucrada(String personaInvolucrada) {
        this.personaInvolucrada = personaInvolucrada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getAccionesTomadas() {
        return accionesTomadas;
    }

    public void setAccionesTomadas(String accionesTomadas) {
        this.accionesTomadas = accionesTomadas;
    }

    public EstadoIncidente getEstado() {
        return estado;
    }

    public void setEstado(EstadoIncidente estado) {
        this.estado = estado;
    }

    public String getObservacionAdmin() {
        return observacionAdmin;
    }

    public void setObservacionAdmin(String observacionAdmin) {
        this.observacionAdmin = observacionAdmin;
    }

    public LocalDateTime getFechaEstado() {
        return fechaEstado;
    }

    public void setFechaEstado(LocalDateTime fechaEstado) {
        this.fechaEstado = fechaEstado;
    }

    /** Se usa al cargar un incidente que ya existe en la base de datos. */
    public static Incidente cargado(int id, LocalDateTime fechaHora, int idUsuarioSistema, String reportadoPor,
            TipoIncidente tipo, GravedadIncidente gravedad, String lugar, String placa, String persona,
            String descripcion, String acciones, EstadoIncidente estado, String observacion,
            LocalDateTime fechaEstado) {
        Incidente i = new Incidente(idUsuarioSistema, reportadoPor, tipo, gravedad, lugar, placa, persona,
                descripcion, acciones);
        i.idIncidente = id;
        i.fechaHora = fechaHora;
        i.estado = estado;
        i.observacionAdmin = observacion;
        i.fechaEstado = fechaEstado;
        return i;
    }

    /** Resumen de varias lineas, para el paso final del formulario y para revisar el reporte. */
    public String resumen() {
        return "Fecha y hora:      " + getFechaHoraTexto() + "\n"
                + "Reportado por:     " + reportadoPor + "\n"
                + "Tipo:              " + tipo + "\n"
                + "Gravedad:          " + gravedad + "\n"
                + "Lugar:             " + lugar + "\n"
                + "Vehículo (placa):  " + placa + "\n"
                + "Persona:           " + personaInvolucrada + "\n"
                + "Estado:            " + estado + "\n\n"
                + "Qué pasó:\n" + descripcion + "\n\n"
                + "Acciones tomadas:\n" + accionesTomadas + "\n\n"
                + "Observación del administrador:\n" + observacionAdmin;
    }
}
