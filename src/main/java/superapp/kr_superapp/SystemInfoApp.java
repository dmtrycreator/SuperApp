package superapp.kr_superapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SystemInfoApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        String processName = getParameters().getUnnamed().size() > 1 ? getParameters().getUnnamed().get(1) : "System Info Process";

        FXMLLoader loader = new FXMLLoader(getClass().getResource("SystemInfo.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Информация о системе");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/superapp/kr_superapp/icons/System.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Similarly update ProcessTrackingApp and TerminalApp
