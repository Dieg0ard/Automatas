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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
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
    private ImageView imgAutomata;
    @FXML
    private WebView imagenAutomata;

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

                mostrarAutomataImagen(archivo); 

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

    LectorAutomata lector = new LectorAutomata();
    AutomataRenderer renderer = new AutomataRenderer();

    Automata automata = lector.leerDesdeCSV(archivoAutomata.getPath());
    renderer.renderAutomata(automata);

    String userHome = System.getProperty("user.home");
    String rutaImagen = userHome + "/.automatas/img/automata.svg";
    File svgFile = new File(rutaImagen);

    if (!svgFile.exists()) {
        System.err.println("No se encontró: " + rutaImagen);
        return;
    }

    // URI + anti-cache
    String fileUri = svgFile.toURI().toString() + "?t=" + System.currentTimeMillis();
    System.out.println("Cargando SVG desde: " + fileUri);

    WebEngine engine = imagenAutomata.getEngine();

    // limpiar antes
    engine.load("about:blank");

    String html = """
        <html>
            <body style="margin:0; padding:0; background:white;">
                <img src="%s" style="width:100%%; height:auto; display:block;" />
            </body>
        </html>
        """.formatted(fileUri);

    engine.loadContent(html);
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
            stage.initModality(Modality.APPLICATION_MODAL); 
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
