package superapp.kr_superapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.application.Platform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class ProcessTracking {

    @FXML
    private TableView<ProcessInfo> processTrackingTableView;

    @FXML
    private MenuItem menu_item_report;

    @FXML
    private Label statusLabel;

    @FXML
    private RadioMenuItem actProcessesMenuItem;

    @FXML
    private RadioMenuItem allProcessesMenuItem;

    @FXML
    private RadioMenuItem superAppProcessesMenuItem;

    @FXML
    private VBox main_vbox;

    @FXML
    private MenuItem resourseMenuItem;

    @FXML
    private Button restartButton;

    @FXML
    private MenuItem searchMenuItem;

    @FXML
    private MenuItem settingsMenuItem;

    @FXML
    private MenuBar settingsMenu;

    @FXML
    private StackPane stackMain;

    private TableColumn<ProcessInfo, Integer> pidColumn = new TableColumn<>("PID");
    private TableColumn<ProcessInfo, String> nameColumn = new TableColumn<>("Name");
    private TableColumn<ProcessInfo, Double> cpuUsageColumn = new TableColumn<>("CPU Usage");
    private TableColumn<ProcessInfo, Long> memoryUsageColumn = new TableColumn<>("Memory Usage");

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long updateInterval = 5; // Update interval in seconds, can be adjusted in settings

    private SystemMonitor systemMonitor = new SystemMonitor();
    private long previousBytesReceived = 0;
    private long previousBytesSent = 0;
    private long startTime = System.currentTimeMillis() / 1000;

    private StringBuilder logBuilder = new StringBuilder();

    private static final String PROCESS_NAME_PREFIX = "SuperApp";

    public void initialize() {
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

        menu_item_report.setOnAction(event -> saveLogReport());

        System.out.println("ProcessTracking initialized successfully.");
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
        System.out.println("Initializing table columns.");

        pidColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPid()));
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getName()));
        cpuUsageColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(Math.round(data.getValue().getCpuUsage() * 100.0) / 100.0));
        memoryUsageColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(Math.round(data.getValue().getMemoryUsage())));

        processTrackingTableView.getColumns().addAll(pidColumn, nameColumn, cpuUsageColumn, memoryUsageColumn);
    }

    private void updateProcessTable() {
        Platform.runLater(() -> {
            List<ProcessInfo> processes = getAllProcesses();
            switch (getSelectedFilter()) {
                case "active":
                    processes = processes.stream().filter(process -> process.getCpuUsage() > 0 && !getSuperAppPIDs().contains(process.getPid())).collect(Collectors.toList());
                    break;
                case "superApp":
                    processes = getSuperAppProcesses();
                    break;
            }
            ObservableList<ProcessInfo> processList = FXCollections.observableArrayList(processes);
            processTrackingTableView.setItems(processList);

            try {
                FileMappingHandler fileMappingHandler = new FileMappingHandler();
                String processCount = String.valueOf(processList.size());
                fileMappingHandler.writeData(processCount.getBytes());
                log("Количество процессов записано в общую память: " + processCount);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                log("Ошибка записи количества процессов в общую память: " + e.getMessage());
            }
        });
    }

    private List<Integer> getSuperAppPIDs() {
        String currentProcessName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        String currentPid = currentProcessName.split("@")[0];

        List<Integer> superAppPIDs = new ArrayList<>();
        superAppPIDs.add(Integer.parseInt(currentPid));

        List<ProcessHandle> descendants = ProcessHandle.current().descendants().toList();
        for (ProcessHandle ph : descendants) {
            superAppPIDs.add((int) ph.pid());
        }

        return superAppPIDs;
    }

    private List<ProcessInfo> getAllProcesses() {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        List<OSProcess> processes = os.getProcesses();

        processes.sort(Comparator.comparingDouble(OSProcess::getProcessCpuLoadCumulative).reversed());

        return processes.stream().map(proc -> {
            String name = proc.getName();
            int pid = proc.getProcessID();
            // Check if the process is a superApp process and update its name if necessary
            if (isSuperAppProcess(pid)) {
                name = getSuperAppProcessName(pid);
            }
            return new ProcessInfo(
                    pid,
                    name,
                    Math.round(100d * (proc.getKernelTime() + proc.getUserTime()) / proc.getUpTime() * 100.0) / 100.0,
                    proc.getResidentSetSize() / (1024 * 1024),
                    proc.getState().name(),
                    proc.getPriority(),
                    proc.getStartTime(),
                    proc.getPath(),
                    proc.getUser()
            );
        }).collect(Collectors.toList());
    }

    private boolean isSuperAppProcess(int pid) {
        // Check if the process is a superApp process based on the pid
        List<Integer> superAppPIDs = getSuperAppPIDs();
        return superAppPIDs.contains(pid);
    }

    private String getSuperAppProcessName(int pid) {
        // Retrieve the name of the superApp process based on the pid
        // This implementation assumes that the process name is passed as an argument
        // You can modify this logic based on your requirements
        String processName = "SuperApp Process";
        try {
            List<String> args = Files.readAllLines(Paths.get("/proc/" + pid + "/cmdline"));
            if (args.size() > 1) {
                processName = args.get(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log("Ошибка чтения имени процесса: " + e.getMessage());
        }
        return processName;
    }

    private List<ProcessInfo> getSuperAppProcesses() {
        List<Integer> superAppPIDs = getSuperAppPIDs();
        List<ProcessInfo> processes = getAllProcesses();
        return processes.stream()
                .filter(process -> superAppPIDs.contains(process.getPid()))
                .collect(Collectors.toList());
    }

    private void showAllProcesses() {
        ObservableList<ProcessInfo> processList = FXCollections.observableArrayList(getAllProcesses());
        processTrackingTableView.setItems(processList);
    }

    private void showActiveProcesses() {
        List<Integer> superAppPIDs = getSuperAppPIDs();
        List<ProcessInfo> activeProcesses = getAllProcesses().stream()
                .filter(process -> process.getCpuUsage() > 0 && !superAppPIDs.contains(process.getPid()))
                .collect(Collectors.toList());

        processTrackingTableView.setItems(FXCollections.observableArrayList(activeProcesses));
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
        List<ProcessInfo> searchResults = processTrackingTableView.getItems().stream()
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

    private void log(String message) {
        logBuilder.append(message).append("\n");
        setStatusMessage(message);
    }

    private void setStatusMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        } else {
            System.err.println("statusLabel is not initialized");
        }
    }

    private void saveLogReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет журнала");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Log Files", "*.log"));
        fileChooser.setInitialFileName("log.log");

        File initialDirectory = new File("src/main/log");
        if (!initialDirectory.exists()) {
            initialDirectory.mkdirs();
        }
        fileChooser.setInitialDirectory(initialDirectory);

        File file = fileChooser.showSaveDialog(stackMain.getScene().getWindow());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(logBuilder.toString());
                log("Отчет журнала сохранен в: " + file.getAbsolutePath());
                setStatusMessage("Отчет журнала сохранен");

                if (file.setReadOnly()) {
                    log("Файл установлен в режим только для чтения");
                } else {
                    log("Не удалось установить файл в режим только для чтения");
                }
            } catch (IOException e) {
                log("Ошибка сохранения отчета журнала: " + e.getMessage());
                setStatusMessage("Ошибка сохранения отчета журнала");
            }
        } else {
            log("Сохранение отчета журнала отменено");
            setStatusMessage("Сохранение отчета журнала отменено");
        }
    }

    public void initializeWithData(String sharedData) {
        Path currentDirectory = Paths.get(sharedData);
        updateProcessList(currentDirectory);
    }

    private void updateProcessList(Path directoryPath) {
        List<ProcessInfo> processes = getAllProcesses();
        List<ProcessInfo> filteredProcesses = processes.stream()
                .filter(process -> process.getExecutablePath().startsWith(directoryPath.toString()))
                .collect(Collectors.toList());
        ObservableList<ProcessInfo> processList = FXCollections.observableArrayList(filteredProcesses);
        processTrackingTableView.setItems(processList);
    }

    public void setDefaultSelection() {
        Platform.runLater(() -> {
            if (!processTrackingTableView.getItems().isEmpty()) {
                processTrackingTableView.getSelectionModel().selectFirst();
            }
        });
    }

    private void showSuperAppProcesses() {
        ObservableList<ProcessInfo> superAppProcesses = FXCollections.observableArrayList(getSuperAppProcesses());
        processTrackingTableView.setItems(superAppProcesses);
    }
}

