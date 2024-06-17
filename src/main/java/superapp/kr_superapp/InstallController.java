package superapp.kr_superapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InstallController {

    @FXML
    private TextField directoryField;

    @FXML
    private Label statusLabel;

    private Stage stage;

    @FXML
    private void initialize() {
        // Инициализация контроллера
    }

    @FXML
    private void handleBrowse() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Installation Directory");
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            directoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleInstall() {
        String directory = directoryField.getText();
        if (directory == null || directory.isEmpty()) {
            statusLabel.setText("Please select a valid installation directory.");
            return;
        }

        try {
            Path installPath = Paths.get(directory);
            Files.createDirectories(installPath.resolve("system/home"));
            Files.createDirectories(installPath.resolve("system/trash"));
            Files.createDirectories(installPath.resolve("system/fonts"));
            Files.createDirectories(installPath.resolve("system/javafx/lib"));

            // Создание файла флага установки
            Files.createFile(installPath.resolve("system/.installed"));

            statusLabel.setText("Installation successful!");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Installation failed: " + e.getMessage());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
