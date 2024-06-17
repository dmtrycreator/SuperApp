package superapp.kr_superapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SuperApp extends Application {
    private static final String HOME_DIR = "system/home";
    private static final String TRASH_DIR = "system/trash";
    private static final String FONTS_DIR = "system/fonts";
    private static final String JAVA_FX_LIB = "system/javafx/lib";
    private static final String INSTALL_FLAG = "system/.installed";

    @Override
    public void start(Stage stage) throws IOException {
        if (!isInstalled()) {
            showInstallationWindow(stage);
        } else {
            showMainWindow(stage);
        }
    }

    private boolean isInstalled() {
        return Files.exists(Paths.get(INSTALL_FLAG));
    }

    private void showInstallationWindow(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SuperApp.class.getResource("install.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("SuperApp Installation");
        stage.show();
    }

    private void showMainWindow(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SuperApp.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        String css = this.getClass().getResource("/superapp/kr_superapp/style.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
        } else {
            System.err.println("CSS file not found");
        }

        stage.setTitle("SuperApp");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
