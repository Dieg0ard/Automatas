package automatas.regex;

import automatas.core.AFND;
import automatas.io.EscritorAutomata;
import automatas.regex.RegexAST.*;
import java.io.IOException;
import java.util.*;

/**
 * Constructor de Thompson para convertir expresiones regulares a AFND
 * Soporta: literales, concatenación, unión, *, +, ?
 */
public class ThompsonConstructor {
    
    private static class Fragment {
        String start;
        String end;
        
        Fragment(String s, String e) {
            this.start = s;
            this.end = e;
        }
    }
    
    private int id = 0;
    
    private String newState() {
        return "q" + (id++);
    }
    
    public AFND convert(Node node) throws IOException {
        Map<String, Map<Character, Set<String>>> transitions = new HashMap<>();
        Set<String> states = new HashSet<>();
        Set<Character> alphabet = new HashSet<>();
        
        Fragment frag = build(node, transitions, states, alphabet);
        
        Set<String> finals = Set.of(frag.end);
        
        AFND afnd = new AFND(states, alphabet, transitions, frag.start, finals);
        String userHome = System.getProperty("user.home");
        String rutaCSV = userHome + "/.automatas/csv/afd.csv";
        EscritorAutomata.guardarAFND(afnd, rutaCSV);
        return afnd;
    }
    
    private Fragment build(Node node,
                           Map<String, Map<Character, Set<String>>> trans,
                           Set<String> states,
                           Set<Character> alphabet) {
        
        if (node instanceof Literal lit) {
            return buildLiteral(lit.c(), trans, states, alphabet);
        }
        
        if (node instanceof Concat cat) {
            return buildConcat(cat.left(), cat.right(), trans, states, alphabet);
        }
        
        if (node instanceof Union uni) {
            return buildUnion(uni.left(), uni.right(), trans, states, alphabet);
        }
        
        if (node instanceof Star st) {
            return buildStar(st.node(), trans, states, alphabet);
        }
        
        if (node instanceof Plus plus) {
            return buildPlus(plus.node(), trans, states, alphabet);
        }
        
        if (node instanceof Question q) {
            return buildQuestion(q.node(), trans, states, alphabet);
        }
        
        throw new RuntimeException("Nodo desconocido: " + node.getClass());
    }
    
    /**
     * Construcción para literal: a
     * 
     *   a
     * s --> e
     */
    private Fragment buildLiteral(char c,
                                   Map<String, Map<Character, Set<String>>> trans,
                                   Set<String> states,
                                   Set<Character> alphabet) {
        String s = newState();
        String e = newState();
        states.add(s);
        states.add(e);
        alphabet.add(c);
        
        trans.computeIfAbsent(s, k -> new HashMap<>())
             .computeIfAbsent(c, k -> new HashSet<>())
             .add(e);
        
        return new Fragment(s, e);
    }
    
    /**
     * Construcción para concatenación: ab
     * 
     *        ε
     * s1 --> e1 --> s2 --> e2
     *   a           b
     */
    private Fragment buildConcat(Node left, Node right,
                                 Map<String, Map<Character, Set<String>>> trans,
                                 Set<String> states,
                                 Set<Character> alphabet) {
        Fragment f1 = build(left, trans, states, alphabet);
        Fragment f2 = build(right, trans, states, alphabet);
        
        // Conectar con epsilon
        trans.computeIfAbsent(f1.end, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(f2.start);
        
        return new Fragment(f1.start, f2.end);
    }
    
    /**
     * Construcción para unión: a|b
     * 
     *      ε     a    ε
     *   ┌-----> s1 ----┐
     *   │              │
     * s │              ├---> e
     *   │              │
     *   └-----> s2 ----┘
     *      ε     b    ε
     */
    private Fragment buildUnion(Node left, Node right,
                                Map<String, Map<Character, Set<String>>> trans,
                                Set<String> states,
                                Set<Character> alphabet) {
        String s = newState();
        String e = newState();
        states.add(s);
        states.add(e);
        
        Fragment f1 = build(left, trans, states, alphabet);
        Fragment f2 = build(right, trans, states, alphabet);
        
        // Epsilon desde inicio a ambos fragmentos
        trans.computeIfAbsent(s, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(f1.start);
        trans.get(s).get(null).add(f2.start);
        
        // Epsilon desde ambos finales al estado final
        trans.computeIfAbsent(f1.end, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(e);
        trans.computeIfAbsent(f2.end, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(e);
        
        return new Fragment(s, e);
    }
    
    /**
     * Construcción para estrella: a*
     * 
     *      ┌--------ε--------┐
     *      │                 ↓
     *      │    ε    a    ε
     * s ---┼---> s1 --> e1 --┼---> e
     *      │                 │
     *      └--------ε--------┘
     */
    private Fragment buildStar(Node node,
                               Map<String, Map<Character, Set<String>>> trans,
                               Set<String> states,
                               Set<Character> alphabet) {
        String s = newState();
        String e = newState();
        states.add(s);
        states.add(e);
        
        Fragment f = build(node, trans, states, alphabet);
        
        // Epsilon desde inicio a fragmento y a final
        trans.computeIfAbsent(s, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(f.start);
        trans.get(s).get(null).add(e);
        
        // Epsilon desde final del fragmento de vuelta al inicio y al final
        trans.computeIfAbsent(f.end, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(f.start);
        trans.get(f.end).get(null).add(e);
        
        return new Fragment(s, e);
    }
    
    /**
     * Construcción para plus: a+
     * Equivalente a: aa*
     * 
     *           ┌----ε----┐
     *           │         ↓
     *      ε    a    ε
     * s -----> s1 --> e1 -----> e
     */
    private Fragment buildPlus(Node node,
                               Map<String, Map<Character, Set<String>>> trans,
                               Set<String> states,
                               Set<Character> alphabet) {
        String s = newState();
        String e = newState();
        states.add(s);
        states.add(e);
        
        Fragment f = build(node, trans, states, alphabet);
        
        // Epsilon desde inicio al fragmento
        trans.computeIfAbsent(s, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(f.start);
        
        // Epsilon desde final del fragmento al final Y de vuelta al inicio (bucle)
        trans.computeIfAbsent(f.end, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(e);
        trans.get(f.end).get(null).add(f.start);
        
        return new Fragment(s, e);
    }
    
    /**
     * Construcción para question: a?
     * Equivalente a: a|ε
     * 
     *      ┌--------ε--------┐
     *      │                 ↓
     *      │    ε    a    ε
     * s ---┼---> s1 --> e1 --┼---> e
     *      │                 │
     *      └--------ε--------┘
     */
    private Fragment buildQuestion(Node node,
                                   Map<String, Map<Character, Set<String>>> trans,
                                   Set<String> states,
                                   Set<Character> alphabet) {
        String s = newState();
        String e = newState();
        states.add(s);
        states.add(e);
        
        Fragment f = build(node, trans, states, alphabet);
        
        // Epsilon desde inicio al fragmento Y directamente al final
        trans.computeIfAbsent(s, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(f.start);
        trans.get(s).get(null).add(e);
        
        // Epsilon desde final del fragmento al final
        trans.computeIfAbsent(f.end, k -> new HashMap<>())
             .computeIfAbsent(null, k -> new HashSet<>())
             .add(e);
        
        return new Fragment(s, e);
    }
}