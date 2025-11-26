// ============================================================================
// Package: automatas.generador
// ============================================================================

package automatas.generador;

import automatas.core.AP;
import java.util.*;
import java.util.regex.*;

/**
 * Parser y generador de autómatas de pila a partir de expresiones
 * Soporta variables dependientes (a^n b^2n) e independientes (a^n b^m c^k)
 * Soporta condiciones individuales por variable
 */
public class GeneradorAP {
    
    /**
     * Genera un autómata de pila a partir de una expresión
     * @param expresion Expresión como "a^n b^m, n >= 2, m < 5" o "a^n b^2n, n > 1"
     * @return Autómata de pila generado
     */
    public static AP generar(String expresion) throws Exception {
        // Separar expresión y condiciones
        String[] partes = expresion.split(",");
        if (partes.length < 1) {
            throw new Exception("Expresión inválida");
        }
        
        String bloqueExpresion = partes[0].trim();
        
        // Parsear condiciones (pueden ser múltiples)
        Map<String, Condicion> condiciones = new HashMap<>();
        for (int i = 1; i < partes.length; i++) {
            Condicion cond = parsearCondicion(partes[i].trim());
            if (cond != null) {
                condiciones.put(cond.variable, cond);
            }
        }
        
        // Parsear la expresión
        List<BloqueSimbolos> bloques = parsearExpresion(bloqueExpresion);
        
        // Detectar tipo de expresión
        TipoExpresion tipo = detectarTipoExpresion(bloques);
        
        // Generar el autómata según el tipo
        return construirAutomata(bloques, condiciones, tipo);
    }
    
    private static List<BloqueSimbolos> parsearExpresion(String expresion) throws Exception {
        List<BloqueSimbolos> bloques = new ArrayList<>();
        
        // Pattern para detectar bloques como a^n, b^m, c^2n, etc.
        Pattern pattern = Pattern.compile("([a-z])\\^(\\d*[a-z]\\d*|\\d+|[a-z])");
        Matcher matcher = pattern.matcher(expresion.replaceAll("\\s+", ""));
        
        while (matcher.find()) {
            char simbolo = matcher.group(1).charAt(0);
            String exponente = matcher.group(2);
            
            bloques.add(new BloqueSimbolos(simbolo, exponente));
        }
        
        if (bloques.isEmpty()) {
            throw new Exception("No se encontraron bloques válidos en la expresión");
        }
        
        if (bloques.size() > 3) {
            throw new Exception("Solo se soportan hasta 3 bloques");
        }
        
        return bloques;
    }
    
    private static Condicion parsearCondicion(String condicionStr) {
        condicionStr = condicionStr.toLowerCase().trim();
        
        // Ignorar condiciones de paridad
        if (condicionStr.contains("par") || condicionStr.contains("impar")) {
            return null;
        }
        
        // Extraer variable (n, m, k, etc.)
        String variable = null;
        Pattern varPattern = Pattern.compile("([a-z])\\s*[><=]");
        Matcher varMatcher = varPattern.matcher(condicionStr);
        if (varMatcher.find()) {
            variable = varMatcher.group(1);
        }
        
        if (variable == null) {
            return null;
        }
        
        // Parsear condición
        if (condicionStr.matches(".*" + variable + "\\s*>=\\s*(\\d+).*")) {
            Pattern p = Pattern.compile(variable + "\\s*>=\\s*(\\d+)");
            Matcher m = p.matcher(condicionStr);
            if (m.find()) {
                return new Condicion(variable, TipoCondicion.MAYOR_IGUAL, Integer.parseInt(m.group(1)));
            }
        } else if (condicionStr.matches(".*" + variable + "\\s*>\\s*(\\d+).*")) {
            Pattern p = Pattern.compile(variable + "\\s*>\\s*(\\d+)");
            Matcher m = p.matcher(condicionStr);
            if (m.find()) {
                return new Condicion(variable, TipoCondicion.MAYOR, Integer.parseInt(m.group(1)));
            }
        } else if (condicionStr.matches(".*" + variable + "\\s*<=\\s*(\\d+).*")) {
            Pattern p = Pattern.compile(variable + "\\s*<=\\s*(\\d+)");
            Matcher m = p.matcher(condicionStr);
            if (m.find()) {
                return new Condicion(variable, TipoCondicion.MENOR_IGUAL, Integer.parseInt(m.group(1)));
            }
        } else if (condicionStr.matches(".*" + variable + "\\s*<\\s*(\\d+).*")) {
            Pattern p = Pattern.compile(variable + "\\s*<\\s*(\\d+)");
            Matcher m = p.matcher(condicionStr);
            if (m.find()) {
                return new Condicion(variable, TipoCondicion.MENOR, Integer.parseInt(m.group(1)));
            }
        } else if (condicionStr.contains("mayor que")) {
            Pattern p = Pattern.compile(variable + "\\s+mayor que\\s*(\\d+)");
            Matcher m = p.matcher(condicionStr);
            if (m.find()) {
                return new Condicion(variable, TipoCondicion.MAYOR, Integer.parseInt(m.group(1)));
            }
        } else if (condicionStr.contains("menor que")) {
            Pattern p = Pattern.compile(variable + "\\s+menor que\\s*(\\d+)");
            Matcher m = p.matcher(condicionStr);
            if (m.find()) {
                return new Condicion(variable, TipoCondicion.MENOR, Integer.parseInt(m.group(1)));
            }
        }
        
        return null;
    }
    
    private static TipoExpresion detectarTipoExpresion(List<BloqueSimbolos> bloques) {
        // Extraer todas las variables únicas
        Set<String> variables = new HashSet<>();
        
        for (BloqueSimbolos bloque : bloques) {
            String var = extraerVariable(bloque.exponente);
            variables.add(var);
        }
        
        if (variables.size() == 1) {
            // Todas usan la misma variable (dependientes)
            return TipoExpresion.DEPENDIENTE;
        } else {
            // Variables diferentes (independientes)
            return TipoExpresion.INDEPENDIENTE;
        }
    }
    
    private static String extraerVariable(String exponente) {
        // Extraer la variable del exponente (n, m, k, etc.)
        Pattern pattern = Pattern.compile("([a-z])");
        Matcher matcher = pattern.matcher(exponente);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "n"; // Por defecto
    }
    
    private static int extraerCoeficiente(String exponente) {
        // "n" -> 1
        // "2n" -> 2
        // "3n" -> 3
        // "n2" -> 2 (se interpreta como 2n)
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(exponente);
        
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        return 1;
    }
    
    private static AP construirAutomata(List<BloqueSimbolos> bloques, 
                                       Map<String, Condicion> condiciones,
                                       TipoExpresion tipo) throws Exception {
        AP ap = new AP();
        
        // Configurar símbolos de entrada
        for (BloqueSimbolos bloque : bloques) {
            ap.addSimboloEntrada(bloque.simbolo);
        }
        ap.addSimboloPila('Z'); // Símbolo inicial de pila
        
        if (tipo == TipoExpresion.DEPENDIENTE) {
            return construirAutomataDependiente(ap, bloques, condiciones);
        } else {
            return construirAutomataIndependiente(ap, bloques, condiciones);
        }
    }
    
    /**
     * Construye autómata para variables dependientes (a^n b^2n, etc.)
     */
    private static AP construirAutomataDependiente(AP ap, List<BloqueSimbolos> bloques,
                                                   Map<String, Condicion> condiciones) throws Exception {
        ap.addSimboloPila('X');
        
        if (bloques.size() == 2) {
            BloqueSimbolos primero = bloques.get(0);
            BloqueSimbolos segundo = bloques.get(1);
            
            int coefPrimero = extraerCoeficiente(primero.exponente);
            int coefSegundo = extraerCoeficiente(segundo.exponente);
            
            if (coefPrimero == 1 && coefSegundo == 1) {
                // a^n b^n
                return construirAnBn(ap, bloques, condiciones);
            } else if (coefPrimero == 1) {
                // a^n b^kn
                return construirAnBkn(ap, bloques, coefSegundo, condiciones);
            } else {
                throw new Exception("Patrón no soportado");
            }
        } else if (bloques.size() == 3) {
            // a^n b^kn c^jn
            return construirAnBknCjn(ap, bloques, condiciones);
        } else {
            throw new Exception("Número de bloques no soportado");
        }
    }
    
    /**
     * Construye autómata para variables independientes (a^n b^m c^k)
     */
    private static AP construirAutomataIndependiente(AP ap, List<BloqueSimbolos> bloques,
                                                     Map<String, Condicion> condiciones) throws Exception {
        // Para variables independientes, simplemente verificamos la estructura
        // El autómata acepta cualquier combinación de cantidades
        
        ap.setEstadoInicial("q0");
        
        String estadoActual = "q0";
        
        for (int i = 0; i < bloques.size(); i++) {
            BloqueSimbolos bloque = bloques.get(i);
            String estadoSiguiente = (i == bloques.size() - 1) ? "qf" : "q" + (i + 1);
            String variable = extraerVariable(bloque.exponente);
            Condicion cond = condiciones.get(variable);
            
            // Aplicar restricciones de cantidad si existen
            if (cond != null && (cond.tipo == TipoCondicion.MAYOR || cond.tipo == TipoCondicion.MAYOR_IGUAL)) {
                int min = cond.tipo == TipoCondicion.MAYOR ? cond.valor + 1 : cond.valor;
                
                // Crear estados para forzar mínimo
                for (int j = 0; j < min; j++) {
                    String tempEstado = estadoActual + "_" + bloque.simbolo + j;
                    ap.addEstado(tempEstado);
                    
                    if (j == 0) {
                        ap.addTransicion(estadoActual, bloque.simbolo, 'Z', tempEstado, "Z");
                    } else {
                        ap.addTransicion(estadoActual + "_" + bloque.simbolo + (j-1), 
                                       bloque.simbolo, 'Z', tempEstado, "Z");
                    }
                    
                    if (j == min - 1) {
                        estadoActual = tempEstado;
                    }
                }
            }
            
            // Leer símbolo repetidamente
            ap.addTransicion(estadoActual, bloque.simbolo, 'Z', estadoActual, "Z");
            
            // Transición epsilon al siguiente estado
            ap.addEstado(estadoSiguiente);
            ap.addTransicion(estadoActual, null, 'Z', estadoSiguiente, "Z");
            
            estadoActual = estadoSiguiente;
        }
        
        ap.addEstadoFinal("qf");
        
        return ap;
    }
    
    /**
     * Construye autómata para a^n b^n
     */
    private static AP construirAnBn(AP ap, List<BloqueSimbolos> bloques, 
                                   Map<String, Condicion> condiciones) {
        char a = bloques.get(0).simbolo;
        char b = bloques.get(1).simbolo;
        String variable = extraerVariable(bloques.get(0).exponente);
        Condicion cond = condiciones.get(variable);
        
        ap.setEstadoInicial("q0");
        ap.addEstado("q1");
        ap.addEstadoFinal("qf");
        
        // Apilar X por cada 'a'
        ap.addTransicion("q0", a, 'Z', "q0", "XZ");
        ap.addTransicion("q0", a, 'X', "q0", "XX");
        
        // Aplicar restricciones
        if (cond != null && (cond.tipo == TipoCondicion.MAYOR || cond.tipo == TipoCondicion.MAYOR_IGUAL)) {
            int min = cond.tipo == TipoCondicion.MAYOR ? cond.valor + 1 : cond.valor;
            
            for (int i = 0; i < min; i++) {
                String estadoActual = "qmin" + i;
                String estadoSig = (i == min - 1) ? "q0" : "qmin" + (i + 1);
                
                if (i == 0) {
                    ap.setEstadoInicial(estadoActual);
                    ap.addTransicion(estadoActual, a, 'Z', estadoSig, "XZ");
                } else {
                    ap.addEstado(estadoActual);
                    ap.addTransicion(estadoActual, a, 'X', estadoSig, "XX");
                }
            }
        }
        
        // Desapilar
        ap.addTransicion("q0", b, 'X', "q1", "");
        ap.addTransicion("q1", b, 'X', "q1", "");
        ap.addTransicion("q1", null, 'Z', "qf", "Z");
        
        // Cadena vacía si no hay restricciones
        if (cond == null || cond.tipo == TipoCondicion.CUALQUIERA) {
            ap.addTransicion("q0", null, 'Z', "qf", "Z");
        }
        
        return ap;
    }
    
    /**
     * Construye autómata para a^n b^(k*n)
     */
    private static AP construirAnBkn(AP ap, List<BloqueSimbolos> bloques, int k,
                                    Map<String, Condicion> condiciones) {
        char a = bloques.get(0).simbolo;
        char b = bloques.get(1).simbolo;
        String variable = extraerVariable(bloques.get(0).exponente);
        Condicion cond = condiciones.get(variable);
        
        ap.setEstadoInicial("q0");
        
        // Apilar k X's por cada 'a'
        for (int i = 0; i <= k; i++) {
            ap.addEstado("qpush" + i);
        }
        
        ap.addTransicion("q0", a, 'Z', "qpush0", "Z");
        
        for (int i = 0; i < k; i++) {
            ap.addTransicion("qpush" + i, null, 'Z', "qpush" + (i+1), "XZ");
            ap.addTransicion("qpush" + i, null, 'X', "qpush" + (i+1), "XX");
        }
        
        ap.addTransicion("qpush" + k, null, 'X', "q0", "X");
        ap.addTransicion("q0", a, 'X', "qpush0", "X");
        
        // Desapilar
        ap.addEstado("q1");
        ap.addTransicion("q0", b, 'X', "q1", "");
        ap.addTransicion("q1", b, 'X', "q1", "");
        
        ap.addEstadoFinal("qf");
        ap.addTransicion("q1", null, 'Z', "qf", "Z");
        
        if (cond == null) {
            ap.addTransicion("q0", null, 'Z', "qf", "Z");
        }
        
        return ap;
    }
    
    /**
     * Construye autómata para a^n b^kn c^jn
     */
    private static AP construirAnBknCjn(AP ap, List<BloqueSimbolos> bloques,
                                       Map<String, Condicion> condiciones) throws Exception {
        char a = bloques.get(0).simbolo;
        char b = bloques.get(1).simbolo;
        char c = bloques.get(2).simbolo;
        
        int coefA = extraerCoeficiente(bloques.get(0).exponente);
        int coefB = extraerCoeficiente(bloques.get(1).exponente);
        int coefC = extraerCoeficiente(bloques.get(2).exponente);
        
        if (coefA != 1) {
            throw new Exception("El primer bloque debe tener coeficiente 1");
        }
        
        // Estrategia: apilar coefB X's por cada a, desapilar con b's
        // Luego apilar coefC Y's por cada a contada, desapilar con c's
        // Problema: necesitamos contar a's dos veces
        
        // Solución simplificada: solo soportamos casos específicos
        throw new Exception("Autómata con 3 bloques dependientes aún no implementado completamente");
    }
    
    // Clases auxiliares
    private static class BloqueSimbolos {
        char simbolo;
        String exponente;
        
        BloqueSimbolos(char simbolo, String exponente) {
            this.simbolo = simbolo;
            this.exponente = exponente;
        }
        
        @Override
        public String toString() {
            return simbolo + "^" + exponente;
        }
    }
    
    private static class Condicion {
        String variable;
        TipoCondicion tipo;
        int valor;
        
        Condicion(String variable, TipoCondicion tipo, int valor) {
            this.variable = variable;
            this.tipo = tipo;
            this.valor = valor;
        }
        
        @Override
        public String toString() {
            switch (tipo) {
                case MAYOR: return variable + " > " + valor;
                case MAYOR_IGUAL: return variable + " >= " + valor;
                case MENOR: return variable + " < " + valor;
                case MENOR_IGUAL: return variable + " <= " + valor;
                default: return variable + " cualquiera";
            }
        }
    }
    
    private enum TipoCondicion {
        MAYOR_IGUAL, MAYOR, MENOR_IGUAL, MENOR, CUALQUIERA
    }
    
    private enum TipoExpresion {
        DEPENDIENTE,    // a^n b^2n (misma variable)
        INDEPENDIENTE   // a^n b^m (variables distintas)
    }
    
    // Método main para pruebas
    public static void main(String[] args) {
        try {
            // Prueba 1: Variables dependientes a^n b^n
            System.out.println("=== Prueba 1: a^n b^n ===");
            AP ap1 = generar("a^n b^n");
            System.out.println(ap1);
            System.out.println("Tipo: Variables dependientes\n");
            
            String[] pruebas1 = {"ab", "aabb", "aaabbb", "aab", ""};
            for (String cadena : pruebas1) {
                boolean acepta = ap1.acepta(cadena);
                System.out.println("'" + cadena + "': " + (acepta ? "✓ ACEPTA" : "✗ RECHAZA"));
            }
            
            // Prueba 2: Variables independientes a^n b^m
            System.out.println("\n=== Prueba 2: a^n b^m, m mayor que 2===");
            AP ap2 = generar("a^n b^m, m > 2");
            System.out.println(ap2);
            System.out.println("Tipo: Variables independientes\n");
            
            String[] pruebas2 = {"ab", "aabb", "aaab", "abbb", "a", "b", ""};
            for (String cadena : pruebas2) {
                boolean acepta = ap2.acepta(cadena);
                System.out.println("'" + cadena + "': " + (acepta ? "✓ ACEPTA" : "✗ RECHAZA"));
            }
            
            // Prueba 3: Variables independientes con condiciones
            System.out.println("\n=== Prueba 3: a^n b^m, n >= 2, m >= 1 ===");
            AP ap3 = generar("a^n b^m, n >= 2, m >= 1");
            System.out.println(ap3);
            
            String[] pruebas3 = {"ab", "aab", "aabb", "aaabbb", "a", "b", ""};
            for (String cadena : pruebas3) {
                boolean acepta = ap3.acepta(cadena);
                System.out.println("'" + cadena + "': " + (acepta ? "✓ ACEPTA" : "✗ RECHAZA"));
            }
            
            // Prueba 4: Tres variables independientes
            System.out.println("\n=== Prueba 4: a^n b^m c^k ===");
            AP ap4 = generar("a^n b^m c^k");
            System.out.println(ap4);
            
            String[] pruebas4 = {"abc", "aabbcc", "aaabbbccc", "abc", "aabbcccc", ""};
            for (String cadena : pruebas4) {
                boolean acepta = ap4.acepta(cadena);
                System.out.println("'" + cadena + "': " + (acepta ? "✓ ACEPTA" : "✗ RECHAZA"));
            }
            
            // Prueba 5: a^n b^2n
            System.out.println("\n=== Prueba 5: a^n b^2n ===");
            AP ap5 = generar("a^n b^2n");
            System.out.println(ap5);
            
            String[] pruebas5 = {"abb", "aabbbb", "aaabbbbbb", "ab", "aabbb"};
            for (String cadena : pruebas5) {
                boolean acepta = ap5.acepta(cadena);
                System.out.println("'" + cadena + "': " + (acepta ? "✓ ACEPTA" : "✗ RECHAZA"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}