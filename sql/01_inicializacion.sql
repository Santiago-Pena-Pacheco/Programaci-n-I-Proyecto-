-- 01_inicializacion.sql: base de datos control_acceso (tablas, restricciones, índices y datos iniciales).
-- Ejecutar primero. Borra y vuelve a crear la base, por eso se puede repetir.

DROP DATABASE IF EXISTS control_acceso;
CREATE DATABASE control_acceso CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE control_acceso;

-- Escenario: tipo de espacio y las etiquetas que usa la interfaz.
CREATE TABLE escenario (
    id_escenario                      INT         NOT NULL AUTO_INCREMENT,
    nombre                            VARCHAR(60) NOT NULL,
    etiqueta_usuario_fijo             VARCHAR(40) NOT NULL,
    etiqueta_usuario_fijo_plural      VARCHAR(40) NOT NULL,
    etiqueta_ubicacion                VARCHAR(40) NOT NULL,
    etiqueta_zona                     VARCHAR(40) NOT NULL,
    cantidad_parqueaderos_visitantes  INT         NOT NULL DEFAULT 10,
    PRIMARY KEY (id_escenario),
    UNIQUE KEY uq_escenario_nombre (nombre),
    CONSTRAINT ck_escenario_cantidad CHECK (cantidad_parqueaderos_visitantes > 0)
) ENGINE = InnoDB;

-- Configuracion: pares clave/valor (guarda el escenario activo).
CREATE TABLE configuracion (
    clave  VARCHAR(40)  NOT NULL,
    valor  VARCHAR(100) NOT NULL,
    PRIMARY KEY (clave)
) ENGINE = InnoDB;

-- Usuarios que inician sesion en el programa.
CREATE TABLE usuario_sistema (
    id_usuario_sistema  INT         NOT NULL AUTO_INCREMENT,
    usuario             VARCHAR(30) NOT NULL,
    nombre              VARCHAR(80) NOT NULL,
    clave_hash          CHAR(64)    NOT NULL,
    rol                 ENUM('ADMINISTRADOR', 'VIGILANTE') NOT NULL,
    activo              TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id_usuario_sistema),
    UNIQUE KEY uq_usuario_sistema_usuario (usuario),
    CONSTRAINT ck_usuario_sistema_activo CHECK (activo IN (0, 1))
) ENGINE = InnoDB;

-- Parqueadero: FIJO (de una ubicacion) o VISITANTE (pool compartido).
CREATE TABLE parqueadero (
    id_parqueadero  INT NOT NULL AUTO_INCREMENT,
    id_escenario    INT NOT NULL,
    numero          INT NOT NULL,
    tipo            ENUM('FIJO', 'VISITANTE') NOT NULL,
    PRIMARY KEY (id_parqueadero),
    UNIQUE KEY uq_parqueadero_numero (id_escenario, tipo, numero),
    CONSTRAINT fk_parqueadero_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario) ON DELETE CASCADE,
    CONSTRAINT ck_parqueadero_numero CHECK (numero > 0)
) ENGINE = InnoDB;

-- Ubicacion: destino dentro del escenario (apartamento y torre, local y piso...).
CREATE TABLE ubicacion (
    id_ubicacion    INT         NOT NULL AUTO_INCREMENT,
    id_escenario    INT         NOT NULL,
    codigo          VARCHAR(30) NOT NULL,
    zona            VARCHAR(30) NOT NULL,
    id_parqueadero  INT         NOT NULL,
    PRIMARY KEY (id_ubicacion),
    UNIQUE KEY uq_ubicacion_codigo (id_escenario, codigo, zona),
    UNIQUE KEY uq_ubicacion_parqueadero (id_parqueadero),
    CONSTRAINT fk_ubicacion_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario) ON DELETE CASCADE,
    CONSTRAINT fk_ubicacion_parqueadero
        FOREIGN KEY (id_parqueadero) REFERENCES parqueadero (id_parqueadero),
    CONSTRAINT ck_ubicacion_texto CHECK (CHAR_LENGTH(TRIM(codigo)) > 0 AND CHAR_LENGTH(TRIM(zona)) > 0)
) ENGINE = InnoDB;

-- Usuario fijo: propietario, empleado o personal. Baja logica: estado INACTIVO.
-- baja_id vale 0 si esta ACTIVO y su propio id al darlo de baja, para poder
-- volver a registrar la misma cedula o placa sin romper los UNIQUE.
CREATE TABLE usuario_fijo (
    id_usuario_fijo  INT         NOT NULL AUTO_INCREMENT,
    id_escenario     INT         NOT NULL,
    nombre           VARCHAR(80) NOT NULL,
    cedula           VARCHAR(20) NOT NULL,
    placa            VARCHAR(10) NOT NULL,
    id_ubicacion     INT         NOT NULL,
    estado           ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    presencia        ENUM('DENTRO', 'FUERA')    NOT NULL DEFAULT 'FUERA',
    fecha_estado     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    baja_id          INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id_usuario_fijo),
    UNIQUE KEY uq_usuario_fijo_cedula_baja (id_escenario, cedula, baja_id),
    UNIQUE KEY uq_usuario_fijo_placa_baja (id_escenario, placa, baja_id),
    UNIQUE KEY uq_usuario_fijo_ubicacion_baja (id_ubicacion, baja_id),
    CONSTRAINT fk_usuario_fijo_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_fijo_ubicacion
        FOREIGN KEY (id_ubicacion) REFERENCES ubicacion (id_ubicacion),
    CONSTRAINT ck_usuario_fijo_nombre CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
    CONSTRAINT ck_usuario_fijo_cedula CHECK (cedula REGEXP '^[0-9]{5,12}$'),
    CONSTRAINT ck_usuario_fijo_placa CHECK (placa REGEXP '^[A-Z]{3}[0-9]{2}[0-9A-Z]$')
) ENGINE = InnoDB;

-- Visitante: una fila por placa. Solo ocupa su parqueadero mientras esta DENTRO.
CREATE TABLE visitante (
    id_visitante    INT         NOT NULL AUTO_INCREMENT,
    id_escenario    INT         NOT NULL,
    nombre          VARCHAR(80) NOT NULL,
    cedula          VARCHAR(20) NOT NULL,
    placa           VARCHAR(10) NOT NULL,
    destino         VARCHAR(60) NOT NULL,
    id_parqueadero  INT         NOT NULL,
    presencia       ENUM('DENTRO', 'FUERA') NOT NULL DEFAULT 'DENTRO',
    ultimo_cambio   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_visitante),
    UNIQUE KEY uq_visitante_placa (id_escenario, placa),
    CONSTRAINT fk_visitante_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario) ON DELETE CASCADE,
    CONSTRAINT fk_visitante_parqueadero
        FOREIGN KEY (id_parqueadero) REFERENCES parqueadero (id_parqueadero),
    CONSTRAINT ck_visitante_texto CHECK (CHAR_LENGTH(TRIM(nombre)) > 0 AND CHAR_LENGTH(TRIM(destino)) > 0),
    CONSTRAINT ck_visitante_cedula CHECK (cedula REGEXP '^[0-9]{5,12}$'),
    CONSTRAINT ck_visitante_placa CHECK (placa REGEXP '^[A-Z]{3}[0-9]{2}[0-9A-Z]$')
) ENGINE = InnoDB;

-- Movimiento: historial de ingresos y salidas (guarda copia del nombre y la placa).
CREATE TABLE movimiento (
    id_movimiento   BIGINT       NOT NULL AUTO_INCREMENT,
    id_escenario    INT          NOT NULL,
    tipo            ENUM('INGRESO', 'SALIDA') NOT NULL,
    fecha_hora      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    placa           VARCHAR(10)  NOT NULL,
    tipo_persona    ENUM('USUARIO_FIJO', 'VISITANTE') NOT NULL,
    id_persona      INT          NOT NULL,
    nombre          VARCHAR(80)  NOT NULL,
    id_parqueadero  INT          NOT NULL,
    origen          ENUM('CAMARA', 'MANUAL') NOT NULL DEFAULT 'CAMARA',
    detalle         VARCHAR(200) NOT NULL,
    PRIMARY KEY (id_movimiento),
    CONSTRAINT fk_movimiento_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_parqueadero
        FOREIGN KEY (id_parqueadero) REFERENCES parqueadero (id_parqueadero),
    CONSTRAINT ck_movimiento_placa CHECK (placa REGEXP '^[A-Z]{3}[0-9]{2}[0-9A-Z]$')
) ENGINE = InnoDB;

-- Incidente: reporte de seguridad. placa y persona valen 'N/A' si no aplican.
CREATE TABLE incidente (
    id_incidente         INT          NOT NULL AUTO_INCREMENT,
    id_escenario         INT          NOT NULL,
    fecha_hora           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_sistema   INT          NOT NULL,
    reportado_por        VARCHAR(80)  NOT NULL,
    tipo                 ENUM('ACCESO_NO_AUTORIZADO', 'ROBO_O_HURTO', 'DANO_A_VEHICULO',
                              'VEHICULO_MAL_PARQUEADO', 'DISTURBIO', 'PERSONA_SOSPECHOSA',
                              'EMERGENCIA', 'OTRO') NOT NULL,
    gravedad             ENUM('BAJA', 'MEDIA', 'ALTA', 'CRITICA') NOT NULL,
    lugar                VARCHAR(80)  NOT NULL,
    placa                VARCHAR(10)  NOT NULL,
    persona_involucrada  VARCHAR(80)  NOT NULL,
    descripcion          VARCHAR(500) NOT NULL,
    acciones_tomadas     VARCHAR(300) NOT NULL,
    estado               ENUM('ABIERTO', 'EN_REVISION', 'CERRADO') NOT NULL DEFAULT 'ABIERTO',
    observacion_admin    VARCHAR(300) NOT NULL DEFAULT 'Sin revisar',
    fecha_estado         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_incidente),
    CONSTRAINT fk_incidente_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario) ON DELETE CASCADE,
    CONSTRAINT fk_incidente_usuario_sistema
        FOREIGN KEY (id_usuario_sistema) REFERENCES usuario_sistema (id_usuario_sistema),
    CONSTRAINT ck_incidente_texto CHECK (
        CHAR_LENGTH(TRIM(reportado_por)) > 0 AND CHAR_LENGTH(TRIM(lugar)) > 0
        AND CHAR_LENGTH(TRIM(persona_involucrada)) > 0 AND CHAR_LENGTH(TRIM(descripcion)) > 0
        AND CHAR_LENGTH(TRIM(acciones_tomadas)) > 0 AND CHAR_LENGTH(TRIM(observacion_admin)) > 0),
    CONSTRAINT ck_incidente_placa CHECK (placa = 'N/A' OR placa REGEXP '^[A-Z]{3}[0-9]{2}[0-9A-Z]$'),
    CONSTRAINT ck_incidente_cierre CHECK (estado = 'ABIERTO' OR observacion_admin <> 'Sin revisar')
) ENGINE = InnoDB;

-- Indices: llaves foraneas y columnas usadas en JOIN, WHERE y GROUP BY.
CREATE INDEX ix_movimiento_escenario_fecha ON movimiento (id_escenario, fecha_hora);
CREATE INDEX ix_movimiento_placa           ON movimiento (id_escenario, placa, fecha_hora);
CREATE INDEX ix_movimiento_parqueadero     ON movimiento (id_parqueadero);
CREATE INDEX ix_visitante_parqueadero      ON visitante (id_parqueadero, presencia);
CREATE INDEX ix_usuario_fijo_presencia     ON usuario_fijo (id_escenario, presencia, estado);
CREATE INDEX ix_incidente_escenario_fecha  ON incidente (id_escenario, fecha_hora);
CREATE INDEX ix_incidente_estado           ON incidente (id_escenario, estado, gravedad);
CREATE INDEX ix_incidente_placa            ON incidente (id_escenario, placa);
CREATE INDEX ix_incidente_usuario          ON incidente (id_usuario_sistema);

-- Datos iniciales: cuatro escenarios, sus parqueaderos de visitantes,
-- el escenario activo y los usuarios del programa (admin123 / vigilante123).
INSERT INTO escenario
    (id_escenario, nombre, etiqueta_usuario_fijo, etiqueta_usuario_fijo_plural,
     etiqueta_ubicacion, etiqueta_zona, cantidad_parqueaderos_visitantes)
VALUES
    (1, 'Conjunto residencial',  'Propietario', 'Propietarios', 'Apartamento', 'Torre', 10),
    (2, 'Centro comercial',      'Empleado',    'Empleados',    'Local',       'Piso',  20),
    (3, 'Concierto',             'Personal',    'Personal',     'Puesto',      'Zona',  30),
    (4, 'Oficinas corporativas', 'Empleado',    'Empleados',    'Oficina',     'Piso',  15);

INSERT INTO parqueadero (id_escenario, numero, tipo)
SELECT e.id_escenario, n.n, 'VISITANTE'
FROM escenario e
JOIN (
    SELECT a.d + b.d * 10 + 1 AS n
    FROM (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
          UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7
          UNION ALL SELECT 8 UNION ALL SELECT 9) a
    CROSS JOIN (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
                UNION ALL SELECT 4) b
) n ON n.n <= e.cantidad_parqueaderos_visitantes;

INSERT INTO configuracion (clave, valor) VALUES ('escenario_activo', '1');

INSERT INTO usuario_sistema (usuario, nombre, clave_hash, rol) VALUES
    ('admin',     'Administrador General', SHA2('admin123', 256),     'ADMINISTRADOR'),
    ('vigilante', 'Vigilante de Turno',    SHA2('vigilante123', 256), 'VIGILANTE');

-- Datos de ejemplo
SET NAMES utf8mb4;

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

-- Visitantes (usan los parqueaderos de visitantes 1 a 10 creados en 01_inicializacion.sql)

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
