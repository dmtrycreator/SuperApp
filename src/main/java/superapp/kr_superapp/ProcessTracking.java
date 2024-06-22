package superapp.kr_superapp;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProcessTracking {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private RadioMenuItem actProcessesMenuItem;

    @FXML
    private RadioMenuItem allProcessesMenuItem;

    @FXML
    private VBox main_vbox;

    @FXML
    private TableView<ProcessInfo> processTrackingTableView;

    @FXML
    private MenuItem resourseMenuItem;

    @FXML
    private Button restartButton;

    @FXML
    private MenuItem menu_item_report;

    @FXML
    private MenuItem searchMenuItem;

    @FXML
    private MenuItem settingsMenuItem;

    @FXML
    private MenuBar settingsMenu;

    @FXML
    private Label statusLabel;

    @FXML
    private RadioMenuItem superAppProcessesMenuItem;

    private ObservableList<ProcessInfo> processList;

    @FXML
    private StackPane stackMain;

    private SystemMonitor systemMonitor = new SystemMonitor();
    private long previousBytesReceived = 0;
    private long previousBytesSent = 0;
    private long startTime = System.currentTimeMillis() / 1000;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long updateInterval = 5; // Update interval in seconds, can be adjusted in settings

    @FXML
    void initialize() {
        initializeTableColumns();
        updateProcessTable();

        allProcessesMenuItem.setOnAction(event -> showAllProcesses());
        actProcessesMenuItem.setOnAction(event -> showActiveProcesses());
        superAppProcessesMenuItem.setOnAction(event -> showSuperAppProcesses());

        actProcessesMenuItem.setSelected(true);

        addContextMenu();

        startProcessUpdateScheduler();

        settingsMenuItem.setOnAction(event -> openSettingsOverlay());
        searchMenuItem.setOnAction(event -> openSearchOverlay());
        resourseMenuItem.setOnAction(event -> openResourceMonitorOverlay());

        setupKeyShortcuts();
    }

    private void setupKeyShortcuts() {
        processTrackingTableView.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case P:
                        executeForSelectedProcess(this::openProcessInfoWindow);
                        break;
                    case S:
                        executeForSelectedProcess(this::interruptProcess);
                        break;
                    case C:
                        executeForSelectedProcess(this::continueProcess);
                        break;
                    case T:
                        executeForSelectedProcess(this::terminateProcess);
                        break;
                    case I:
                        executeForSelectedProcess(this::interruptProcess);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void executeForSelectedProcess(java.util.function.Consumer<ProcessInfo> action) {
        ProcessInfo selectedProcess = processTrackingTableView.getSelectionModel().getSelectedItem();
        if (selectedProcess != null) {
            action.accept(selectedProcess);
        }
    }

    private void initializeTableColumns() {
        TableColumn<ProcessInfo, Integer> pidColumn = new TableColumn<>("PID");
        pidColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPid()));

        TableColumn<ProcessInfo, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));

        TableColumn<ProcessInfo, Double> cpuColumn = new TableColumn<>("CPU Usage (%)");
        cpuColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCpuUsage()));

        TableColumn<ProcessInfo, Double> memoryColumn = new TableColumn<>("Memory Usage (MB)");
        memoryColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMemoryUsage()));

        processTrackingTableView.getColumns().addAll(pidColumn, nameColumn, cpuColumn, memoryColumn);
    }

    private void updateProcessTable() {
        List<ProcessInfo> processes = getAllProcesses();
        switch (getSelectedFilter()) {
            case "active":
                processes = processes.stream().filter(process -> process.getCpuUsage() > 0).collect(Collectors.toList());
                break;
            case "superApp":
                processes = processes.stream().filter(process -> process.getName().contains("SuperApp") || process.getName().contains("SystemInfoApp")).collect(Collectors.toList());
                break;
        }
        processList = FXCollections.observableArrayList(processes);
        processTrackingTableView.setItems(processList);

        try {
            FileMappingHandler fileMappingHandler = new FileMappingHandler();
            String processCount = String.valueOf(processList.size());
            fileMappingHandler.writeData(processCount.getBytes());
            Controller.log("Количество процессов записано в общую память: " + processCount);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Controller.log("Ошибка записи количества процессов в общую память: " + e.getMessage());
        }
    }

    private List<ProcessInfo> getAllProcesses() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        List<OSProcess> processes = os.getProcesses();

        processes.sort(Comparator.comparingDouble(OSProcess::getProcessCpuLoadCumulative).reversed());

        return processes.stream().map(proc -> new ProcessInfo(
                proc.getProcessID(),
                proc.getName(),
                100d * (proc.getKernelTime() + proc.getUserTime()) / proc.getUpTime(),
                proc.getResidentSetSize() / (1024 * 1024),
                proc.getState().name(),
                proc.getPriority(),
                proc.getStartTime(),
                proc.getPath(),
                proc.getUser()
        )).collect(Collectors.toList());
    }


    private void showAllProcesses() {
        processList = FXCollections.observableArrayList(getAllProcesses());
        processTrackingTableView.setItems(processList);
    }

    private void showActiveProcesses() {
        List<ProcessInfo> activeProcesses = processList.stream()
                .filter(process -> process.getCpuUsage() > 0)
                .collect(Collectors.toList());
        processTrackingTableView.setItems(FXCollections.observableArrayList(activeProcesses));
    }

    private void showSuperAppProcesses() {
        List<ProcessInfo> superAppProcesses = processList.stream()
                .filter(process -> process.getName().contains("SuperApp") || process.getName().contains("SystemInfoApp"))
                .collect(Collectors.toList());
        processTrackingTableView.setItems(FXCollections.observableArrayList(superAppProcesses));
    }

    private String getSelectedFilter() {
        if (actProcessesMenuItem.isSelected()) {
            return "active";
        } else if (superAppProcessesMenuItem.isSelected()) {
            return "superApp";
        } else {
            return "all";
        }
    }

    private void addContextMenu() {
        processTrackingTableView.setRowFactory(tv -> {
            TableRow<ProcessInfo> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem propertiesItem = new MenuItem("Свойства");
            propertiesItem.setOnAction(event -> {
                if (!row.isEmpty()) {
                    ProcessInfo process = row.getItem();
                    openProcessInfoWindow(process);
                }
            });
            propertiesItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

            MenuItem stopItem = new MenuItem("Остановить");
            stopItem.setOnAction(event -> {
                if (!row.isEmpty()) {
                    ProcessInfo process = row.getItem();
                    stopProcess(process);
                }
            });

            MenuItem continueItem = new MenuItem("Продолжить");
            continueItem.setOnAction(event -> {
                if (!row.isEmpty()) {
                    ProcessInfo process = row.getItem();
                    continueProcess(process);
                }
            });

            MenuItem terminateItem = new MenuItem("Завершить");
            terminateItem.setOnAction(event -> {
                if (!row.isEmpty()) {
                    ProcessInfo process = row.getItem();
                    terminateProcess(process);
                }
            });

            MenuItem interruptItem = new MenuItem("Прервать");
            interruptItem.setOnAction(event -> {
                if (!row.isEmpty()) {
                    ProcessInfo process = row.getItem();
                    interruptProcess(process);
                }
            });
            interruptItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

            contextMenu.getItems().addAll(propertiesItem, stopItem, continueItem, terminateItem, interruptItem);
            row.contextMenuProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));

            return row;
        });
    }

    private void openProcessInfoWindow(ProcessInfo process) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FileInfo.fxml"));
            Parent root = loader.load();

            FileInfoController controller = loader.getController();
            controller.setProcessInfo(process);

            Stage stage = new Stage();
            stage.setTitle("Свойства процесса");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopProcess(ProcessInfo process) {
        try {
            ProcessUtils.sendSignalToProcess(process.getPid(), "STOP");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void continueProcess(ProcessInfo process) {
        try {
            ProcessUtils.sendSignalToProcess(process.getPid(), "CONT");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void terminateProcess(ProcessInfo process) {
        try {
            ProcessUtils.sendSignalToProcess(process.getPid(), "TERM");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void interruptProcess(ProcessInfo process) {
        try {
            ProcessUtils.sendSignalToProcess(process.getPid(), "INT");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startProcessUpdateScheduler() {
        scheduler = Executors.newScheduledThreadPool(1);
        Runnable updateTask = () -> Platform.runLater(this::updateProcessTable);
        scheduler.scheduleAtFixedRate(updateTask, 0, updateInterval, TimeUnit.SECONDS);
    }

    private void openSearchOverlay() {
        HBox searchOverlay = new HBox();
        searchOverlay.setAlignment(Pos.CENTER);
        searchOverlay.setMaxHeight(138);
        searchOverlay.setMaxWidth(538);
        searchOverlay.setSpacing(10);
        searchOverlay.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 18; -fx-padding: 30 40 30 40;");

        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по ключевым словам");
        searchField.setPrefWidth(341);
        searchField.setStyle("-fx-background-color: #272727; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12; -fx-padding: 16 20 16 20;");
        Button searchButton = new Button("Найти");
        searchButton.setPrefHeight(46);
        searchButton.setPrefWidth(107);
        searchButton.setStyle("-fx-background-color: #469EE9; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");

        Rectangle background = new Rectangle(
                stackMain.getScene().getWidth(),
                stackMain.getScene().getHeight(),
                Color.rgb(0, 0, 0, 0.7)
        );

        searchButton.setOnAction(event -> {
            String keywords = searchField.getText();
            if (keywords != null && !keywords.trim().isEmpty()) {
                performSearch(keywords);
            }
            stackMain.getChildren().removeAll(background, searchOverlay);
        });

        searchOverlay.getChildren().addAll(searchField, searchButton);
        stackMain.getChildren().addAll(background, searchOverlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, searchOverlay));
    }

    private void performSearch(String keywords) {
        List<ProcessInfo> searchResults = processList.stream()
                .filter(process -> process.getName().toLowerCase().contains(keywords.toLowerCase()))
                .collect(Collectors.toList());
        processTrackingTableView.setItems(FXCollections.observableArrayList(searchResults));
    }

    private void openSettingsOverlay() {
        HBox settingsOverlay = new HBox();
        settingsOverlay.setAlignment(Pos.CENTER);
        settingsOverlay.setMaxHeight(138);
        settingsOverlay.setMaxWidth(538);
        settingsOverlay.setSpacing(10);
        settingsOverlay.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 18; -fx-padding: 30 40 30 40;");

        Label updateIntervalLabel = new Label("Интервал обновления (сек):");
        updateIntervalLabel.setStyle("-fx-text-fill: #F5FAFF; -fx-font-size: 14;");

        TextField updateIntervalField = new TextField(String.valueOf(updateInterval));
        updateIntervalField.setPrefWidth(50);
        updateIntervalField.setStyle("-fx-background-color: #272727; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12; -fx-padding: 16 20 16 20;");

        Button increaseButton = new Button("+");
        increaseButton.setPrefWidth(36);
        increaseButton.setPrefHeight(36);
        increaseButton.setStyle("-fx-background-color: #469EE9; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");
        increaseButton.setOnAction(event -> {
            int interval = Integer.parseInt(updateIntervalField.getText());
            if (interval < 60) {
                interval++;
                updateIntervalField.setText(String.valueOf(interval));
                updateInterval = interval;
                restartScheduler();
            }
        });

        Button decreaseButton = new Button("-");
        decreaseButton.setPrefWidth(36);
        decreaseButton.setPrefHeight(36);
        decreaseButton.setStyle("-fx-background-color: #469EE9; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");
        decreaseButton.setOnAction(event -> {
            int interval = Integer.parseInt(updateIntervalField.getText());
            if (interval > 1) {
                interval--;
                updateIntervalField.setText(String.valueOf(interval));
                updateInterval = interval;
                restartScheduler();
            }
        });

        Rectangle background = new Rectangle(
                stackMain.getScene().getWidth(),
                stackMain.getScene().getHeight(),
                Color.rgb(0, 0, 0, 0.7)
        );

        settingsOverlay.getChildren().addAll(updateIntervalLabel, decreaseButton, updateIntervalField, increaseButton);
        stackMain.getChildren().addAll(background, settingsOverlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, settingsOverlay));
    }

    private void restartScheduler() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        startProcessUpdateScheduler();
    }

    private void openResourceMonitorOverlay() {
        VBox monitorOverlay = new VBox();
        monitorOverlay.setAlignment(Pos.CENTER);
        monitorOverlay.setMaxWidth(600);
        monitorOverlay.setSpacing(10);
        monitorOverlay.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 18; -fx-padding: 30 40 30 40;");

        AreaChart<Number, Number> cpuChart = createChart("Процессор", "Время", "(%)");
        AreaChart<Number, Number> memoryChart = createChart("Память", "Time", "(MB)");
        AreaChart<Number, Number> networkChart = createChart("Сеть", "Time", "(KB/s)");

        monitorOverlay.getChildren().addAll(cpuChart, memoryChart, networkChart);

        Rectangle background = new Rectangle(
                stackMain.getScene().getWidth(),
                stackMain.getScene().getHeight(),
                Color.rgb(0, 0, 0, 0.7)
        );

        stackMain.getChildren().addAll(background, monitorOverlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, monitorOverlay));

        startChartUpdateScheduler(cpuChart, memoryChart, networkChart);
    }

    private AreaChart<Number, Number> createChart(String title, String xAxisLabel, String yAxisLabel) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle(title);
        return chart;
    }

    private void startChartUpdateScheduler(AreaChart<Number, Number> cpuChart, AreaChart<Number, Number> memoryChart, AreaChart<Number, Number> networkChart) {
        scheduler = Executors.newScheduledThreadPool(1);
        Runnable updateTask = () -> Platform.runLater(() -> updateCharts(cpuChart, memoryChart, networkChart));
        scheduler.scheduleAtFixedRate(updateTask, 0, updateInterval, TimeUnit.SECONDS);
    }

    private void updateCharts(AreaChart<Number, Number> cpuChart, AreaChart<Number, Number> memoryChart, AreaChart<Number, Number> networkChart) {
        // Обновление данных для графиков
        updateCpuChart(cpuChart);
        updateMemoryChart(memoryChart);
        updateNetworkChart(networkChart);
    }

    private void updateCpuChart(AreaChart<Number, Number> cpuChart) {
        double cpuLoad = systemMonitor.getCpuLoad();

        XYChart.Series<Number, Number> series;
        if (cpuChart.getData().isEmpty()) {
            series = new XYChart.Series<>();
            cpuChart.getData().add(series);
        } else {
            series = cpuChart.getData().get(0);
        }

        long currentTime = (System.currentTimeMillis() / 1000) - startTime;
        series.getData().add(new XYChart.Data<>(currentTime, cpuLoad));

        if (series.getData().size() > 60) {
            series.getData().remove(0);
        }
    }

    private void updateMemoryChart(AreaChart<Number, Number> memoryChart) {
        long usedMemory = systemMonitor.getUsedMemory();
        double usedMemoryInMB = usedMemory / (1024.0 * 1024.0);

        XYChart.Series<Number, Number> series;
        if (memoryChart.getData().isEmpty()) {
            series = new XYChart.Series<>();
            memoryChart.getData().add(series);
        } else {
            series = memoryChart.getData().get(0);
        }

        long currentTime = (System.currentTimeMillis() / 1000) - startTime;
        series.getData().add(new XYChart.Data<>(currentTime, usedMemoryInMB));

        if (series.getData().size() > 60) {
            series.getData().remove(0);
        }
    }

    private void updateNetworkChart(AreaChart<Number, Number> networkChart) {
        systemMonitor.updateNetworkStats();

        long totalBytesReceived = systemMonitor.getTotalBytesReceived();
        long totalBytesSent = systemMonitor.getTotalBytesSent();

        double receivedInKBps = (totalBytesReceived - previousBytesReceived) / 1024.0;
        double sentInKBps = (totalBytesSent - previousBytesSent) / 1024.0;

        previousBytesReceived = totalBytesReceived;
        previousBytesSent = totalBytesSent;

        XYChart.Series<Number, Number> seriesRecv;
        XYChart.Series<Number, Number> seriesSent;

        if (networkChart.getData().isEmpty()) {
            seriesRecv = new XYChart.Series<>();
            seriesSent = new XYChart.Series<>();
            networkChart.getData().addAll(seriesRecv, seriesSent);
        } else {
            seriesRecv = networkChart.getData().get(0);
            seriesSent = networkChart.getData().get(1);
        }

        long currentTime = (System.currentTimeMillis() / 1000) - startTime;
        seriesRecv.getData().add(new XYChart.Data<>(currentTime, receivedInKBps));
        seriesSent.getData().add(new XYChart.Data<>(currentTime, sentInKBps));

        if (seriesRecv.getData().size() > 60) {
            seriesRecv.getData().remove(0);
            seriesSent.getData().remove(0);
        }
    }

    public void initializeWithData(String sharedData) {
        // Предположим, что sharedData - это путь к текущей директории
        Path currentDirectory = Paths.get(sharedData);

        // Логика инициализации с использованием пути к директории
        updateProcessList(currentDirectory);
    }

    // Пример метода для обновления списка процессов с использованием пути к директории
    private void updateProcessList(Path directoryPath) {
        // Логика для обновления представления процессов с использованием переданного пути
        // Например, фильтрация процессов, связанных с указанной директорией
        List<ProcessInfo> processes = getAllProcesses();
        List<ProcessInfo> filteredProcesses = processes.stream()
                .filter(process -> process.getExecutablePath().startsWith(directoryPath.toString()))
                .collect(Collectors.toList());
        processList = FXCollections.observableArrayList(filteredProcesses);
        processTrackingTableView.setItems(processList);
    }
}
