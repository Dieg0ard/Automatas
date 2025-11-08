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
    Set<String> getEstados();
    String getEstadoInicial();
    Set<String> getEstadosFinales();
}

