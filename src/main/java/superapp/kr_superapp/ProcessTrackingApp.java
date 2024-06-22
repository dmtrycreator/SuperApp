package superapp.kr_superapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProcessTrackingApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProcessTracking.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Process Tracking");
        primaryStage.setScene(scene);

        // Чтение данных из общей памяти
        FileMappingHandler fileMappingHandler = new FileMappingHandler();
        String sharedData = new String(fileMappingHandler.readData()).trim();

        // Использование прочитанных данных
        // Например, передача данных в контроллер
        ProcessTracking controller = loader.getController();
        controller.initializeWithData(sharedData);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
