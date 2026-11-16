package com.github.santiago.fundamentos.logica;

import com.github.santiago.fundamentos.excepciones.DatoInvalidoException;
import com.github.santiago.fundamentos.modelo.GravedadIncidente;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.TipoIncidente;
import com.github.santiago.fundamentos.modelo.UsuarioSistema;

/**
 * BUILDER (patron creacional): arma un Incidente dato por dato, en el mismo
 * orden en que el vigilante lo llena en el formulario, y valida todo al
 * final con build(). Evita el constructor de nueve parametros del Incidente y
 * garantiza que nunca se cree un reporte incompleto.
 *
 * Uso:
 *   Incidente i = new IncidenteBuilder(vigilante)
 *           .tipo(TipoIncidente.DANO_A_VEHICULO).gravedad(GravedadIncidente.MEDIA)
 *           .lugar("Parqueadero 4").placa("ABC123").persona("N/A")
 *           .descripcion("...").acciones("...").build();
 *
 * @author salas
 */
public class IncidenteBuilder {

    private final UsuarioSistema vigilante;
    private TipoIncidente tipo;
    private GravedadIncidente gravedad;
    private String lugar;
    private String placa;
    private String persona;
    private String descripcion;
    private String acciones;

    public IncidenteBuilder(UsuarioSistema vigilante) {
        this.vigilante = vigilante;
    }

    public IncidenteBuilder tipo(TipoIncidente tipo) {
        this.tipo = tipo;
        return this;
    }

    public IncidenteBuilder gravedad(GravedadIncidente gravedad) {
        this.gravedad = gravedad;
        return this;
    }

    public IncidenteBuilder lugar(String lugar) {
        this.lugar = lugar;
        return this;
    }

    public IncidenteBuilder placa(String placa) {
        this.placa = placa;
        return this;
    }

    public IncidenteBuilder persona(String persona) {
        this.persona = persona;
        return this;
    }

    public IncidenteBuilder descripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public IncidenteBuilder acciones(String acciones) {
        this.acciones = acciones;
        return this;
    }

    /** Valida cada dato y crea el Incidente; lanza DatoInvalidoException si falta o esta mal alguno. */
    public Incidente build() throws DatoInvalidoException {
        if (vigilante == null || tipo == null || gravedad == null) {
            throw new DatoInvalidoException("Faltan el tipo, la gravedad o el vigilante que reporta.");
        }
        return new Incidente(vigilante.getIdUsuarioSistema(), vigilante.getNombre(), tipo, gravedad,
                ReglasIncidente.lugar(lugar), ReglasIncidente.placa(placa), ReglasIncidente.persona(persona),
                ReglasIncidente.descripcion(descripcion), ReglasIncidente.acciones(acciones));
    }
}
