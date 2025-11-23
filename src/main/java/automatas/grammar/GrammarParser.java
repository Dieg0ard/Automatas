package automatas.grammar;

import java.util.*;

/**
 * Parser para gramáticas libres de contexto desde notación textual
 * 
 * Formato esperado:
 * S -> aSb | ε
 * S -> bSa
 * 
 * Reglas:
 * - Cada línea es una producción
 * - No terminales son strings (usualmente una letra mayúscula)
 * - Terminales son caracteres individuales (minúsculas o símbolos)
 * - ε o vacío representa la cadena vacía
 * - | separa alternativas en la misma línea
 * - -> o → separa lado izquierdo de derecho
 */
public class GrammarParser {
    
    public GLC parse(String input) {
        Set<String> noTerminales = new HashSet<>();
        Set<Character> terminales = new HashSet<>();
        List<GLC.Produccion> producciones = new ArrayList<>();
        String simboloInicial = null;
        
        String[] lineas = input.trim().split("\n");
        
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("//") || linea.startsWith("#")) {
                continue; // Ignorar comentarios y líneas vacías
            }
            
            // Separar por -> o →
            String[] partes = linea.split("->|→");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Formato inválido en línea: " + linea);
            }
            
            String noTerminal = partes[0].trim();
            String derivaciones = partes[1].trim();
            
            // El primer no terminal es el símbolo inicial
            if (simboloInicial == null) {
                simboloInicial = noTerminal;
            }
            
            // Agregar el no terminal al conjunto
            noTerminales.add(noTerminal);
            
            // Separar alternativas por |
            String[] alternativas = derivaciones.split("\\|");
            
            for (String alt : alternativas) {
                alt = alt.trim();
                
                // Normalizar epsilon
                if (alt.equals("ε") || alt.equals("epsilon") || alt.isEmpty()) {
                    alt = "";
                }
                
                // Agregar producción
                producciones.add(new GLC.Produccion(noTerminal, alt));
                
                // Identificar terminales en la derivación
                for (char c : alt.toCharArray()) {
                    String s = String.valueOf(c);
                    // Si no es un no terminal conocido, es un terminal
                    if (!noTerminales.contains(s) && !esNoTerminalPotencial(s)) {
                        terminales.add(c);
                    }
                }
            }
        }
        
        // Segunda pasada: identificar no terminales que no fueron detectados
        for (GLC.Produccion p : producciones) {
            for (char c : p.getDerivacion().toCharArray()) {
                String s = String.valueOf(c);
                if (esNoTerminalPotencial(s)) {
                    noTerminales.add(s);
                    terminales.remove(c);
                }
            }
        }
        
        if (simboloInicial == null) {
            throw new IllegalArgumentException("No se encontraron producciones");
        }
        
        return new GLC(noTerminales, terminales, producciones, simboloInicial);
    }
    
    /**
     * Determina si un string parece ser un no terminal
     * Por convención: letra mayúscula
     */
    private boolean esNoTerminalPotencial(String s) {
        if (s.length() != 1) return false;
        char c = s.charAt(0);
        return Character.isUpperCase(c);
    }
    
    /**
     * Método de conveniencia para crear gramáticas comunes
     */
    public static GLC fromString(String input) {
        return new GrammarParser().parse(input);
    }
    
    /**
     * Ejemplos de uso
     */
    public static void main(String[] args) {
        // Ejemplo 1: a^n b^n
        String gramatica1 = """
            S -> aSb | ε
            """;
        
        GLC g1 = fromString(gramatica1);
        System.out.println(g1);
        
        // Ejemplo 2: Palíndromos
        String gramatica2 = """
            S -> aSa
            S -> bSb
            S -> a | b | ε
            """;
        
        GLC g2 = fromString(gramatica2);
        System.out.println(g2);
        
        // Ejemplo 3: Paréntesis balanceados
        String gramatica3 = """
            S -> (S) | SS | ε
            """;
        
        GLC g3 = fromString(gramatica3);
        System.out.println(g3);
    }
}