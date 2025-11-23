package automatas.core;

import java.util.*;

/**
 * Autómata de Pila (Pushdown Automaton - PDA)
 * 
 * Un AP es una 7-tupla (Q, Σ, Γ, δ, q0, Z0, F) donde:
 * - Q: conjunto finito de estados
 * - Σ: alfabeto de entrada
 * - Γ: alfabeto de la pila
 * - δ: función de transición Q × (Σ ∪ {ε}) × Γ → P(Q × Γ*)
 * - q0: estado inicial
 * - Z0: símbolo inicial de la pila
 * - F: conjunto de estados finales
 */
public class AP implements Automata {
    
    /**
     * Representa una transición del autómata de pila
     * (estadoActual, simboloEntrada, topePila) → (estadoDestino, reemplazoEnPila)
     */
    public static class Transicion {
        private String estadoOrigen;
        private Character simboloEntrada;  // null representa ε
        private Character topePila;
        private String estadoDestino;
        private String reemplazoEnPila;    // cadena a colocar en la pila (puede ser vacía)
        
        public Transicion(String estadoOrigen, Character simboloEntrada, 
                         Character topePila, String estadoDestino, 
                         String reemplazoEnPila) {
            this.estadoOrigen = estadoOrigen;
            this.simboloEntrada = simboloEntrada;
            this.topePila = topePila;
            this.estadoDestino = estadoDestino;
            this.reemplazoEnPila = reemplazoEnPila;
        }
        
        // Getters
        public String getEstadoOrigen() { return estadoOrigen; }
        public Character getSimboloEntrada() { return simboloEntrada; }
        public Character getTopePila() { return topePila; }
        public String getEstadoDestino() { return estadoDestino; }
        public String getReemplazoEnPila() { return reemplazoEnPila; }
        
        @Override
        public String toString() {
            String entrada = simboloEntrada == null ? "ε" : simboloEntrada.toString();
            return String.format("δ(%s, %s, %c) = (%s, %s)", 
                estadoOrigen, entrada, topePila, estadoDestino, 
                reemplazoEnPila.isEmpty() ? "ε" : reemplazoEnPila);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Transicion)) return false;
            Transicion t = (Transicion) o;
            return Objects.equals(estadoOrigen, t.estadoOrigen) &&
                   Objects.equals(simboloEntrada, t.simboloEntrada) &&
                   Objects.equals(topePila, t.topePila) &&
                   Objects.equals(estadoDestino, t.estadoDestino) &&
                   Objects.equals(reemplazoEnPila, t.reemplazoEnPila);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(estadoOrigen, simboloEntrada, topePila, 
                              estadoDestino, reemplazoEnPila);
        }
    }
    
    /**
     * Configuración del autómata (estado + contenido de pila)
     */
    public static class Configuracion {
        private String estado;
        private Stack<Character> pila;
        private String entradaRestante;
        
        public Configuracion(String estado, Stack<Character> pila, String entradaRestante) {
            this.estado = estado;
            this.pila = new Stack<>();
            this.pila.addAll(pila);  // Copia profunda
            this.entradaRestante = entradaRestante;
        }
        
        public String getEstado() { return estado; }
        public Stack<Character> getPila() { return pila; }
        public String getEntradaRestante() { return entradaRestante; }
        
        @Override
        public String toString() {
            return String.format("(%s, \"%s\", [%s])", 
                estado, entradaRestante, pilaToString());
        }
        
        private String pilaToString() {
            if (pila.isEmpty()) return "vacía";
            StringBuilder sb = new StringBuilder();
            for (int i = pila.size() - 1; i >= 0; i--) {
                sb.append(pila.get(i));
            }
            return sb.toString();
        }
    }
    
    // Componentes del autómata
    private Set<String> estados;
    private Set<Character> alfabetoEntrada;
    private Set<Character> alfabetoPila;
    private List<Transicion> transiciones;
    private String estadoInicial;
    private Character simboloInicialPila;
    private Set<String> estadosFinales;
    private boolean aceptaPorEstadoFinal;  // true: acepta por estado final, false: por pila vacía
    
    /**
     * Constructor completo
     */
    public AP(Set<String> estados, 
              Set<Character> alfabetoEntrada,
              Set<Character> alfabetoPila,
              List<Transicion> transiciones,
              String estadoInicial,
              Character simboloInicialPila,
              Set<String> estadosFinales,
              boolean aceptaPorEstadoFinal) {
        
        this.estados = new HashSet<>(estados);
        this.alfabetoEntrada = new HashSet<>(alfabetoEntrada);
        this.alfabetoPila = new HashSet<>(alfabetoPila);
        this.transiciones = new ArrayList<>(transiciones);
        this.estadoInicial = estadoInicial;
        this.simboloInicialPila = simboloInicialPila;
        this.estadosFinales = new HashSet<>(estadosFinales);
        this.aceptaPorEstadoFinal = aceptaPorEstadoFinal;
        
        validar();
    }
    
    /**
     * Constructor simplificado (acepta por estado final por defecto)
     */
    public AP(Set<String> estados, 
              Set<Character> alfabetoEntrada,
              Set<Character> alfabetoPila,
              List<Transicion> transiciones,
              String estadoInicial,
              Character simboloInicialPila,
              Set<String> estadosFinales) {
        this(estados, alfabetoEntrada, alfabetoPila, transiciones, 
             estadoInicial, simboloInicialPila, estadosFinales, true);
    }
    
    /**
     * Valida la consistencia del autómata
     */
    private void validar() {
        if (!estados.contains(estadoInicial)) {
            throw new IllegalArgumentException("Estado inicial no está en el conjunto de estados");
        }
        
        if (!alfabetoPila.contains(simboloInicialPila)) {
            throw new IllegalArgumentException("Símbolo inicial de pila no está en el alfabeto de pila");
        }
        
        for (String estado : estadosFinales) {
            if (!estados.contains(estado)) {
                throw new IllegalArgumentException("Estado final " + estado + " no está en el conjunto de estados");
            }
        }
        
        for (Transicion t : transiciones) {
            if (!estados.contains(t.getEstadoOrigen()) || !estados.contains(t.getEstadoDestino())) {
                throw new IllegalArgumentException("Transición contiene estados inválidos: " + t);
            }
            
            if (t.getSimboloEntrada() != null && !alfabetoEntrada.contains(t.getSimboloEntrada())) {
                throw new IllegalArgumentException("Símbolo de entrada inválido en transición: " + t);
            }
            
            if (!alfabetoPila.contains(t.getTopePila())) {
                throw new IllegalArgumentException("Símbolo de pila inválido en transición: " + t);
            }
            
            for (char c : t.getReemplazoEnPila().toCharArray()) {
                if (!alfabetoPila.contains(c)) {
                    throw new IllegalArgumentException("Símbolo de reemplazo inválido en transición: " + t);
                }
            }
        }
    }
    
    /**
     * Procesa una cadena y determina si es aceptada
     * Implementa el método de la interfaz Automata
     */
    @Override
    public boolean acepta(String cadena) {
        Stack<Character> pilaInicial = new Stack<>();
        pilaInicial.push(simboloInicialPila);
        
        Configuracion inicial = new Configuracion(estadoInicial, pilaInicial, cadena);
        
        return procesarConfiguracion(inicial, new HashSet<>());
    }
    
    /**
     * Procesa una configuración recursivamente (búsqueda en profundidad)
     */
    private boolean procesarConfiguracion(Configuracion config, Set<String> visitados) {
        // Generar ID único para esta configuración
        String configId = config.toString();
        if (visitados.contains(configId)) {
            return false;  // Ya visitamos esta configuración
        }
        visitados.add(configId);
        
        // Verificar condición de aceptación
        if (esConfiguracionAceptacion(config)) {
            return true;
        }
        
        // Obtener transiciones aplicables
        List<Transicion> transAplicables = getTransicionesAplicables(config);
        
        // Intentar cada transición
        for (Transicion t : transAplicables) {
            Configuracion nuevaConfig = aplicarTransicion(config, t);
            if (procesarConfiguracion(nuevaConfig, new HashSet<>(visitados))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Determina si una configuración es de aceptación
     */
    private boolean esConfiguracionAceptacion(Configuracion config) {
        if (aceptaPorEstadoFinal) {
            // Acepta si la entrada se consumió y estamos en estado final
            return config.getEntradaRestante().isEmpty() && 
                   estadosFinales.contains(config.getEstado());
        } else {
            // Acepta si la entrada se consumió y la pila está vacía
            return config.getEntradaRestante().isEmpty() && 
                   config.getPila().isEmpty();
        }
    }
    
    /**
     * Obtiene las transiciones aplicables a una configuración
     */
    private List<Transicion> getTransicionesAplicables(Configuracion config) {
        List<Transicion> aplicables = new ArrayList<>();
        
        if (config.getPila().isEmpty()) {
            return aplicables;  // No hay transiciones si la pila está vacía
        }
        
        char topePila = config.getPila().peek();
        String entrada = config.getEntradaRestante();
        
        for (Transicion t : transiciones) {
            if (!t.getEstadoOrigen().equals(config.getEstado())) {
                continue;
            }
            
            if (t.getTopePila() != topePila) {
                continue;
            }
            
            // Transición epsilon
            if (t.getSimboloEntrada() == null) {
                aplicables.add(t);
            }
            // Transición con símbolo
            else if (!entrada.isEmpty() && t.getSimboloEntrada() == entrada.charAt(0)) {
                aplicables.add(t);
            }
        }
        
        return aplicables;
    }
    
    /**
     * Aplica una transición a una configuración
     */
    private Configuracion aplicarTransicion(Configuracion config, Transicion t) {
        Stack<Character> nuevaPila = new Stack<>();
        nuevaPila.addAll(config.getPila());
        
        // Quitar el tope de la pila
        nuevaPila.pop();
        
        // Agregar el reemplazo (en orden inverso para que quede correcto)
        String reemplazo = t.getReemplazoEnPila();
        for (int i = reemplazo.length() - 1; i >= 0; i--) {
            nuevaPila.push(reemplazo.charAt(i));
        }
        
        // Consumir símbolo de entrada si no es epsilon
        String nuevaEntrada = config.getEntradaRestante();
        if (t.getSimboloEntrada() != null) {
            nuevaEntrada = nuevaEntrada.substring(1);
        }
        
        return new Configuracion(t.getEstadoDestino(), nuevaPila, nuevaEntrada);
    }
    
    // Getters - Implementación de interfaz Automata
    @Override
    public Set<String> getEstados() { return new HashSet<>(estados); }
    
    @Override
    public Set<Character> getAlfabeto() { 
        // Retorna el alfabeto de entrada (el más relevante para la interfaz común)
        return new HashSet<>(alfabetoEntrada); 
    }
    
    @Override
    public String getEstadoInicial() { return estadoInicial; }
    
    @Override
    public Set<String> getEstadosFinales() { return new HashSet<>(estadosFinales); }
    
    @Override
    public void mostrar() {
        System.out.println(this.toString());
    }
    
    // Getters específicos de AP
    public Set<Character> getAlfabetoEntrada() { return new HashSet<>(alfabetoEntrada); }
    public Set<Character> getAlfabetoPila() { return new HashSet<>(alfabetoPila); }
    public List<Transicion> getTransiciones() { return new ArrayList<>(transiciones); }
    public Character getSimboloInicialPila() { return simboloInicialPila; }
    public boolean aceptaPorEstadoFinal() { return aceptaPorEstadoFinal; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Autómata de Pila:\n");
        sb.append("  Estados: ").append(estados).append("\n");
        sb.append("  Alfabeto entrada: ").append(alfabetoEntrada).append("\n");
        sb.append("  Alfabeto pila: ").append(alfabetoPila).append("\n");
        sb.append("  Estado inicial: ").append(estadoInicial).append("\n");
        sb.append("  Símbolo inicial pila: ").append(simboloInicialPila).append("\n");
        sb.append("  Estados finales: ").append(estadosFinales).append("\n");
        sb.append("  Acepta por: ").append(aceptaPorEstadoFinal ? "estado final" : "pila vacía").append("\n");
        sb.append("  Transiciones:\n");
        for (Transicion t : transiciones) {
            sb.append("    ").append(t).append("\n");
        }
        return sb.toString();
    }
}