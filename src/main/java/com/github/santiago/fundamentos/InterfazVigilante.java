/*
 * Interfaz que vera el vigilante de seguridad.
 * Envia los datos capturados a SistemaControlAcceso, que es quien
 * contiene la logica real (crea Propietario, Visitante, Incidente, etc.)
 */
package com.github.santiago.fundamentos;

import javax.swing.JOptionPane;

/**
 *
 * @author salas
 */
public class InterfazVigilante {

    public static void main(String[] args) {

        SistemaControlAcceso sistema = new SistemaControlAcceso();
        int opcion = 0;

        do {

            String menu = """
                ========================================
                     CONTROL DE ACCESO - VIGILANTE
                ========================================

                1. Registrar propietario
                2. Ver registro de propietarios
                3. Registrar visitante
                4. Ver registro de visitantes
                5. Reportar incidente
                6. Ver registro de incidentes
                7. Salir

                Seleccione una opcion:
                          """;

            String entrada = JOptionPane.showInputDialog(menu);

            if (entrada == null) {
                // el vigilante cerro el dialogo, se sale del menu
                break;
            }

            try {
                opcion = Integer.parseInt(entrada.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un numero entre 1 y 7.");
                continue;
            }

            switch (opcion) {
                case 1:
                    registrarPropietario(sistema);
                    break;

                case 2:
                    sistema.mostrarPropietarios();
                    break;

                case 3:
                    registrarVisitante(sistema);
                    break;

                case 4:
                    sistema.mostrarVisitantes();
                    break;

                case 5:
                    reportarIncidente(sistema);
                    break;

                case 6:
                    sistema.mostrarIncidentes();
                    break;

                case 7:
                    JOptionPane.showMessageDialog(null, "Cerrando sesion del vigilante...");
                    break;

                default:
                    JOptionPane.showMessageDialog(null, "Opcion invalida. Ingrese un numero entre 1 y 7.");
            }

        } while (opcion != 7);
    }

    public static void registrarPropietario(SistemaControlAcceso sistema) {
        try {
            String nombre = JOptionPane.showInputDialog("Nombre del propietario: ");
            String cedula = JOptionPane.showInputDialog("Cedula del propietario: ");
            int numeroApartamento = Integer.parseInt(JOptionPane.showInputDialog("Numero de apartamento: "));
            int numeroTorre = Integer.parseInt(JOptionPane.showInputDialog("Numero de torre: "));
            int numeroParqueadero = Integer.parseInt(JOptionPane.showInputDialog("Numero de parqueadero: "));
            String placa = JOptionPane.showInputDialog("Placa del vehiculo: ");

            Propietario propietario = sistema.registrarPropietario(
                    nombre, cedula, numeroApartamento, numeroTorre, numeroParqueadero, placa);

            propietario.mostrarInformacion(sistema.getContextoActivo());

            JOptionPane.showMessageDialog(null,
                    "Propietario registrado correctamente.\n"
                    + "Apartamento: " + numeroApartamento + " - Torre: " + numeroTorre + "\n"
                    + "Parqueadero fijo asignado: #" + numeroParqueadero);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Apartamento, torre y parqueadero deben ser numeros. Intente de nuevo.");
        }
    }

    public static void registrarVisitante(SistemaControlAcceso sistema) {
        String placa = JOptionPane.showInputDialog("Placa del vehiculo del visitante: ");
        String nombre = JOptionPane.showInputDialog("Nombre del visitante: ");
        String cedula = JOptionPane.showInputDialog("Cedula del visitante: ");
        String apartamentoDestino = JOptionPane.showInputDialog("Apartamento al que se dirige: ");

        Visitante visitante = sistema.registrarVisitante(placa, nombre, cedula, apartamentoDestino);

        visitante.mostrarInformacion(sistema.getContextoActivo());

        if (visitante.getParqueaderoAsignado() != null) {
            JOptionPane.showMessageDialog(null,
                    "Visitante registrado correctamente.\n"
                    + "Se dirige al apartamento: " + apartamentoDestino + "\n"
                    + "Parqueadero de visitante asignado: #" + visitante.getParqueaderoAsignado().getNumero());
        } else {
            JOptionPane.showMessageDialog(null,
                    "Visitante registrado, pero no hay parqueaderos de visitantes disponibles en este momento.");
        }
    }

    public static void reportarIncidente(SistemaControlAcceso sistema) {
        String descripcion = JOptionPane.showInputDialog("Descripcion del incidente: ");

        Incidente incidente = sistema.reportarIncidente(descripcion);
        incidente.mostrarInformacion();

        JOptionPane.showMessageDialog(null, "Incidente reportado correctamente.");
    }
}
