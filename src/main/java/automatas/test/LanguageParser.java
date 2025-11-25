package automatas.test;

import java.util.*;
import java.util.regex.*;

public class LanguageParser {

    private static class Token {
        enum Type { VARIABLE, NUMBER, OPERATOR, LPAREN, RPAREN, LBRACE, RBRACE, COMMA, EOF }
        final Type type;
        final String value;
        Token(Type t, String v) { this.type = t; this.value = v; }
        public String toString() { return type + "(" + value + ")"; }
    }

    // ===========================
    // RESTRICCIONES
    // ===========================
    public interface Constraint {
        String toRegex(String base);
    }

    public static class ExactConstraint implements Constraint {
        final int value;
        ExactConstraint(int v) { value = v; }
        public String toRegex(String base) {
            return "(" + base + "){" + value + "}";
        }
    }

    public static class EvenConstraint implements Constraint {
        public String toRegex(String base) {
            // Si base es un solo carácter, no necesita paréntesis
            if (base.length() == 1) {
                return "(" + base + base + ")*";
            }
            return "((" + base + ")(" + base + "))*";
        }
    }

    public static class OddConstraint implements Constraint {
        public String toRegex(String base) {
            if (base.length() == 1) {
                return base + "(" + base + base + ")*";
            }
            return "(" + base + ")((" + base + ")(" + base + "))*";
        }
    }

    public static class MinConstraint implements Constraint {
        final int min;
        MinConstraint(int m) { min = m; }
        public String toRegex(String base) {
            if (min == 0) {
                return base.length() == 1 ? base + "*" : "(" + base + ")*";
            }
            if (min == 1) {
                return base.length() == 1 ? base + "+" : "(" + base + ")+";
            }
            return base.length() == 1 ? base + "{" + min + ",}" : "(" + base + "){" + min + ",}";
        }
    }

    public static class MaxConstraint implements Constraint {
        final int max;
        MaxConstraint(int m) { max = m; }
        public String toRegex(String base) {
            return base.length() == 1 ? base + "{0," + max + "}" : "(" + base + "){0," + max + "}";
        }
    }

    public static class RangeConstraint implements Constraint {
        final int min, max;
        RangeConstraint(int a, int b) { min = a; max = b; }
        public String toRegex(String base) {
            return "(" + base + "){" + min + "," + max + "}";
        }
    }

    public static class AnyConstraint implements Constraint {
        public String toRegex(String base) {
            return "(" + base + ")*";
        }
    }

    // ===========================
    // LEXER
    // ===========================
    private static class Lexer {
        private final String input;
        private int pos;

        Lexer(String input) {
            this.input = input.replaceAll("\\s+", "");
            this.pos = 0;
        }

        List<Token> tokenize() {
            List<Token> tokens = new ArrayList<>();

            while (pos < input.length()) {
                char c = input.charAt(pos);

                if (Character.isLetter(c)) {
                    tokens.add(readVariable());
                }
                else if (Character.isDigit(c)) {
                    tokens.add(readNumber());
                }
                else if (c == '(') { tokens.add(new Token(Token.Type.LPAREN, "(")); pos++; }
                else if (c == ')') { tokens.add(new Token(Token.Type.RPAREN, ")")); pos++; }
                else if (c == '{') { tokens.add(new Token(Token.Type.LBRACE, "{")); pos++; }
                else if (c == '}') { tokens.add(new Token(Token.Type.RBRACE, "}")); pos++; }
                else if (c == ',') { tokens.add(new Token(Token.Type.COMMA, ",")); pos++; }
                else if ("+*|^".indexOf(c) >= 0) {
                    tokens.add(new Token(Token.Type.OPERATOR, String.valueOf(c)));
                    pos++;
                }
                else {
                    throw new RuntimeException("Carácter inválido: " + c);
                }
            }

            tokens.add(new Token(Token.Type.EOF, ""));
            return tokens;
        }

        private Token readVariable() {
            StringBuilder sb = new StringBuilder();
            sb.append(input.charAt(pos++));
            
            // Solo continuar leyendo si es underscore o si ya tenemos más de 1 carácter
            // Esto permite variables de una letra (a, b, n, m) y variables con underscore (var_1)
            while (pos < input.length() && input.charAt(pos) == '_') {
                sb.append(input.charAt(pos++));
                // Después de underscore, leer más letras/dígitos
                while (pos < input.length() && 
                       (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
                    sb.append(input.charAt(pos++));
                }
            }
            
            return new Token(Token.Type.VARIABLE, sb.toString());
        }

        private Token readNumber() {
            StringBuilder sb = new StringBuilder();
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                sb.append(input.charAt(pos++));
            }
            return new Token(Token.Type.NUMBER, sb.toString());
        }
    }

    // ===========================
    // PARSER DE RESTRICCIONES
    // ===========================
    public static Map<String, Constraint> parseConstraints(String constraintText) {
        Map<String, Constraint> constraints = new HashMap<>();
        
        if (constraintText == null || constraintText.trim().isEmpty()) {
            return constraints;
        }

        // Separar por "donde" o "con"
        String normalized = constraintText.toLowerCase()
            .replaceAll("\\s+", " ")
            .trim();

        // Separar múltiples restricciones por comas
        String[] parts = normalized.split(",");

        for (String part : parts) {
            part = part.trim();

            // Patrones de regex para diferentes tipos de restricciones
            
            // 2 <= n <= 5 (DEBE IR PRIMERO antes de los otros patrones)
            Pattern rangePattern = Pattern.compile("(\\d+)\\s*<=?\\s*([a-z]+)\\s*<=?\\s*(\\d+)");
            Matcher rangeMatcher = rangePattern.matcher(part);
            if (rangeMatcher.find()) {
                String var = rangeMatcher.group(2);
                int min = Integer.parseInt(rangeMatcher.group(1));
                int max = Integer.parseInt(rangeMatcher.group(3));
                constraints.put(var, new RangeConstraint(min, max));
                continue;
            }
            
            // n = 3, m=5
            Pattern exactPattern = Pattern.compile("([a-z]+)\\s*=\\s*(\\d+)");
            Matcher exactMatcher = exactPattern.matcher(part);
            if (exactMatcher.find()) {
                String var = exactMatcher.group(1);
                int value = Integer.parseInt(exactMatcher.group(2));
                constraints.put(var, new ExactConstraint(value));
                continue;
            }

            // n es par, n par
            if (part.matches(".*\\bpar\\b.*")) {
                Pattern evenPattern = Pattern.compile("([a-z]+)");
                Matcher evenMatcher = evenPattern.matcher(part);
                if (evenMatcher.find()) {
                    String var = evenMatcher.group(1);
                    constraints.put(var, new EvenConstraint());
                    continue;
                }
            }

            // n es impar, n impar
            if (part.matches(".*\\bimpar\\b.*")) {
                Pattern oddPattern = Pattern.compile("([a-z]+)");
                Matcher oddMatcher = oddPattern.matcher(part);
                if (oddMatcher.find()) {
                    String var = oddMatcher.group(1);
                    constraints.put(var, new OddConstraint());
                    continue;
                }
            }

            // n >= 3, n>=3
            Pattern minPattern = Pattern.compile("([a-z]+)\\s*>=?\\s*(\\d+)");
            Matcher minMatcher = minPattern.matcher(part);
            if (minMatcher.find()) {
                String var = minMatcher.group(1);
                int min = Integer.parseInt(minMatcher.group(2));
                constraints.put(var, new MinConstraint(min));
                continue;
            }

            // n <= 5, n<=5
            Pattern maxPattern = Pattern.compile("([a-z]+)\\s*<=?\\s*(\\d+)");
            Matcher maxMatcher = maxPattern.matcher(part);
            if (maxMatcher.find()) {
                String var = maxMatcher.group(1);
                int max = Integer.parseInt(maxMatcher.group(2));
                constraints.put(var, new MaxConstraint(max));
                continue;
            }

            // n > 0, n mayor que 0
            Pattern gtPattern = Pattern.compile("([a-z]+)\\s*(>|mayor\\s+que)\\s*(\\d+)");
            Matcher gtMatcher = gtPattern.matcher(part);
            if (gtMatcher.find()) {
                String var = gtMatcher.group(1);
                int value = Integer.parseInt(gtMatcher.group(3));
                constraints.put(var, new MinConstraint(value + 1));
                continue;
            }

            // n cualquiera, cualquier n
            Pattern anyPattern = Pattern.compile("(cualquier|cualquiera)\\s+([a-z]+)|([a-z]+)\\s+(cualquier|cualquiera)");
            Matcher anyMatcher = anyPattern.matcher(part);
            if (anyMatcher.find()) {
                String var = anyMatcher.group(2) != null ? anyMatcher.group(2) : anyMatcher.group(3);
                constraints.put(var, new AnyConstraint());
                continue;
            }
        }

        return constraints;
    }

    // ===============================
    // AST NODES
    // ===============================
    private interface ASTNode {}

    private static class LiteralNode implements ASTNode {
        final String value;
        LiteralNode(String v) { value = v; }
    }

    private static class ConcatNode implements ASTNode {
        final ASTNode left, right;
        ConcatNode(ASTNode l, ASTNode r) { left = l; right = r; }
    }

    private static class UnionNode implements ASTNode {
        final ASTNode left, right;
        UnionNode(ASTNode l, ASTNode r) { left = l; right = r; }
    }

    private static class StarNode implements ASTNode {
        final ASTNode child;
        StarNode(ASTNode c) { child = c; }
    }

    private static class RepetitionNode implements ASTNode {
        final ASTNode child;
        final int count;
        RepetitionNode(ASTNode c, int n) { child = c; count = n; }
    }

    private static class VariableRepetitionNode implements ASTNode {
        final ASTNode child;
        final String var;
        VariableRepetitionNode(ASTNode c, String v) { child = c; var = v; }
    }

    private static class SetNode implements ASTNode {
        final List<String> elements;
        SetNode(List<String> e) { elements = e; }
    }

    // =============================================
    // PARSER
    // =============================================

    private List<Token> tokens;
    private int pos;
    private Map<String, Constraint> constraints;

    public LanguageParser(String input, Map<String, Constraint> constraints) {
        this.tokens = new Lexer(input).tokenize();
        this.pos = 0;
        this.constraints = constraints != null ? constraints : new HashMap<>();
    }

    public LanguageParser(String input, String constraintText) {
        this(input, parseConstraints(constraintText));
    }

    private boolean match(Token.Type t) {
        return peek().type == t;
    }

    private Token peek() { return tokens.get(pos); }

    private Token advance() { return tokens.get(pos++); }

    public String parse() {
        ASTNode ast = parseExpression();
        return generateRegex(ast);
    }

    private ASTNode parseExpression() {
        ASTNode node = parseTerm();
        while (match(Token.Type.OPERATOR) && peek().value.equals("|")) {
            advance();
            node = new UnionNode(node, parseTerm());
        }
        return node;
    }

    private ASTNode parseTerm() {
        ASTNode node = parseFactor();
        while (true) {
            if (match(Token.Type.VARIABLE) ||
                match(Token.Type.NUMBER) ||
                match(Token.Type.LPAREN) ||
                match(Token.Type.LBRACE)) {
                node = new ConcatNode(node, parseFactor());
            } else break;
        }
        return node;
    }

    private ASTNode parseFactor() {
        ASTNode node = parsePrimary();

        while (match(Token.Type.OPERATOR)) {
            String op = peek().value;

            if (op.equals("*")) {
                advance();
                node = new StarNode(node);
            }
            else if (op.equals("^")) {
                advance();

                if (match(Token.Type.NUMBER) || match(Token.Type.VARIABLE)) {
                    String v = advance().value;

                    if (v.matches("\\d+") && !constraints.containsKey(v)) {
                        node = new RepetitionNode(node, Integer.parseInt(v));
                    } else {
                        node = new VariableRepetitionNode(node, v);
                    }
                }
                else {
                    throw new RuntimeException("Se esperaba variable o número después de ^");
                }
            }
            else break;
        }

        return node;
    }

    private ASTNode parsePrimary() {
        if (match(Token.Type.VARIABLE)) return new LiteralNode(advance().value);

        if (match(Token.Type.NUMBER)) return new LiteralNode(advance().value);

        if (match(Token.Type.LBRACE)) {
            advance();
            List<String> el = new ArrayList<>();
            do {
                if (match(Token.Type.VARIABLE) || match(Token.Type.NUMBER))
                    el.add(advance().value);
                else
                    throw new RuntimeException("Elemento inválido en el conjunto");

                if (match(Token.Type.COMMA)) advance();
            } while (!match(Token.Type.RBRACE));
            advance();
            return new SetNode(el);
        }

        if (match(Token.Type.LPAREN)) {
            advance();
            ASTNode n = parseExpression();
            if (!match(Token.Type.RPAREN)) throw new RuntimeException("Falta ')'");
            advance();
            return n;
        }

        throw new RuntimeException("Token inesperado: " + peek());
    }

    // =============================================
    // GENERADOR DE REGEX
    // =============================================
    private String generateRegex(ASTNode n) {
        if (n instanceof LiteralNode l) return l.value;

        if (n instanceof SetNode s)
            return "(" + String.join("|", s.elements) + ")";

        if (n instanceof ConcatNode c)
            return generateRegex(c.left) + generateRegex(c.right);

        if (n instanceof UnionNode u)
            return "(" + generateRegex(u.left) + "|" + generateRegex(u.right) + ")";

        if (n instanceof StarNode s)
            return "(" + generateRegex(s.child) + ")*";

        if (n instanceof RepetitionNode r) {
            String base = generateRegex(r.child);
            return "(" + base + "){" + r.count + "}";
        }

        if (n instanceof VariableRepetitionNode v) {
            String base = generateRegex(v.child);
            
            if (!constraints.containsKey(v.var)) {
                throw new RuntimeException("Variable no definida: " + v.var);
            }
            
            return constraints.get(v.var).toRegex(base);
        }

        throw new RuntimeException("Nodo AST desconocido");
    }

    // =============================================
    // MAIN DE EJEMPLO
    // =============================================
    public static void main(String[] args) {
        System.out.println("=== PARSER CON RESTRICCIONES EN LENGUAJE NATURAL ===\n");

        // Ejemplo 1: n es par, m >= 0
        System.out.println("Ejemplo 1: 0^n 1^m donde n es par, m >= 0");
        LanguageParser p1 = new LanguageParser("0^n 1^m", "n es par, m >= 0");
        System.out.println("Regex: " + p1.parse());
        System.out.println("Esperado: (00)*1*");
        System.out.println();

        // Ejemplo 2: Valores exactos
        System.out.println("Ejemplo 2: 0^n 1^m donde n = 3, m = 5");
        LanguageParser p2 = new LanguageParser("0^n 1^m", "n = 3, m = 5");
        System.out.println("Regex: " + p2.parse());
        System.out.println();

        // Ejemplo 3: n impar
        System.out.println("Ejemplo 3: a^n donde n es impar");
        LanguageParser p3 = new LanguageParser("a^n", "n es impar");
        System.out.println("Regex: " + p3.parse());
        System.out.println();

        // Ejemplo 4: Rango
        System.out.println("Ejemplo 4: a^n donde 2 <= n <= 5");
        LanguageParser p4 = new LanguageParser("a^n", "2 <= n <= 5");
        System.out.println("Regex: " + p4.parse());
        System.out.println();

        // Ejemplo 5: Múltiples restricciones
        System.out.println("Ejemplo 5: a^n b^m c^k donde n es par, m >= 1, k <= 3");
        LanguageParser p5 = new LanguageParser("a^n b^m c^k", "n es par, m >= 1, k <= 3");
        System.out.println("Regex: " + p5.parse());
        System.out.println();

        // Ejemplo 6: Combinación compleja
        System.out.println("Ejemplo 6: {0,1}^n 2^m donde n impar, m = 2");
        LanguageParser p6 = new LanguageParser("{0,1}^n 2^m", "n impar, m = 2");
        System.out.println("Regex: " + p6.parse());
        System.out.println();
    }
}