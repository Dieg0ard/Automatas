package automatas.grammar;

import java.util.*;

/**
 * Representa una Gramática Libre de Contexto (GLC)
 * G = (V, T, P, S) donde:
 * - V: variables (no terminales)
 * - T: terminales
 * - P: producciones
 * - S: símbolo inicial
 */
public class GLC {
    
    /**
     * Representa una producción: A → α
     * Donde A es un no terminal y α es una cadena de terminales y no terminales
     */
    public static class Produccion {
        private String noTerminal;  // Lado izquierdo
        private String derivacion;  // Lado derecho (puede contener terminales y no terminales)
        
        public Produccion(String noTerminal, String derivacion) {
            this.noTerminal = noTerminal;
            this.derivacion = derivacion;
        }
        
        public String getNoTerminal() { return noTerminal; }
        public String getDerivacion() { return derivacion; }
        
        public boolean esEpsilon() {
            return derivacion.isEmpty() || derivacion.equals("ε");
        }
        
        @Override
        public String toString() {
            String der = derivacion.isEmpty() ? "ε" : derivacion;
            return noTerminal + " → " + der;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Produccion)) return false;
            Produccion p = (Produccion) o;
            return noTerminal.equals(p.noTerminal) && derivacion.equals(p.derivacion);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(noTerminal, derivacion);
        }
    }
    
    private Set<String> noTerminales;      // Variables (V)
    private Set<Character> terminales;     // Alfabeto (T)
    private List<Produccion> producciones; // Reglas (P)
    private String simboloInicial;         // Axioma (S)
    
    public GLC(Set<String> noTerminales, Set<Character> terminales, 
               List<Produccion> producciones, String simboloInicial) {
        this.noTerminales = new HashSet<>(noTerminales);
        this.terminales = new HashSet<>(terminales);
        this.producciones = new ArrayList<>(producciones);
        this.simboloInicial = simboloInicial;
        
        validar();
    }
    
    private void validar() {
        if (!noTerminales.contains(simboloInicial)) {
            throw new IllegalArgumentException("El símbolo inicial debe ser un no terminal");
        }
        
        for (Produccion p : producciones) {
            if (!noTerminales.contains(p.getNoTerminal())) {
                throw new IllegalArgumentException("No terminal desconocido en producción: " + p);
            }
            
            // Validar que los símbolos en la derivación sean válidos
            for (char c : p.getDerivacion().toCharArray()) {
                boolean esNoTerminal = noTerminales.contains(String.valueOf(c));
                boolean esTerminal = terminales.contains(c);
                
                if (!esNoTerminal && !esTerminal) {
                    throw new IllegalArgumentException(
                        "Símbolo desconocido '" + c + "' en producción: " + p);
                }
            }
        }
    }
    
    /**
     * Obtiene todas las producciones para un no terminal dado
     */
    public List<Produccion> getProduccionesDe(String noTerminal) {
        List<Produccion> resultado = new ArrayList<>();
        for (Produccion p : producciones) {
            if (p.getNoTerminal().equals(noTerminal)) {
                resultado.add(p);
            }
        }
        return resultado;
    }
    
    /**
     * Verifica si un símbolo es terminal
     */
    public boolean esTerminal(char simbolo) {
        return terminales.contains(simbolo);
    }
    
    /**
     * Verifica si un string es no terminal
     */
    public boolean esNoTerminal(String simbolo) {
        return noTerminales.contains(simbolo);
    }
    
    // Getters
    public Set<String> getNoTerminales() { return new HashSet<>(noTerminales); }
    public Set<Character> getTerminales() { return new HashSet<>(terminales); }
    public List<Produccion> getProducciones() { return new ArrayList<>(producciones); }
    public String getSimboloInicial() { return simboloInicial; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Gramática Libre de Contexto:\n");
        sb.append("  No terminales: ").append(noTerminales).append("\n");
        sb.append("  Terminales: ").append(terminales).append("\n");
        sb.append("  Símbolo inicial: ").append(simboloInicial).append("\n");
        sb.append("  Producciones:\n");
        for (Produccion p : producciones) {
            sb.append("    ").append(p).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Ejemplos de gramáticas predefinidas
     */
    public static class Ejemplos {
        
        /**
         * Gramática para a^n b^n (n >= 0)
         * S → aSb | ε
         */
        public static GLC anbn() {
            Set<String> noTerm = Set.of("S");
            Set<Character> term = Set.of('a', 'b');
            List<Produccion> prod = List.of(
                new Produccion("S", "aSb"),
                new Produccion("S", "")
            );
            return new GLC(noTerm, term, prod, "S");
        }
        
        /**
         * Gramática para palíndromos sobre {a, b}
         * S → aSa | bSb | a | b | ε
         */
        public static GLC palindromos() {
            Set<String> noTerm = Set.of("S");
            Set<Character> term = Set.of('a', 'b');
            List<Produccion> prod = List.of(
                new Produccion("S", "aSa"),
                new Produccion("S", "bSb"),
                new Produccion("S", "a"),
                new Produccion("S", "b"),
                new Produccion("S", "")
            );
            return new GLC(noTerm, term, prod, "S");
        }
        
        /**
         * Gramática para paréntesis balanceados
         * S → (S) | SS | ε
         */
        public static GLC parentesis() {
            Set<String> noTerm = Set.of("S");
            Set<Character> term = Set.of('(', ')');
            List<Produccion> prod = List.of(
                new Produccion("S", "(S)"),
                new Produccion("S", "SS"),
                new Produccion("S", "")
            );
            return new GLC(noTerm, term, prod, "S");
        }
        
        /**
         * Gramática para expresiones aritméticas simples
         * E → E+T | T
         * T → T*F | F
         * F → (E) | a
         */
        public static GLC expresionesAritmeticas() {
            Set<String> noTerm = Set.of("E", "T", "F");
            Set<Character> term = Set.of('+', '*', '(', ')', 'a');
            List<Produccion> prod = List.of(
                new Produccion("E", "E+T"),
                new Produccion("E", "T"),
                new Produccion("T", "T*F"),
                new Produccion("T", "F"),
                new Produccion("F", "(E)"),
                new Produccion("F", "a")
            );
            return new GLC(noTerm, term, prod, "E");
        }
    }
}