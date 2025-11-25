package automatas.test;

import automatas.algoritmos.GLCtoAP;
import automatas.core.AP;
import automatas.grammar.CFExpressionParser;
import automatas.grammar.GLC;
import automatas.visual.AutomataRenderer;
import java.io.IOException;

public class TestGLC {

    public static void main(String[] args) throws IOException {

        System.out.println("=== Prueba de Gramáticas ===\n");

        // Gramática a^n b^n
        GLC g1 = CFExpressionParser.parse("a^n b^n");
        AP automata = GLCtoAP.convertirGramatica(g1);
        AutomataRenderer.renderAutomata(automata);

    }
}
