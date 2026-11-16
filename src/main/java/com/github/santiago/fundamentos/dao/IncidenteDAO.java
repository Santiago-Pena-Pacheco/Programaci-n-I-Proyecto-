package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.modelo.Incidente;
import java.sql.SQLException;
import java.util.List;

public interface IncidenteDAO {

    // Registrar un incidente (la fecha y hora las pone la base de datos)
    boolean insertar(int idEscenario, Incidente i) throws SQLException;

    // Listar los incidentes de un escenario, los mas recientes primero
    List<Incidente> listar(int idEscenario) throws SQLException;

    // Modificar un incidente (administrador). No cambia la fecha ni quien lo reporto.
    boolean actualizar(int idEscenario, Incidente i) throws SQLException;
}
