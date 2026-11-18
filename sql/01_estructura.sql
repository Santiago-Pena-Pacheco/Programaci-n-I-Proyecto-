-- 01_estructura.sql: crea la base control_acceso (MySQL / MariaDB).
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
