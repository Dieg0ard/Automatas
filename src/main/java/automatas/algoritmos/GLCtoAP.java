package automatas.algoritmos;

import automatas.core.AP;
import automatas.grammar.GLC;
import automatas.io.EscritorAutomata;
import java.io.IOException;
import java.util.*;

/**
 * Convierte una Gramática Libre de Contexto (GLC) a un Autómata de Pila (AP)
 * 
 * Usa el algoritmo estándar:
 * 1. El AP tiene 3 estados: q0 (inicial), q1 (procesamiento), q2 (final)
 * 2. Inicialmente apila el símbolo inicial de la gramática
 * 3. Para cada producción A → α, crea transición que reemplaza A por α
 * 4. Para cada terminal a, crea transición que consume a de la entrada y de la pila
 * 5. Transición final cuando la pila queda con solo Z0
 */
public class GLCtoAP {
    
    private GLC gramatica;
    
    public GLCtoAP(GLC gramatica) {
        this.gramatica = gramatica;
    }
    
    public AP convertir() throws IOException {
        // Estados: q0 (inicial), q1 (procesamiento), q2 (final)
        Set<String> estados = Set.of("q0", "q1", "q2");
        
        // Alfabeto de entrada: terminales de la gramática
        Set<Character> alfabetoEntrada = new HashSet<>(gramatica.getTerminales());
        
        // Alfabeto de pila: terminales + no terminales + Z0 (símbolo inicial pila)
        Set<Character> alfabetoPila = new HashSet<>(gramatica.getTerminales());
        alfabetoPila.add('Z'); // Símbolo inicial de pila
        
        // Agregar no terminales a la pila (convertir a caracteres)
        for (String nt : gramatica.getNoTerminales()) {
            if (nt.length() == 1) {
                alfabetoPila.add(nt.charAt(0));
            } else {
                // Si el no terminal tiene más de un carácter, necesitamos manejarlo diferente
                throw new IllegalArgumentException(
                    "Los no terminales deben ser de un solo carácter: " + nt);
            }
        }
        
        List<AP.Transicion> transiciones = new ArrayList<>();
        
        // Transición inicial: (q0, ε, Z) → (q1, SZ)
        // Donde S es el símbolo inicial de la gramática
        char simboloInicial = gramatica.getSimboloInicial().charAt(0);
        transiciones.add(new AP.Transicion(
            "q0", null, 'Z', "q1", 
            String.valueOf(simboloInicial) + "Z"
        ));
        
        // Para cada producción A → α, crear transición (q1, ε, A) → (q1, α)
        for (GLC.Produccion prod : gramatica.getProducciones()) {
            char noTerminal = prod.getNoTerminal().charAt(0);
            String derivacion = prod.getDerivacion();
            
            // Normalizar epsilon
            if (derivacion.isEmpty() || derivacion.equals("ε")) {
                derivacion = "";
            }
            
            // Invertir la derivación para que se apile en el orden correcto
            String derivacionInvertida = new StringBuilder(derivacion).reverse().toString();
            
            transiciones.add(new AP.Transicion(
                "q1", null, noTerminal, "q1", derivacionInvertida
            ));
        }
        
        // Para cada terminal a, crear transición (q1, a, a) → (q1, ε)
        for (char terminal : gramatica.getTerminales()) {
            transiciones.add(new AP.Transicion(
                "q1", terminal, terminal, "q1", ""
            ));
        }
        
        // Transición final: (q1, ε, Z) → (q2, Z)
        transiciones.add(new AP.Transicion(
            "q1", null, 'Z', "q2", "Z"
        ));
        
        // El autómata acepta por estado final
        Set<String> estadosFinales = Set.of("q2");
        
        
        AP ap = new AP(estados, alfabetoEntrada, alfabetoPila, 
                     transiciones, "q0", 'Z', estadosFinales, true);
        String userHome = System.getProperty("user.home");
        String rutaCSV = userHome + "/.automatas/csv/afd.csv";
        EscritorAutomata.guardarAP(ap, rutaCSV);
        
        return ap;
    }
    
    /**
     * Convierte y muestra el proceso detallado
     */
    public void mostrarConversion() throws IOException {
        System.out.println("=== CONVERSIÓN GLC → AP ===\n");
        System.out.println("Gramática de entrada:");
        System.out.println(gramatica);
        
        AP ap = convertir();
        
        System.out.println("\nAutómata de Pila resultante:");
        System.out.println(ap);
        
        System.out.println("\nExplicación de transiciones:");
        System.out.println("1. Transición inicial apila el símbolo inicial de la gramática");
        System.out.println("2. Transiciones epsilon reemplazan no terminales según producciones");
        System.out.println("3. Transiciones con terminales consumen entrada y desapilan");
        System.out.println("4. Transición final lleva al estado de aceptación");
    }
    
    /**
     * Método estático de conveniencia
     */
    public static AP convertirGramatica(GLC gramatica) throws IOException {
        return new GLCtoAP(gramatica).convertir();
    }
    
    /**
     * Ejemplos de uso
     */
    public static void main(String[] args) throws IOException {
        // Ejemplo 1: a^n b^n
        System.out.println("=== EJEMPLO 1: a^n b^n ===");
        GLC g1 = GLC.Ejemplos.anbn();
        GLCtoAP conversor1 = new GLCtoAP(g1);
        conversor1.mostrarConversion();
        
        AP ap1 = conversor1.convertir();
        System.out.println("\nPruebas:");
        System.out.println("  acepta(\"\") = " + ap1.acepta(""));
        System.out.println("  acepta(\"ab\") = " + ap1.acepta("ab"));
        System.out.println("  acepta(\"aabb\") = " + ap1.acepta("aabb"));
        System.out.println("  acepta(\"aaabbb\") = " + ap1.acepta("aaabbb"));
        System.out.println("  acepta(\"aab\") = " + ap1.acepta("aab"));
        System.out.println("  acepta(\"abb\") = " + ap1.acepta("abb"));
        
        // Ejemplo 2: Paréntesis balanceados
        System.out.println("\n\n=== EJEMPLO 2: Paréntesis balanceados ===");
        GLC g2 = GLC.Ejemplos.parentesis();
        GLCtoAP conversor2 = new GLCtoAP(g2);
        AP ap2 = conversor2.convertir();
        
        System.out.println("Pruebas:");
        System.out.println("  acepta(\"\") = " + ap2.acepta(""));
        System.out.println("  acepta(\"()\") = " + ap2.acepta("()"));
        System.out.println("  acepta(\"(())\") = " + ap2.acepta("(())"));
        System.out.println("  acepta(\"()()\") = " + ap2.acepta("()()"));
        System.out.println("  acepta(\"(()\") = " + ap2.acepta("(()"));
        System.out.println("  acepta(\"())\") = " + ap2.acepta("())"));
    }
}