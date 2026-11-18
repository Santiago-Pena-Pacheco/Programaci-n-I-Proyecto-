package com.github.santiago.fundamentos.dao;

import com.github.santiago.fundamentos.excepciones.ControlAccesoException;
import com.github.santiago.fundamentos.excepciones.ReglaNegocioException;
import com.github.santiago.fundamentos.excepciones.SinCupoException;
import com.github.santiago.fundamentos.modelo.Movimiento;
import com.github.santiago.fundamentos.modelo.Origen;
import com.github.santiago.fundamentos.modelo.Presencia;
import com.github.santiago.fundamentos.modelo.TipoMovimiento;
import com.github.santiago.fundamentos.modelo.VehiculoAdentro;
import com.github.santiago.fundamentos.modelo.Visitante;
import java.sql.SQLException;
import java.util.List;

/** Ingresos y salidas */
public interface AccesoDAO {

    /** Alterna la presencia de un usuario fijo ACTIVO (FUERA -> DENTRO es un ingreso, DENTRO -> FUERA es una salida) */
    TipoMovimiento alternarUsuarioFijo(int idEscenario, int idUsuarioFijo, int segundosEspera) throws SQLException, ControlAccesoException;

    /** Saca a un visitante que esta DENTRO (libera su parqueadero) */
    TipoMovimiento salidaVisitante(int idEscenario, int idVisitante, int segundosEspera) throws SQLException, ControlAccesoException;

    /** Ingreso de un visitante: si la placa ya existe como visitante (estaba FUERA) actualiza sus datos; si no, lo crea */
    Visitante ingresoVisitante(int idEscenario, Visitante v, Origen origen, String detalle) throws SQLException, ControlAccesoException;

    /** Correccion manual del administrador: fija la presencia de un usuario fijo. false si ya estaba asi. */
    boolean corregirPresenciaUsuarioFijo(int idEscenario, int idUsuarioFijo, Presencia nueva, String detalle)
            throws SQLException, ControlAccesoException;

    /** Correccion manual del administrador: fija la presencia de un visitante. false si ya estaba asi. */
    boolean corregirPresenciaVisitante(int idEscenario, int idVisitante, Presencia nueva, String detalle)
            throws SQLException, ControlAccesoException;

    /** true si esa placa tuvo un movimiento hace menos de segundos segundos. */
    boolean enTiempoDeEspera(int idEscenario, String placa, int segundos) throws SQLException;

    /** Los ultimos movimientos del escenario, los mas recientes primero. */
    List<Movimiento> listarMovimientos(int idEscenario, int limite) throws SQLException;

    /** Vehiculos que estan DENTRO ahora (usuarios fijos activos y visitantes), los que llevan mas tiempo primero. */
    List<VehiculoAdentro> listarVehiculosAdentro(int idEscenario) throws SQLException;
}
