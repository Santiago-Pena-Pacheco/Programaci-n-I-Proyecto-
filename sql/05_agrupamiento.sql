-- 05_agrupamiento.sql: COUNT, SUM, AVG, MIN y MAX con GROUP BY, WHERE y HAVING.
-- Ejecutar después de 01_inicializacion.sql. WHERE filtra las filas antes de agrupar;
-- HAVING solo filtra condiciones sobre los resultados agregados.

USE control_acceso;

-- COUNT
-- ¿Cuántos movimientos tuvo cada placa en los últimos 7 días? (solo las que tienen 2 o más)
SELECT placa, COUNT(*) AS total_movimientos
FROM movimiento
WHERE id_escenario = 1 AND fecha_hora >= NOW() - INTERVAL 7 DAY
GROUP BY placa
HAVING COUNT(*) >= 2
ORDER BY total_movimientos DESC, placa;

-- SUM
-- ¿Cuántos ingresos y cuántas salidas hay por tipo de persona? (solo grupos con algún ingreso)
SELECT tipo_persona,
       SUM(CASE WHEN tipo = 'INGRESO' THEN 1 ELSE 0 END) AS ingresos,
       SUM(CASE WHEN tipo = 'SALIDA' THEN 1 ELSE 0 END) AS salidas
FROM movimiento
WHERE id_escenario = 1
GROUP BY tipo_persona
HAVING SUM(CASE WHEN tipo = 'INGRESO' THEN 1 ELSE 0 END) > 0;

-- AVG
-- ¿Cuál es el promedio de movimientos por placa en cada tipo de persona?
SELECT t.tipo_persona, AVG(t.total) AS promedio_movimientos
FROM (
    SELECT tipo_persona, placa, COUNT(*) AS total
    FROM movimiento
    WHERE id_escenario = 1
    GROUP BY tipo_persona, placa
) t
GROUP BY t.tipo_persona
HAVING AVG(t.total) >= 1;

-- MIN y MAX
-- ¿Cuándo fue el primer y el último movimiento de cada visitante que se movió en los últimos 7 días?
SELECT placa, MIN(fecha_hora) AS primer_movimiento, MAX(fecha_hora) AS ultimo_movimiento
FROM movimiento
WHERE id_escenario = 1 AND tipo_persona = 'VISITANTE'
GROUP BY placa
HAVING MAX(fecha_hora) >= NOW() - INTERVAL 7 DAY
ORDER BY ultimo_movimiento DESC;

-- Las cinco funciones juntas
-- ¿Cuántos parqueaderos de visitantes tienen los escenarios, agrupados por el nombre de su zona?
SELECT etiqueta_zona,
       COUNT(*) AS escenarios,
       SUM(cantidad_parqueaderos_visitantes) AS total_parqueaderos,
       AVG(cantidad_parqueaderos_visitantes) AS promedio,
       MIN(cantidad_parqueaderos_visitantes) AS minimo,
       MAX(cantidad_parqueaderos_visitantes) AS maximo
FROM escenario
WHERE cantidad_parqueaderos_visitantes > 0
GROUP BY etiqueta_zona
HAVING SUM(cantidad_parqueaderos_visitantes) >= 10
ORDER BY total_parqueaderos DESC;

-- COUNT por gravedad
-- ¿Cuántos incidentes sin cerrar hay por gravedad? (solo gravedades con al menos uno)
SELECT gravedad, COUNT(*) AS incidentes
FROM incidente
WHERE id_escenario = 1 AND estado IN ('ABIERTO', 'EN_REVISION')
GROUP BY gravedad
HAVING COUNT(*) >= 1
ORDER BY incidentes DESC;
