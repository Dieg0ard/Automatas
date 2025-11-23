package automatas.regex;

public class RegexAST {

    // ====== Nodo base del AST ======
    public static abstract class Node { }

    // ====== Literal ======
    public static class Literal extends Node {
        public final char c;

        public Literal(char c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return "Literal(" + c + ")";
        }
    }

    // ====== Concatenación ======
    public static class Concat extends Node {
        public final Node left;
        public final Node right;

        public Concat(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "Concat(" + left + ", " + right + ")";
        }
    }

    // ====== Unión ======
    public static class Union extends Node {
        public final Node left;
        public final Node right;

        public Union(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "Union(" + left + ", " + right + ")";
        }
    }

    // ====== Cierre Kleene (*) ======
    public static class Star extends Node {
        public final Node node;

        public Star(Node node) {
            this.node = node;
        }

        @Override
        public String toString() {
            return "Star(" + node + ")";
        }
    }
}
