/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package automatas.utils;

/**
 *
 * @author diego
 */
public class IDGenerator {

    private static int counter = 0;

    public static String next() {
        return "q" + (counter++);
    }
}
