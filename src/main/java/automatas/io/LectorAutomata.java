/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package automatas.io;

import automatas.core.AFD;
import automatas.core.AFND;
import automatas.core.Automata;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author diego
 */
public class LectorAutomata {

    public static Automata leerDesdeCSV(String rutaArchivo) throws IOException {

        Set<String> estados = new HashSet<>();
        Set<Character> alfabeto = new HashSet<>();
        Map<String, Map<Character, Set<String>>> transiciones = new HashMap<>();
        String estadoInicial = null;
        Set<String> estadosFinales = new HashSet<>();

        List<String> lineas = Files.readAllLines(Paths.get(rutaArchivo));

        for (String linea : lineas) {
            if (linea.trim().isEmpty() || linea.startsWith("#")) continue;

            // formato: origen,símbolo,destino
            String[] partes = linea.split(",");
            if (partes.length < 3) continue;

            String origen = partes[0].trim();
            String simboloStr = partes[1].trim();
            String destino = partes[2].trim();

            estados.add(origen);
            estados.add(destino);

            // transición vacía (ε)
            Character simbolo = simboloStr.equals("ε") ? null : simboloStr.charAt(0);
            if (simbolo != null) alfabeto.add(simbolo);

            transiciones
                .computeIfAbsent(origen, k -> new HashMap<>())
                .computeIfAbsent(simbolo, k -> new HashSet<>())
                .add(destino);
        }

        // Por simplicidad, asumimos que el archivo incluye líneas especiales:
        // #INICIAL:q0
        // #FINALES:q2;q3
        for (String linea : lineas) {
            if (linea.startsWith("#INICIAL:")) {
                estadoInicial = linea.substring(9).trim();
            } else if (linea.startsWith("#FINALES:")) {
                String[] finales = linea.substring(9).split(";");
                for (String f : finales) estadosFinales.add(f.trim());
            }
        }

        // Verificar si es determinista
        boolean esDeterminista = esDeterminista(transiciones);

        if (esDeterminista) {
            // Convertir las transiciones a formato de AFD
            Map<String, Map<Character, String>> transicionesAFD = new HashMap<>();
            for (var e : transiciones.entrySet()) {
                Map<Character, String> mapaEstado = new HashMap<>();
                for (var t : e.getValue().entrySet()) {
                    if (t.getKey() != null && t.getValue().size() == 1) {
                        mapaEstado.put(t.getKey(), t.getValue().iterator().next());
                    }
                }
                transicionesAFD.put(e.getKey(), mapaEstado);
            }

            return new AFD(estados, alfabeto, transicionesAFD, estadoInicial, estadosFinales);
        } else {
            return new AFND(estados, alfabeto, transiciones, estadoInicial, estadosFinales);
        }
    }

    private static boolean esDeterminista(Map<String, Map<Character, Set<String>>> transiciones) {
        for (var estado : transiciones.values()) {
            for (var entrada : estado.entrySet()) {
                Character simbolo = entrada.getKey();
                Set<String> destinos = entrada.getValue();
                if (simbolo == null) return false;           // transición ε
                if (destinos.size() > 1) return false;       // no determinista
            }
        }
        return true;
    }
}
