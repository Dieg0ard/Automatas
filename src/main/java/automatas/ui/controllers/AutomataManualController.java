package automatas.ui.controllers;

import automatas.core.AFD;
import automatas.core.AFND;
import automatas.io.EscritorAutomata;
import automatas.ui.models.TransicionRow;
import java.io.File;
import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AutomataManualController {

    @FXML
    private TextField txtAlfabeto;
    @FXML
    private TextField txtNuevoEstado;
    @FXML
    private ListView<String> listaEstados;
    @FXML
    private ComboBox<String> comboInicial;
    @FXML
    private ListView<String> listaFinales;   // seguirá mostrando nombres, pero con checkboxes

    @FXML
    private TableView<TransicionRow> tablaTransiciones;
    @FXML
    private TableColumn<TransicionRow, String> colOrigen;
    @FXML
    private TableColumn<TransicionRow, String> colSimbolo;
    @FXML
    private TableColumn<TransicionRow, String> colDestino;

    private final ObservableList<String> estados = FXCollections.observableArrayList();
    private final ObservableList<String> posiblesFinales = FXCollections.observableArrayList();
    private final ObservableList<TransicionRow> transiciones = FXCollections.observableArrayList();

    // mapa estado -> propiedad booleana que indica si es final (para las casillas)
    private final Map<String, BooleanProperty> finalesMap = new HashMap<>();

    private MainController mainController;

    @FXML
    public void initialize() {

        // =============================================
        // ESTADOS
        // =============================================
        listaEstados.setItems(estados);
        comboInicial.setItems(estados);

        // listaFinales muestra los nombres de posiblesFinales, pero cada celda será un CheckBox
        listaFinales.setItems(posiblesFinales);

        // Cell factory que crea CheckBoxListCell y lo enlaza con la propiedad del mapa
        listaFinales.setCellFactory(CheckBoxListCell.forListView(item -> {
            // devuelve la BooleanProperty asociada al estado (la crea si no existe)
            return finalesMap.computeIfAbsent(item, k -> new SimpleBooleanProperty(false));
        }));

        // =============================================
        // TABLA DE TRANSICIONES (editable)
        // =============================================
        colOrigen.setCellValueFactory(c -> c.getValue().origenProperty());
        colSimbolo.setCellValueFactory(c -> c.getValue().simboloProperty());
        colDestino.setCellValueFactory(c -> c.getValue().destinoProperty());

        tablaTransiciones.setItems(transiciones);
        tablaTransiciones.setEditable(true);

        colOrigen.setCellFactory(TextFieldTableCell.forTableColumn());
        colSimbolo.setCellFactory(TextFieldTableCell.forTableColumn());
        colDestino.setCellFactory(TextFieldTableCell.forTableColumn());

        colOrigen.setOnEditCommit(e -> e.getRowValue().setOrigen(e.getNewValue()));
        colSimbolo.setOnEditCommit(e -> e.getRowValue().setSimbolo(e.getNewValue()));
        colDestino.setOnEditCommit(e -> e.getRowValue().setDestino(e.getNewValue()));
    }

    // =============================================
    // AGREGAR / ELIMINAR ESTADOS
    // =============================================
    @FXML
    private void agregarEstado() {
        String estado = txtNuevoEstado.getText().trim();

        if (estado.isEmpty()) {
            return;
        }
        if (estados.contains(estado)) {
            mostrarError("El estado ya existe.");
            return;
        }

        // Crear la propiedad booleana para el estado final antes de agregarlo
        BooleanProperty esFinal = new SimpleBooleanProperty(false);
        finalesMap.put(estado, esFinal);

        // Agregar a la lista de estados y a la lista de posibles finales
        estados.add(estado);
        posiblesFinales.add(estado); // listaFinales usará finalesMap para los checkboxes

        // Limpiar el campo de texto
        txtNuevoEstado.clear();
    }

    @FXML
    private void eliminarEstado() {
        String sel = listaEstados.getSelectionModel().getSelectedItem();
        if (sel != null) {
            estados.remove(sel);
            posiblesFinales.remove(sel);
            finalesMap.remove(sel);

            // también eliminar transiciones que involucren ese estado
            transiciones.removeIf(t -> sel.equals(t.getOrigen()) || sel.equals(t.getDestino()));

            // si el estado era el seleccionado como inicial, limpiarlo
            if (sel.equals(comboInicial.getValue())) {
                comboInicial.setValue(null);
            }
        }
    }

    // =============================================
    // AGREGAR / ELIMINAR TRANSICIONES
    // =============================================
    @FXML
    private void agregarTransicion() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Agregar transición (formato: origen,símbolo,destino)");
        dialog.setContentText("Ejemplo: q0,a,q1");

        dialog.showAndWait().ifPresent(input -> {
            String[] partes = input.split(",");

            if (partes.length != 3) {
                mostrarError("Debes ingresar: origen,símbolo,destino");
                return;
            }

            String origen = partes[0].trim();
            String simbolo = partes[1].trim();
            String destino = partes[2].trim();

            if (!estados.contains(origen)) {
                mostrarError("El estado origen no existe: " + origen);
                return;
            }
            if (!estados.contains(destino)) {
                mostrarError("El estado destino no existe: " + destino);
                return;
            }

            transiciones.add(new TransicionRow(origen, simbolo, destino));
        });
    }

    @FXML
    private void eliminarTransicion() {
        TransicionRow sel = tablaTransiciones.getSelectionModel().getSelectedItem();
        if (sel != null) {
            transiciones.remove(sel);
        }
    }

    // =============================================
    // CREAR AUTOMATA
    // =============================================
    @FXML
    private void crearAutomata() throws IOException {

        // ==============================
        // Recolectar datos base
        // ==============================
        Set<String> Q = new HashSet<>(estados);
        Set<String> F = finalesMap.entrySet().stream()
                .filter(entry -> entry.getValue().get()) // solo los que están marcados
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        String q0 = comboInicial.getValue();
        if (q0 == null) {
            mostrarError("Debes seleccionar un estado inicial.");
            return;
        }

        // Alfabeto como Set<Character>
        Set<Character> Sigma = txtAlfabeto.getText().chars()
                .mapToObj(c -> (char) c)
                .filter(c -> c != ',' && !Character.isWhitespace(c))
                .collect(Collectors.toSet());

        // ==============================
        // Construir transiciones
        // ==============================
        Map<String, Map<Character, String>> deltaAFD = new HashMap<>();
        Map<String, Map<Character, Set<String>>> deltaAFND = new HashMap<>();
        boolean esAFND = false;

        for (String estado : Q) {
            deltaAFD.put(estado, new HashMap<>());
            deltaAFND.put(estado, new HashMap<>());
        }

        for (TransicionRow tr : transiciones) {

            String origen = tr.getOrigen();
            String simboloStr = tr.getSimbolo();
            String destino = tr.getDestino();

            if (simboloStr.isEmpty() || simboloStr.equals("ε") || simboloStr.equals("lambda")) {
                esAFND = true;
            }

            if (simboloStr.length() != 1) {
                mostrarError("Los símbolos deben ser caracteres individuales: " + simboloStr);
                return;
            }
            Character simbolo = simboloStr.charAt(0);

            // Construcción AFND
            deltaAFND.get(origen).putIfAbsent(simbolo, new HashSet<>());
            deltaAFND.get(origen).get(simbolo).add(destino);

            // Construcción AFD
            Map<Character, String> mapaAFD = deltaAFD.get(origen);
            if (mapaAFD.containsKey(simbolo)) {
                esAFND = true;
            } else {
                mapaAFD.put(simbolo, destino);
            }
        }

        // ==============================
        // Crear el autómata correcto
        // ==============================
        if (!esAFND) {
            AFD afd = new AFD(Q, Sigma, deltaAFD, q0, F);
            System.out.println("SE CREÓ UN AFD");
            //System.out.println(afd);
            EscritorAutomata escritor = null;
            String userHome = System.getProperty("user.home");
            String rutaCSV = userHome + "/.automatas/csv/afd.csv";
            escritor.guardarAFD(afd, rutaCSV);
            mainController.mostrarAutomataImagen(new File(rutaCSV));

            cerrar();
            return;
        }

        AFND afnd = new AFND(Q, Sigma, deltaAFND, q0, F);
        System.out.println("SE CREÓ UN AFND");
        //System.out.println(afnd);
        EscritorAutomata escritor = null;
        String userHome = System.getProperty("user.home");
        String rutaCSV = userHome + "/.automatas/csv/afd.csv";
        escritor.guardarAFND(afnd, rutaCSV);
        mainController.mostrarAutomataImagen(new File(rutaCSV));
        cerrar();
    }

    // =============================================
    // CERRAR VENTANA
    // =============================================
    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtAlfabeto.getScene().getWindow();
        stage.close();
    }

    // =============================================
    // UTILIDAD PARA ERRORES
    // =============================================
    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}
