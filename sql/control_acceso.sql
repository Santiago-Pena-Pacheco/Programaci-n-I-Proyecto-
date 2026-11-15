-- =====================================================================
--  Base de datos: control_acceso  (XAMPP: MariaDB/MySQL, phpMyAdmin)
--  Sistema de control de acceso por placa, escalable a varios escenarios
--  (conjunto residencial, centro comercial, concierto, oficinas, ...).
--
--  Como usarlo: phpMyAdmin -> pestana "Importar" -> elegir este archivo.
--  Esta version REEMPLAZA a las anteriores: antes de importarla borre la
--  base vieja con   DROP DATABASE IF EXISTS control_acceso;
--
--  REGLAS DE DISENO
--   * Ningun dato que se captura es opcional: todas las columnas de datos
--     son NOT NULL y las de texto no pueden quedar vacias (CHECK).
--   * Cada persona tiene una PRESENCIA (DENTRO / FUERA). La camara alterna
--     el estado: la primera lectura de una placa es un ingreso y la
--     siguiente es una salida.
--   * Cada ingreso y salida queda en la tabla `movimiento` (historial).
--   * Los usuarios fijos nunca se borran: "dar de baja" los pasa a INACTIVO.
--   * La ocupacion de un parqueadero NO se guarda aparte (asi no puede
--     quedar inconsistente): se deduce de quien esta DENTRO.
--   * Lo que cambia entre escenarios vive en la tabla `escenario`; un
--     escenario nuevo es una fila nueva (o el boton "Nuevo escenario").
-- =====================================================================

CREATE DATABASE IF NOT EXISTS control_acceso
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE control_acceso;

-- ---------------------------------------------------------------------
-- escenario: contexto de uso y como se llaman las cosas en el
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS escenario (
    id_escenario                      INT          NOT NULL AUTO_INCREMENT,
    nombre                            VARCHAR(60)  NOT NULL,
    etiqueta_usuario_fijo             VARCHAR(40)  NOT NULL,  -- p. ej. Propietario
    etiqueta_usuario_fijo_plural      VARCHAR(40)  NOT NULL,  -- p. ej. Propietarios
    etiqueta_ubicacion                VARCHAR(40)  NOT NULL,  -- p. ej. Apartamento
    etiqueta_zona                     VARCHAR(40)  NOT NULL,  -- p. ej. Torre
    cantidad_parqueaderos_visitantes  INT          NOT NULL DEFAULT 10,
    PRIMARY KEY (id_escenario),
    UNIQUE KEY uq_escenario_nombre (nombre),
    CONSTRAINT ck_escenario_cantidad CHECK (cantidad_parqueaderos_visitantes > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- configuracion: pares clave/valor (aqui se guarda el escenario activo)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS configuracion (
    clave  VARCHAR(40)  NOT NULL,
    valor  VARCHAR(100) NOT NULL,
    PRIMARY KEY (clave)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- usuario_sistema: quienes inician sesion en el programa (login por rol)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario_sistema (
    id_usuario_sistema  INT         NOT NULL AUTO_INCREMENT,
    usuario             VARCHAR(30) NOT NULL,
    nombre              VARCHAR(80) NOT NULL,                 -- nombre completo (firma los reportes)
    clave_hash          CHAR(64)    NOT NULL,                 -- SHA-256 en hexadecimal
    rol                 ENUM('ADMINISTRADOR', 'VIGILANTE') NOT NULL,
    activo              TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id_usuario_sistema),
    UNIQUE KEY uq_usuario_sistema_usuario (usuario)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- parqueadero: espacios de parqueo de un escenario.
--   FIJO      -> pertenece a una ubicacion (a su dueno)
--   VISITANTE -> pool compartido que se asigna a los visitantes
-- No guarda quien lo ocupa: se deduce de la presencia (DENTRO) de la persona.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parqueadero (
    id_parqueadero  INT NOT NULL AUTO_INCREMENT,
    id_escenario    INT NOT NULL,
    numero          INT NOT NULL,
    tipo            ENUM('FIJO', 'VISITANTE') NOT NULL,
    PRIMARY KEY (id_parqueadero),
    UNIQUE KEY uq_parqueadero_numero (id_escenario, tipo, numero),
    CONSTRAINT fk_parqueadero_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario)
        ON DELETE CASCADE,
    CONSTRAINT ck_parqueadero_numero CHECK (numero > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- ubicacion: destino dentro del escenario (apartamento+torre, local+piso,
-- puesto+zona, oficina+piso...). Todas tienen su parqueadero fijo (1:1).
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ubicacion (
    id_ubicacion    INT         NOT NULL AUTO_INCREMENT,
    id_escenario    INT         NOT NULL,
    codigo          VARCHAR(20) NOT NULL,                     -- p. ej. 101
    zona            VARCHAR(30) NOT NULL,                     -- p. ej. 3
    id_parqueadero  INT         NOT NULL,
    PRIMARY KEY (id_ubicacion),
    UNIQUE KEY uq_ubicacion_codigo (id_escenario, codigo, zona),
    UNIQUE KEY uq_ubicacion_parqueadero (id_parqueadero),
    CONSTRAINT fk_ubicacion_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario)
        ON DELETE CASCADE,
    CONSTRAINT fk_ubicacion_parqueadero
        FOREIGN KEY (id_parqueadero) REFERENCES parqueadero (id_parqueadero),
    CONSTRAINT ck_ubicacion_texto CHECK (CHAR_LENGTH(TRIM(codigo)) > 0 AND CHAR_LENGTH(TRIM(zona)) > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- usuario_fijo: propietario / empleado / personal (segun el escenario)
--
--   estado     ACTIVO / INACTIVO (baja logica: nunca se borra)
--   presencia  DENTRO / FUERA (la camara lo alterna en cada lectura)
--   fecha_estado  fecha del ultimo cambio de estado (alta, baja o reactivacion)
--
-- baja_id vale 0 mientras esta ACTIVO y, al darlo de baja, toma su propio id:
-- asi las llaves unicas (cedula, placa, ubicacion) solo cuentan entre los
-- ACTIVOS y una placa o ubicacion se puede volver a registrar tras una baja.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario_fijo (
    id_usuario_fijo  INT          NOT NULL AUTO_INCREMENT,
    id_escenario     INT          NOT NULL,
    nombre           VARCHAR(80)  NOT NULL,
    cedula           VARCHAR(20)  NOT NULL,
    placa            VARCHAR(10)  NOT NULL,
    id_ubicacion     INT          NOT NULL,
    estado           ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    presencia        ENUM('DENTRO', 'FUERA')    NOT NULL DEFAULT 'FUERA',
    fecha_estado     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    baja_id          INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id_usuario_fijo),
    UNIQUE KEY uq_usuario_fijo_cedula_baja (id_escenario, cedula, baja_id),
    UNIQUE KEY uq_usuario_fijo_placa_baja (id_escenario, placa, baja_id),
    UNIQUE KEY uq_usuario_fijo_ubicacion_baja (id_ubicacion, baja_id),
    CONSTRAINT fk_usuario_fijo_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario)
        ON DELETE CASCADE,
    CONSTRAINT fk_usuario_fijo_ubicacion
        FOREIGN KEY (id_ubicacion) REFERENCES ubicacion (id_ubicacion),
    CONSTRAINT ck_usuario_fijo_texto CHECK (
        CHAR_LENGTH(TRIM(nombre)) > 0 AND CHAR_LENGTH(TRIM(cedula)) > 0 AND CHAR_LENGTH(TRIM(placa)) > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- visitante: una fila por placa de visitante (los datos se conservan para
-- cuando regrese). id_parqueadero es el de su ultima visita; solo esta
-- ocupado mientras presencia = DENTRO. El historial de visitas esta en
-- `movimiento`.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS visitante (
    id_visitante    INT          NOT NULL AUTO_INCREMENT,
    id_escenario    INT          NOT NULL,
    nombre          VARCHAR(80)  NOT NULL,
    cedula          VARCHAR(20)  NOT NULL,
    placa           VARCHAR(10)  NOT NULL,
    destino         VARCHAR(60)  NOT NULL,
    id_parqueadero  INT          NOT NULL,
    presencia       ENUM('DENTRO', 'FUERA') NOT NULL DEFAULT 'DENTRO',
    ultimo_cambio   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_visitante),
    UNIQUE KEY uq_visitante_placa (id_escenario, placa),
    KEY ix_visitante_parqueadero (id_parqueadero, presencia),
    CONSTRAINT fk_visitante_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario)
        ON DELETE CASCADE,
    CONSTRAINT fk_visitante_parqueadero
        FOREIGN KEY (id_parqueadero) REFERENCES parqueadero (id_parqueadero),
    CONSTRAINT ck_visitante_texto CHECK (
        CHAR_LENGTH(TRIM(nombre)) > 0 AND CHAR_LENGTH(TRIM(cedula)) > 0
        AND CHAR_LENGTH(TRIM(placa)) > 0 AND CHAR_LENGTH(TRIM(destino)) > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- movimiento: historial de ingresos y salidas (de usuarios fijos y visitantes).
--   origen CAMARA: lectura de la camara (o ingreso confirmado por el vigilante)
--   origen MANUAL: registro manual o correccion del administrador
-- Guarda copia del nombre y la placa del momento, asi el historial no cambia
-- si despues se edita a la persona.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimiento (
    id_movimiento   BIGINT       NOT NULL AUTO_INCREMENT,
    id_escenario    INT          NOT NULL,
    tipo            ENUM('INGRESO', 'SALIDA') NOT NULL,
    fecha_hora      DATETIME     NOT NULL,
    placa           VARCHAR(10)  NOT NULL,
    tipo_persona    ENUM('USUARIO_FIJO', 'VISITANTE') NOT NULL,
    id_persona      INT          NOT NULL,
    nombre          VARCHAR(80)  NOT NULL,
    id_parqueadero  INT          NOT NULL,
    origen          ENUM('CAMARA', 'MANUAL') NOT NULL,
    detalle         VARCHAR(200) NOT NULL,
    PRIMARY KEY (id_movimiento),
    KEY ix_movimiento_placa (id_escenario, placa, fecha_hora),
    KEY ix_movimiento_fecha (id_escenario, fecha_hora),
    CONSTRAINT fk_movimiento_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario)
        ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_parqueadero
        FOREIGN KEY (id_parqueadero) REFERENCES parqueadero (id_parqueadero)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- incidente: reporte de seguridad hecho por un vigilante.
--   fecha_hora        la pone la base de datos sola, al guardar el reporte
--   id_usuario_sistema / reportado_por  quien lo reporto (se toma de la sesion;
--                     reportado_por es una copia del nombre del momento)
--   tipo, gravedad    clasificacion del suceso
--   lugar             donde ocurrio, dentro del escenario
--   placa             placa del vehiculo afectado ('N/A' si no hay vehiculo)
--   persona_involucrada  nombre de la persona involucrada ('N/A' si no hay)
--   descripcion, acciones_tomadas   que paso y que hizo el vigilante
--   estado            ABIERTO -> EN_REVISION -> CERRADO (lo cambia el administrador)
--   observacion_admin lo que concluyo el administrador ('Sin revisar' al inicio)
--   fecha_estado      ultima vez que cambio el estado
-- Ninguna columna admite NULL.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS incidente (
    id_incidente         INT           NOT NULL AUTO_INCREMENT,
    id_escenario         INT           NOT NULL,
    fecha_hora           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_sistema   INT           NOT NULL,
    reportado_por        VARCHAR(80)   NOT NULL,
    tipo                 ENUM('ACCESO_NO_AUTORIZADO', 'ROBO_O_HURTO', 'DANO_A_VEHICULO',
                              'VEHICULO_MAL_PARQUEADO', 'DISTURBIO', 'PERSONA_SOSPECHOSA',
                              'EMERGENCIA', 'OTRO') NOT NULL,
    gravedad             ENUM('BAJA', 'MEDIA', 'ALTA', 'CRITICA') NOT NULL,
    lugar                VARCHAR(80)   NOT NULL,
    placa                VARCHAR(10)   NOT NULL,
    persona_involucrada  VARCHAR(80)   NOT NULL,
    descripcion          VARCHAR(500)  NOT NULL,
    acciones_tomadas     VARCHAR(300)  NOT NULL,
    estado               ENUM('ABIERTO', 'EN_REVISION', 'CERRADO') NOT NULL DEFAULT 'ABIERTO',
    observacion_admin    VARCHAR(300)  NOT NULL DEFAULT 'Sin revisar',
    fecha_estado         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_incidente),
    KEY ix_incidente_escenario (id_escenario, fecha_hora),
    KEY ix_incidente_placa (id_escenario, placa),
    CONSTRAINT fk_incidente_escenario
        FOREIGN KEY (id_escenario) REFERENCES escenario (id_escenario)
        ON DELETE CASCADE,
    CONSTRAINT fk_incidente_usuario_sistema
        FOREIGN KEY (id_usuario_sistema) REFERENCES usuario_sistema (id_usuario_sistema),
    CONSTRAINT ck_incidente_texto CHECK (
        CHAR_LENGTH(TRIM(reportado_por)) > 0 AND CHAR_LENGTH(TRIM(lugar)) > 0
        AND CHAR_LENGTH(TRIM(placa)) > 0 AND CHAR_LENGTH(TRIM(persona_involucrada)) > 0
        AND CHAR_LENGTH(TRIM(descripcion)) > 0 AND CHAR_LENGTH(TRIM(acciones_tomadas)) > 0
        AND CHAR_LENGTH(TRIM(observacion_admin)) > 0)
) ENGINE = InnoDB;

-- =====================================================================
--  DATOS INICIALES
-- =====================================================================

-- Cuatro escenarios de ejemplo (se pueden crear mas desde el programa)
INSERT IGNORE INTO escenario
    (id_escenario, nombre, etiqueta_usuario_fijo, etiqueta_usuario_fijo_plural,
     etiqueta_ubicacion, etiqueta_zona, cantidad_parqueaderos_visitantes)
VALUES
    (1, 'Conjunto residencial', 'Propietario', 'Propietarios', 'Apartamento', 'Torre', 10),
    (2, 'Centro comercial',     'Empleado',    'Empleados',    'Local',       'Piso',  20),
    (3, 'Concierto',            'Personal',    'Personal',     'Puesto',      'Zona',  30),
    (4, 'Oficinas corporativas','Empleado',    'Empleados',    'Oficina',     'Piso',  15);

-- Parqueaderos de visitantes: uno por cada numero 1..cantidad del escenario
INSERT IGNORE INTO parqueadero (id_escenario, numero, tipo)
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

-- Escenario activo al arrancar (el administrador puede cambiarlo)
INSERT IGNORE INTO configuracion (clave, valor) VALUES ('escenario_activo', '1');

-- Usuarios del sistema. CAMBIE estas claves antes de usar el programa en serio.
--   admin / admin123 (ADMINISTRADOR)      vigilante / vigilante123 (VIGILANTE)
INSERT IGNORE INTO usuario_sistema (usuario, nombre, clave_hash, rol) VALUES
    ('admin',     'Administrador General', SHA2('admin123', 256),     'ADMINISTRADOR'),
    ('vigilante', 'Vigilante de Turno',    SHA2('vigilante123', 256), 'VIGILANTE');
