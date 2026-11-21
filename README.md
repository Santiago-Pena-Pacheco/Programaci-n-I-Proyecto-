# Control de Acceso Vehicular por Placa

Proyecto de **Programación 1** – Ingeniería de Sistemas, Universidad Libre. Docente: Andrés Castaño.

**Integrantes:** 
- Nicole Camila Abril Reyes
- Gabriel Enrique Pacheco Salazar
- Santiago Peña Pacheco
- Julieta Quevedo González


Aplicación de escritorio (Java Swing) que lee la placa de un vehículo con una cámara (OpenCV + Tess4J), decide si entra o sale y lo guarda en MySQL. Sirve para varios escenarios (residencial, centro comercial, concierto, oficinas) cambiando solo los datos del escenario. la GUI es la interfaz.

## Cómo ejecutarlo

1. Instalar JDK 17 y Maven (o abrir en NetBeans). Iniciar **XAMPP** (Apache y MySQL).
2. En phpMyAdmin → pestaña SQL, ejecutar en este orden: `sql/inicializacion.sql` (borra y vuelve a crear la base `control_acceso`, con sus tablas, índices y datos de ejemplo). Los scripts 02 a 06 son las pruebas de la rúbrica de base de datos: validación de constraints, transacciones, JOIN, agrupamiento e índices con EXPLAIN.
3. La carpeta `tessdata` (con `eng.traineddata`, el idioma que usa el OCR) ya viene incluida en la raíz del proyecto; el programa la encuentra solo.
4. `mvn compile exec:java` (clase principal `com.github.santiago.fundamentos.App`).
5. Usuarios: `admin / admin123` y `vigilante / vigilante123`.
6. Fuente de video: número de cámara (`0`), ruta de un video o `http://IP:8080/video` (celular con IP Webcam).

Conexión a la BD: `localhost:3306`, base `control_acceso`, usuario `root` sin clave (ver `conexion/Conexion.java`).

## Estructura de paquetes (`com.github.santiago.fundamentos`)

| Paquete | Contenido |
|---|---|
| `modelo` | Entidades y enums |
| `dao` | Interfaces DAO y sus implementaciones JDBC |
| `logica` | Reglas de negocio (`SistemaControlAcceso`, `IncidenteBuilder`, `ReglasIncidente`) |
| `excepciones` | Jerarquía de excepciones propias |
| `conexion` | Conexión a MySQL |
| `video` | Fuentes de video (cámara, archivo, MJPEG) |
| `vision` | Detección de placa y OCR |
| `gui` | Ventanas Swing |

Scripts de base de datos: `sql/` (01 a 06, en orden). Números de línea = archivos tal como están en este repositorio.

## Mapa del proyecto (dónde se aplicó cada tema)

Rutas relativas a `src/main/java/com/github/santiago/fundamentos/`.

### Programación orientada a objetos

| Tema | Clase | Líneas | Qué se ve |
|---|---|---|---|
| **Abstracción** | `modelo/Persona` | 7, 93, 96 | Clase abstracta con `getTipoTexto` y `getDetalleAcceso` abstractos |
| **Herencia** | `modelo/UsuarioFijo` | 8 | `extends Persona` |
| **Herencia** | `modelo/Visitante` | 8 | `extends Persona` |
| **Herencia (excepciones)** | `excepciones/ControlAccesoException` → `DatoInvalidoException`, `ReglaNegocioException` → `SinCupoException`, `AccesoDenegadoException` | 4 / 4 / 4 / 4 / 4 | Jerarquía de 3 niveles |
| **Polimorfismo** | `modelo/UsuarioFijo` y `modelo/Visitante` | 58-69 / 54-65 | Cada una sobrescribe (`@Override`) `getTipoTexto` y `getDetalleAcceso` |
| **Polimorfismo (uso)** | `gui/InterfazVigilante` | 232-234 | El aviso se arma llamando a esos métodos sobre una `Persona`, sin saber qué subclase es |
| **Encapsulamiento** | `modelo/Persona` | 17-88 | Atributos privados, getters y setters que validan (nombre, cédula, vehículo) |
| **Encapsulamiento** | `modelo/Vehiculo` | 14-42 | Placa privada y validada (`ABC123` / `ABC12D`) |
| **Encapsulamiento** | `modelo/UsuarioFijo`, `modelo/Visitante` | 23-49 / 23-50 | Setters con validación |
| **Interfaces** | `dao/AccesoDAO`, `UsuarioFijoDAO`, `VisitanteDAO`, `IncidenteDAO`, `EscenarioDAO`, `UsuarioSistemaDAO` | 16 / 8 / 8 / 7 / 8 / 6 | Contratos de persistencia |
| **Interfaces** | `video/FuenteVideo` | 6 | Contrato de cualquier fuente de video |
| **Interfaces** | `gui/PanelCamara.OnPlacaDetectadaListener` | 22-24 | Contrato del observador |
| **Implementación de interfaces** | `dao/AccesoDAOImpl`, `dao/IncidenteDAOImpl`, `video/FuenteOpenCV`, `video/FuenteMjpeg` | 28 / 18 / 7 / 17 | `implements` |
| **Manejo de errores** | `excepciones/*` | todo el paquete | Excepciones propias verificadas (`extends Exception`) |
| **Manejo de errores** | `modelo/Persona` | 42, 57, 68 | `throw new DatoInvalidoException` |
| **Manejo de errores** | `logica/ReglasIncidente` | 14-76 | Validadores que lanzan `DatoInvalidoException` |
| **Manejo de errores** | `dao/AccesoDAOImpl` | 51-54, 154, 163, 289 y catch en 77, 124, 201, 249, 310 | `ReglaNegocioException`, `AccesoDenegadoException`, `SinCupoException`; `rollback` en el catch |
| **Manejo de errores** | `logica/SistemaControlAcceso` | 80, 163-175, 208 | `throws` y `catch` que traducen errores de BD |
| **Manejo de errores** | `gui/InterfazVigilante` | 258, 282 | El `catch` muestra el mensaje al usuario |
| **Manejo de errores** | `conexion/Conexion` | 38-40 | Mensaje claro si MySQL está apagado |
| **Manejo de errores** | `App` | 24 | `catch (SQLException)` al arrancar |

### Persistencia (MySQL, JDBC, DAO)

| Tema | Ubicación | Líneas |
|---|---|---|
| Tablas con PK, FK, NOT NULL, UNIQUE, DEFAULT y CHECK, 9 `CREATE INDEX` y datos de ejemplo | `sql/inicializacion.sql` | todo |
| CRUD con `PreparedStatement` | `dao/*DAOImpl` | todo |
| Transacción (`setAutoCommit(false)`, `FOR UPDATE`, `commit`, `rollback`) | `dao/AccesoDAOImpl` | 37-78 |
| Incidentes: insertar / listar / actualizar | `dao/IncidenteDAOImpl` | 21 / 54 / 92 |
| Vehículos adentro | `dao/AccesoDAOImpl` | 360 |
| Movimientos (historial) | `dao/AccesoDAOImpl` | 325 |

### GUI (Swing)

| Pantalla | Clase | Línea |
|---|---|---|
| Login | `gui/LoginFrame` | 16 |
| Vigilante (cámara, avisos, incidentes) | `gui/InterfazVigilante` | ver clase |
| Administrador (pestañas) | `gui/VentanaAdministrador` | 24 |
| Cámara en vivo | `gui/PanelCamara` | 19 |
| Vehículos adentro | `gui/PanelVehiculosAdentro` | 19 |
| Registros | `gui/PanelRegistros` | 16 |
| Formularios | `gui/FormularioUsuarioFijo`, `FormularioVisitante`, `FormularioIncidente`, `DialogoEscenario` | clase completa |

### Principios SOLID

| Principio | Dónde | Líneas |
|---|---|---|
| **S** – una responsabilidad | `ReglasIncidente` solo valida (16-83); `IncidenteBuilder` solo construye (23-82); `Conexion` solo conecta (15-53); los DAO solo acceden a datos; `gui/*` solo dibuja | |
| **O** – abierto/cerrado | Nueva fuente de video = nueva clase que implementa `FuenteVideo` (sin tocar `PanelCamara`); nuevo tipo de persona = nueva subclase de `Persona` | `video/FuenteVideo` 6; `modelo/Persona` 7 |
| **L** – sustitución | `UsuarioFijo` y `Visitante` se usan como `Persona` en `InterfazVigilante` | 232-234 |
| **I** – interfaces pequeñas | Un DAO por tema (6 interfaces) y `FuenteVideo` con 4 métodos | `dao/*.java` |
| **D** – inversión de dependencias | `SistemaControlAcceso` depende de las interfaces DAO, recibidas por constructor | `logica/SistemaControlAcceso` 49-54 y 62 |

### Patrones de diseño (funcionan al ejecutar)

| Tipo | Patrón | Clase y líneas | Se ve cuando… |
|---|---|---|---|
| Creacional | **Singleton** | `conexion/Conexion` 15-42 (constructor privado 25, `getInstancia` 30) | Cualquier consulta usa la única instancia |
| Creacional | **Factory Method** | `video/FuenteVideoFactory.crear` 19-58; usado en `gui/PanelCamara` 94 | Al iniciar la cámara según escriba `0`, un video o `http://` |
| Creacional | **Builder** | `logica/IncidenteBuilder` 23-82; usado en `logica/SistemaControlAcceso` 276 | Al reportar un incidente |
| Estructural | **Adapter** | `video/FuenteOpenCV` 11-38 y `video/FuenteMjpeg` 22-138 adaptan a `FuenteVideo` | Cámara USB / archivo vs. celular por Wi-Fi se manejan igual |
| Estructural | **Facade** | `logica/SistemaControlAcceso` 56-381; usado en `gui/InterfazVigilante` 50 y `gui/VentanaAdministrador` 236 | La GUI llama una sola clase y no conoce los DAO |
| Comportamiento | **Observer** | `gui/PanelCamara` 33-74 (`addListener`, `notificarObservadores`, disparo en 144); suscripción en `gui/InterfazVigilante` 61 | Al confirmarse una placa, la ventana reacciona |

## Reglas de negocio principales

- Cada lectura de placa alterna el estado **DENTRO/FUERA** de la persona (`SistemaControlAcceso.procesarPlaca`, 102); el vigilante no registra salidas, las detecta la cámara.
- Anti-rebote de 20 s (`SEGUNDOS_ESPERA`, 59).
- Ningún dato es opcional; ninguna columna acepta NULL.
- El administrador edita usuarios fijos, visitantes, incidentes y escenario; las bajas son lógicas.

## Historial de versiones del repositorio

Cada fase del proyecto es una etiqueta (`git checkout fase-1`, `git checkout fase-2`, `git checkout fase-3`; para volver, `git checkout main`). El trabajo entre fases se hizo en ramas `feature/...` que se fusionaron en `main`.

| Etiqueta | Rama | Qué agrega |
|---|---|---|
| `fase-1` | `main` | Conjunto residencial: consola con JOptionPane y listas en memoria |
| `fase-1b` | `feature/jerarquia-persona` | `GestorParqueaderos`, `Persona` abstracta (herencia y polimorfismo) y `TipoContexto` |
| `fase-2` | `feature/roles-y-contextos` | `UsuarioFijo`, `UbicacionDestino`, login por rol y menús según el contexto |
| `fase-2b` | `feature/paquetes` | Proyecto organizado en paquetes `modelo`, `logica` y `gui` |
| `fase-2c` | `feature/base-de-datos` | Scripts SQL de MySQL, clase `Conexion` y excepciones propias |
| `fase-2d` | `feature/camara-placas` | Lectura de placas con OpenCV + Tess4J y fuentes de video (Adapter y Factory) |
| `fase-2e` | `feature/modelo-persistente` | Nuevo modelo, DAO con JDBC y capa lógica (Facade y Builder) |
| `fase-2f` | `feature/interfaz-swing` | Interfaz Swing: login, vigilante con cámara y administrador; incluye la carpeta `tessdata` del OCR |
| `fase-3` | `feature/documentacion` | Este README con el Mapa del proyecto |

Entre `fase-2e` y `fase-2f` el programa aún no tiene ventana principal: se retiró la interfaz de diálogos de la fase 2 y la nueva Swing llega en `fase-2f`.
