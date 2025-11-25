package automatas.io;

import automatas.core.AFD;
import automatas.core.AFND;
import automatas.core.AP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class EscritorAutomata {
    
    public static void guardarAP(AP ap, String rutaArchivo) throws IOException {
    File archivo = new File(rutaArchivo);
    // Crear directorios padre si no existen
    if (archivo.getParentFile() != null) {
        archivo.getParentFile().mkdirs();
    }
    
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
        // Estado inicial
        writer.write("#INICIAL," + ap.getEstadoInicial());
        writer.newLine();
        
        // Estados finales
        writer.write("#FINALES," + String.join(",", ap.getEstadosFinales()));
        writer.newLine();
        
        // Símbolo inicial de pila
        writer.write("#SIMBOLO_PILA," + ap.getSimboloInicialPila());
        writer.newLine();
        
        // Modo de aceptación
        writer.write("#ACEPTA_POR," + (ap.aceptaPorEstadoFinal() ? "ESTADO_FINAL" : "PILA_VACIA"));
        writer.newLine();
        
        writer.newLine();
        
        // Transiciones: origen,simboloEntrada,topePila,destino,reemplazo
        for (AP.Transicion t : ap.getTransiciones()) {
            String entrada = t.getSimboloEntrada() == null ? "ε" : t.getSimboloEntrada().toString();
            String reemplazo = t.getReemplazoEnPila().isEmpty() ? "ε" : t.getReemplazoEnPila();
            
            writer.write(t.getEstadoOrigen() + "," + 
                        entrada + "," + 
                        t.getTopePila() + "," + 
                        t.getEstadoDestino() + "," + 
                        reemplazo);
            writer.newLine();
        }
    }
}

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
