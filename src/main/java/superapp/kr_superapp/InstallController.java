package superapp.kr_superapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private static final String HOME_DIR = System.getProperty("user.home") + "/SuperApp/src/main/home";
    private static final String TRASH_DIR = System.getProperty("user.home") + "/SuperApp/src/main/trash";
    private static final String FONTS_DIR = System.getProperty("user.home") + "/SuperApp/src/main/fonts";
    private static final String JAVA_FX_LIB = System.getProperty("user.home") + "/SuperApp/src/main/javafx/lib";
    private static final String INSTALL_FLAG = System.getProperty("user.home") + "/SuperApp/src/main/.installed";

    @FXML
    private void initialize() {
        // Установка пути по умолчанию в текстовое поле
        directoryField.setText(System.getProperty("user.home") + "/SuperApp");
        directoryField.setEditable(false);
    }

    @FXML
    private void handleBrowse() {
        statusLabel.setText("Изменить в данной версии ещё нельзя.");
        statusLabel.setStyle("-fx-text-fill: #FF0000;");
    }

    @FXML
    private void handleInstall() {
        String directory = directoryField.getText();

        try {
            Path installPath = Paths.get(directory);
            Files.createDirectories(installPath.resolve(HOME_DIR));
            Files.createDirectories(installPath.resolve(TRASH_DIR));
            Files.createDirectories(installPath.resolve(FONTS_DIR));
            Files.createDirectories(installPath.resolve(JAVA_FX_LIB));

            // Создание файла флага установки
            Files.createFile(installPath.resolve(INSTALL_FLAG));

            statusLabel.setText("Установка завершена успешно! Перейзадите в приложение.");
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
