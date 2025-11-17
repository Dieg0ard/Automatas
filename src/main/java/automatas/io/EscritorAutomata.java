package automatas.io;

import automatas.core.AFD;
import automatas.core.AFND;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class EscritorAutomata {

    public static void guardarAFD(AFD afd, String rutaArchivo) throws IOException {
        afd.mostrar();
        File archivo = new File(rutaArchivo);

        // Crear directorios padre si no existen
        if (archivo.getParentFile() != null) {
            archivo.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            // Estado inicial
            writer.write("#INICIAL," + afd.getEstadoInicial());
            writer.newLine();

            // Estados finales
            writer.write("#FINALES," + String.join(",", afd.getEstadosFinales()));
            writer.newLine();
            writer.newLine();

            // Transiciones
            Map<String, Map<Character, String>> transiciones = afd.getTransiciones();
            for (String origen : transiciones.keySet()) {
                for (Character simbolo : transiciones.get(origen).keySet()) {
                    String destino = transiciones.get(origen).get(simbolo);
                    writer.write(origen + "," + simbolo + "," + destino);
                    writer.newLine();
                }
            }
        }
    }

    public static void guardarAFND(AFND afnd, String rutaArchivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            // Estado inicial
            writer.write("#INICIAL," + afnd.getEstadoInicial());
            writer.newLine();

            // Estados finales
            writer.write("#FINALES," + String.join(";", afnd.getEstadosFinales()));
            writer.newLine();
            writer.newLine();

            // Transiciones
            Map<String, Map<Character, Set<String>>> transiciones = afnd.getTransiciones();
            for (String origen : transiciones.keySet()) {
                for (Character simbolo : transiciones.get(origen).keySet()) {
                    for (String destino : transiciones.get(origen).get(simbolo)) {
                        writer.write(origen + "," + simbolo + "," + destino);
                        writer.newLine();
                    }
                }
            }
        }
    }
}
