/*
 * Interfaz principal del sistema. Implementa:
 *   1) Un login sencillo con dos roles (Administrador y Vigilante).
 *   2) Una configuracion de CONTEXTO que el Administrador elige al iniciar
 *      el sistema (o cambia despues): a partir de esa eleccion, TODOS los
 *      menus, formularios y mensajes usan el vocabulario correcto
 *      (Propietario/Visitante, Empleado/Visitante, Local/Cliente,
 *      Personal del Evento/Asistente...).
 * Toda la logica real vive en SistemaControlAcceso; esta clase solo
 * captura datos con JOptionPane y los envia.
 */
package com.github.santiago.fundamentos;

import javax.swing.JOptionPane;

/**
 *
 * @author salas
 */
public class InterfazPrincipal {

    // Clave simple del administrador. En un sistema real esto vendria
    // de una base de datos / hash, aqui se mantiene deliberadamente simple.
    private static final String CLAVE_ADMINISTRADOR = "admin123";

    public static void main(String[] args) {
        SistemaControlAcceso sistema = new SistemaControlAcceso();

        // El software no opera sin contexto: la primera vez, se obliga a
        // un administrador a elegir donde se va a usar (residencial,
        // centro comercial, oficina, concierto...) antes de mostrar
        // cualquier otro menu.
        if (sistema.getContextoActivo() == null) {
            JOptionPane.showMessageDialog(null,
                    "Primer uso del sistema.\nUn administrador debe elegir el contexto de uso.");
            if (!configurarContexto(sistema)) {
                return; // el administrador cerro el dialogo, no hay como continuar
            }
        }

        while (true) {
            Rol rol = login();
            if (rol == null) {
                // el usuario cerro el dialogo de login
                break;
            }

            if (rol == Rol.ADMINISTRADOR) {
                menuAdministrador(sistema);
            } else {
                menuVigilante(sistema);
            }
        }
    }

    // ==================== LOGIN ====================

    private static Rol login() {
        String menuLogin = """
            ========================================
                  SISTEMA DE CONTROL DE ACCESO
            ========================================

            1. Ingresar como Administrador
            2. Ingresar como Vigilante
            3. Salir

            Seleccione una opcion:
                      """;

        while (true) {
            String entrada = JOptionPane.showInputDialog(menuLogin);
            if (entrada == null) {
                return null;
            }

            switch (entrada.trim()) {
                case "1":
                    if (validarClaveAdministrador()) {
                        return Rol.ADMINISTRADOR;
                    }
                    JOptionPane.showMessageDialog(null, "Clave incorrecta.");
                    break;

                case "2":
                    return Rol.VIGILANTE;

                case "3":
                    return null;

                default:
                    JOptionPane.showMessageDialog(null, "Opcion invalida. Ingrese 1, 2 o 3.");
            }
        }
    }

    private static boolean validarClaveAdministrador() {
        String clave = JOptionPane.showInputDialog("Clave de administrador: ");
        return clave != null && clave.equals(CLAVE_ADMINISTRADOR);
    }

    // ==================== CONFIGURACION DE CONTEXTO ====================
    // Solo el Administrador elige donde se va a usar el software. Esta
    // eleccion es la que "cambia todo desde el inicio": una vez elegida,
    // se guarda en SistemaControlAcceso y de ahi la toman todos los menus.

    private static boolean configurarContexto(SistemaControlAcceso sistema) {
        TipoContexto[] opciones = TipoContexto.values();

        StringBuilder menu = new StringBuilder();
        menu.append("========================================\n");
        menu.append("      DONDE SE VA A USAR EL SOFTWARE\n");
        menu.append("========================================\n\n");
        for (int i = 0; i < opciones.length; i++) {
            menu.append(i + 1).append(". ").append(opciones[i].getDescripcionMenu()).append("\n");
        }
        menu.append("\nSeleccione una opcion:\n          ");

        while (true) {
            String entrada = JOptionPane.showInputDialog(menu.toString());
            if (entrada == null) {
                return false;
            }

            try {
                int opcion = Integer.parseInt(entrada.trim());
                if (opcion >= 1 && opcion <= opciones.length) {
                    TipoContexto elegido = opciones[opcion - 1];
                    sistema.configurarContexto(elegido);
                    JOptionPane.showMessageDialog(null,
                            "Contexto configurado: " + elegido.getNombreContexto() + "\n"
                            + "Usuario fijo = " + elegido.getEtiquetaUsuarioFijo() + "\n"
                            + "Visitante = " + elegido.getEtiquetaVisitante() + "\n"
                            + "Ubicacion = " + elegido.getEtiquetaUbicacion()
                            + " / Zona = " + elegido.getEtiquetaZona());
                    return true;
                }
                JOptionPane.showMessageDialog(null,
                        "Opcion invalida. Ingrese un numero entre 1 y " + opciones.length + ".");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un numero.");
            }
        }
    }

    // ==================== VISTA ADMINISTRADOR ====================
    // Permisos: registrar, modificar y eliminar UsuarioFijo, asignarles
    // su parqueadero fijo, y cambiar el contexto del sistema.

    private static void menuAdministrador(SistemaControlAcceso sistema) {
        int opcion = 0;

        do {
            TipoContexto contexto = sistema.getContextoActivo();
            String etiquetaFijo = contexto.getEtiquetaUsuarioFijo().toLowerCase();
            String etiquetaFijoPlural = contexto.getEtiquetaUsuarioFijoPlural().toLowerCase();

            String menu = """
                ========================================
                  PANEL DEL ADMINISTRADOR - %s
                ========================================

                1. Registrar %s
                2. Ver %s registrados
                3. Modificar %s
                4. Eliminar %s
                5. Cambiar contexto del sistema
                6. Cerrar sesion

                Seleccione una opcion:
                          """.formatted(contexto.getNombreContexto(),
                        etiquetaFijo, etiquetaFijoPlural, etiquetaFijo, etiquetaFijo);

            String entrada = JOptionPane.showInputDialog(menu);
            if (entrada == null) {
                break;
            }

            try {
                opcion = Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un numero entre 1 y 6.");
                continue;
            }

            switch (opcion) {
                case 1:
                    registrarUsuarioFijo(sistema);
                    break;
                case 2:
                    sistema.mostrarUsuariosFijos();
                    break;
                case 3:
                    modificarUsuarioFijo(sistema);
                    break;
                case 4:
                    eliminarUsuarioFijo(sistema);
                    break;
                case 5:
                    configurarContexto(sistema);
                    break;
                case 6:
                    JOptionPane.showMessageDialog(null, "Cerrando sesion de administrador...");
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opcion invalida. Ingrese un numero entre 1 y 6.");
            }

        } while (opcion != 6);
    }

    private static void registrarUsuarioFijo(SistemaControlAcceso sistema) {
        TipoContexto contexto = sistema.getContextoActivo();
        String etiquetaFijo = contexto.getEtiquetaUsuarioFijo();
        String etiquetaUbicacion = contexto.getEtiquetaUbicacion();
        String etiquetaZona = contexto.getEtiquetaZona();

        try {
            String nombre = JOptionPane.showInputDialog("Nombre del " + etiquetaFijo.toLowerCase() + ": ");
            String cedula = JOptionPane.showInputDialog("Cedula del " + etiquetaFijo.toLowerCase() + ": ");
            String identificadorUbicacion = JOptionPane.showInputDialog(
                    "Numero/nombre de " + etiquetaUbicacion.toLowerCase() + ": ");
            String zona = JOptionPane.showInputDialog(
                    "Numero/nombre de " + etiquetaZona.toLowerCase() + ": ");
            int numeroParqueadero = Integer.parseInt(JOptionPane.showInputDialog("Numero de parqueadero fijo: "));
            String placa = JOptionPane.showInputDialog("Placa del vehiculo: ");

            UsuarioFijo usuario = sistema.registrarUsuarioFijo(
                    nombre, cedula, identificadorUbicacion, zona, numeroParqueadero, placa);

            usuario.mostrarInformacion(contexto);

            JOptionPane.showMessageDialog(null,
                    etiquetaFijo + " registrado correctamente.\n"
                    + etiquetaUbicacion + ": " + usuario.getUbicacion() + "\n"
                    + "Parqueadero fijo asignado: #" + numeroParqueadero);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El numero de parqueadero debe ser numerico. Intente de nuevo.");
        }
    }

    private static void modificarUsuarioFijo(SistemaControlAcceso sistema) {
        String etiquetaFijo = sistema.getContextoActivo().getEtiquetaUsuarioFijo();

        String cedula = JOptionPane.showInputDialog("Cedula del " + etiquetaFijo.toLowerCase() + " a modificar: ");
        if (cedula == null) {
            return;
        }
        String nuevoNombre = JOptionPane.showInputDialog("Nuevo nombre (dejar vacio para no cambiar): ");
        String nuevaCedula = JOptionPane.showInputDialog("Nueva cedula (dejar vacio para no cambiar): ");

        boolean actualizado = sistema.modificarUsuarioFijo(cedula, nuevoNombre, nuevaCedula);
        JOptionPane.showMessageDialog(null,
                actualizado ? etiquetaFijo + " actualizado correctamente."
                            : "No se encontro un " + etiquetaFijo.toLowerCase() + " con esa cedula.");
    }

    private static void eliminarUsuarioFijo(SistemaControlAcceso sistema) {
        String etiquetaFijo = sistema.getContextoActivo().getEtiquetaUsuarioFijo();

        String cedula = JOptionPane.showInputDialog("Cedula del " + etiquetaFijo.toLowerCase() + " a eliminar: ");
        if (cedula == null) {
            return;
        }
        boolean eliminado = sistema.eliminarUsuarioFijo(cedula);
        JOptionPane.showMessageDialog(null,
                eliminado ? etiquetaFijo + " eliminado y su parqueadero fijo liberado."
                          : "No se encontro un " + etiquetaFijo.toLowerCase() + " con esa cedula.");
    }

    // ==================== VISTA VIGILANTE ====================
    // Permisos: registrar entrada/salida de Visitantes, asignarles
    // parqueaderos disponibles y reportar Incidentes.

    private static void menuVigilante(SistemaControlAcceso sistema) {
        int opcion = 0;

        do {
            TipoContexto contexto = sistema.getContextoActivo();
            String etiquetaVisitante = contexto.getEtiquetaVisitante().toLowerCase();
            String etiquetaVisitantePlural = contexto.getEtiquetaVisitantePlural().toLowerCase();

            String menu = """
                ========================================
                    PANEL DEL VIGILANTE - %s
                ========================================

                1. Registrar entrada de %s
                2. Registrar salida de %s
                3. Ver %s registrados
                4. Reportar incidente
                5. Ver registro de incidentes
                6. Cerrar sesion

                Seleccione una opcion:
                          """.formatted(contexto.getNombreContexto(),
                        etiquetaVisitante, etiquetaVisitante, etiquetaVisitantePlural);

            String entrada = JOptionPane.showInputDialog(menu);
            if (entrada == null) {
                break;
            }

            try {
                opcion = Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un numero entre 1 y 6.");
                continue;
            }

            switch (opcion) {
                case 1:
                    registrarVisitante(sistema);
                    break;
                case 2:
                    registrarSalidaVisitante(sistema);
                    break;
                case 3:
                    sistema.mostrarVisitantes();
                    break;
                case 4:
                    reportarIncidente(sistema);
                    break;
                case 5:
                    sistema.mostrarIncidentes();
                    break;
                case 6:
                    JOptionPane.showMessageDialog(null, "Cerrando sesion de vigilante...");
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opcion invalida. Ingrese un numero entre 1 y 6.");
            }

        } while (opcion != 6);
    }

    private static void registrarVisitante(SistemaControlAcceso sistema) {
        TipoContexto contexto = sistema.getContextoActivo();
        String etiquetaVisitante = contexto.getEtiquetaVisitante();

        String placa = JOptionPane.showInputDialog("Placa del vehiculo del " + etiquetaVisitante.toLowerCase() + ": ");
        String nombre = JOptionPane.showInputDialog("Nombre del " + etiquetaVisitante.toLowerCase() + ": ");
        String cedula = JOptionPane.showInputDialog("Cedula del " + etiquetaVisitante.toLowerCase() + ": ");
        String destino = JOptionPane.showInputDialog(
                "Destino (" + contexto.getEtiquetaUbicacion().toLowerCase() + " / "
                + contexto.getEtiquetaZona().toLowerCase() + "): ");

        Visitante visitante = sistema.registrarVisitante(placa, nombre, cedula, destino);

        visitante.mostrarInformacion(contexto);

        if (visitante.getParqueaderoAsignado() != null) {
            JOptionPane.showMessageDialog(null,
                    etiquetaVisitante + " registrado correctamente.\n"
                    + "Se dirige a: " + destino + "\n"
                    + "Parqueadero asignado: #" + visitante.getParqueaderoAsignado().getNumero());
        } else {
            JOptionPane.showMessageDialog(null,
                    etiquetaVisitante + " registrado, pero no hay parqueaderos disponibles en este momento.");
        }
    }

    private static void registrarSalidaVisitante(SistemaControlAcceso sistema) {
        String placa = JOptionPane.showInputDialog("Placa del vehiculo que sale: ");
        if (placa == null) {
            return;
        }
        boolean salioCorrectamente = sistema.registrarSalidaVisitante(placa);
        JOptionPane.showMessageDialog(null,
                salioCorrectamente ? "Salida registrada. Parqueadero liberado."
                                    : "No se encontro un registro activo con esa placa.");
    }

    private static void reportarIncidente(SistemaControlAcceso sistema) {
        String descripcion = JOptionPane.showInputDialog("Descripcion del incidente: ");

        Incidente incidente = sistema.reportarIncidente(descripcion);
        incidente.mostrarInformacion();

        JOptionPane.showMessageDialog(null, "Incidente reportado correctamente.");
    }
}
