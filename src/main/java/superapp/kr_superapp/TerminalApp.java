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
        String processName = getParameters().getUnnamed().size() > 1 ? getParameters().getUnnamed().get(1) : "Terminal Process";
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Terminal.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Терминал");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/superapp/kr_superapp/icons/Terminal.png")));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
