package automatas.test;

import automatas.grammar.GLC;

public class TestGLC {

    public static void main(String[] args) {

        System.out.println("=== Prueba de Gramáticas ===\n");

        // Gramática a^n b^n
        GLC g1 = GLC.Ejemplos.anbn();
        System.out.println("Gramática a^n b^n:");
        System.out.println(g1);

        // Gramática de palíndromos
        GLC g2 = GLC.Ejemplos.palindromos();
        System.out.println("Gramática de palíndromos:");
        System.out.println(g2);

        // Gramática de paréntesis balanceados
        GLC g3 = GLC.Ejemplos.parentesis();
        System.out.println("Gramática de paréntesis balanceados:");
        System.out.println(g3);

        // Gramática de expresiones aritméticas
        GLC g4 = GLC.Ejemplos.expresionesAritmeticas();
        System.out.println("Gramática de expresiones aritméticas:");
        System.out.println(g4);

        System.out.println("=== Fin de pruebas ===");
    }
}
