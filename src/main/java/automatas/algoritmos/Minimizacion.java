package automatas.algoritmos;

import automatas.core.AFD;
import automatas.io.EscritorAutomata;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Minimizacion {
    private AFD afd;
    private List<Set<String>> particiones;
    
    public Minimizacion(AFD afd) {
        this.afd = afd;
        this.particiones = new ArrayList<>();
    }
    
    /**
     * Minimiza el AFD usando el algoritmo de partición por subgrupos
     * @return AFD minimizado equivalente al original
     */
    public AFD minimizar() {
        // Paso 1: Eliminar estados inaccesibles
        AFD afdAccesible = eliminarEstadosInaccesibles();
        
        // Paso 2: Crear partición inicial (finales vs no finales)
        crearParticionInicial(afdAccesible);
        
        // Paso 3: Refinar particiones hasta que no haya cambios
        refinarParticiones(afdAccesible);
        
        // Paso 4: Construir el AFD minimizado
        return construirAFDMinimizado(afdAccesible);
    }
    
    /**
     * Minimiza el AFD y guarda el resultado en un archivo CSV
     * @param rutaSalida Ruta donde guardar el AFD minimizado
     * @return AFD minimizado
     * @throws IOException si hay error al guardar
     */
    public AFD minimizarYGuardar(String rutaSalida) throws IOException {
        AFD minimizado = minimizar();
        EscritorAutomata.guardarAFD(minimizado, rutaSalida);
        return minimizado;
    }
    
    /**
     * Elimina estados que no son accesibles desde el estado inicial
     */
    private AFD eliminarEstadosInaccesibles() {
        Set<String> accesibles = new HashSet<>();
        Queue<String> porProcesar = new LinkedList<>();
        
        // Comenzar desde el estado inicial
        porProcesar.offer(afd.getEstadoInicial());
        accesibles.add(afd.getEstadoInicial());
        
        // BFS para encontrar todos los estados accesibles
        while (!porProcesar.isEmpty()) {
            String estadoActual = porProcesar.poll();
            
            Map<Character, String> transicionesEstado = afd.getTransiciones().get(estadoActual);
            if (transicionesEstado != null) {
                for (String destino : transicionesEstado.values()) {
                    if (!accesibles.contains(destino)) {
                        accesibles.add(destino);
                        porProcesar.offer(destino);
                    }
                }
            }
        }
        
        // Si todos los estados son accesibles, devolver el AFD original
        if (accesibles.size() == afd.getEstados().size()) {
            return afd;
        }
        
        // Construir nuevo AFD solo con estados accesibles
        Map<String, Map<Character, String>> nuevasTransiciones = new HashMap<>();
        for (String estado : accesibles) {
            Map<Character, String> trans = afd.getTransiciones().get(estado);
            if (trans != null) {
                Map<Character, String> transFiltradas = new HashMap<>();
                for (Map.Entry<Character, String> entry : trans.entrySet()) {
                    if (accesibles.contains(entry.getValue())) {
                        transFiltradas.put(entry.getKey(), entry.getValue());
                    }
                }
                nuevasTransiciones.put(estado, transFiltradas);
            }
        }
        
        Set<String> nuevosFinales = afd.getEstadosFinales().stream()
                .filter(accesibles::contains)
                .collect(Collectors.toSet());
        
        return new AFD(accesibles, afd.getAlfabeto(), nuevasTransiciones, 
                      afd.getEstadoInicial(), nuevosFinales);
    }
    
    /**
     * Crea la partición inicial: estados finales y no finales
     */
    private void crearParticionInicial(AFD afdAccesible) {
        particiones.clear();
        
        // Grupo de estados no finales
        Set<String> noFinales = new HashSet<>();
        for (String estado : afdAccesible.getEstados()) {
            if (!afdAccesible.getEstadosFinales().contains(estado)) {
                noFinales.add(estado);
            }
        }
        
        // Agregar grupos no vacíos
        if (!noFinales.isEmpty()) {
            particiones.add(noFinales);
        }
        
        if (!afdAccesible.getEstadosFinales().isEmpty()) {
            particiones.add(new HashSet<>(afdAccesible.getEstadosFinales()));
        }
    }
    
    /**
     * Refina las particiones iterativamente hasta alcanzar punto fijo
     */
    private void refinarParticiones(AFD afdAccesible) {
        boolean cambio = true;
        
        while (cambio) {
            cambio = false;
            List<Set<String>> nuevasParticiones = new ArrayList<>();
            
            // Para cada partición actual
            for (Set<String> particion : particiones) {
                Map<String, Set<String>> subgrupos = dividirParticion(particion, afdAccesible);
                
                // Si la partición se dividió, hubo un cambio
                if (subgrupos.size() > 1) {
                    cambio = true;
                }
                
                nuevasParticiones.addAll(subgrupos.values());
            }
            
            particiones = nuevasParticiones;
        }
    }
    
    /**
     * Divide una partición en subgrupos según el comportamiento de transiciones
     */
    private Map<String, Set<String>> dividirParticion(Set<String> particion, AFD afdAccesible) {
        Map<String, Set<String>> subgrupos = new HashMap<>();
        
        for (String estado : particion) {
            // Crear una "firma" para este estado basada en a qué particiones va
            String firma = calcularFirma(estado, afdAccesible);
            
            // Agrupar estados con la misma firma
            subgrupos.computeIfAbsent(firma, k -> new HashSet<>()).add(estado);
        }
        
        return subgrupos;
    }
    
    /**
     * Calcula una firma única para un estado basada en sus transiciones
     */
    private String calcularFirma(String estado, AFD afdAccesible) {
        StringBuilder firma = new StringBuilder();
        
        // Ordenar el alfabeto para consistencia
        List<Character> alfabetoOrdenado = new ArrayList<>(afdAccesible.getAlfabeto());
        Collections.sort(alfabetoOrdenado);
        
        Map<Character, String> transiciones = afdAccesible.getTransiciones().get(estado);
        
        for (Character simbolo : alfabetoOrdenado) {
            String destino = null;
            if (transiciones != null) {
                destino = transiciones.get(simbolo);
            }
            
            // Encontrar a qué partición pertenece el destino
            int indiceParticion = encontrarParticion(destino);
            firma.append(simbolo).append("->").append(indiceParticion).append(";");
        }
        
        return firma.toString();
    }
    
    /**
     * Encuentra el índice de la partición que contiene un estado
     */
    private int encontrarParticion(String estado) {
        if (estado == null) {
            return -1;
        }
        
        for (int i = 0; i < particiones.size(); i++) {
            if (particiones.get(i).contains(estado)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Construye el AFD minimizado a partir de las particiones finales
     */
    private AFD construirAFDMinimizado(AFD afdAccesible) {
        // Crear nombres para los nuevos estados (representantes de cada partición)
        Map<Set<String>, String> nombreParticion = new HashMap<>();
        Set<String> nuevosEstados = new HashSet<>();
        
        for (int i = 0; i < particiones.size(); i++) {
            String nombreEstado = obtenerRepresentante(particiones.get(i));
            nombreParticion.put(particiones.get(i), nombreEstado);
            nuevosEstados.add(nombreEstado);
        }
        
        // Determinar el nuevo estado inicial
        String nuevoEstadoInicial = null;
        for (Set<String> particion : particiones) {
            if (particion.contains(afdAccesible.getEstadoInicial())) {
                nuevoEstadoInicial = nombreParticion.get(particion);
                break;
            }
        }
        
        // Determinar nuevos estados finales
        Set<String> nuevosFinales = new HashSet<>();
        for (Set<String> particion : particiones) {
            for (String estado : particion) {
                if (afdAccesible.getEstadosFinales().contains(estado)) {
                    nuevosFinales.add(nombreParticion.get(particion));
                    break;
                }
            }
        }
        
        // Construir nuevas transiciones
        Map<String, Map<Character, String>> nuevasTransiciones = new HashMap<>();
        
        for (Set<String> particion : particiones) {
            String nombreOrigen = nombreParticion.get(particion);
            Map<Character, String> transicionesEstado = new HashMap<>();
            
            // Tomar cualquier estado de la partición como representante
            String representante = particion.iterator().next();
            Map<Character, String> transiciones = afdAccesible.getTransiciones().get(representante);
            
            if (transiciones != null) {
                for (Map.Entry<Character, String> entry : transiciones.entrySet()) {
                    Character simbolo = entry.getKey();
                    String destino = entry.getValue();
                    
                    // Encontrar la partición del destino
                    for (Set<String> particionDestino : this.particiones) {
                        if (particionDestino.contains(destino)) {
                            String nombreDestino = nombreParticion.get(particionDestino);
                            transicionesEstado.put(simbolo, nombreDestino);
                            break;
                        }
                    }
                }
            }
            
            nuevasTransiciones.put(nombreOrigen, transicionesEstado);
        }
        
        return new AFD(nuevosEstados, afdAccesible.getAlfabeto(), nuevasTransiciones,
                      nuevoEstadoInicial, nuevosFinales);
    }
    
    /**
     * Obtiene un representante (nombre) para una partición
     * Usa el estado lexicográficamente menor o crea un nombre compuesto
     */
    private String obtenerRepresentante(Set<String> particion) {
        if (particion.size() == 1) {
            return particion.iterator().next();
        }
        
        // Si hay múltiples estados, usar el menor lexicográficamente
        return particion.stream()
                .sorted()
                .findFirst()
                .orElse("q" + particion.hashCode());
    }
    
    /**
     * Muestra el proceso de minimización paso a paso (útil para debug)
     */
    public void mostrarProcesoMinimizacion() {
        System.out.println("=== PROCESO DE MINIMIZACIÓN ===");
        System.out.println("\nAFD Original:");
        System.out.println("  Estados: " + afd.getEstados());
        System.out.println("  Estado inicial: " + afd.getEstadoInicial());
        System.out.println("  Estados finales: " + afd.getEstadosFinales());
        mostrarTransiciones(afd);
        
        // Eliminar inaccesibles
        AFD afdAccesible = eliminarEstadosInaccesibles();
        System.out.println("\nDespués de eliminar estados inaccesibles:");
        System.out.println("  Estados: " + afdAccesible.getEstados());
        
        // Crear partición inicial
        crearParticionInicial(afdAccesible);
        System.out.println("\nPartición inicial:");
        mostrarParticiones();
        
        // Refinar
        int iteracion = 1;
        boolean cambio = true;
        while (cambio) {
            List<Set<String>> particionesAnteriores = new ArrayList<>();
            for (Set<String> p : particiones) {
                particionesAnteriores.add(new HashSet<>(p));
            }
            
            refinarParticiones(afdAccesible);
            
            cambio = !particionesIguales(particionesAnteriores, particiones);
            if (cambio) {
                System.out.println("\nIteración " + iteracion + ":");
                mostrarParticiones();
                iteracion++;
                
                // Restablecer para siguiente iteración
                particiones = particionesAnteriores;
                refinarParticiones(afdAccesible);
            }
        }
        
        System.out.println("\nParticiones finales:");
        mostrarParticiones();
        
        // Construir minimizado
        AFD minimizado = construirAFDMinimizado(afdAccesible);
        System.out.println("\nAFD Minimizado:");
        System.out.println("  Estados: " + minimizado.getEstados());
        System.out.println("  Estado inicial: " + minimizado.getEstadoInicial());
        System.out.println("  Estados finales: " + minimizado.getEstadosFinales());
        mostrarTransiciones(minimizado);
    }
    
    private void mostrarTransiciones(AFD automata) {
        System.out.println("  Transiciones:");
        for (String estado : automata.getEstados()) {
            Map<Character, String> trans = automata.getTransiciones().get(estado);
            if (trans != null) {
                for (Map.Entry<Character, String> entry : trans.entrySet()) {
                    System.out.println("    " + estado + " --" + entry.getKey() + "--> " + entry.getValue());
                }
            }
        }
    }
    
    private void mostrarParticiones() {
        for (int i = 0; i < particiones.size(); i++) {
            System.out.println("  P" + i + ": " + particiones.get(i));
        }
    }
    
    private boolean particionesIguales(List<Set<String>> p1, List<Set<String>> p2) {
        if (p1.size() != p2.size()) return false;
        
        for (Set<String> grupo1 : p1) {
            boolean encontrado = false;
            for (Set<String> grupo2 : p2) {
                if (grupo1.equals(grupo2)) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) return false;
        }
        return true;
    }
}