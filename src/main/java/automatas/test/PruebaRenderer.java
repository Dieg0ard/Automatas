/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package automatas.test;

import automatas.core.Automata;
import automatas.io.LectorAutomata;
import automatas.visual.AutomataRenderer;
import java.io.File;

/**
 *
 * @author diego
 */
public class PruebaRenderer {
    
    public static void main(String[] args) {
        try {
            Automata automata = LectorAutomata.leerDesdeCSV("automata_ejemplo.csv");
            AutomataRenderer ar = null;
            
            ar.renderAutomata(automata);
            

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
