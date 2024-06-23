package superapp.kr_superapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oshi.hardware.CentralProcessor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SystemInfo {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button aboutProcessorButton;

    @FXML
    private Button changeProcessButton;

    @FXML
    private Label countModuleLabel;

    @FXML
    private Menu menu_settings;

    @FXML
    private Label processLabel;

    @FXML
    private Label processorLabel;

    @FXML
    private MenuItem reportMenuItem;

    @FXML
    private Button restartButton;

    @FXML
    private MenuBar settingsMenu;

    @FXML
    private Label statusLabel;

    @FXML
    private Label timeOSLabel;

    @FXML
    private StackPane stackMain;

    private boolean showOsUptime = true;

    private static final String[] STATUS_MESSAGES = {
            "Окно успешно открыто",
            "Обновление выполнено успешно",
            "Ошибка при обновлении",
            "Процессорная информация загружена",
            "Ошибка загрузки информации о процессоре",
            "Выбор процесса выполнен",
            "Ошибка выбора процесса"
    };

    private static StringBuilder logBuilder = new StringBuilder();

    @FXML
    void initialize() {
        log("Initializing SystemInfo...");

        Thread processorInfoThread = new Thread(this::updateProcessorInfo);
        processorInfoThread.setPriority(Thread.MIN_PRIORITY);
        processorInfoThread.start();

        Thread uptimeThread = new Thread(this::updateUptime);
        uptimeThread.setPriority(Thread.MIN_PRIORITY);
        uptimeThread.start();

        restartButton.setOnAction(event -> {
            log("Button clicked: Restart");
            new Thread(this::updateProcessorInfo).start();
            new Thread(this::updateUptime).start();
        });

        timeOSLabel.setOnMouseClicked(event -> {
            log("Label clicked: TimeOSLabel");
            switchUptimeMode();
            new Thread(this::updateUptime).start();
        });

        aboutProcessorButton.setOnAction(event -> {
            log("Button clicked: AboutProcessorButton");
            openProcessorInfoWindow();
        });

        changeProcessButton.setOnAction(event -> {
            log("Button clicked: ChangeProcessButton");
            openChangeProcessOverlay();
        });

        reportMenuItem.setOnAction(event -> {
            log("MenuItem clicked: ReportMenuItem");
            saveLogReport();
        });

        log("SystemInfo initialized");
    }

    private void switchUptimeMode() {
        showOsUptime = !showOsUptime;
        log("Uptime mode switched: " + (showOsUptime ? "OS Uptime" : "App Uptime"));
    }

    private void updateUptime() {
        Runnable uptimeUpdater = () -> {
            while (true) {
                try {
                    Duration uptime = showOsUptime ? ProcessUtils.getOsUptime() : ProcessUtils.getAppUptime();
                    String formattedUptime = ProcessUtils.formatDuration(uptime);
                    Platform.runLater(() -> timeOSLabel.setText(formattedUptime));
                    Thread.sleep(1000); // Обновляем каждую секунду
                } catch (Exception e) {
                    log("Error updating uptime: " + e.getMessage());
                }
            }
        };
        Thread uptimeThread = new Thread(uptimeUpdater);
        uptimeThread.setDaemon(true); // Позволяет завершить поток при выходе из приложения
        uptimeThread.start();
    }

    private void updateProcessorInfo() {
        Runnable processorUpdater = () -> {
            while (true) {
                try {
                    String processorName = ProcessUtils.getProcessorName();
                    Platform.runLater(() -> processorLabel.setText(processorName));
                    Thread.sleep(10000); // Обновляем каждые 10 секунд
                } catch (Exception e) {
                    log("Error updating processor info: " + e.getMessage());
                }
            }
        };
        Thread processorThread = new Thread(processorUpdater);
        processorThread.setDaemon(true); // Позволяет завершить поток при выходе из приложения
        processorThread.start();
    }

    private void setStatusMessage(int index) {
        Platform.runLater(() -> statusLabel.setText(STATUS_MESSAGES[index]));
        log("Status message set: " + STATUS_MESSAGES[index]);
    }

    private void openProcessorInfoWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FileInfo.fxml"));
            Parent root = loader.load();

            FileInfoController controller = loader.getController();
            controller.setProcessorInfo(ProcessUtils.getProcessorDetails());

            Stage stage = new Stage();
            stage.setTitle("Информация о процессоре");
            stage.setScene(new Scene(root));
            stage.show();
            log("Processor info window opened");
        } catch (IOException e) {
            log("Error opening processor info window: " + e.getMessage());
        }
    }

    private void openChangeProcessOverlay() {
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setMaxHeight(400);
        overlay.setMaxWidth(600);
        overlay.setSpacing(10);
        overlay.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 18; -fx-padding: 30 40 30 40;");

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск процесса");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-color: #272727; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12; -fx-padding: 16 20 16 20;");

        TreeView<String> processTreeView = new TreeView<>();
        processTreeView.setPrefHeight(200);
        processTreeView.setPrefWidth(300);
        updateProcessTree(processTreeView, "");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateProcessTree(processTreeView, newValue));

        Button selectButton = new Button("Выбрать");
        selectButton.setPrefWidth(100);
        selectButton.setPrefHeight(36);
        selectButton.setStyle("-fx-background-color: #469EE9; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");

        Rectangle background = new Rectangle(
                stackMain.getScene().getWidth(),
                stackMain.getScene().getHeight(),
                Color.rgb(0, 0, 0, 0.7)
        );
        selectButton.setOnAction(event -> {
            TreeItem<String> selectedItem = processTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                String selectedProcess = selectedItem.getValue();
                log("Selected process: " + selectedProcess);
                new Thread(() -> {
                    int moduleCount = ProcessUtils.getModuleCount(selectedProcess);
                    log("Module count: " + moduleCount);
                    Platform.runLater(() -> updateProcessSelection(selectedProcess, moduleCount));
                }).start();
                stackMain.getChildren().removeAll(background, overlay);
            } else {
                log("No process selected");
            }
        });

        overlay.getChildren().addAll(searchField, processTreeView, selectButton);
        stackMain.getChildren().addAll(background, overlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, overlay));
        log("Change process overlay opened");
    }

    private void updateProcessTree(TreeView<String> processTreeView, String filter) {
        TreeItem<String> rootItem = new TreeItem<>("Процессы");
        List<ProcessInfo> processes = ProcessUtils.getLinuxProcesses().stream()
                .filter(process -> process.getName().toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());

        for (ProcessInfo process : processes) {
            TreeItem<String> item = new TreeItem<>(process.getName());
            rootItem.getChildren().add(item);
        }
        processTreeView.setRoot(rootItem);
        processTreeView.setShowRoot(false);
        log("Process tree updated with filter: " + filter);
    }

    private void updateProcessSelection(String processName, int moduleCount) {
        Platform.runLater(() -> {
            processLabel.setText(processName);
            countModuleLabel.setText(String.valueOf(moduleCount));
        });
        setStatusMessage(5);
        log("Process selection updated: " + processName + " with module count: " + moduleCount);
    }

    private void saveLogReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Log Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.log"));
        fileChooser.setInitialFileName("log_report.log");

        File initialDirectory = new File("src/main/log");
        if (!initialDirectory.exists()) {
            initialDirectory.mkdirs();
        }
        fileChooser.setInitialDirectory(initialDirectory);

        File file = fileChooser.showSaveDialog(stackMain.getScene().getWindow());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(logBuilder.toString());
                setStatusMessage(1);
                log("Log report saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                log("Error saving log report: " + e.getMessage());
                setStatusMessage(2);
            }
        } else {
            log("Log report save canceled");
        }
    }

    public static void log(String message) {
        logBuilder.append(LocalTime.now()).append(" - ").append(message).append("\n");
    }
}
