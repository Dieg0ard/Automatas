package automatas.ui.controllers;

import automatas.core.Automata;
import automatas.io.LectorAutomata;
import automatas.visual.AutomataRenderer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;

public class MainController {

    @FXML
    private Label labelArchivo;
    @FXML
    private Button btnConvertirAFD;
    @FXML
    private Button btnConvertirAP;
    @FXML
    private Button btnMinimizar;
    @FXML
    private ImageView imagenAutomata;

    private Automata automataActual;

    // ----------------------------------------------------
    //         MÉTODO: Cargar archivo CSV
    // ----------------------------------------------------
    @FXML
    private void onCargarCSV() {

        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo CSV de Autómata");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        File archivo = fc.showOpenDialog(getStage());

        if (archivo != null) {
            try {
                automataActual = LectorAutomata.leerDesdeCSV(archivo.getAbsolutePath());
                labelArchivo.setText("Archivo cargado: " + archivo.getName());

                mostrarAutomataImagen(archivo);  // si tu generador usa rutas derivadas del CSV

                habilitarOperaciones();
            } catch (Exception e) {
                labelArchivo.setText("Error cargando archivo.");
                e.printStackTrace();
            }
        }
    }

    // ----------------------------------------------------
    //      MÉTODO: Mostrar imagen del autómata
    // ----------------------------------------------------
    /**
     * Muestra la imagen generada del autómata en el ImageView
     */
    void mostrarAutomataImagen(File archivoAutomata) throws IOException {

        AutomataRenderer ar = null;
        LectorAutomata lector = null;
        Automata automata = lector.leerDesdeCSV(archivoAutomata.getPath());
        ar.renderAutomata(automata);
        String userHome = System.getProperty("user.home");
        String rutaImagen = userHome + "/.automatas/img/automata.png";

        File imgFile = new File(rutaImagen);

        if (imgFile.exists()) {
            imagenAutomata.setImage(new Image("file:" + rutaImagen));
        } else {
            System.out.println("No se encontró la imagen del autómata: " + rutaImagen);
        }
    }

    // ----------------------------------------------------
    //      MÉTODO: Crear autómata manualmente
    // ----------------------------------------------------
    @FXML
    private void onCrearAutomata() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/automatas/ui/views/automata_manual.fxml")
            );

            Parent root = loader.load();

            // Obtener el controller de la ventana cargada
            AutomataManualController ctrl = loader.getController();

            // Pasar la referencia del MainController (this)
            ctrl.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Crear Autómata Manualmente");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Opcional: ventana modal
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la ventana de creación manual.");
            alert.showAndWait();
        }
    }

    // ----------------------------------------------------
    //                Habilitar botones
    // ----------------------------------------------------
    private void habilitarOperaciones() {
        btnConvertirAFD.setDisable(false);
        btnConvertirAP.setDisable(false);
        btnMinimizar.setDisable(false);
    }

    // ----------------------------------------------------
    //              Obtener la ventana actual
    // ----------------------------------------------------
    private Stage getStage() {
        return (Stage) labelArchivo.getScene().getWindow();
    }
}
