/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package automatas.core;

import java.util.Set;

/**
 *
 * @author Diego
 */
public interface Automata {

    /**
     * Determina si el autómata acepta una cadena de entrada.
     * @param cadena Cadena a evaluar.
     * @return true si la cadena es aceptada; false en caso contrario.
     */
    boolean acepta(String cadena);

    /**
     * Devuelve el conjunto de estados del autómata.
     */
    Set<String> getEstados();

    /**
     * Devuelve el alfabeto del autómata.
     */
    Set<Character> getAlfabeto();

    /**
     * Devuelve el estado inicial del autómata.
     */
    String getEstadoInicial();

    /**
     * Devuelve el conjunto de estados finales del autómata.
     */
    Set<String> getEstadosFinales();

    /**
     * Muestra una representación textual del autómata (por consola o string).
     */
    default void mostrar() {
        System.out.println("Estados: " + getEstados());
        System.out.println("Alfabeto: " + getAlfabeto());
        System.out.println("Estado inicial: " + getEstadoInicial());
        System.out.println("Estados finales: " + getEstadosFinales());
    }
}



