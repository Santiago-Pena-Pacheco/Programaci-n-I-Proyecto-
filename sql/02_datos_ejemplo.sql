-- 02_datos_ejemplo.sql: datos inventados para el escenario 1 (conjunto residencial).
-- Ejecutar una sola vez, despues de 01_estructura.sql.

SET NAMES utf8mb4;
USE control_acceso;

-- Propietarios: parqueadero fijo, ubicacion (apartamento y torre) y usuario fijo

-- Carlos Andrés Rodríguez - apto 101, torre 1, placa KJH451
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 1, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '101', '1', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Carlos Andrés Rodríguez', '1001000101', 'KJH451', LAST_INSERT_ID(), 'DENTRO');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 20 MINUTE, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000101';

-- María Fernanda Gómez - apto 102, torre 1, placa TRM238
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 2, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '102', '1', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'María Fernanda Gómez', '1001000102', 'TRM238', LAST_INSERT_ID(), 'FUERA');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 6 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000102';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 4 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000102';

-- Juan Sebastián Pérez - apto 201, torre 1, placa BNQ764
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 3, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '201', '1', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Juan Sebastián Pérez', '1001000103', 'BNQ764', LAST_INSERT_ID(), 'DENTRO');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 90 MINUTE, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000103';

-- Laura Valentina Torres - apto 202, torre 1, placa GFD912
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 4, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '202', '1', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Laura Valentina Torres', '1001000104', 'GFD912', LAST_INSERT_ID(), 'FUERA');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 6 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000104';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 4 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000104';

-- Andrés Felipe Martínez - apto 301, torre 2, placa WXY305
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 5, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '301', '2', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Andrés Felipe Martínez', '1001000105', 'WXY305', LAST_INSERT_ID(), 'DENTRO');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 160 MINUTE, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000105';

-- Diana Carolina Ramírez - apto 302, torre 2, placa PLK687
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 6, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '302', '2', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Diana Carolina Ramírez', '1001000106', 'PLK687', LAST_INSERT_ID(), 'FUERA');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 6 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000106';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 4 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000106';

-- Jorge Eduardo Castillo - apto 401, torre 2, placa MNB45C (moto)
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 7, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '401', '2', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Jorge Eduardo Castillo', '1001000107', 'MNB45C', LAST_INSERT_ID(), 'DENTRO');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 230 MINUTE, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000107';

-- Sandra Milena Herrera - apto 402, torre 2, placa ZAQ574
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 8, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '402', '2', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Sandra Milena Herrera', '1001000108', 'ZAQ574', LAST_INSERT_ID(), 'FUERA');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 6 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000108';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 4 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000108';

-- Luis Alberto Vargas - apto 501, torre 3, placa CVB850
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 9, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '501', '3', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Luis Alberto Vargas', '1001000109', 'CVB850', LAST_INSERT_ID(), 'DENTRO');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 300 MINUTE, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000109';

-- Paola Andrea Moreno - apto 502, torre 3, placa RTY33F (moto)
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 10, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '502', '3', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Paola Andrea Moreno', '1001000110', 'RTY33F', LAST_INSERT_ID(), 'FUERA');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 6 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000110';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 4 HOUR, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000110';

-- Camilo José Ortiz - apto 601, torre 3, placa UIO296
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 11, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '601', '3', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Camilo José Ortiz', '1001000111', 'UIO296', LAST_INSERT_ID(), 'FUERA');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 11 DAY, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000111';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 10 DAY, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000111';

-- Natalia Sofía Duarte - apto 602, torre 3, placa EDC618
INSERT INTO parqueadero (id_escenario, numero, tipo) VALUES (1, 12, 'FIJO');
INSERT INTO ubicacion (id_escenario, codigo, zona, id_parqueadero) VALUES (1, '602', '3', LAST_INSERT_ID());
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion, presencia)
VALUES (1, 'Natalia Sofía Duarte', '1001000112', 'EDC618', LAST_INSERT_ID(), 'FUERA');
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 11 DAY, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000112';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 10 DAY, uf.placa, 'USUARIO_FIJO', uf.id_usuario_fijo, uf.nombre, ub.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM usuario_fijo uf JOIN ubicacion ub ON ub.id_ubicacion = uf.id_ubicacion
 WHERE uf.id_escenario = 1 AND uf.cedula = '1001000112';

-- Baja logica: dos propietarios pasan a INACTIVO (no se borran)
UPDATE usuario_fijo
   SET estado = 'INACTIVO', baja_id = id_usuario_fijo, fecha_estado = NOW() - INTERVAL 10 DAY
 WHERE id_escenario = 1 AND cedula IN ('1001000111', '1001000112');

-- Visitantes (usan los parqueaderos de visitantes 1 a 10 creados en 01_estructura.sql)

-- Pedro Pablo Salazar -> 101 (DENTRO)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Pedro Pablo Salazar', '1002000001', 'ASD147', '101', id_parqueadero, 'DENTRO', NOW() - INTERVAL 45 MINUTE
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 1;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 45 MINUTE, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'ASD147';

-- Ximena Alejandra Ruiz -> 302 (DENTRO)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Ximena Alejandra Ruiz', '1002000002', 'QWE369', '302', id_parqueadero, 'DENTRO', NOW() - INTERVAL 2 HOUR
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 2;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 2 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'QWE369';

-- Hernán Darío Lozano -> 501 (DENTRO)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Hernán Darío Lozano', '1002000003', 'YHN258', '501', id_parqueadero, 'DENTRO', NOW() - INTERVAL 20 MINUTE
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 3;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 20 MINUTE, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'YHN258';

-- Tatiana Isabel Mejía -> 201 (DENTRO)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Tatiana Isabel Mejía', '1002000004', 'POI753', '201', id_parqueadero, 'DENTRO', NOW() - INTERVAL 10 MINUTE
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 4;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 10 MINUTE, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'POI753';

-- Óscar Iván Peña -> 202 (FUERA)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Óscar Iván Peña', '1002000005', 'LKJ842', '202', id_parqueadero, 'FUERA', NOW() - INTERVAL 1 DAY - INTERVAL 1 HOUR
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 1;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 1 DAY - INTERVAL 3 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'LKJ842';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 1 DAY - INTERVAL 1 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'LKJ842';

-- Gloria Patricia Ávila -> 401 (FUERA)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Gloria Patricia Ávila', '1002000006', 'MNV516', '401', id_parqueadero, 'FUERA', NOW() - INTERVAL 1 DAY - INTERVAL 4 HOUR
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 2;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 1 DAY - INTERVAL 5 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'MNV516';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 1 DAY - INTERVAL 4 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'MNV516';

-- Ricardo Esteban Cruz -> 102 (FUERA)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Ricardo Esteban Cruz', '1002000007', 'BGT679', '102', id_parqueadero, 'FUERA', NOW() - INTERVAL 2 DAY - INTERVAL 2 HOUR
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 5;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 2 DAY - INTERVAL 6 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'BGT679';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 2 DAY - INTERVAL 2 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'BGT679';

-- Mónica Lucía Rincón -> 301 (FUERA)
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero, presencia, ultimo_cambio)
SELECT 1, 'Mónica Lucía Rincón', '1002000008', 'VCX024', '301', id_parqueadero, 'FUERA', NOW() - INTERVAL 3 DAY - INTERVAL 3 HOUR
  FROM parqueadero WHERE id_escenario = 1 AND tipo = 'VISITANTE' AND numero = 3;
INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'INGRESO', NOW() - INTERVAL 3 DAY - INTERVAL 4 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'VCX024';

INSERT INTO movimiento (id_escenario, tipo, fecha_hora, placa, tipo_persona, id_persona, nombre, id_parqueadero, origen, detalle)
SELECT 1, 'SALIDA', NOW() - INTERVAL 3 DAY - INTERVAL 3 HOUR, v.placa, 'VISITANTE', v.id_visitante, v.nombre, v.id_parqueadero, 'CAMARA', 'Lectura de cámara'
  FROM visitante v WHERE v.id_escenario = 1 AND v.placa = 'VCX024';

-- Incidentes reportados por el vigilante
INSERT INTO incidente (id_escenario, fecha_hora, id_usuario_sistema, reportado_por, tipo, gravedad, lugar,
                       placa, persona_involucrada, descripcion, acciones_tomadas, estado, observacion_admin, fecha_estado)
SELECT 1, NOW() - INTERVAL 2 DAY, u.id_usuario_sistema, u.nombre, 'DANO_A_VEHICULO', 'MEDIA', 'Parqueadero 4, sótano',
       'KJH451', 'Carlos Andrés Rodríguez',
       'El propietario encontró el espejo lateral derecho roto al llegar a su parqueadero.',
       'Se tomaron fotos, se revisó la grabación de la cámara y se informó al propietario.',
       'CERRADO', 'Se revisó el video: el daño lo causó un visitante al maniobrar. Se contactó a su familia.', NOW() - INTERVAL 1 DAY
  FROM usuario_sistema u WHERE u.usuario = 'vigilante';

INSERT INTO incidente (id_escenario, fecha_hora, id_usuario_sistema, reportado_por, tipo, gravedad, lugar,
                       placa, persona_involucrada, descripcion, acciones_tomadas, estado, observacion_admin, fecha_estado)
SELECT 1, NOW() - INTERVAL 5 HOUR, u.id_usuario_sistema, u.nombre, 'VEHICULO_MAL_PARQUEADO', 'BAJA', 'Entrada vehicular',
       'ASD147', 'Pedro Pablo Salazar',
       'Un visitante dejó su carro bloqueando parcialmente la entrada vehicular.',
       'Se le pidió por el citófono que lo moviera al parqueadero asignado.',
       'ABIERTO', 'Sin revisar', NOW() - INTERVAL 5 HOUR
  FROM usuario_sistema u WHERE u.usuario = 'vigilante';

INSERT INTO incidente (id_escenario, fecha_hora, id_usuario_sistema, reportado_por, tipo, gravedad, lugar,
                       placa, persona_involucrada, descripcion, acciones_tomadas, estado, observacion_admin, fecha_estado)
SELECT 1, NOW() - INTERVAL 40 MINUTE, u.id_usuario_sistema, u.nombre, 'PERSONA_SOSPECHOSA', 'ALTA', 'Torre 2, pasillo del primer piso',
       'N/A', 'N/A',
       'Una persona desconocida intentó ingresar siguiendo a un residente sin identificarse.',
       'Se le pidió identificación y se retiró del lugar. Se avisó a la administración.',
       'EN_REVISION', 'Se están revisando las cámaras de la entrada peatonal.', NOW() - INTERVAL 20 MINUTE
  FROM usuario_sistema u WHERE u.usuario = 'vigilante';
