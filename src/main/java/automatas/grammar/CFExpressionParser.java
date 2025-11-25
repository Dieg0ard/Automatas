package automatas.grammar;

import automatas.algoritmos.GLCtoAP;
import automatas.core.AP;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para expresiones de lenguajes libres de contexto
 * 
 * Soporta notaciones como:
 * - a^n b^n         (mismo número de a's y b's)
 * - a^n b^2n        (doble de b's que a's)
 * - a^n b^m (n=m)   (equivalente a a^n b^n)
 * - w w^R           (palíndromos)
 * - a^n b^n c^n     (tres símbolos iguales - NO es libre de contexto, lanza error)
 */
public class CFExpressionParser {
    
    /**
     * Parsea una expresión y genera la gramática correspondiente
     */
    public static GLC parse(String expression) {
        expression = expression.trim().toLowerCase();
        
        // Patrón 1: a^n b^n (mismo exponente)
        Pattern pattern1 = Pattern.compile("([a-z])\\^n\\s+([a-z])\\^n");
        Matcher matcher1 = pattern1.matcher(expression);
        if (matcher1.matches()) {
            char a = matcher1.group(1).charAt(0);
            char b = matcher1.group(2).charAt(0);
            return generarAnBn(a, b);
        }
        
        // Patrón 2: a^n b^m con condición (n=m)
        Pattern pattern2 = Pattern.compile("([a-z])\\^([nm])\\s+([a-z])\\^([nm])\\s*\\(\\s*([nm])\\s*=\\s*([nm])\\s*\\)");
        Matcher matcher2 = pattern2.matcher(expression);
        if (matcher2.matches()) {
            char a = matcher2.group(1).charAt(0);
            char b = matcher2.group(3).charAt(0);
            String var1 = matcher2.group(2);
            String var2 = matcher2.group(4);
            String eq1 = matcher2.group(5);
            String eq2 = matcher2.group(6);
            
            // Verificar que n=m o m=n
            if ((var1.equals("n") && var2.equals("m") && eq1.equals("n") && eq2.equals("m")) ||
                (var1.equals("m") && var2.equals("n") && eq1.equals("m") && eq2.equals("n"))) {
                return generarAnBn(a, b);
            }
        }
        
        // Patrón 3: a^n b^2n (doble exponente)
        Pattern pattern3 = Pattern.compile("([a-z])\\^n\\s+([a-z])\\^2n");
        Matcher matcher3 = pattern3.matcher(expression);
        if (matcher3.matches()) {
            char a = matcher3.group(1).charAt(0);
            char b = matcher3.group(2).charAt(0);
            return generarAnB2n(a, b);
        }
        
        // Patrón 4: w w^R (palíndromos)
        Pattern pattern4 = Pattern.compile("w\\s+w\\^r");
        Matcher matcher4 = pattern4.matcher(expression);
        if (matcher4.matches()) {
            return generarPalindromos();
        }
        
        // Patrón 5: palindromo o palindromos
        if (expression.matches("palin?dromos?")) {
            return generarPalindromos();
        }
        
        // Patrón 6: (a^n b^n)* - repetición del patrón
        Pattern pattern6 = Pattern.compile("\\(([a-z])\\^n\\s+([a-z])\\^n\\)\\*");
        Matcher matcher6 = pattern6.matcher(expression);
        if (matcher6.matches()) {
            char a = matcher6.group(1).charAt(0);
            char b = matcher6.group(2).charAt(0);
            return generarAnBnRepetido(a, b);
        }
        
        // Patrón 7: paréntesis balanceados
        if (expression.matches("parentesis\\s+balanceados?") || 
            expression.matches("balanced\\s+parenthes[ei]s")) {
            return generarParentesisBalanceados();
        }
        
        // Patrón 8: a^n b^n c^n - NO es libre de contexto
        Pattern pattern8 = Pattern.compile("([a-z])\\^n\\s+([a-z])\\^n\\s+([a-z])\\^n");
        Matcher matcher8 = pattern8.matcher(expression);
        if (matcher8.matches()) {
            throw new IllegalArgumentException(
                "El lenguaje a^n b^n c^n NO es libre de contexto. " +
                "No se puede construir un autómata de pila para este lenguaje.");
        }
        
        throw new IllegalArgumentException(
            "Expresión no reconocida: " + expression + "\n" +
            "Formatos soportados:\n" +
            "  - a^n b^n\n" +
            "  - a^n b^2n\n" +
            "  - w w^R (palíndromos)\n" +
            "  - palindromos\n" +
            "  - (a^n b^n)*\n" +
            "  - parentesis balanceados"
        );
    }
    
    /**
     * Genera gramática para a^n b^n
     * S → aSb | ε
     */
    private static GLC generarAnBn(char a, char b) {
        Set<String> noTerminales = Set.of("S");
        Set<Character> terminales = Set.of(a, b);
        List<GLC.Produccion> producciones = List.of(
            new GLC.Produccion("S", a + "S" + b),
            new GLC.Produccion("S", "")
        );
        return new GLC(noTerminales, terminales, producciones, "S");
    }
    
    /**
     * Genera gramática para a^n b^2n
     * S → aSbb | ε
     */
    private static GLC generarAnB2n(char a, char b) {
        Set<String> noTerminales = Set.of("S");
        Set<Character> terminales = Set.of(a, b);
        List<GLC.Produccion> producciones = List.of(
            new GLC.Produccion("S", a + "S" + b + b),
            new GLC.Produccion("S", "")
        );
        return new GLC(noTerminales, terminales, producciones, "S");
    }
    
    /**
     * Genera gramática para palíndromos sobre {a, b}
     * S → aSa | bSb | a | b | ε
     */
    private static GLC generarPalindromos() {
        Set<String> noTerminales = Set.of("S");
        Set<Character> terminales = Set.of('a', 'b');
        List<GLC.Produccion> producciones = List.of(
            new GLC.Produccion("S", "aSa"),
            new GLC.Produccion("S", "bSb"),
            new GLC.Produccion("S", "a"),
            new GLC.Produccion("S", "b"),
            new GLC.Produccion("S", "")
        );
        return new GLC(noTerminales, terminales, producciones, "S");
    }
    
    /**
     * Genera gramática para (a^n b^n)* - repeticiones de a^n b^n
     * S → TS | ε
     * T → aTb | ε
     */
    private static GLC generarAnBnRepetido(char a, char b) {
        Set<String> noTerminales = Set.of("S", "T");
        Set<Character> terminales = Set.of(a, b);
        List<GLC.Produccion> producciones = List.of(
            new GLC.Produccion("S", "TS"),
            new GLC.Produccion("S", ""),
            new GLC.Produccion("T", a + "T" + b),
            new GLC.Produccion("T", "")
        );
        return new GLC(noTerminales, terminales, producciones, "S");
    }
    
    /**
     * Genera gramática para paréntesis balanceados
     * S → (S) | SS | ε
     */
    private static GLC generarParentesisBalanceados() {
        Set<String> noTerminales = Set.of("S");
        Set<Character> terminales = Set.of('(', ')');
        List<GLC.Produccion> producciones = List.of(
            new GLC.Produccion("S", "(S)"),
            new GLC.Produccion("S", "SS"),
            new GLC.Produccion("S", "")
        );
        return new GLC(noTerminales, terminales, producciones, "S");
    }
    
    /**
     * Método principal con ejemplos
     */
    public static void main(String[] args) {
        String[] ejemplos = {
            "a^n b^n",
            "a^n b^2n",
            "w w^R",
            "palindromos",
            "(a^n b^n)*",
            "parentesis balanceados"
        };
        
        System.out.println("=== EJEMPLOS DE PARSING ===\n");
        
        for (String expr : ejemplos) {
            try {
                System.out.println("Expresión: " + expr);
                GLC gramatica = CFExpressionParser.parse(expr);
                System.out.println(gramatica);
                
                // Convertir a AP
                AP automata = GLCtoAP.convertirGramatica(gramatica);
                System.out.println("AP generado con " + automata.getEstados().size() + " estados\n");
                
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage() + "\n");
            }
        }
        
        // Probar expresión inválida
        System.out.println("Expresión: a^n b^n c^n");
        try {
            CFExpressionParser.parse("a^n b^n c^n");
        } catch (Exception e) {
            System.out.println("ERROR (esperado): " + e.getMessage());
        }
    }
}