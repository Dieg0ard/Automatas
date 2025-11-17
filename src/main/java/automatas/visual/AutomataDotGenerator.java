package automatas.visual;

import automatas.core.AFD;
import automatas.core.AFND;
import automatas.core.Automata;
import java.util.Set;

public class AutomataDotGenerator {

    public static String generarDot(AFD afd) {
    StringBuilder sb = new StringBuilder();

    sb.append("digraph Automata {\n");
    sb.append("    rankdir=LR;\n");
    sb.append("    node [shape=circle];\n\n");

    // Estados normales y finales
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

    // Transiciones
    for (String origen : afd.getEstados()) {
        var mapaSimbolos = afd.getTransiciones().get(origen);
        if (mapaSimbolos == null) continue;

        for (var entry : mapaSimbolos.entrySet()) {
            char simbolo = entry.getKey();
            String destino = entry.getValue();

            sb.append("    ")
              .append(origen).append(" -> ").append(destino)
              .append(" [label=\"").append(simbolo).append("\"];\n");
        }
    }

    sb.append("}\n");
    return sb.toString();
}


    public static String generarDot(AFND afnd) {
    StringBuilder sb = new StringBuilder();

    sb.append("digraph Automata {\n");
    sb.append("    rankdir=LR;\n");
    sb.append("    node [shape=circle];\n\n");

    // Estados normales y finales
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

    // Transiciones
    for (String origen : afnd.getEstados()) {
        var mapaSimbolos = afnd.getTransiciones().get(origen);
        if (mapaSimbolos == null) continue;

        for (var entry : mapaSimbolos.entrySet()) {
            Character simbolo = entry.getKey();      // puede ser null → ε
            Set<String> destinos = entry.getValue();

            for (String destino : destinos) {
                sb.append("    ")
                  .append(origen).append(" -> ").append(destino)
                  .append(" [label=\"")
                  .append(simbolo == null ? "ε" : simbolo)
                  .append("\"];\n");
            }
        }
    }

    sb.append("}\n");
    return sb.toString();
}


    public static String generarDot(Automata a) {
        if (a instanceof AFD afd) {
            return generarDot(afd);
        }
        if (a instanceof AFND afnd) {
            return generarDot(afnd);
        }
        throw new IllegalArgumentException("Tipo de autómata no soportado");
    }
}
