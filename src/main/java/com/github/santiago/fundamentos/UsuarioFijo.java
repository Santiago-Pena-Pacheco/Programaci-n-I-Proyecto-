package com.github.santiago.fundamentos;

/**
 * Representa a una persona con vinculo permanente en la zona controlada.
 * Segun el TipoContexto activo del sistema, esta MISMA clase se muestra
 * en pantalla como "Propietario" (residencial), "Empleado" (oficina),
 * "Local Comercial" (centro comercial) o "Personal del Evento" (concierto)
 * — la clase no cambia, solo cambia la etiqueta que consulta del contexto.
 *
 * No requiere registro constante: se registra una vez (Vista Administrador)
 * y su parqueadero queda fijo, asociado a su UbicacionDestino.
 *
 * @author salas
 */
public class UsuarioFijo extends Persona {

    private UbicacionDestino ubicacion;

    public UsuarioFijo(String nombre, String cedula, Vehiculo vehiculo, UbicacionDestino ubicacion) {
        super(nombre, cedula, vehiculo);
        this.ubicacion = ubicacion;
    }

    public UbicacionDestino getUbicacion() {
        return ubicacion;
    }

    /**
     * Un usuario fijo no "compite" por un parqueadero del pool: ocupa el
     * parqueadero que ya viene asociado a su ubicacion con la placa de
     * su vehiculo.
     */
    @Override
    public boolean asignarParqueadero(GestorParqueaderos gestor) {
        Parqueadero fijo = ubicacion.getParqueaderoFijo();
        if (fijo == null) {
            return false;
        }
        return fijo.asignarVehiculo(vehiculo.getPlaca());
    }

    @Override
    public void liberarParqueadero() {
        if (ubicacion.getParqueaderoFijo() != null) {
            ubicacion.getParqueaderoFijo().liberar();
        }
    }

    @Override
    public Parqueadero getParqueaderoAsignado() {
        return ubicacion.getParqueaderoFijo();
    }

    @Override
    public String getEtiquetaTipo(TipoContexto contexto) {
        return contexto.getEtiquetaUsuarioFijo();
    }

    @Override
    public void mostrarInformacion(TipoContexto contexto) {
        super.mostrarInformacion(contexto);
        Parqueadero fijo = ubicacion.getParqueaderoFijo();
        System.out.println(contexto.getEtiquetaUbicacion() + " = " + ubicacion.getIdentificador());
        System.out.println(contexto.getEtiquetaZona() + " = " + ubicacion.getZona());
        System.out.println("parqueadero fijo = " + (fijo != null ? "#" + fijo.getNumero() : "SIN ASIGNAR"));
    }
}
