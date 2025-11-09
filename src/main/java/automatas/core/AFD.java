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
public class AFD implements Automata {

    private Set<String> estados;
    private Set<Character> alfabeto;
    private Map<String, Map<Character, String>> transiciones;
    private String estadoInicial;
    private Set<String> estadosFinales;
    
    public AFD(Set<String> estados,
               Set<Character> alfabeto,
               Map<String, Map<Character, String>> transiciones,
               String estadoInicial,
               Set<String> estadosFinales) {
        this.estados = estados;
        this.alfabeto = alfabeto;
        this.transiciones = transiciones;
        this.estadoInicial = estadoInicial;
        this.estadosFinales = estadosFinales;
    }

    @Override
    public boolean acepta(String cadena) {
        String estadoActual = estadoInicial;
        for (char simbolo : cadena.toCharArray()) {
            if (!transiciones.containsKey(estadoActual)) return false;
            Map<Character, String> mapa = transiciones.get(estadoActual);
            if (!mapa.containsKey(simbolo)) return false;
            estadoActual = mapa.get(simbolo);
        }
        return estadosFinales.contains(estadoActual);
    }

    @Override
    public Set<String> getEstadosFinales() {
        return estadosFinales;
    }

    @Override
    public Set<String> getEstados() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Set<Character> getAlfabeto() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getEstadoInicial() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
