package automatas.test;

import automatas.core.Automata;
import automatas.io.LectorAutomata;

public class PruebaLector {
    public static void main(String[] args) {
        try {
            Automata automata = LectorAutomata.leerDesdeCSV("automata_ejemplo.csv");

            System.out.println("Tipo detectado: " + automata.getClass().getSimpleName());
            System.out.println("Estados: " + automata.getEstados());
            System.out.println("Alfabeto: " + automata.getAlfabeto());
            System.out.println("Estado inicial: " + automata.getEstadoInicial());
            System.out.println("Estados finales: " + automata.getEstadosFinales());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
