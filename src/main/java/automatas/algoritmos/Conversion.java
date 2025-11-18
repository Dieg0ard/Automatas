package automatas.algoritmos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import automatas.core.AFD;
import automatas.core.AFND;

public class Conversion {
    private AFND afnd;

    public Conversion(AFND afnd) {
        this.afnd = afnd;
    }

    public AFD convertir() {
        // Paso 1: Inicializar estructuras para el AFD
        Set<String> estadosAFD = new HashSet<>();
        Map<String, Map<Character, String>> transicionesAFD = new HashMap<>();
        Set<String> estadosFinalesAFD = new HashSet<>();
        
        // El alfabeto del AFD es el mismo (sin epsilon si existe)
        Set<Character> alfabetoAFD = new HashSet<>(afnd.getAlfabeto());
        alfabetoAFD.remove(null); // Remover transiciones epsilon
        
        // Estado inicial del AFD es el estado inicial del AFND
        String estadoInicialAFD = afnd.getEstadoInicial();
        estadosAFD.add(estadoInicialAFD);
        
        // Cola para procesar estados pendientes
        Queue<String> estadosPorProcesar = new LinkedList<>();
        estadosPorProcesar.offer(estadoInicialAFD);
        
        // Procesar estados hasta que no queden nuevos
        while (!estadosPorProcesar.isEmpty()) {
            String estadoActual = estadosPorProcesar.poll();
            
            // Si el estado actual es compuesto, verificar si es final
            if (esEstadoFinal(estadoActual)) {
                estadosFinalesAFD.add(estadoActual);
            }
            
            // Inicializar transiciones para este estado
            transicionesAFD.put(estadoActual, new HashMap<>());
            
            // Para cada símbolo del alfabeto
            for (Character simbolo : alfabetoAFD) {
                Set<String> destinosAFND = calcularTransicion(estadoActual, simbolo);
                
                if (!destinosAFND.isEmpty()) {
                    // Crear nombre del estado destino (fusionar si hay múltiples)
                    String estadoDestino = fusionarEstados(destinosAFND);
                    
                    // Agregar la transición
                    transicionesAFD.get(estadoActual).put(simbolo, estadoDestino);
                    
                    // Si es un estado nuevo, agregarlo a la cola
                    if (!estadosAFD.contains(estadoDestino)) {
                        estadosAFD.add(estadoDestino);
                        estadosPorProcesar.offer(estadoDestino);
                    }
                }
            }
        }
        
        return new AFD(estadosAFD, alfabetoAFD, transicionesAFD, estadoInicialAFD, estadosFinalesAFD);
    }
    
    /**
     * Calcula la transición para un estado del AFD y un símbolo
     * Si el estado es compuesto, fusiona las transiciones de sus componentes
     */
    private Set<String> calcularTransicion(String estadoAFD, Character simbolo) {
        Set<String> destinos = new HashSet<>();
        
        // Obtener los estados componentes del AFND
        Set<String> componentes = obtenerComponentes(estadoAFD);
        
        // Para cada componente, obtener sus destinos con este símbolo
        for (String componente : componentes) {
            Map<Character, Set<String>> transComponente = afnd.getTransiciones().get(componente);
            if (transComponente != null) {
                Set<String> destinosComponente = transComponente.get(simbolo);
                if (destinosComponente != null) {
                    destinos.addAll(destinosComponente);
                }
            }
        }
        
        return destinos;
    }
    
    /**
     * Fusiona un conjunto de estados en un solo nombre
     * Ejemplo: {"A", "B", "C"} -> "ABC"
     * Letras repetidas cuentan una sola vez
     */
    private String fusionarEstados(Set<String> estados) {
        if (estados.size() == 1) {
            return estados.iterator().next(); // Estado simple
        }
        
        // Para estados múltiples, fusionar nombres eliminando duplicados
        String nombres = estados.stream()
            .flatMap(estado -> estado.chars().mapToObj(c -> (char) c))
            .distinct()
            .sorted()
            .map(String::valueOf)
            .collect(Collectors.joining());
        
        return nombres;
    }
    
    /**
     * Obtiene los componentes de un estado del AFD
     * Si es estado simple, devuelve un conjunto con solo ese estado
     * Si es estado compuesto, separa por caracteres individuales
     */
    private Set<String> obtenerComponentes(String estadoAFD) {
        Set<String> componentes = new HashSet<>();
        
        // Si el estado tiene más de un carácter, asumimos que es compuesto
        if (estadoAFD.length() > 1) {
            for (char c : estadoAFD.toCharArray()) {
                componentes.add(String.valueOf(c));
            }
        } else {
            componentes.add(estadoAFD);
        }
        
        return componentes;
    }
    
    /**
     * Determina si un estado del AFD es final
     * Un estado es final si alguno de sus componentes es final en el AFND original
     */
    private boolean esEstadoFinal(String estadoAFD) {
        Set<String> componentes = obtenerComponentes(estadoAFD);
        
        for (String componente : componentes) {
            if (afnd.getEstadosFinales().contains(componente)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Método auxiliar para mostrar el proceso de conversión (debug)
     */
    public void mostrarProcesoConversion() {
        System.out.println("=== PROCESO DE CONVERSIÓN AFND -> AFD ===");
        System.out.println("AFND Original:");
        System.out.println("  Estados: " + afnd.getEstados());
        System.out.println("  Estado inicial: " + afnd.getEstadoInicial());
        System.out.println("  Estados finales: " + afnd.getEstadosFinales());
        System.out.println("  Alfabeto: " + afnd.getAlfabeto());
        System.out.println("  Transiciones:");
        mostrarTransicionesAFND();
        
        AFD afd = convertir();
        
        System.out.println("\nAFD Resultante:");
        System.out.println("  Estados: " + afd.getEstados());
        System.out.println("  Estado inicial: " + afd.getEstadoInicial());
        System.out.println("  Estados finales: " + afd.getEstadosFinales());
        System.out.println("  Alfabeto: " + afd.getAlfabeto());
        System.out.println("  Transiciones:");
        mostrarTransicionesAFD(afd);
    }
    
    private void mostrarTransicionesAFND() {
        for (String estado : afnd.getEstados()) {
            Map<Character, Set<String>> trans = afnd.getTransiciones().get(estado);
            if (trans != null) {
                for (Map.Entry<Character, Set<String>> entry : trans.entrySet()) {
                    String simbolo = entry.getKey() == null ? "ε" : entry.getKey().toString();
                    System.out.println("    " + estado + " --" + simbolo + "--> " + entry.getValue());
                }
            }
        }
    }
    
    private void mostrarTransicionesAFD(AFD afd) {
        for (String estado : afd.getEstados()) {
            Map<Character, String> trans = afd.getTransiciones().get(estado);
            if (trans != null) {
                for (Map.Entry<Character, String> entry : trans.entrySet()) {
                    System.out.println("    " + estado + " --" + entry.getKey() + "--> " + entry.getValue());
                }
            }
        }
    }
}
