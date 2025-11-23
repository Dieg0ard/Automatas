package automatas.algoritmos;

import java.util.*;
import java.util.stream.Collectors;
import automatas.core.AFD;
import automatas.core.AFND;
import automatas.io.EscritorAutomata;
import java.io.IOException;

public class Conversion {
    private AFND afnd;
    private boolean debug = false;
    
    public Conversion(AFND afnd) {
        this.afnd = afnd;
    }

    public AFD convertir() {
        // Inicializar estructuras para el AFD
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
            
            if (debug) {
                System.out.println("\n--- Paso " + (++pasos) + " ---");
                System.out.println("Procesando estado " + idActual + ": " + estadoActual);
            }
            
            // Verificar si es final
            if (esFinal(estadoActual)) {
                finales.add(idActual);
                if (debug) System.out.println("  → Es estado FINAL");
            }
            
            // Calcular transiciones
            Map<Character, Integer> transEstado = new HashMap<>();
            for (Character simbolo : alfabeto) {
                Set<String> destino = mover(estadoActual, simbolo);
                
                if (debug) {
                    System.out.println("  Con '" + simbolo + "':");
                    System.out.println("    Destinos directos: " + getDestinosDirectos(estadoActual, simbolo));
                    System.out.println("    Después de ε-clausura: " + destino);
                }
                
                if (!destino.isEmpty()) {
                    Integer idDestino;
                    if (estadoAId.containsKey(destino)) {
                        idDestino = estadoAId.get(destino);
                        if (debug) System.out.println("    → Ya existe como estado " + idDestino);
                    } else {
                        idDestino = idAEstado.size();
                        estadoAId.put(destino, idDestino);
                        idAEstado.add(destino);
                        cola.offer(idDestino);
                        if (debug) System.out.println("    → NUEVO estado " + idDestino + " descubierto");
                    }
                    transEstado.put(simbolo, idDestino);
                }
            }
            transiciones.put(idActual, transEstado);
        }
        
        if (debug) {
            System.out.println("\n========================================");
            System.out.println("RESULTADO FINAL");
            System.out.println("========================================");
            System.out.println("Total de estados generados: " + idAEstado.size());
            System.out.println("\nMapeo de estados:");
            for (int i = 0; i < idAEstado.size(); i++) {
                System.out.println("  Estado " + i + ": " + idAEstado.get(i));
            }
        }
        
        // Construir AFD final
        return construirAFDFinal(idAEstado, transiciones, finales, alfabeto);
    }
    
    /**
     * ε-clausura: todos los estados alcanzables solo con transiciones epsilon
     */
    private Set<String> clausuraEpsilon(Set<String> estados) {
        Set<String> resultado = new HashSet<>(estados);
        Stack<String> pila = new Stack<>();
        pila.addAll(estados);
        
        while (!pila.isEmpty()) {
            String actual = pila.pop();
            
            Map<Character, Set<String>> trans = afnd.getTransiciones().get(actual);
            if (trans != null && trans.containsKey(null)) {
                for (String destino : trans.get(null)) {
                    if (!resultado.contains(destino)) {
                        resultado.add(destino);
                        pila.push(destino);
                    }
                }
            }
        }
        
        return resultado;
    }
    
    /**
     * mover(T, a) = ε-clausura(δ(T, a))
     */
    private Set<String> mover(Set<String> estados, Character simbolo) {
        Set<String> destinos = new HashSet<>();
        
        for (String estado : estados) {
            Map<Character, Set<String>> trans = afnd.getTransiciones().get(estado);
            if (trans != null && trans.containsKey(simbolo)) {
                destinos.addAll(trans.get(simbolo));
            }
        }
        
        return destinos.isEmpty() ? destinos : clausuraEpsilon(destinos);
    }
    
    /**
     * Solo para debug: obtener destinos directos sin ε-clausura
     */
    private Set<String> getDestinosDirectos(Set<String> estados, Character simbolo) {
        Set<String> destinos = new HashSet<>();
        for (String estado : estados) {
            Map<Character, Set<String>> trans = afnd.getTransiciones().get(estado);
            if (trans != null && trans.containsKey(simbolo)) {
                destinos.addAll(trans.get(simbolo));
            }
        }
        return destinos;
    }
    
    private boolean esFinal(Set<String> conjunto) {
        return conjunto.stream().anyMatch(e -> afnd.getEstadosFinales().contains(e));
    }
    
    private AFD construirAFDFinal(
            List<Set<String>> estados,
            Map<Integer, Map<Character, Integer>> transiciones,
            Set<Integer> finales,
            Set<Character> alfabeto) throws IOException {
        
        // Usar nombres simples: q0, q1, q2, etc.
        Map<Integer, String> nombres = new HashMap<>();
        for (int i = 0; i < estados.size(); i++) {
            nombres.put(i, "q" + i);
        }
        
        Set<String> estadosStr = new HashSet<>(nombres.values());
        Set<String> finalesStr = finales.stream()
                .map(nombres::get)
                .collect(Collectors.toSet());
        
        Map<String, Map<Character, String>> transStr = new HashMap<>();
        for (Map.Entry<Integer, Map<Character, Integer>> entry : transiciones.entrySet()) {
            String origen = nombres.get(entry.getKey());
            Map<Character, String> trans = new HashMap<>();
            for (Map.Entry<Character, Integer> t : entry.getValue().entrySet()) {
                trans.put(t.getKey(), nombres.get(t.getValue()));
            }
            transStr.put(origen, trans);
        }
        
        if (debug) {
            System.out.println("\nMapeo final de nombres:");
            for (int i = 0; i < estados.size(); i++) {
                System.out.println("  " + nombres.get(i) + " = " + estados.get(i));
            }
        }
        
        AFD afd = new AFD(estadosStr, alfabeto, transStr, nombres.get(0), finalesStr);
        
        String userHome = System.getProperty("user.home");
        String rutaCSV = userHome + "/.automatas/csv/afd.csv";
        EscritorAutomata.guardarAFD(afd, rutaCSV);
        
        return afd;
    }
    
    /**
     * Método auxiliar para mostrar el proceso de conversión (debug)
     */
    public void mostrarProcesoConversion() {
        System.out.println("--- PROCESO DE CONVERSIÓN AFND -> AFD ---");
        System.out.println("AFND Original:");
        afnd.mostrar();
        System.out.println("  Transiciones:");
        mostrarTransicionesAFND();
        
        AFD afd = convertir();
        
        System.out.println("\nAFD Resultante:");
        afd.mostrar();
        System.out.println("  Transiciones:");
        mostrarTransicionesAFD(afd);
    }
    
    private void mostrarTransicionesAFND() {
        for (String estado : afnd.getEstados()) {
            Map<Character, Set<String>> trans = afnd.getTransiciones().get(estado);
            if (trans == null || trans.isEmpty()) {
                System.out.println("    " + estado + ": (sin transiciones)");
            } else {
                for (Map.Entry<Character, Set<String>> e : trans.entrySet()) {
                    String sim = e.getKey() == null ? "ε" : "'" + e.getKey() + "'";
                    System.out.println("    δ(" + estado + ", " + sim + ") = " + e.getValue());
                }
            }
        }
    }
    
    public void mostrarProcesoConversion() throws IOException {
        this.debug = true;
        convertir();
        this.debug = false;
    }
}