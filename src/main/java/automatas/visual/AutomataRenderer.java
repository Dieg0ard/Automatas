package automatas.visual;

import automatas.core.AFD;
import automatas.core.AFND;
import automatas.core.Automata;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;

import java.io.File;
import java.io.IOException;

/**
 * Clase utilitaria para renderizar autómatas en diferentes formatos gráficos
 * usando la librería Graphviz (guru.nidi.graphviz).
 */
public class AutomataRenderer {

    /**
     * Renderiza un autómata genérico a PNG.
     */
    public static void renderAutomata(Automata a) throws IOException {
        String dot = AutomataDotGenerator.generarDot(a);
        Graphviz.fromString(dot)
                .render(Format.PNG)
                .toFile(new File(".output/automatas/automata.png"));
    }

}
