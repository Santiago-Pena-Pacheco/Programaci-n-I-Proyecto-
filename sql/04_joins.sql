-- 04_joins.sql: los cuatro tipos de JOIN. Ejecutar después de 01_inicializacion.sql.

USE control_acceso;

-- INNER JOIN
-- ¿Qué propietarios activos tienen su apartamento y su parqueadero fijo asignados?
SELECT uf.nombre, uf.placa, ub.codigo AS apartamento, ub.zona AS torre, p.numero AS parqueadero
FROM usuario_fijo uf
INNER JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
INNER JOIN parqueadero p ON p.id_parqueadero = ub.id_parqueadero
WHERE uf.id_escenario = 1 AND uf.estado = 'ACTIVO'
ORDER BY ub.zona, ub.codigo;

-- LEFT JOIN
-- ¿Qué propietarios activos no han ingresado en las últimas 2 horas?
SELECT uf.nombre, uf.placa
FROM usuario_fijo uf
LEFT JOIN movimiento m ON m.id_escenario = uf.id_escenario AND m.placa = uf.placa
                      AND m.tipo = 'INGRESO' AND m.fecha_hora >= NOW() - INTERVAL 2 HOUR
WHERE uf.id_escenario = 1 AND uf.estado = 'ACTIVO' AND m.id_movimiento IS NULL
ORDER BY uf.nombre;

-- RIGHT JOIN
-- ¿Qué usuarios del sistema nunca han reportado un incidente?
SELECT u.usuario, u.rol
FROM incidente i
RIGHT JOIN usuario_sistema u ON u.id_usuario_sistema = i.id_usuario_sistema
WHERE i.id_incidente IS NULL
ORDER BY u.usuario;

-- FULL JOIN (en MySQL: LEFT JOIN + UNION + RIGHT JOIN)
-- ¿Cuál es el estado de todos los parqueaderos de visitantes y de todos los visitantes,
-- incluso los parqueaderos que nadie ha usado?
SELECT p.numero AS parqueadero, v.nombre AS visitante, v.presencia
FROM parqueadero p
LEFT JOIN visitante v ON v.id_parqueadero = p.id_parqueadero
WHERE p.id_escenario = 1 AND p.tipo = 'VISITANTE'
UNION
SELECT p.numero AS parqueadero, v.nombre AS visitante, v.presencia
FROM parqueadero p
RIGHT JOIN visitante v ON v.id_parqueadero = p.id_parqueadero
WHERE v.id_escenario = 1
ORDER BY parqueadero;

-- EXPLAIN de los JOIN: en la columna "key" deben aparecer los índices creados en 01_inicializacion.sql.
-- ¿El INNER JOIN de propietarios usa los índices de usuario_fijo y las llaves de ubicacion y parqueadero?
EXPLAIN
SELECT uf.nombre, uf.placa, ub.codigo AS apartamento, ub.zona AS torre, p.numero AS parqueadero
FROM usuario_fijo uf
INNER JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
INNER JOIN parqueadero p ON p.id_parqueadero = ub.id_parqueadero
WHERE uf.id_escenario = 1 AND uf.estado = 'ACTIVO'
ORDER BY ub.zona, ub.codigo;

-- ¿El LEFT JOIN con movimiento usa el índice ix_movimiento_placa (escenario, placa, fecha)?
EXPLAIN
SELECT uf.nombre, uf.placa
FROM usuario_fijo uf
LEFT JOIN movimiento m ON m.id_escenario = uf.id_escenario AND m.placa = uf.placa
                      AND m.tipo = 'INGRESO' AND m.fecha_hora >= NOW() - INTERVAL 2 HOUR
WHERE uf.id_escenario = 1 AND uf.estado = 'ACTIVO' AND m.id_movimiento IS NULL
ORDER BY uf.nombre;
