package superapp.kr_superapp;

import javafx.scene.control.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java",
                    "--module-path", javafxPath,
                    "--add-modules", "javafx.controls,javafx.fxml",
                    "-cp", System.getProperty("java.class.path"),
                    appClassName,
                    filePath
            );

            log("Запуск приложения с командой: " + String.join(" ", processBuilder.command()));
            
            Process process = processBuilder.start();

            // Read output and error streams
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    log("OUTPUT: " + line);
                }
                while ((line = errorReader.readLine()) != null) {
                    log("ERROR: " + line);
                }
            }

            int exitCode = process.waitFor();
            log("Приложение завершилось с кодом: " + exitCode);

        } catch (IOException e) {
            e.printStackTrace();
            log("Ошибка ввода/вывода при чтении из общей памяти или запуске приложения: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            log("Ошибка прерывания при запуске приложения: " + e.getMessage());
        }
    }

    public static void log(String message) {
        Controller.log(message);
    }
}
