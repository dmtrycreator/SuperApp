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
    private static final String HOME_DIR = "src/main/home";
    private static final String TRASH_DIR = "src/main/trash";
    private static final String FONTS_DIR = "src/main/fonts";
    private static final String JAVA_FX_LIB = "src/main/javafx/lib";
    private static final String INSTALL_FLAG = "src/main/.installed";

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
            Files.createDirectories(installPath.resolve(HOME_DIR));
            Files.createDirectories(installPath.resolve(TRASH_DIR));
            Files.createDirectories(installPath.resolve(FONTS_DIR));
            Files.createDirectories(installPath.resolve(JAVA_FX_LIB));

            // Создание файла флага установки
            Files.createFile(installPath.resolve(INSTALL_FLAG));

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
