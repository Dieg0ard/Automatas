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
    StringBuilder sb = new StringBuilder();

    sb.append("digraph AP {\n");
    sb.append("    rankdir=LR;\n");
    sb.append("    node [shape=circle];\n\n");

    // Estados
    for (String estado : ap.getEstados()) {
        if (ap.getEstadosFinales().contains(estado)) {
            sb.append("    ").append(estado).append(" [shape=doublecircle];\n");
        } else {
            sb.append("    ").append(estado).append(";\n");
        }
    }
    sb.append("\n");

    // Estado inicial
    sb.append("    start [shape=point];\n");
    sb.append("    start -> ").append(ap.getEstadoInicial()).append(";\n\n");

    // === Obtener transiciones por reflexión ===
    Map<?, ?> trans = ap.getTransiciones();

    try {

        for (var entry : trans.entrySet()) {

            Object keyObj = entry.getKey();
            Object valListObj = entry.getValue();

            var keyClass = keyObj.getClass();

            var fEstado = keyClass.getDeclaredField("estado");
            var fSimboloEntrada = keyClass.getDeclaredField("simboloEntrada");
            var fSimboloPila = keyClass.getDeclaredField("simboloPila");

            fEstado.setAccessible(true);
            fSimboloEntrada.setAccessible(true);
            fSimboloPila.setAccessible(true);

            String estado = (String) fEstado.get(keyObj);
            Character simboloEntrada = (Character) fSimboloEntrada.get(keyObj);
            char simboloPila = (char) fSimboloPila.get(keyObj);

            @SuppressWarnings("unchecked")
            List<?> valores = (List<?>) valListObj;

            for (Object valObj : valores) {
                var valClass = valObj.getClass();

                var fEstadoSig = valClass.getDeclaredField("estadoSiguiente");
                var fReemplazo = valClass.getDeclaredField("cadenaReemplazo");

                fEstadoSig.setAccessible(true);
                fReemplazo.setAccessible(true);

                String estadoSig = (String) fEstadoSig.get(valObj);
                String reemplazo = (String) fReemplazo.get(valObj);

                String label = (simboloEntrada == null ? "ε" : simboloEntrada)
                        + " , "
                        + simboloPila
                        + " → "
                        + (reemplazo.isEmpty() ? "ε" : reemplazo);

                sb.append("    ")
                        .append(estado)
                        .append(" -> ")
                        .append(estadoSig)
                        .append(" [label=\"")
                        .append(label)
                        .append("\"];\n");
            }
        }

    } catch (Exception e) {
        throw new RuntimeException("Error al procesar transiciones del AP", e);
    }

    sb.append("}\n");
    return sb.toString();
}






    /* -------------------- Método general -------------------- */

    public static String generarDot(Automata a) {
        if (a instanceof AFD afd) return generarDot(afd);
        if (a instanceof AFND afnd) return generarDot(afnd);
        if (a instanceof AP ap) return generarDot(ap);
        throw new IllegalArgumentException("Tipo de autómata no soportado");
    }
}
