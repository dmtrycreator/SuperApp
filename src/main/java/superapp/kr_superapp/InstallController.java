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

    // Константы для директорий установки
    private static final String HOME_DIR = System.getProperty("user.home") + "/SuperApp/home";
    private static final String TRASH_DIR = System.getProperty("user.home") + "/SuperApp/trash";
    private static final String FONTS_DIR = System.getProperty("user.home") + "/SuperApp/fonts";
    private static final String JAVA_FX_LIB = System.getProperty("user.home") + "/SuperApp/javafx/lib";
    private static final String INSTALL_FLAG = System.getProperty("user.home") + "/SuperApp/.installed";

    @FXML
    private void initialize() {
        // Инициализация контроллера
    }

    @FXML
    private void handleBrowse() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выбор директории для установки");
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            directoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleInstall() {
        String directory = directoryField.getText();
        if (directory == null || directory.isEmpty()) {
            statusLabel.setText("Пожалуйста, выберите корректную директорию для установки.");
            return;
        }

        try {
            Path installPath = Paths.get(directory);
            Files.createDirectories(installPath.resolve("home"));
            Files.createDirectories(installPath.resolve("trash"));
            Files.createDirectories(installPath.resolve("fonts"));
            Files.createDirectories(installPath.resolve("javafx/lib"));

            // Создание файла флага установки
            Files.createFile(installPath.resolve(".installed"));

            statusLabel.setText("Установка завершена успешно!");
            statusLabel.setStyle("-fx-text-fill: #2CB67D;");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Ошибка установки: " + e.getMessage());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
