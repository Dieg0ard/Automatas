package automatas.ui.controllers;

import automatas.ui.models.TransicionRow;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AutomataManualController {

    @FXML private TextField txtAlfabeto;
    @FXML private TextField txtNuevoEstado;
    @FXML private ListView<String> listaEstados;
    @FXML private ComboBox<String> comboInicial;
    @FXML private ListView<String> listaFinales;   // seguirá mostrando nombres, pero con checkboxes

    @FXML private TableView<TransicionRow> tablaTransiciones;
    @FXML private TableColumn<TransicionRow, String> colOrigen;
    @FXML private TableColumn<TransicionRow, String> colSimbolo;
    @FXML private TableColumn<TransicionRow, String> colDestino;

    private final ObservableList<String> estados = FXCollections.observableArrayList();
    private final ObservableList<String> posiblesFinales = FXCollections.observableArrayList();
    private final ObservableList<TransicionRow> transiciones = FXCollections.observableArrayList();

    // mapa estado -> propiedad booleana que indica si es final (para las casillas)
    private final Map<String, BooleanProperty> finalesMap = new HashMap<>();

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

        if (estado.isEmpty()) return;
        if (estados.contains(estado)) {
            mostrarError("El estado ya existe.");
            return;
        }

        estados.add(estado);
        posiblesFinales.add(estado);

        // inicialmente no es final
        finalesMap.put(estado, new SimpleBooleanProperty(false));

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
        if (sel != null) transiciones.remove(sel);
    }

    // =============================================
    // CREAR AUTOMATA
    // =============================================
    @FXML
    private void crearAutomata() {

        System.out.println("=== Datos del Autómata ===");
        System.out.println("Alfabeto: " + txtAlfabeto.getText());
        System.out.println("Estados: " + estados);
        System.out.println("Inicial: " + comboInicial.getValue());

        // recoger finales consultando el mapa
        List<String> finales = posiblesFinales.stream()
                .filter(s -> {
                    BooleanProperty p = finalesMap.get(s);
                    return p != null && p.get();
                })
                .collect(Collectors.toList());

        System.out.println("Finales: " + finales);

        System.out.println("Transiciones: ");
        for (TransicionRow tr : transiciones) {
            System.out.println("  " + tr.getOrigen() + " --" + tr.getSimbolo() + "--> " + tr.getDestino());
        }

        // Aquí construirías tu objeto Automata con (estados, alfabeto, inicial, finales, transiciones)
        // Ejemplo: Automata a = AutomataFactory.fromManual(...)

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
}
