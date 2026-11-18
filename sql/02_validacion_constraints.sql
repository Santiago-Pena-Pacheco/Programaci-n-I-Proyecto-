-- 02_validacion_constraints.sql: cinco inserciones inválidas que la base de datos debe rechazar.
-- Ejecutar después de 01_inicializacion.sql. Cada instrucción falla con un error distinto;
-- en phpMyAdmin marcar "Continuar en caso de error" para ver los cinco.

USE control_acceso;

-- 1. CHECK ck_usuario_fijo_placa: ¿la base rechaza una placa con formato inválido (AB1234)?
INSERT INTO usuario_fijo (id_escenario, nombre, cedula, placa, id_ubicacion)
VALUES (1, 'Prueba Placa', '1000000001', 'AB1234', 1);

-- 2. CHECK ck_escenario_cantidad: ¿rechaza un escenario sin parqueaderos de visitantes?
INSERT INTO escenario (nombre, etiqueta_usuario_fijo, etiqueta_usuario_fijo_plural,
                       etiqueta_ubicacion, etiqueta_zona, cantidad_parqueaderos_visitantes)
VALUES ('Escenario de prueba', 'Residente', 'Residentes', 'Casa', 'Manzana', 0);

-- 3. CHECK ck_incidente_cierre: ¿rechaza cerrar un incidente sin observación del administrador?
INSERT INTO incidente (id_escenario, id_usuario_sistema, reportado_por, tipo, gravedad, lugar,
                       placa, persona_involucrada, descripcion, acciones_tomadas, estado)
VALUES (1, 1, 'Administrador General', 'OTRO', 'BAJA', 'Portería', 'N/A', 'Ninguna',
        'Prueba de cierre', 'Ninguna', 'CERRADO');

-- 4. UNIQUE uq_visitante_placa: ¿rechaza un segundo visitante con una placa que ya existe (ASD147)?
INSERT INTO visitante (id_escenario, nombre, cedula, placa, destino, id_parqueadero)
VALUES (1, 'Prueba Duplicado', '1000000002', 'ASD147', '101', 1);

-- 5. FOREIGN KEY fk_movimiento_parqueadero: ¿rechaza un movimiento en un parqueadero que no existe?
INSERT INTO movimiento (id_escenario, tipo, placa, tipo_persona, id_persona, nombre, id_parqueadero, detalle)
VALUES (1, 'INGRESO', 'ABC123', 'VISITANTE', 1, 'Prueba FK', 999999, 'Parqueadero inexistente');
