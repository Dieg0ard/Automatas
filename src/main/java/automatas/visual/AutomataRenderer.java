package automatas.visual;

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
        String userHome = System.getProperty("user.home");
        String outPath = userHome + "/.automatas/img/automata.png";
        Graphviz.fromString(dot)
                .render(Format.PNG)
                .toFile(new File(outPath));
    }

}
