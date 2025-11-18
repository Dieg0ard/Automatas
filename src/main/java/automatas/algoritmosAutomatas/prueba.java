package automatas.algoritmosAutomatas;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import automatas.core.AFD;
import automatas.core.AFND;

public class prueba {
    public static void main(String[] args) {
        // Crear un AFND basado en la tabla proporcionada
        Set<String> estados = Set.of("X", "Y", "Z", "W", "P");
        Set<Character> alfabeto = Set.of('a', 'b');
        String estadoInicial = "X";
        Set<String> estadosFinales = Set.of("W"); // Asumiendo que P es final
        
        // Configurar transiciones según la tabla
        Map<String, Map<Character, Set<String>>> transiciones = new HashMap<>();
        
        // Estado X
        Map<Character, Set<String>> transX = new HashMap<>();
        transX.put('a', Set.of("Y"));
        transX.put('b', Set.of("Z"));
        transiciones.put("X", transX);
        
        // Estado Y
        Map<Character, Set<String>> transY = new HashMap<>();
        transY.put('a', Set.of("W"));
        transY.put('b', Set.of("P"));
        transiciones.put("Y", transY);
        
        // Estado Z
        Map<Character, Set<String>> transZ = new HashMap<>();
        transZ.put('a', Set.of("Y", "P")); // YP según la tabla
        transZ.put('b', Set.of("Y"));
        transiciones.put("Z", transZ);
        
        // Estado W
        Map<Character, Set<String>> transW = new HashMap<>();
        transW.put('a', Set.of("Y"));
        transW.put('b', Set.of("W"));
        transiciones.put("W", transW);
        
        // Estado P
        Map<Character, Set<String>> transP = new HashMap<>();
        transP.put('a', Set.of("W"));
        transP.put('b', Set.of("P"));
        transiciones.put("P", transP);
        
        // Crear AFND
        AFND afnd = new AFND(estados, alfabeto, transiciones, estadoInicial, estadosFinales);
        
        // Convertir a AFD
        Conversion convertidor = new Conversion(afnd);
        AFD afd = convertidor.convertir();
        
        // Mostrar proceso
        convertidor.mostrarProcesoConversion();
        
        // Probar con alguna cadena
        String cadenaPrueba = "aab";
        System.out.println("\nPrueba con cadena: '" + cadenaPrueba + "'");
        System.out.println("AFND acepta: " + afnd.acepta(cadenaPrueba));
        System.out.println("AFD acepta: " + afd.acepta(cadenaPrueba));
    }
    }

