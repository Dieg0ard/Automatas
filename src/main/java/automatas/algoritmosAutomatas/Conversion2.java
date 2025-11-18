package automatas.algoritmosAutomatas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import automatas.core.AFD;
import automatas.core.AFND;

public class Conversion2 {

    private AFND afnd;

    public Conversion2(AFND afnd) {
        this.afnd = afnd;
    }

    public AFD convertir() {
        //Se crea el objeto del nuevo automata y lo que se necesita
        AFD afd = new AFD(null, this.afnd.getAlfabeto(), null, this.afnd.getEstadoInicial(), null);
        Set<String> estados = new HashSet<String>();
        Map<String, Map<Character, String>> transiciones = new HashMap<String, Map<Character, String>>();
        Set<String> estadosFinales = new HashSet<String>();

        //Variables que van cambiando
        boolean continuar = true;
        String estadoInicial = afnd.getEstadoInicial();
        estados.add(estadoInicial);

        //Si el estado incial es final se agrega
        if (afnd.getEstadosFinales().contains(estadoInicial)) {
            estadosFinales.add(estadoInicial);
        }
        String estadoActual = estadoInicial;
        
        while (continuar) {
            Map<Character, Set<String>> transicionesEstadoActual = afnd.getTransiciones().get(estadoActual);
            Map<Character, String> transicionesFinalesEstadoActual = new HashMap<Character,String>();
            for (Map.Entry<Character, Set<String>> entrada : transicionesEstadoActual.entrySet()) {
                Character simbolo = entrada.getKey();
                Set<String> estadosDestino = entrada.getValue();
                if(estadosDestino.size() == 1){
                    transicionesFinalesEstadoActual.put(simbolo, estadosDestino.iterator().next());
                } else{
                    String nuevoEstadoDestino = "";
                    for(String estado : estadosDestino){
                        nuevoEstadoDestino += estado;
                    }
                    transicionesFinalesEstadoActual.put(simbolo,nuevoEstadoDestino);

                }



                for (String estado : estadosDestino) {
                    
                }
            }

        }

        return afd;
    }

    private Map<Character, String> crearTransicionesEstadoNuevo(String estadoFusionado, Set<String> padres, HashMap<String, Map<Character, String>> transiciones){
            return null;


    }

    public AFND getAfnd() {
        return afnd;
    }

    public void setAfnd(AFND afnd) {
        this.afnd = afnd;
    }
}
