package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.modelo.UsuarioFijo;
import java.sql.SQLException;
import java.util.List;

public interface UsuarioFijoDAO {

    // Insertar un usuario fijo con su ubicacion y su parqueadero fijo (todo obligatorio)
    boolean insertar(int idEscenario, UsuarioFijo u) throws SQLException, ControlAccesoException;

    // Modificar los datos de un usuario ACTIVO (nombre, cedula, placa, ubicacion, zona y parqueadero)
    boolean actualizar(int idEscenario, UsuarioFijo u) throws SQLException, ControlAccesoException;

    // Listar los usuarios fijos de un escenario (opcionalmente tambien los dados de baja)
    List<UsuarioFijo> listar(int idEscenario, boolean incluirInactivos) throws SQLException;

    // Buscar por placa ya normalizada entre los ACTIVOS (null si no existe)
    UsuarioFijo buscarPorPlaca(int idEscenario, String placa) throws SQLException;

    // Buscar por placa un usuario dado de baja (null si no hay): sirve para negarle el acceso
    UsuarioFijo buscarInactivoPorPlaca(int idEscenario, String placa) throws SQLException;

    // Baja logica: el usuario NO se borra, cambia a INACTIVO (si estaba DENTRO se anota su salida)
    boolean darDeBaja(int idEscenario, int idUsuarioFijo) throws SQLException;

    // Vuelve a dejar ACTIVO (y FUERA) a un usuario dado de baja
    boolean reactivar(int idUsuarioFijo) throws SQLException;
}
