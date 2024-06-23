package superapp.kr_superapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SuperApp extends Application {
    private static final String HOME_DIR = System.getProperty("user.home") + "/SuperApp/src/main/home";
    private static final String TRASH_DIR = System.getProperty("user.home") + "/SuperApp/src/main/trash";
    private static final String FONTS_DIR = System.getProperty("user.home") + "/SuperApp/src/main/fonts";
    private static final String JAVA_FX_LIB = System.getProperty("user.home") + "/SuperApp/src/main/javafx/lib";
    private static final String INSTALL_FLAG = System.getProperty("user.home") + "/SuperApp/src/main/.installed";

    @Override
    public void start(Stage stage) throws IOException {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/superapp/kr_superapp/icons/Icon_SuperApp.png")));

        createDirectories();

        if (!isInstalled()) {
            showInstallationWindow(stage);
        } else {
            showMainWindow(stage);
        }

        trackSuperAppProcesses();
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

        String css = this.getClass().getResource("style.css").toExternalForm();
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

    private void trackSuperAppProcesses() {
        List<String> pids = ProcessUtils.getLinuxProcesses().stream()
                .filter(process -> process.getCommandLine().contains("SuperApp"))
                .map(ProcessInfo::getPid)
                .map(String::valueOf)
                .collect(Collectors.toList());

        try {
            FileMappingHandler fileMappingHandler = new FileMappingHandler();
            fileMappingHandler.writeData(String.join(",", pids).getBytes());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
