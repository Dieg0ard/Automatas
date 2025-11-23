/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package automatas.regex;

import automatas.regex.RegexAST.Concat;
import automatas.regex.RegexAST.Literal;
import automatas.regex.RegexAST.Node;
import automatas.regex.RegexAST.Star;
import automatas.regex.RegexAST.Union;

/**
 *
 * @author diego
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

    // repeat = atom ('*')*
    private Node parseRepeat() {
        Node node = parseAtom();
        if (node == null) return null;

        while (match('*')) {
            node = new Star(node);
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

        if ("|)*".indexOf(c) != -1) {
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
