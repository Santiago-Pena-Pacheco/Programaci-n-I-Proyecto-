package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.modelo.Visitante;
import java.sql.SQLException;
import java.util.List;

/**
 * Consultas y edicion de visitantes. Sus ingresos y salidas (que cambian la
 * presencia) los hace AccesoDAO.
 */
public interface VisitanteDAO {

    // Listar los visitantes guardados de un escenario, los de movimiento mas reciente primero
    List<Visitante> listar(int idEscenario) throws SQLException;

    // Buscar un visitante guardado por su placa ya normalizada (null si nunca ha venido)
    Visitante buscarPorPlaca(int idEscenario, String placa) throws SQLException;

    // Modificar nombre, cedula, placa y destino (no cambia presencia ni parqueadero)
    boolean actualizar(int idEscenario, Visitante v) throws SQLException;

    // Cuantos parqueaderos de visitantes estan libres (ningun visitante DENTRO los usa)
    int contarParqueaderosDisponibles(int idEscenario) throws SQLException;
}
