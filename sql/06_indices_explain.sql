-- 06_indices_explain.sql: EXPLAIN de las consultas que usan los índices creados en 01_inicializacion.sql.
-- Ejecutar después de 01_inicializacion.sql. En la columna "key" de cada resultado debe aparecer
-- el índice indicado; "rows" muestra cuántas filas revisa MySQL.

USE control_acceso;

-- Índices creados: ix_movimiento_escenario_fecha, ix_movimiento_placa, ix_movimiento_parqueadero,
-- ix_visitante_parqueadero, ix_usuario_fijo_presencia, ix_incidente_escenario_fecha,
-- ix_incidente_estado, ix_incidente_placa, ix_incidente_usuario.
SHOW INDEX FROM movimiento;

-- ¿Qué ingresos y salidas tuvo una placa? (key = ix_movimiento_placa)
EXPLAIN
SELECT fecha_hora, tipo
FROM movimiento
WHERE id_escenario = 1 AND placa = 'KJH451'
ORDER BY fecha_hora DESC;

-- ¿Qué movimientos hubo en las últimas 24 horas? (key = ix_movimiento_escenario_fecha)
-- El rango compara la columna directamente: no se usa DATE(fecha_hora) ni YEAR(fecha_hora).
EXPLAIN
SELECT placa, tipo, fecha_hora
FROM movimiento
WHERE id_escenario = 1 AND fecha_hora >= NOW() - INTERVAL 1 DAY;

-- ¿Qué propietarios activos están DENTRO ahora? (key = ix_usuario_fijo_presencia)
EXPLAIN
SELECT nombre, placa
FROM usuario_fijo
WHERE id_escenario = 1 AND presencia = 'DENTRO' AND estado = 'ACTIVO';

-- ¿Qué parqueaderos de visitantes están ocupados? (key = ix_visitante_parqueadero)
EXPLAIN
SELECT p.numero, v.nombre
FROM parqueadero p
INNER JOIN visitante v ON v.id_parqueadero = p.id_parqueadero
WHERE p.id_escenario = 1 AND p.tipo = 'VISITANTE' AND v.presencia = 'DENTRO';

-- ¿Cuántos incidentes sin cerrar hay por gravedad? (key = ix_incidente_estado)
EXPLAIN
SELECT gravedad, COUNT(*) AS incidentes
FROM incidente
WHERE id_escenario = 1 AND estado IN ('ABIERTO', 'EN_REVISION')
GROUP BY gravedad;

-- ¿Qué incidentes reportó cada usuario del sistema? (key = ix_incidente_usuario)
EXPLAIN
SELECT i.id_incidente, i.tipo, i.fecha_hora
FROM usuario_sistema u
INNER JOIN incidente i ON i.id_usuario_sistema = u.id_usuario_sistema
WHERE u.usuario = 'vigilante';
