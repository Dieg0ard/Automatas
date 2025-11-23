package automatas.regex;

import automatas.core.AFND;
import automatas.io.EscritorAutomata;
import automatas.regex.RegexAST.*;
import java.io.File;
import java.io.IOException;

import java.util.*;

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
            String s = newState();
            String e = newState();
            states.add(s);
            states.add(e);
            alphabet.add(lit.c);

            trans.computeIfAbsent(s, k -> new HashMap<>())
                 .computeIfAbsent(lit.c, k -> new HashSet<>())
                 .add(e);

            return new Fragment(s, e);
        }

        if (node instanceof Concat cat) {
            Fragment f1 = build(cat.left, trans, states, alphabet);
            Fragment f2 = build(cat.right, trans, states, alphabet);

            trans.computeIfAbsent(f1.end, k -> new HashMap<>())
                 .computeIfAbsent(null, k -> new HashSet<>())
                 .add(f2.start);

            return new Fragment(f1.start, f2.end);
        }

        if (node instanceof Union uni) {
            String s = newState();
            String e = newState();
            states.add(s);
            states.add(e);

            Fragment f1 = build(uni.left, trans, states, alphabet);
            Fragment f2 = build(uni.right, trans, states, alphabet);

            trans.computeIfAbsent(s, k -> new HashMap<>())
                 .computeIfAbsent(null, k -> new HashSet<>())
                 .add(f1.start);

            trans.get(s).get(null).add(f2.start);

            trans.computeIfAbsent(f1.end, k -> new HashMap<>())
                 .computeIfAbsent(null, k -> new HashSet<>())
                 .add(e);

            trans.computeIfAbsent(f2.end, k -> new HashMap<>())
                 .computeIfAbsent(null, k -> new HashSet<>())
                 .add(e);

            return new Fragment(s, e);
        }

        if (node instanceof Star st) {
            String s = newState();
            String e = newState();
            states.add(s);
            states.add(e);

            Fragment f = build(st.node, trans, states, alphabet);

            trans.computeIfAbsent(s, k -> new HashMap<>())
                 .computeIfAbsent(null, k -> new HashSet<>())
                 .add(f.start);

            trans.get(s).get(null).add(e);

            trans.computeIfAbsent(f.end, k -> new HashMap<>())
                 .computeIfAbsent(null, k -> new HashSet<>())
                 .add(f.start);

            trans.get(f.end).get(null).add(e);

            return new Fragment(s, e);
        }

        throw new RuntimeException("Nodo desconocido: " + node.getClass());
    }
}
