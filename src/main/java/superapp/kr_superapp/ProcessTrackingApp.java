package superapp.kr_superapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ProcessTrackingApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProcessTracking.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Process Tracking");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/superapp/kr_superapp/icons/Process.png")));
        primaryStage.setScene(scene);

        FileMappingHandler fileMappingHandler = new FileMappingHandler();
        String sharedData = new String(fileMappingHandler.readData()).trim();

        ProcessTracking controller = loader.getController();
        controller.initializeWithData(sharedData);
        controller.setDefaultSelection();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
