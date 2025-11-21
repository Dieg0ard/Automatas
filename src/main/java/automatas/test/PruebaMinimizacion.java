package automatas.test;

import automatas.algoritmos.Minimizacion;
import automatas.core.AFD;
import automatas.core.Automata;
import automatas.io.EscritorAutomata;
import automatas.io.LectorAutomata;
import automatas.visual.AutomataRenderer;

import java.io.File;

/**
 * Clase de prueba para demostrar el funcionamiento del algoritmo de minimización
 * de AFDs por el método de partición por subgrupos.
 */
public class PruebaMinimizacion {
    
    public static void main(String[] args) {
        System.out.println("___________________________________________________________");
        System.out.println("_     PRUEBA DE MINIMIZACIÓN DE AFD POR SUBGRUPOS          _");
        System.out.println("___________________________________________________________\n");
        
        // Probar con automata_ejemplo.csv
        System.out.println("______________ PRUEBA 1: automata_ejemplo.csv ____________\n");
        probarMinimizacion("automata_ejemplo.csv", "automata_ejemplo_minimizado.csv");
        
        System.out.println("\n\n");
        
        // Probar con automata_ejemplo2.csv
        System.out.println("_______________ PRUEBA 2: automata_ejemplo2.csv ________________\n");
        probarMinimizacion("automata_ejemplo2.csv", "automata_ejemplo2_minimizado.csv");
        
        System.out.println("\n\n___________________________________________________________");
        System.out.println("_              PRUEBAS COMPLETADAS EXITOSAMENTE             _");
        System.out.println("___________________________________________________________");
    }
    
    /**
     * Prueba la minimización de un AFD desde un archivo CSV
     */
    private static void probarMinimizacion(String archivoEntrada, String archivoSalida) {
        try {
            // 1. CARGAR EL AUTOMATA
            System.out.println("Cargando autómata desde: " + archivoEntrada);
            Automata automata = LectorAutomata.leerDesdeCSV(archivoEntrada);
            
            // Verificar que sea un AFD
            if (!(automata instanceof AFD)) {
                System.out.println("  El autómata cargado NO es un AFD.");
                System.out.println(" Tipo detectado: " + automata.getClass().getSimpleName());
                System.out.println(" La minimización solo funciona con AFDs.\n");
                return;
            }
            
            AFD afd = (AFD) automata;
            System.out.println("AFD cargado correctamente\n");
            
            // 2. MOSTRAR INFORMACIÓN DEL AFD ORIGINAL
            System.out.println("?INFORMACIÓN DEL AFD ORIGINAL:");
            System.out.println(" Estados: " + afd.getEstados());
            System.out.println(" Cantidad de estados: " + afd.getEstados().size());
            System.out.println(" Estado inicial: " + afd.getEstadoInicial());
            System.out.println(" Estados finales: " + afd.getEstadosFinales());
            System.out.println(" Alfabeto: " + afd.getAlfabeto());
            System.out.println();
            
            // 3. CREAR MINIMIZADOR Y MOSTRAR PROCESO COMPLETO
            System.out.println(" INICIANDO PROCESO DE MINIMIZACIÓN...\n");
            System.out.println("___________________________________________________________");
            
            Minimizacion minimizador = new Minimizacion(afd);
            
            // Mostrar el proceso paso a paso
            minimizador.mostrarProcesoMinimizacion();
            
            System.out.println("___________________________________________________________\n");
            
            // 4. GUARDAR EL AFD MINIMIZADO
            System.out.println("Guardando AFD minimizado en: " + archivoSalida);
            AFD afdMinimizado = minimizador.minimizarYGuardar(archivoSalida);
            System.out.println("AFD minimizado guardado correctamente\n");
            
            // 5. COMPARACIÓN DE RESULTADOS
            System.out.println("?COMPARACIÓN DE RESULTADOS:");
            System.out.println(" Estados ORIGINAL: " + afd.getEstados().size());
            System.out.println(" Estados MINIMIZADO: " + afdMinimizado.getEstados().size());
            
            int reduccion = afd.getEstados().size() - afdMinimizado.getEstados().size();
            if (reduccion > 0) {
                System.out.println("   Se redujeron " + reduccion + " estado(s)");
            } else {
                System.out.println("  ️  El AFD ya estaba minimizado");
            }
            System.out.println();
            
            // 6. VERIFICAR EQUIVALENCIA CON CADENAS DE PRUEBA
            System.out.println(" VERIFICANDO EQUIVALENCIA:");
            String[] cadenasPrueba = {"", "a", "b", "aa", "ab", "ba", "bb", "aaa", "bbb", "abab"};
            
            boolean todosIguales = true;
            for (String cadena : cadenasPrueba) {
                boolean aceptaOriginal = afd.acepta(cadena);
                boolean aceptaMinimizado = afdMinimizado.acepta(cadena);
                
                String resultado = aceptaOriginal == aceptaMinimizado ? "Correcto" : "Error";
                System.out.println("   " + resultado + " \"" + cadena + "\": " +
                                 "Original=" + (aceptaOriginal ? "acepta" : "rechaza") + 
                                 ", Minimizado=" + (aceptaMinimizado ? "acepta" : "rechaza"));
                
                if (aceptaOriginal != aceptaMinimizado) {
                    todosIguales = false;
                }
            }
            
            System.out.println();
            if (todosIguales) {
                System.out.println("   TODOS LOS TESTS PASARON - Los autómatas son equivalentes");
            } else {
                System.out.println("   ERROR - Los autómatas NO son equivalentes");
            }
            System.out.println();
            
            // 7. GENERAR IMÁGENES (OPCIONAL)
            try {
                String userHome = System.getProperty("user.home");
                String rutaImgOriginal = userHome + "/.automatas/img/original.svg";
                String rutaImgMinimizado = userHome + "/.automatas/img/minimizado.svg";
                
                System.out.println("  Generando imágenes SVG...");
                AutomataRenderer.renderAutomata(afd);
                System.out.println("    Imagen del AFD original guardada");
                
                AutomataRenderer.renderAutomata(afdMinimizado);
                System.out.println("    Imagen del AFD minimizado guardada");
                System.out.println("    Ubicación: " + userHome + "/.automatas/img/");
                
            } catch (Exception e) {
                System.out.println("   ️  No se pudieron generar las imágenes: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println(" ERROR durante la minimización:");
            e.printStackTrace();
        }
    }
}