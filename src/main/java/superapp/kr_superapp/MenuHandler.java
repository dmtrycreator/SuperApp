package superapp.kr_superapp;

import javafx.scene.control.MenuItem;

import java.io.IOException;

public class MenuHandler {

    private MenuItem menu_item_system_info;
    private MenuItem menu_item_processes;
    private MenuItem menu_item_terminal;

    public MenuHandler(MenuItem menu_item_system_info, MenuItem menu_item_processes, MenuItem menu_item_terminal) {
        this.menu_item_system_info = menu_item_system_info;
        this.menu_item_processes = menu_item_processes;
        this.menu_item_terminal = menu_item_terminal;
        addMenuHandlers();
    }

    private void addMenuHandlers() {
        menu_item_system_info.setOnAction(event -> openSystemInfo());
        menu_item_processes.setOnAction(event -> openProcessTracking());
        menu_item_terminal.setOnAction(event -> openTerminal());
    }

    private void openSystemInfo() {
        openApp("superapp.kr_superapp.SystemInfoApp", "Информация о системе");
    }

    private void openProcessTracking() {
        openApp("superapp.kr_superapp.ProcessTrackingApp", "Процессы");
    }

    private void openTerminal() {
        openApp("superapp.kr_superapp.TerminalApp", "Терминал");
    }

    private void openApp(String appClassName, String title) {
        try {
            FileMappingHandler fileMappingHandler = new FileMappingHandler();
            String filePath = new String(fileMappingHandler.readData()).trim();
            log("Путь к файлу прочитан из общей памяти: " + filePath);

            String javafxPath = "lib/javafx-sdk-22.0.1/lib";

            new ProcessBuilder(
                    "java",
                    "--module-path", javafxPath,
                    "--add-modules", "javafx.controls,javafx.fxml",
                    "-cp", System.getProperty("java.class.path"),
                    appClassName,
                    filePath
            ).start();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log("Ошибка чтения из общей памяти или запуска приложения: " + e.getMessage());
        }
    }

    public static void log(String message) {
        Controller.log(message);
    }
}
