package automatas.io;

import automatas.core.AFD;
import automatas.core.AFND;
import automatas.core.AP;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EscritorAutomata {
    
public static void guardarAP(AP ap, String rutaArchivo) throws IOException {

    File archivo = new File(rutaArchivo);

    if (archivo.getParentFile() != null) {
        archivo.getParentFile().mkdirs();
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {

        writer.write("#INICIAL," + ap.getEstadoInicial());
        writer.newLine();

        writer.write("#FINALES," + String.join(",", ap.getEstadosFinales()));
        writer.newLine();
        writer.newLine();

        // Obtener transiciones
        Map<?, ?> transiciones = ap.getTransiciones();

        for (var entry : transiciones.entrySet()) {

            Object keyObj = entry.getKey();
            Object valoresObj = entry.getValue();

            // ==== ACCEDER A CAMPOS PRIVADOS ====
            var keyClass = keyObj.getClass();

            var fEstado = keyClass.getDeclaredField("estado");
            var fEntrada = keyClass.getDeclaredField("simboloEntrada");
            var fPila = keyClass.getDeclaredField("simboloPila");

            fEstado.setAccessible(true);
            fEntrada.setAccessible(true);
            fPila.setAccessible(true);

            String estado = (String) fEstado.get(keyObj);
            Character simboloEntrada = (Character) fEntrada.get(keyObj);
            char simboloPila = (char) fPila.get(keyObj);

            @SuppressWarnings("unchecked")
            List<Object> listaValores = (List<Object>) valoresObj;

            for (Object valObj : listaValores) {
                var valClass = valObj.getClass();

                var fSig = valClass.getDeclaredField("estadoSiguiente");
                var fRep = valClass.getDeclaredField("cadenaReemplazo");

                fSig.setAccessible(true);
                fRep.setAccessible(true);

                String estadoSig = (String) fSig.get(valObj);
                String reemplazo = (String) fRep.get(valObj);

                writer.write(
                    estado + "," +
                    (simboloEntrada == null ? "ε" : simboloEntrada) + "," +
                    simboloPila + "," +
                    estadoSig + "," +
                    (reemplazo.isEmpty() ? "ε" : reemplazo)
                );

                writer.newLine();
            }
        }

    } catch (ReflectiveOperationException e) {
        throw new IOException("Error al extraer datos internos del AP", e);
    }
}





    public static void guardarAFD(AFD afd, String rutaArchivo) throws IOException {
        afd.mostrar();
        File archivo = new File(rutaArchivo);

        // Crear directorios padre si no existen
        if (archivo.getParentFile() != null) {
            archivo.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            // Estado inicial
            writer.write("#INICIAL," + afd.getEstadoInicial());
            writer.newLine();

            // Estados finales
            writer.write("#FINALES," + String.join(",", afd.getEstadosFinales()));
            writer.newLine();
            writer.newLine();

            // Transiciones
            Map<String, Map<Character, String>> transiciones = afd.getTransiciones();
            for (String origen : transiciones.keySet()) {
                for (Character simbolo : transiciones.get(origen).keySet()) {
                    String destino = transiciones.get(origen).get(simbolo);
                    writer.write(origen + "," + simbolo + "," + destino);
                    writer.newLine();
                }
            }
        }
    }
    


    public static void guardarAFND(AFND afnd, String rutaArchivo) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            // Estado inicial
            writer.write("#INICIAL," + afnd.getEstadoInicial());
            writer.newLine();

            // Estados finales
            writer.write("#FINALES," + String.join(";", afnd.getEstadosFinales()));
            writer.newLine();
            writer.newLine();

            // Transiciones
            Map<String, Map<Character, Set<String>>> transiciones = afnd.getTransiciones();
            for (String origen : transiciones.keySet()) {
                for (Character simbolo : transiciones.get(origen).keySet()) {
                    for (String destino : transiciones.get(origen).get(simbolo)) {
                        writer.write(origen + "," + simbolo + "," + destino);
                        writer.newLine();
                    }
                }
            }
        }
    }
}
