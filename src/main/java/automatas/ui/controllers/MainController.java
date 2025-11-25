package automatas.ui.controllers;

import automatas.algoritmos.Conversion;
import automatas.algoritmos.GLCtoAP;
import automatas.core.AFD;
import automatas.core.AFND;
import automatas.core.AP;
import automatas.core.Automata;
import automatas.grammar.CFExpressionParser;
import automatas.grammar.GLC;
import automatas.io.LectorAutomata;
import automatas.regex.LanguageParser;
import automatas.regex.RegexAST;
import automatas.regex.RegexParser;
import automatas.regex.ThompsonConstructor;
import automatas.visual.AutomataRenderer;
import java.awt.Insets;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.util.Pair;

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

@FXML
private void onCrearDesdeRegex() {
    // Crear un diálogo personalizado con dos campos
    Dialog<Pair<String, String>> dialog = new Dialog<>();
    dialog.setTitle("Crear desde lenguaje");
    dialog.setHeaderText("Ingrese los valores");

    ButtonType okButtonType = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

    // Campos de texto
    TextField regexField = new TextField();
    regexField.setPromptText("Lenguaje");

    TextField extraField = new TextField();
    extraField.setPromptText("Condiciones");

    // Layout
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Lenguaje:"), 0, 0);
    grid.add(regexField, 1, 0);

    grid.add(new Label("Condiciones:"), 0, 1);
    grid.add(extraField, 1, 1);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == okButtonType) {
            return new Pair<>(regexField.getText(), extraField.getText());
        }
        return null;
    });

    Optional<Pair<String, String>> resultado = dialog.showAndWait();

    resultado.ifPresent(pair -> {
        String expresion = pair.getKey();
        String condicion = pair.getValue(); 

        try {
            
            LanguageParser pl = new LanguageParser(expresion, condicion);
            // Parsear la expresión regular
            RegexParser parser = new RegexParser(pl.parse());
            RegexAST.Node ast = parser.parse();

            // Construir AFND usando Thompson
            ThompsonConstructor thompson = new ThompsonConstructor();
            AFND afnd = thompson.convert(ast);

            // Actualizar visualización
            this.automataActual = afnd;
            String userHome = System.getProperty("user.home");
            String rutaCSV = userHome + "/.automatas/csv/afd.csv";
            mostrarAutomataImagen(new File(rutaCSV));

            btnMinimizar.setDisable(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    });
}


@FXML
private void onConstruirAP() {
    // Crear diálogo para ingresar la expresión
    TextInputDialog dialog = new TextInputDialog("a^n b^n");
    dialog.setTitle("Crear AP desde Expresión");
    dialog.setHeaderText("Ingrese una expresión de lenguaje libre de contexto");
    dialog.setContentText("Expresión:");
   
    
    Optional<String> resultado = dialog.showAndWait();
    
    resultado.ifPresent(expresion -> {
        try {
            // 1. Parsear la expresión a gramática
            GLC gramatica = CFExpressionParser.parse(expresion);
            
            // 2. Convertir gramática a AP
            AP automata = GLCtoAP.convertirGramatica(gramatica);
            
            // 3. Guardar y visualizar
            this.automataActual = automata;
            String userHome = System.getProperty("user.home");
            String rutaCSV = userHome + "/.automatas/csv/afd.csv";
            mostrarAutomataImagen(new File(rutaCSV));
            
            // 5. Mostrar información
            
        } catch (IllegalArgumentException e) {
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    });
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

    @FXML
    private void onMinimizar() {
        if (automataActual == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Advertencia");
            alert.setContentText("Debes cargar un autómata primero.");
            alert.showAndWait();
            return;
        }

        // Verificar que sea un AFD
        if (!(automataActual instanceof automatas.core.AFD)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Advertencia");
            alert.setContentText("Solo se pueden minimizar AFDs. Convierte el AFND a AFD primero.");
            alert.showAndWait();
            return;
        }

        try {
            automatas.core.AFD afd = (automatas.core.AFD) automataActual;
            automatas.algoritmos.Minimizacion min = new automatas.algoritmos.Minimizacion(afd);

            // Mostrar el proceso en consola (opcional)
            min.mostrarProcesoMinimizacion();

            // Guardar el AFD minimizado
            String userHome = System.getProperty("user.home");
            String rutaMinimizado = userHome + "/.automatas/csv/afd_minimizado.csv";

            automatas.core.AFD afdMinimizado = min.minimizarYGuardar(rutaMinimizado);

            // Actualizar la vista
            automataActual = afdMinimizado;
            labelArchivo.setText("AFD Minimizado cargado");
            mostrarAutomataImagen(new File(rutaMinimizado));

            // Mostrar mensaje de éxito
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Minimización exitosa");
            alert.setContentText("El AFD ha sido minimizado correctamente.");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText("No se pudo minimizar el AFD: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void onConvertirAFD() {
        if (automataActual == null) {
            //mostrarError("No hay ningún autómata cargado");
            return;
        }

        if (!(automataActual instanceof AFND)) {
            //mostrarError("El autómata actual no es un AFND");
            return;
        }

        try {
            AFND afnd = (AFND) automataActual;

            // Realizar la conversión
            Conversion conversion = new Conversion(afnd);
            AFD afd = conversion.convertir();

            // Actualizar el autómata actual
            this.automataActual = afd;

            // Visualizar el AFD resultante
            String userHome = System.getProperty("user.home");
            String rutaCSV = userHome + "/.automatas/csv/afd.csv";
            mostrarAutomataImagen(new File(rutaCSV));

            // Actualizar estados de botones
            //  btnConvertirAFD.setDisable(true);  // Ya no es AFND
            btnMinimizar.setDisable(false);    // Ahora se puede minimizar
            btnConvertirAP.setDisable(false);  // Ahora se puede convertir a AP

            //mostrarInfo("Conversión exitosa", "AFND convertido a AFD correctamente");
        } catch (Exception e) {
            //mostrarError("Error al convertir a AFD: " + e.getMessage());
        }
    }


}
