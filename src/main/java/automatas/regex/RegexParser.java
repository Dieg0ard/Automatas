package automatas.regex;

import automatas.regex.RegexAST.*;

public class RegexParser {
    private final String input;
    private int pos = 0;

    public RegexParser(String input) {
        this.input = input;
    }

    public Node parse() {
        Node result = parseUnion();
        if (pos < input.length()) {
            throw new RuntimeException("Unexpected character at end: " + input.charAt(pos));
        }
        return result;
    }

    // union = concat ('|' concat)*
    private Node parseUnion() {
        Node left = parseConcat();
        while (match('|')) {
            Node right = parseConcat();
            left = new Union(left, right);
        }
        return left;
    }

    // concat = repeat+
    private Node parseConcat() {
        Node left = parseRepeat();
        while (true) {
            Node next = parseRepeat();
            if (next == null) break;
            left = new Concat(left, next);
        }
        return left;
    }

    // repeat = atom ('*' | '+' | '?' | '{n}' | '{n,}' | '{n,m}')*
    private Node parseRepeat() {
        Node node = parseAtom();
        if (node == null) return null;

        while (true) {
            if (match('*')) {
                node = new Star(node);
            } else if (match('+')) {
                // a+ = aa*
                node = new Concat(node, new Star(node));
            } else if (match('?')) {
                // a? = (a|ε)
                node = new Union(node, new Literal('\0')); // \0 representa epsilon
            } else if (peek() == '{') {
                node = parseRepetition(node);
            } else {
                break;
            }
        }
        return node;
    }

    // Parsear {n}, {n,}, {n,m}
    private Node parseRepetition(Node node) {
        if (!match('{')) {
            throw new RuntimeException("Expected '{'");
        }

        int min = parseNumber();
        int max = min;
        boolean unbounded = false;

        if (match(',')) {
            if (peek() == '}') {
                unbounded = true; // {n,}
            } else {
                max = parseNumber(); // {n,m}
            }
        }

        if (!match('}')) {
            throw new RuntimeException("Expected '}'");
        }

        // Construir el nodo resultante
        if (unbounded) {
            // {n,} = aaa...a (n veces) seguido de a*
            return buildRepetition(node, min, true);
        } else {
            // {n} o {n,m}
            return buildRepetition(node, min, max);
        }
    }

    // Construir repetición exacta o rango
    private Node buildRepetition(Node node, int min, int max) {
        if (min == 0 && max == 0) {
            return new Literal('\0'); // epsilon
        }

        if (min == max) {
            // Repetición exacta: {n} = nnn...n (n veces)
            Node result = cloneNode(node);
            for (int i = 1; i < min; i++) {
                result = new Concat(result, cloneNode(node));
            }
            return result;
        }

        // Rango {min, max}: construir todas las opciones
        // Por ejemplo: {2,4} = (nn|nnn|nnnn)
        Node result = null;
        
        for (int count = min; count <= max; count++) {
            // Construir 'count' repeticiones
            Node option = cloneNode(node);
            for (int i = 1; i < count; i++) {
                option = new Concat(option, cloneNode(node));
            }
            
            // Agregar esta opción a la unión
            if (result == null) {
                result = option;
            } else {
                result = new Union(result, option);
            }
        }

        return result;
    }

    // Construir repetición ilimitada {n,}
    private Node buildRepetition(Node node, int min, boolean unbounded) {
        if (min == 0) {
            return new Star(node); // {0,} = a*
        }

        // {n,} = aaa...a (n veces) seguido de a*
        Node result = null;
        for (int i = 0; i < min; i++) {
            if (result == null) {
                result = cloneNode(node);
            } else {
                result = new Concat(result, cloneNode(node));
            }
        }

        return new Concat(result, new Star(cloneNode(node)));
    }

    // Clonar un nodo (necesario porque reutilizamos la misma expresión)
    private Node cloneNode(Node node) {
        return switch (node) {
            case Literal lit -> new Literal(lit.c());
            case Star st -> new Star(cloneNode(st.node()));
            case Union un -> new Union(cloneNode(un.left()), cloneNode(un.right()));
            case Concat cat -> new Concat(cloneNode(cat.left()), cloneNode(cat.right()));
            default -> throw new RuntimeException("Unknown node type: " + node.getClass());
        };
    }

    private int parseNumber() {
        int start = pos;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }
        if (start == pos) {
            throw new RuntimeException("Expected number at position " + pos);
        }
        return Integer.parseInt(input.substring(start, pos));
    }

    // atom = literal | '(' union ')'
    private Node parseAtom() {
        if (pos >= input.length())
            return null;

        char c = input.charAt(pos);

        if (c == '(') {
            pos++;
            Node inside = parseUnion();
            if (!match(')')) {
                throw new RuntimeException("Missing closing parenthesis");
            }
            return inside;
        }

        // Caracteres especiales que no son literales
        if ("|)*+?{}".indexOf(c) != -1) {
            return null;
        }

        pos++;
        return new Literal(c);
    }

    private boolean match(char expected) {
        if (pos < input.length() && input.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    private char peek() {
        if (pos < input.length()) {
            return input.charAt(pos);
        }
        return '\0';
    }
}