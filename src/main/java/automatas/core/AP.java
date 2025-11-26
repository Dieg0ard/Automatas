package automatas.core;

import java.util.*;

/**
 * Autómata de Pila (Pushdown Automaton)
 * Representa un autómata de pila para reconocer lenguajes libres de contexto
 */
public class AP implements Automata {

    private Set<String> estados;
    private Set<Character> alfabetoEntrada;
    private Set<Character> alfabetoPila;
    private String estadoInicial;
    private Set<String> estadosFinales;
    private char simboloInicialPila;
    private Map<TransicionKey, List<TransicionValor>> transiciones;

    public AP() {
        this.estados = new HashSet<>();
        this.alfabetoEntrada = new HashSet<>();
        this.alfabetoPila = new HashSet<>();
        this.estadosFinales = new HashSet<>();
        this.transiciones = new HashMap<>();
        this.simboloInicialPila = 'Z';
    }

    // Getters y Setters
    public void addEstado(String estado) {
        estados.add(estado);
    }

    public void addSimboloEntrada(char simbolo) {
        alfabetoEntrada.add(simbolo);
    }

    public void addSimboloPila(char simbolo) {
        alfabetoPila.add(simbolo);
    }

    public void setEstadoInicial(String estado) {
        this.estadoInicial = estado;
        addEstado(estado);
    }

    public void addEstadoFinal(String estado) {
        estadosFinales.add(estado);
        addEstado(estado);
    }

    public void setSimboloInicialPila(char simbolo) {
        this.simboloInicialPila = simbolo;
        addSimboloPila(simbolo);
    }
    
    public Map<TransicionKey, List<TransicionValor>> getTransiciones() {
    return transiciones;
}


    /**
     * Añade una transición al autómata
     */
    public void addTransicion(String estadoActual, Character simboloEntrada,
                              char simboloPila, String estadoSiguiente,
                              String cadenaReemplazo) {
        TransicionKey key = new TransicionKey(estadoActual, simboloEntrada, simboloPila);
        TransicionValor valor = new TransicionValor(estadoSiguiente, cadenaReemplazo);

        transiciones.computeIfAbsent(key, k -> new ArrayList<>()).add(valor);
    }

    /**
     * Verifica si una cadena es aceptada por el autómata
     */
    @Override
    public boolean acepta(String cadena) {
        Stack<Character> pila = new Stack<>();
        pila.push(simboloInicialPila);

        return aceptaRecursivo(estadoInicial, cadena, 0, pila);
    }

    private boolean aceptaRecursivo(String estado, String cadena, int pos, Stack<Character> pila) {
        // Caso base: cadena procesada completamente
        if (pos == cadena.length()) {
            // Aceptación por estado final
            if (estadosFinales.contains(estado)) {
                return true;
            }

            if (!pila.isEmpty()) {
                char topePila = pila.peek();
                TransicionKey keyEpsilon = new TransicionKey(estado, null, topePila);
                List<TransicionValor> transEpsilon = transiciones.get(keyEpsilon);

                if (transEpsilon != null) {
                    for (TransicionValor trans : transEpsilon) {
                        Stack<Character> nuevaPila = (Stack<Character>) pila.clone();
                        nuevaPila.pop();

                        for (int i = trans.cadenaReemplazo.length() - 1; i >= 0; i--) {
                            nuevaPila.push(trans.cadenaReemplazo.charAt(i));
                        }

                        if (aceptaRecursivo(trans.estadoSiguiente, cadena, pos, nuevaPila)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        if (pila.isEmpty()) {
            return false;
        }

        char simboloActual = cadena.charAt(pos);
        char topePila = pila.peek();

        // Intentar transiciones consumiendo entrada
        TransicionKey key = new TransicionKey(estado, simboloActual, topePila);
        List<TransicionValor> trans = transiciones.get(key);

        if (trans != null) {
            for (TransicionValor t : trans) {
                Stack<Character> nuevaPila = (Stack<Character>) pila.clone();
                nuevaPila.pop();

                for (int i = t.cadenaReemplazo.length() - 1; i >= 0; i--) {
                    nuevaPila.push(t.cadenaReemplazo.charAt(i));
                }

                if (aceptaRecursivo(t.estadoSiguiente, cadena, pos + 1, nuevaPila)) {
                    return true;
                }
            }
        }

        // Intentar transiciones epsilon
        TransicionKey keyEpsilon = new TransicionKey(estado, null, topePila);
        List<TransicionValor> transEpsilon = transiciones.get(keyEpsilon);

        if (transEpsilon != null) {
            for (TransicionValor t : transEpsilon) {
                Stack<Character> nuevaPila = (Stack<Character>) pila.clone();
                nuevaPila.pop();

                for (int i = t.cadenaReemplazo.length() - 1; i >= 0; i--) {
                    nuevaPila.push(t.cadenaReemplazo.charAt(i));
                }

                if (aceptaRecursivo(t.estadoSiguiente, cadena, pos, nuevaPila)) {
                    return true;
                }
            }
        }

        return false;
    }

    // === Métodos requeridos por la interfaz Automata === //

    @Override
    public Set<String> getEstados() {
        return estados;
    }

    @Override
    public Set<Character> getAlfabeto() {
        return alfabetoEntrada; // El alfabeto del AP es el de entrada
    }

    @Override
    public String getEstadoInicial() {
        return estadoInicial;
    }

    @Override
    public Set<String> getEstadosFinales() {
        return estadosFinales;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Autómata de Pila:\n");
        sb.append("Estados: ").append(estados).append("\n");
        sb.append("Estado inicial: ").append(estadoInicial).append("\n");
        sb.append("Estados finales: ").append(estadosFinales).append("\n");
        sb.append("Alfabeto entrada: ").append(alfabetoEntrada).append("\n");
        sb.append("Alfabeto pila: ").append(alfabetoPila).append("\n");
        sb.append("Símbolo inicial pila: ").append(simboloInicialPila).append("\n");
        sb.append("Transiciones:\n");

        for (Map.Entry<TransicionKey, List<TransicionValor>> entry : transiciones.entrySet()) {
            TransicionKey key = entry.getKey();
            for (TransicionValor valor : entry.getValue()) {
                sb.append("  δ(").append(key.estado).append(", ");
                sb.append(key.simboloEntrada == null ? "ε" : key.simboloEntrada);
                sb.append(", ").append(key.simboloPila).append(") = (");
                sb.append(valor.estadoSiguiente).append(", ");
                sb.append(valor.cadenaReemplazo.isEmpty() ? "ε" : valor.cadenaReemplazo);
                sb.append(")\n");
            }
        }

        return sb.toString();
    }

    // Clases internas para representar transiciones
    private static class TransicionKey {
        String estado;
        Character simboloEntrada; // null representa epsilon
        char simboloPila;

        TransicionKey(String estado, Character simboloEntrada, char simboloPila) {
            this.estado = estado;
            this.simboloEntrada = simboloEntrada;
            this.simboloPila = simboloPila;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransicionKey)) return false;
            TransicionKey that = (TransicionKey) o;
            return simboloPila == that.simboloPila &&
                    Objects.equals(estado, that.estado) &&
                    Objects.equals(simboloEntrada, that.simboloEntrada);
        }

        @Override
        public int hashCode() {
            return Objects.hash(estado, simboloEntrada, simboloPila);
        }
    }

    private static class TransicionValor {
        String estadoSiguiente;
        String cadenaReemplazo;

        TransicionValor(String estadoSiguiente, String cadenaReemplazo) {
            this.estadoSiguiente = estadoSiguiente;
            this.cadenaReemplazo = cadenaReemplazo;
        }
    }
}
