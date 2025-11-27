package automatas.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/automatas/ui/views/main_view.fxml"));
        Scene scene = new Scene(loader.load());
        
        stage.setTitle("Procesador de Autómatas");
        stage.setScene(scene);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch();
    }
}
