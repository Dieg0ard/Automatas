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
     * Renderiza un AFD y genera un archivo PNG.
     *
     * @param afd       Autómata determinista
     * @param destino   Archivo de salida (por ejemplo: new File("afd.png"))
     */
    public static void renderAfdToPng(AFD afd, File destino) throws IOException {
        String dot = AutomataDotGenerator.generarDot(afd);
        renderDotToPng(dot, destino);
    }

    /**
     * Renderiza un AFND y genera un archivo PNG.
     *
     * @param afnd      Autómata no determinista
     * @param destino   Archivo de salida (por ejemplo: new File("afnd.png"))
     */
    public static void renderAfndToPng(AFND afnd, File destino) throws IOException {
        String dot = AutomataDotGenerator.generarDot(afnd);
        renderDotToPng(dot, destino);
    }

    /**
     * Renderiza un autómata genérico (AFD o AFND) a PNG.
     */
    public static void renderAutomataToPng(Automata a, File destino) throws IOException {
        String dot = AutomataDotGenerator.generarDot(a);
        renderDotToPng(dot, destino);
    }

    /**
     * Renderiza DOT a PNG.
     */
    public static void renderDotToPng(String dot, File destino) throws IOException {
        Graphviz.fromString(dot)
                .render(Format.PNG)
                .toFile(destino);
    }

    /**
     * Renderiza DOT a SVG.
     */
    public static void renderDotToSvg(String dot, File destino) throws IOException {
        Graphviz.fromString(dot)
                .render(Format.SVG)
                .toFile(destino);
    }

    /**
     * Renderiza un autómata genérico a SVG.
     */
    public static void renderAutomataToSvg(Automata a, File destino) throws IOException {
        String dot = AutomataDotGenerator.generarDot(a);
        renderDotToSvg(dot, destino);
    }

    /**
     * Renderiza un autómata genérico a cualquier formato soportado.
     */
    public static void renderAutomata(Automata a) throws IOException {
        String dot = AutomataDotGenerator.generarDot(a);
        Graphviz.fromString(dot)
                .render(Format.PNG)
                .toFile(new File(".output/automatas/automata.png"));
    }

}
