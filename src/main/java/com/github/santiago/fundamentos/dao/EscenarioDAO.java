package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.modelo.Escenario;
import java.sql.SQLException;
import java.util.List;

public interface EscenarioDAO {

    // Listar todos los escenarios disponibles
    List<Escenario> listar() throws SQLException;

    // Buscar un escenario por su llave primaria (null si no existe)
    Escenario buscarPorId(int id) throws SQLException;

    // Crear un escenario nuevo junto con su pool de parqueaderos de visitantes
    boolean insertar(Escenario e) throws SQLException;

    // Escenario con el que trabaja el vigilante (null si no hay ninguno configurado)
    Escenario obtenerActivo() throws SQLException;

    // El administrador elige el escenario activo
    void establecerActivo(int idEscenario) throws SQLException;

    // Modificar nombre, etiquetas y cantidad de parqueaderos de visitantes de un escenario
    boolean actualizar(Escenario e) throws SQLException, ControlAccesoException;
}
