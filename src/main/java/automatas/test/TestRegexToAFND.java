package automatas.test;

import automatas.algoritmos.Conversion;
import automatas.algoritmos.Minimizacion;
import automatas.core.AFD;
import automatas.regex.RegexParser;
import automatas.regex.ThompsonConstructor;
import automatas.regex.RegexAST.Node;
import automatas.core.AFND;
import automatas.visual.AutomataRenderer;
import java.io.IOException;

public class TestRegexToAFND {

    public static void main(String[] args) throws IOException {

        String regex = "a*b*";

        RegexParser parser = new RegexParser(regex);
        Node ast = parser.parse();

        ThompsonConstructor ctor = new ThompsonConstructor();
        AFND automata = ctor.convert(ast);

        /*System.out.println("Estados: " + automata.getEstados());
        System.out.println("Alfabeto: " + automata.getAlfabeto());
        System.out.println("Inicial: " + automata.getEstadoInicial());
        System.out.println("Finales: " + automata.getEstadosFinales());
        System.out.println("Transiciones: " + automata.getTransiciones());

        System.out.println("Acepta 'aabbb'? " + automata.acepta("aabbb"));
        System.out.println("Acepta 'accc'? " + automata.acepta("accc"));
        System.out.println("Acepta 'aabbb'? " + automata.acepta("aabbb"));
        System.out.println("Acepta 'b'? " + automata.acepta("a"));*/
        
        AutomataRenderer ar = null;
        Conversion con = new Conversion(automata);
        AFD afd = con.convertir();
        Minimizacion min = new Minimizacion(afd);
        AFD afdMin = min.minimizar();
        ar.renderAutomata(afdMin);
        System.out.println("Acepta aab? " + afdMin.acepta("b"));
    }
}
