package automatas.visual;

import automatas.core.AFD;
import automatas.core.AFND;
import automatas.core.AP;
import automatas.core.Automata;

import java.util.*;

public class AutomataDotGenerator {

    /* -------------------- AFD -------------------- */

    public static String generarDot(AFD afd) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph Automata {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [shape=circle];\n\n");

        // Estados
        for (String estado : afd.getEstados()) {
            if (afd.getEstadosFinales().contains(estado)) {
                sb.append("    ").append(estado).append(" [shape=doublecircle];\n");
            } else {
                sb.append("    ").append(estado).append(";\n");
            }
        }
        sb.append("\n");

        // Estado inicial
        sb.append("    start [shape=point];\n");
        sb.append("    start -> ").append(afd.getEstadoInicial()).append(";\n\n");

        // AGRUPACIÓN DE TRANSICIONES
        Map<String, Map<String, Set<String>>> agrupado = new HashMap<>();

        for (String origen : afd.getEstados()) {
            var mapaSimbolos = afd.getTransiciones().get(origen);
            if (mapaSimbolos == null) continue;

            for (var entry : mapaSimbolos.entrySet()) {
                String destino = entry.getValue();
                char simbolo = entry.getKey();

                agrupado
                    .computeIfAbsent(origen, k -> new HashMap<>())
                    .computeIfAbsent(destino, k -> new TreeSet<>())
                    .add(String.valueOf(simbolo));
            }
        }

        // Imprimir agrupado
        for (var origenEntry : agrupado.entrySet()) {
            String origen = origenEntry.getKey();
            for (var destEntry : origenEntry.getValue().entrySet()) {
                String destino = destEntry.getKey();
                String label = String.join(",", destEntry.getValue());
                sb.append("    ").append(origen).append(" -> ").append(destino)
                        .append(" [label=\"").append(label).append("\"];\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }


    /* -------------------- AFND -------------------- */

    public static String generarDot(AFND afnd) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph Automata {\n");
        sb.append("    rankdir=LR;\n");
        sb.append("    node [shape=circle];\n\n");

        // Estados
        for (String estado : afnd.getEstados()) {
            if (afnd.getEstadosFinales().contains(estado)) {
                sb.append("    ").append(estado).append(" [shape=doublecircle];\n");
            } else {
                sb.append("    ").append(estado).append(";\n");
            }
        }
        sb.append("\n");

        // Estado inicial
        sb.append("    start [shape=point];\n");
        sb.append("    start -> ").append(afnd.getEstadoInicial()).append(";\n\n");

        // AGRUPACIÓN DE TRANSICIONES
        Map<String, Map<String, Set<String>>> agrupado = new HashMap<>();

        for (String origen : afnd.getEstados()) {
            var mapaSimbolos = afnd.getTransiciones().get(origen);
            if (mapaSimbolos == null) continue;

            for (var entry : mapaSimbolos.entrySet()) {
                Character simbolo = entry.getKey();
                Set<String> destinos = entry.getValue();
                String s = simbolo == null ? "ε" : simbolo.toString();

                for (String destino : destinos) {
                    agrupado
                        .computeIfAbsent(origen, k -> new HashMap<>())
                        .computeIfAbsent(destino, k -> new TreeSet<>())
                        .add(s);
                }
            }
        }

        // Imprimir agrupado
        for (var origenEntry : agrupado.entrySet()) {
            String origen = origenEntry.getKey();
            for (var destEntry : origenEntry.getValue().entrySet()) {
                String destino = destEntry.getKey();
                String label = String.join(",", destEntry.getValue());
                sb.append("    ").append(origen).append(" -> ").append(destino)
                        .append(" [label=\"").append(label).append("\"];\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }
    
    /**
 * Genera representación DOT (GraphViz) del autómata de pila
 * @param ap Autómata de pila a representar
 * @return Código DOT para visualizar el autómata
 */
public static String generarDot(AP ap) {
    StringBuilder dot = new StringBuilder();
    
    // Cabecera del grafo
    dot.append("digraph AP {\n");
    dot.append("  rankdir=LR;\n");
    dot.append("  node [shape=circle];\n\n");
    
    // Nodo invisible para la flecha de inicio
    dot.append("  __start [shape=none, label=\"\"];\n");
    dot.append("  __start -> \"").append(ap.getEstadoInicial()).append("\";\n\n");
    
    // Estados finales con doble círculo
    if (!ap.getEstadosFinales().isEmpty()) {
        dot.append("  node [shape=doublecircle];\n");
        for (String estadoFinal : ap.getEstadosFinales()) {
            dot.append("  \"").append(estadoFinal).append("\";\n");
        }
        dot.append("\n");
    }
    
    // Estados normales
    dot.append("  node [shape=circle];\n\n");
    
    // Agrupar transiciones por (origen, destino) para combinar etiquetas
    Map<String, List<String>> transicionesAgrupadas = new HashMap<>();
    
    for (AP.Transicion t : ap.getTransiciones()) {
        String clave = t.getEstadoOrigen() + " -> " + t.getEstadoDestino();
        
        // Formatear la etiqueta de la transición
        String simboloEntrada = t.getSimboloEntrada() == null ? "ε" : t.getSimboloEntrada().toString();
        String reemplazo = t.getReemplazoEnPila().isEmpty() ? "ε" : t.getReemplazoEnPila();
        String etiqueta = simboloEntrada + ", " + t.getTopePila() + " → " + reemplazo;
        
        transicionesAgrupadas.computeIfAbsent(clave, k -> new ArrayList<>()).add(etiqueta);
    }
    
    // Generar las transiciones
    dot.append("  // Transiciones\n");
    for (Map.Entry<String, List<String>> entry : transicionesAgrupadas.entrySet()) {
        String[] partes = entry.getKey().split(" -> ");
        String origen = partes[0];
        String destino = partes[1];
        
        // Combinar múltiples etiquetas con saltos de línea
        String etiquetaCombinada = String.join("\\n", entry.getValue());
        
        dot.append("  \"").append(origen).append("\" -> \"").append(destino)
           .append("\" [label=\"").append(etiquetaCombinada).append("\"];\n");
    }
    
    // Nota sobre el modo de aceptación
    dot.append("\n  // Modo de aceptación: ")
       .append(ap.aceptaPorEstadoFinal() ? "estado final" : "pila vacía")
       .append("\n");
    dot.append("  labelloc=\"t\";\n");
    dot.append("  label=\"Símbolo inicial de pila: ").append(ap.getSimboloInicialPila()).append("\";\n");
    
    dot.append("}\n");
    
    return dot.toString();
}


    /* -------------------- Método general -------------------- */

    public static String generarDot(Automata a) {
        if (a instanceof AFD afd) return generarDot(afd);
        if (a instanceof AFND afnd) return generarDot(afnd);
        if (a instanceof AP ap) return generarDot(ap);
        throw new IllegalArgumentException("Tipo de autómata no soportado");
    }
}
