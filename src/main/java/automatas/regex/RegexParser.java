package automatas.regex;

import automatas.regex.RegexAST.*;

/**
 * Parser de expresiones regulares con soporte para:
 * - * (cero o más)
 * - + (una o más)
 * - ? (opcional, cero o una)
 * - | (unión)
 * - () (agrupación)
 */
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
    
    // repeat = atom ('*' | '+' | '?')*
    private Node parseRepeat() {
        Node node = parseAtom();
        if (node == null) return null;
        
        while (true) {
            if (match('*')) {
                // a* = cero o más a's
                node = new Star(node);
            } else if (match('+')) {
                // a+ = una o más a's = aa*
                node = new Plus(node);
            } else if (match('?')) {
                // a? = opcional = a | ε
                node = new Question(node);
            } else {
                break;
            }
        }
        
        return node;
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
        if ("|)*+?".indexOf(c) != -1) {
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
}