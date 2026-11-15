package com.github.santiago.fundamentos.modelo;

/**
 * Define los contextos de uso soportados por el software y, para cada uno,
 * el "vocabulario" que debe verse en toda la interfaz: como se llama el
 * usuario fijo (propietario, empleado, personal...), como se llama el
 * visitante, y como se llaman la ubicacion y la zona.
 *
 * El Administrador elige UNO de estos contextos al iniciar el sistema (o
 * lo cambia despues desde su panel) y, a partir de ahi, TODOS los menus,
 * mensajes y formularios se adaptan automaticamente: no hay que tocar
 * ninguna clase del dominio (Persona, UsuarioFijo, Visitante, etc.) para
 * que el mismo software sirva para un conjunto residencial, un centro
 * comercial, una oficina corporativa o un concierto/evento.
 *
 * @author salas
 */
public enum TipoContexto {

    RESIDENCIAL(
            "Conjunto Residencial",
            "Propietario", "Propietarios",
            "Visitante", "Visitantes",
            "Apartamento", "Torre"),

    CENTRO_COMERCIAL(
            "Centro Comercial",
            "Local Comercial", "Locales Comerciales",
            "Cliente", "Clientes",
            "Local", "Piso"),

    OFICINA_CORPORATIVA(
            "Oficina Corporativa",
            "Empleado", "Empleados",
            "Visitante", "Visitantes",
            "Oficina", "Sector"),

    CONCIERTO_EVENTO(
            "Concierto / Evento",
            "Personal del Evento", "Personal del Evento",
            "Asistente", "Asistentes",
            "Zona", "Escenario");

    private final String nombreContexto;
    private final String etiquetaUsuarioFijo;
    private final String etiquetaUsuarioFijoPlural;
    private final String etiquetaVisitante;
    private final String etiquetaVisitantePlural;
    private final String etiquetaUbicacion;
    private final String etiquetaZona;

    TipoContexto(String nombreContexto,
            String etiquetaUsuarioFijo, String etiquetaUsuarioFijoPlural,
            String etiquetaVisitante, String etiquetaVisitantePlural,
            String etiquetaUbicacion, String etiquetaZona) {
        this.nombreContexto = nombreContexto;
        this.etiquetaUsuarioFijo = etiquetaUsuarioFijo;
        this.etiquetaUsuarioFijoPlural = etiquetaUsuarioFijoPlural;
        this.etiquetaVisitante = etiquetaVisitante;
        this.etiquetaVisitantePlural = etiquetaVisitantePlural;
        this.etiquetaUbicacion = etiquetaUbicacion;
        this.etiquetaZona = etiquetaZona;
    }

    public String getNombreContexto() {
        return nombreContexto;
    }

    public String getEtiquetaUsuarioFijo() {
        return etiquetaUsuarioFijo;
    }

    public String getEtiquetaUsuarioFijoPlural() {
        return etiquetaUsuarioFijoPlural;
    }

    public String getEtiquetaVisitante() {
        return etiquetaVisitante;
    }

    public String getEtiquetaVisitantePlural() {
        return etiquetaVisitantePlural;
    }

    public String getEtiquetaUbicacion() {
        return etiquetaUbicacion;
    }

    public String getEtiquetaZona() {
        return etiquetaZona;
    }

    /**
     * Linea descriptiva usada en el menu de seleccion de contexto, ej:
     * "Conjunto Residencial  (Propietarios + Visitantes)"
     */
    public String getDescripcionMenu() {
        return nombreContexto + "  (" + etiquetaUsuarioFijoPlural + " + " + etiquetaVisitantePlural + ")";
    }

    @Override
    public String toString() {
        return nombreContexto;
    }
}
