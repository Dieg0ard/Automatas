package automatas.regex;

/**
 * Árbol de Sintaxis Abstracta para expresiones regulares
 */
public class RegexAST {
    
    /**
     * Nodo base del AST
     */
    public interface Node {}
    
    /**
     * Literal: un carácter individual
     */
    public record Literal(char c) implements Node {
        @Override
        public String toString() {
            return String.valueOf(c);
        }
    }
    
    /**
     * Concatenación: ab
     */
    public record Concat(Node left, Node right) implements Node {
        @Override
        public String toString() {
            return "(" + left + " · " + right + ")";
        }
    }
    
    /**
     * Unión: a|b
     */
    public record Union(Node left, Node right) implements Node {
        @Override
        public String toString() {
            return "(" + left + " | " + right + ")";
        }
    }
    
    /**
     * Estrella de Kleene: a* (cero o más)
     */
    public record Star(Node node) implements Node {
        @Override
        public String toString() {
            return "(" + node + ")*";
        }
    }
    
    /**
     * Plus: a+ (una o más) = aa*
     */
    public record Plus(Node node) implements Node {
        @Override
        public String toString() {
            return "(" + node + ")+";
        }
    }
    
    /**
     * Question: a? (cero o una) = a|ε
     */
    public record Question(Node node) implements Node {
        @Override
        public String toString() {
            return "(" + node + ")?";
        }
    }
}