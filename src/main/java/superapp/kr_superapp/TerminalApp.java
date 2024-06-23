package superapp.kr_superapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class TerminalApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("TerminalApp: start() method called");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Terminal.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Terminal");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/superapp/kr_superapp/icons/Terminal.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println("TerminalApp: Stage shown");
    }

    public static void main(String[] args) {
        System.out.println("TerminalApp: main() method called with args: " + String.join(", ", args));
        launch(args);
    }
}
