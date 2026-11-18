-- 03_transacciones.sql: SELECT, INSERT, UPDATE y DELETE dentro de START TRANSACTION,
-- cerrados con COMMIT o ROLLBACK. Ejecutar después de 02_datos_ejemplo.sql.
-- Caso de prueba: el visitante Mario Test (placa QWE123) en el escenario 1.

USE control_acceso;

-- T1. Ingreso de un visitante nuevo (COMMIT).
START TRANSACTION;

-- ¿Qué parqueadero de visitantes está libre para asignarle?
SELECT p.id_parqueadero, p.numero
FROM parqueadero p
LEFT JOIN visitante v ON v.id_parqueadero = p.id_parqueadero AND v.presencia = 'DENTRO'
WHERE p.id_escenario = 1 AND p.tipo = 'VISITANTE' AND v.id_visitante IS NULL
ORDER BY p.numero
LIMIT 1
FOR UPDATE;

-- ¿Cómo se registra al visitante en el primer parqueadero libre?
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia)
SELECT 1, 'Mario Test', '1003000001', 'QWE123', '101', p.id_parqueadero, 'DENTRO'
FROM parqueadero p
LEFT JOIN visitante v ON v.id_parqueadero = p.id_parqueadero AND v.presencia = 'DENTRO'
WHERE p.id_escenario = 1 AND p.tipo = 'VISITANTE' AND v.id_visitante IS NULL
ORDER BY p.numero
LIMIT 1;

-- ¿Cómo queda el ingreso en el historial?
INSERT INTO movimiento (id_escenario, tipo, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT v.id_escenario, 'INGRESO', v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero,
       'CAMARA', 'Ingreso de visitante'
FROM visitante v
WHERE v.id_escenario = 1 AND v.placa = 'QWE123';

COMMIT;

-- T2. Salida del visitante (COMMIT).
START TRANSACTION;

-- ¿El visitante está DENTRO y puede salir?
SELECT v.id_visitante, v.presencia
FROM visitante v
WHERE v.id_escenario = 1 AND v.placa = 'QWE123'
FOR UPDATE;

-- ¿Cómo se marca su salida y se libera el parqueadero?
UPDATE visitante
SET presencia = 'FUERA', ultimo_cambio = CURRENT_TIMESTAMP
WHERE id_escenario = 1 AND placa = 'QWE123' AND presencia = 'DENTRO';

INSERT INTO movimiento (id_escenario, tipo, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT v.id_escenario, 'SALIDA', v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero,
       'CAMARA', 'Salida de visitante'
FROM visitante v
WHERE v.id_escenario = 1 AND v.placa = 'QWE123';

COMMIT;

-- T3. Eliminar el visitante de prueba y deshacer el cambio (ROLLBACK).
START TRANSACTION;

-- ¿Qué visitantes ya salieron y se pueden eliminar?
SELECT v.id_visitante, v.nombre
FROM visitante v
WHERE v.id_escenario = 1 AND v.placa = 'QWE123' AND v.presencia = 'FUERA'
FOR UPDATE;

DELETE FROM visitante
WHERE id_escenario = 1 AND placa = 'QWE123' AND presencia = 'FUERA';

-- ¿El visitante aún existe después del DELETE? (dentro de la transacción: no)
SELECT COUNT(*) AS visitantes_con_esa_placa
FROM visitante
WHERE id_escenario = 1 AND placa = 'QWE123';

ROLLBACK;

-- ¿Después del ROLLBACK el visitante sigue en la base? (sí)
SELECT COUNT(*) AS visitantes_con_esa_placa
FROM visitante
WHERE id_escenario = 1 AND placa = 'QWE123';

-- T4. Eliminar el visitante de prueba y confirmar (COMMIT).
START TRANSACTION;

DELETE FROM visitante
WHERE id_escenario = 1 AND placa = 'QWE123' AND presencia = 'FUERA';

COMMIT;

-- ¿El visitante ya no está y su historial sigue en movimiento? (0 visitantes, 2 movimientos)
SELECT
    (SELECT COUNT(*) FROM visitante WHERE id_escenario = 1 AND placa = 'QWE123') AS visitantes,
    (SELECT COUNT(*) FROM movimiento WHERE id_escenario = 1 AND placa = 'QWE123') AS movimientos;
