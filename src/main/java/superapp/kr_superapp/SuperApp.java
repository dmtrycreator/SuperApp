package superapp.kr_superapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SuperApp extends Application {
    private static final String BASE_DIR = System.getProperty("user.home") + "/SuperApp";
    private static final String HOME_DIR = BASE_DIR + "/home";
    private static final String TRASH_DIR = BASE_DIR + "/trash";
    private static final String FONTS_DIR = BASE_DIR + "/fonts";
    private static final String JAVA_FX_LIB = BASE_DIR + "/javafx/lib";
    private static final String INSTALL_FLAG = BASE_DIR + "/.installed";
    private static final String LOCK_FILE = BASE_DIR + "/.lock";

    @Override
    public void start(Stage stage) throws IOException {
        // Установка иконки приложения
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/superapp/kr_superapp/icons/Icon_SuperApp.png")));

        // Проверка наличия блокировки
        if (isAppRunning()) {
            System.out.println("Приложение уже запущено.");
            System.exit(1);
        } else {
            createLockFile();
        }

        // Создание необходимых директорий, если они не существуют
        createDirectories();

        if (!isInstalled()) {
            showInstallationWindow(stage);
        } else {
            showMainWindow(stage);
        }

        stage.setOnCloseRequest(event -> {
            deleteLockFile();
        });
    }

    private void createDirectories() throws IOException {
        Files.createDirectories(Paths.get(HOME_DIR));
        Files.createDirectories(Paths.get(TRASH_DIR));
        Files.createDirectories(Paths.get(FONTS_DIR));
        Files.createDirectories(Paths.get(JAVA_FX_LIB));
    }

    private boolean isInstalled() {
        return Files.exists(Paths.get(INSTALL_FLAG));
    }

    private void showInstallationWindow(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SuperApp.class.getResource("install.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        InstallController controller = fxmlLoader.getController();
        controller.setStage(stage);

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

    private boolean isAppRunning() {
        return Files.exists(Paths.get(LOCK_FILE));
    }

    private void createLockFile() {
        try {
            Files.createFile(Paths.get(LOCK_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteLockFile() {
        try {
            Files.deleteIfExists(Paths.get(LOCK_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
