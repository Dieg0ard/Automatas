/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package automatas.core;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author diego
 */
public class AFND implements Automata {

    private final Set<String> estados;
    private final Set<Character> alfabeto;
    private final Map<String, Map<Character, Set<String>>> transiciones;
    private final String estadoInicial;
    private final Set<String> estadosFinales;

    public AFND(Set<String> estados,
            Set<Character> alfabeto,
            Map<String, Map<Character, Set<String>>> transiciones,
            String estadoInicial,
            Set<String> estadosFinales) {
        this.estados = estados;
        this.alfabeto = alfabeto;
        this.transiciones = transiciones;
        this.estadoInicial = estadoInicial;
        this.estadosFinales = estadosFinales;
    }

    @Override
    public Set<String> getEstados() {
        return estados;
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
    public Set<Character> getAlfabeto() {
        return alfabeto;
    }

    @Override
    public boolean acepta(String cadena) {
        // Conjunto de estados actuales comenzando desde el estado inicial
        Set<String> actuales = epsilonCierre(Set.of(estadoInicial));

        // Procesar cada símbolo de la cadena
        for (char c : cadena.toCharArray()) {
            Set<String> siguientes = new java.util.HashSet<>();

            for (String estado : actuales) {
                Map<Character, Set<String>> mapaSimbolos = transiciones.get(estado);

                if (mapaSimbolos == null) {
                    continue;
                }

                Set<String> destinos = mapaSimbolos.get(c);
                if (destinos != null) {
                    // Agrega todos los destinos posibles
                    siguientes.addAll(destinos);
                }
            }

            // Si no hay transiciones válidas → rechazo inmediato
            if (siguientes.isEmpty()) {
                return false;
            }

            // Aplicar epsilon-cierre después de cada movimiento
            actuales = epsilonCierre(siguientes);
        }

        // Acepta si alguno de los estados actuales es final
        for (String estado : actuales) {
            if (estadosFinales.contains(estado)) {
                return true;
            }
        }

        return false;
    }

    private Set<String> epsilonCierre(Set<String> estadosIniciales) {
        Set<String> cierre = new java.util.HashSet<>(estadosIniciales);
        java.util.Stack<String> pila = new java.util.Stack<>();

        // Inicializar pila
        for (String e : estadosIniciales) {
            pila.push(e);
        }

        // Mientras haya estados por procesar
        while (!pila.isEmpty()) {
            String estado = pila.pop();

            Map<Character, Set<String>> mapa = transiciones.get(estado);
            if (mapa == null) {
                continue;
            }

            // Transiciones vacías (~) almacenadas como 'null' en el map
            Set<String> epsilonDestinos = mapa.get(null);

            if (epsilonDestinos != null) {
                for (String destino : epsilonDestinos) {
                    if (!cierre.contains(destino)) {
                        cierre.add(destino);
                        pila.push(destino);
                    }
                }
            }
        }

        return cierre;
    }

}
