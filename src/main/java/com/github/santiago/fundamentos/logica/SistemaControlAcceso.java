package com.github.santiago.fundamentos.logica;

import com.github.santiago.fundamentos.gui.InterfazPrincipal;
import com.github.santiago.fundamentos.modelo.Incidente;
import com.github.santiago.fundamentos.modelo.Parqueadero;
import com.github.santiago.fundamentos.modelo.TipoContexto;
import com.github.santiago.fundamentos.modelo.UbicacionDestino;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;
import com.github.santiago.fundamentos.modelo.Vehiculo;
import com.github.santiago.fundamentos.modelo.Visitante;

import java.util.ArrayList;

/**
 * Centraliza la logica de negocio del sistema de control de acceso y
 * parqueaderos, aplicable a cualquier zona con parqueaderos (centro
 * comercial, conciertos, oficinas corporativas, conjuntos residenciales).
 *
 * El TipoContexto activo (elegido por el Administrador) determina el
 * vocabulario que se usa en toda la interfaz, pero NO cambia la logica
 * de negocio aqui abajo: sigue habiendo UsuarioFijo con parqueadero fijo
 * y Visitante que toma parqueadero del pool, sin importar el contexto.
 *
 * Administra los UsuarioFijo (registrados por el Administrador), los
 * Visitante (registrados por el Vigilante), el pool de parqueaderos de
 * visitantes (a traves de GestorParqueaderos) y los Incidente reportados.
 *
 * @author salas
 */
public class SistemaControlAcceso {

    private ArrayList<UsuarioFijo> usuariosFijos;
    private ArrayList<Visitante> visitantes;
    private ArrayList<Incidente> incidentes;
    private GestorParqueaderos gestorParqueaderos;
    private TipoContexto contextoActivo;

    // Cantidad de parqueaderos de visitantes que se crean al iniciar el sistema
    private static final int CANTIDAD_PARQUEADEROS_VISITANTES = 10;

    public SistemaControlAcceso() {
        this.usuariosFijos = new ArrayList<>();
        this.visitantes = new ArrayList<>();
        this.incidentes = new ArrayList<>();
        this.gestorParqueaderos = new GestorParqueaderos(CANTIDAD_PARQUEADEROS_VISITANTES);
        this.contextoActivo = null; // se exige configurarlo antes de operar (ver InterfazPrincipal)
    }

    // ==================== CONTEXTO (elegido por el Administrador) ====================

    public TipoContexto getContextoActivo() {
        return contextoActivo;
    }

    public void configurarContexto(TipoContexto contexto) {
        this.contextoActivo = contexto;
    }

    // ==================== VISTA ADMINISTRADOR ====================

    /**
     * Registra un nuevo usuario fijo (propietario/empleado/personal/local,
     * segun el contexto activo): crea su Vehiculo, el Parqueadero fijo de
     * su UbicacionDestino y los une. La asignacion del parqueadero se
     * resuelve de forma polimorfica dentro de UsuarioFijo.asignarParqueadero(...).
     */
    public UsuarioFijo registrarUsuarioFijo(String nombre, String cedula, String identificadorUbicacion,
            String zona, int numeroParqueadero, String placa) {

        Vehiculo vehiculo = new Vehiculo(placa);
        Parqueadero parqueaderoFijo = new Parqueadero(numeroParqueadero);
        UbicacionDestino ubicacion = new UbicacionDestino(identificadorUbicacion, zona, parqueaderoFijo);
        UsuarioFijo usuario = new UsuarioFijo(nombre, cedula, vehiculo, ubicacion);

        usuario.asignarParqueadero(gestorParqueaderos); // llamada polimorfica

        this.usuariosFijos.add(usuario);
        return usuario;
    }

    /**
     * Modifica el nombre y/o cedula de un usuario fijo ya registrado.
     * Pasar null o cadena vacia en un campo lo deja sin cambios.
     */
    public boolean modificarUsuarioFijo(String cedulaActual, String nuevoNombre, String nuevaCedula) {
        UsuarioFijo usuario = buscarUsuarioFijoPorCedula(cedulaActual);
        if (usuario == null) {
            return false;
        }
        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            usuario.setNombre(nuevoNombre);
        }
        if (nuevaCedula != null && !nuevaCedula.isBlank()) {
            usuario.setCedula(nuevaCedula);
        }
        return true;
    }

    /**
     * Elimina un usuario fijo y libera su parqueadero fijo asociado.
     */
    public boolean eliminarUsuarioFijo(String cedula) {
        UsuarioFijo usuario = buscarUsuarioFijoPorCedula(cedula);
        if (usuario == null) {
            return false;
        }
        usuario.liberarParqueadero();
        return this.usuariosFijos.remove(usuario);
    }

    public UsuarioFijo buscarUsuarioFijoPorCedula(String cedula) {
        for (UsuarioFijo u : usuariosFijos) {
            if (u.getCedula().equalsIgnoreCase(cedula)) {
                return u;
            }
        }
        return null;
    }

    public void mostrarUsuariosFijos() {
        if (this.usuariosFijos.isEmpty()) {
            System.out.println("No existen " + contextoActivo.getEtiquetaUsuarioFijoPlural().toLowerCase()
                    + " registrados.");
            return;
        }
        for (UsuarioFijo u : this.usuariosFijos) {
            u.mostrarInformacion(contextoActivo); // llamada polimorfica, adaptada al contexto
        }
    }

    // ==================== VISTA VIGILANTE ====================

    /**
     * Registra un visitante (o "cliente"/"asistente", segun el contexto) y
     * le asigna automaticamente un parqueadero disponible del pool (si hay).
     * La asignacion se resuelve de forma polimorfica dentro de
     * Visitante.asignarParqueadero(...).
     */
    public Visitante registrarVisitante(String placa, String nombre, String cedula, String destino) {
        Vehiculo vehiculo = new Vehiculo(placa);
        Visitante visitante = new Visitante(nombre, cedula, vehiculo, destino);

        visitante.asignarParqueadero(gestorParqueaderos); // llamada polimorfica

        this.visitantes.add(visitante);
        return visitante;
    }

    /**
     * Registra la salida de un visitante: libera su parqueadero y lo
     * remueve del registro activo.
     */
    public boolean registrarSalidaVisitante(String placa) {
        for (Visitante v : visitantes) {
            if (v.getVehiculo().getPlaca().equalsIgnoreCase(placa)) {
                v.liberarParqueadero();
                return this.visitantes.remove(v);
            }
        }
        return false;
    }

    public void mostrarVisitantes() {
        if (this.visitantes.isEmpty()) {
            System.out.println("No existen " + contextoActivo.getEtiquetaVisitantePlural().toLowerCase()
                    + " registrados.");
            return;
        }
        for (Visitante v : this.visitantes) {
            v.mostrarInformacion(contextoActivo); // llamada polimorfica, adaptada al contexto
        }
    }

    /**
     * Reporta un incidente. Solo se pide la descripcion; la fecha y hora
     * quedan registradas automaticamente dentro de Incidente.
     */
    public Incidente reportarIncidente(String descripcion) {
        Incidente incidente = new Incidente(descripcion);
        this.incidentes.add(incidente);
        return incidente;
    }

    public void mostrarIncidentes() {
        if (this.incidentes.isEmpty()) {
            System.out.println("No existen incidentes reportados.");
            return;
        }
        for (Incidente i : this.incidentes) {
            i.mostrarInformacion();
        }
    }

    public GestorParqueaderos getGestorParqueaderos() {
        return gestorParqueaderos;
    }
}
