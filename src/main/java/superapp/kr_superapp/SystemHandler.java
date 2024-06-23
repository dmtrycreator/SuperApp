package superapp.kr_superapp;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс SystemHandler отвечает за отображение и управление системными файлами и папками,
 * а также за запуск исполняемых файлов в новом или текущем процессе.
 *
 * The SystemHandler class is responsible for displaying and managing system files and folders,
 * as well as launching executable files in a new or current process.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class SystemHandler {
    public static final String SYSTEM_DIRECTORY = "src/main";
    public static final String HOME_DIRECTORY = "src/main/home";
    public static final String TRASH_DIRECTORY = "src/main/trash";

    private ScrollPane scrollPane;
    private StackPane stackMain;
    private GridPane gridPane;

    private final Image folderImage = new Image(getClass().getResourceAsStream("icons/Folder_lock.png"));
    private final Image fileImage = new Image(getClass().getResourceAsStream("icons/File.png"));
    private final Image systemInfoImage = new Image(getClass().getResourceAsStream("icons/System.png"));
    private final Image terminalImage = new Image(getClass().getResourceAsStream("icons/Terminal.png"));
    private final Image processTrackingImage = new Image(getClass().getResourceAsStream("icons/Process.png"));
    private final Image executableImage = new Image(getClass().getResourceAsStream("icons/Program.png"));
    private final Image storageImage = new Image(getClass().getResourceAsStream("icons/Storage.png"));

    /**
     * Конструктор для создания и инициализации SystemHandler.
     *
     * Constructor to create and initialize the SystemHandler.
     */
    public SystemHandler(ScrollPane scrollPane, StackPane stackMain) {
        this.scrollPane = scrollPane;
        this.stackMain = stackMain;
        this.gridPane = new GridPane();
        this.scrollPane.setContent(gridPane);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setPrefHeight(394);
        this.scrollPane.setMinWidth(606);
        this.scrollPane.setMaxWidth(606);
        this.gridPane.prefWidth(606);
        this.gridPane.setMaxWidth(606);
        this.gridPane.setVgap(15);
        this.gridPane.setHgap(15);
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public void updateSystemView() {
        updateView(SYSTEM_DIRECTORY);
        log("System view updated / Обновлено представление системы");
    }

    public void updateHomeView() {
        Controller.getInstance().updateViews(HOME_DIRECTORY);
        log("Home view updated / Обновлено представление домашней директории");
    }

    public void updateTrashView() {
        Controller.getInstance().switchToTrashHandler();
        Controller.getInstance().updateDirectoryHBox("Корзина");
        log("Trash view updated / Обновлено представление корзины");
    }

    private void updateView(String directory) {
        gridPane.getChildren().clear();
        try (Stream<Path> paths = Files.list(Paths.get(directory))) {
            List<Path> fileList = paths.collect(Collectors.toList());
            int fileCount = fileList.size();
            int rowCount = (fileCount + 6) / 7;
            int columnCount = Math.min(fileCount, 7);

            gridPane.getColumnConstraints().clear();
            for (int i = 0; i < columnCount; i++) {
                ColumnConstraints columnConstraints = new ColumnConstraints();
                columnConstraints.setPercentWidth(100.0 / columnCount);
                gridPane.getColumnConstraints().add(columnConstraints);
            }

            int index = 0;
            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {
                    if (index < fileCount) {
                        Path path = fileList.get(index);
                        VBox vbox = createFileItem(path);
                        gridPane.add(vbox, column, row);
                        index++;
                    }
                }
            }
            scrollPane.setContent(gridPane);
            scrollPane.setUserData(this);
            log("View updated for directory: " + directory + " / Представление обновлено для директории: " + directory);
        } catch (Exception e) {
            e.printStackTrace();
            log("Error updating view for directory: " + directory + " / Ошибка обновления представления для директории: " + directory);
        }
    }

    public VBox createFileItem(Path path) {
        String itemName = path.getFileName().toString();
        ImageView imageView;
        if (Files.isDirectory(path)) {
            if (path.toString().startsWith("/media/")) {
                imageView = new ImageView(storageImage);
            } else {
                imageView = new ImageView(folderImage);
            }
        } else if (isExecutable(path)) {
            if (itemName.equals("System.sh")) {
                imageView = new ImageView(systemInfoImage);
            } else if (itemName.equals("Process.sh")) {
                imageView = new ImageView(processTrackingImage);
            } else if (itemName.equals("Terminal.sh")) {
                imageView = new ImageView(terminalImage);
            } else {
                imageView = new ImageView(executableImage);
            }
        } else {
            imageView = new ImageView(fileImage);
        }

        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        Label label = new Label(itemName, imageView);
        label.setFont(new Font("Inter Medium", 12));
        label.setTextFill(Color.web("#F5FAFF"));
        label.setContentDisplay(ContentDisplay.TOP);
        label.setAlignment(Pos.CENTER);

        VBox vbox = new VBox(imageView, label);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefWidth(74);
        vbox.setMaxWidth(74);
        vbox.setPadding(new Insets(8, 5, 10, 5));
        label.setMaxWidth(64);
        label.setWrapText(false);

        Tooltip tooltip = new Tooltip(path.toString());
        Tooltip.install(vbox, tooltip);

        vbox.setOnMouseEntered(event -> vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;"));
        vbox.setOnMouseExited(event -> vbox.setStyle("-fx-background-color: transparent;"));

        vbox.setOnMouseClicked(event -> handleFileItemClick(event, path, vbox));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem propertiesItem = new MenuItem("Свойства");
        propertiesItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

        if (isExecutable(path)) {
            MenuItem openItem = new MenuItem("Открыть");
            MenuItem openInCurrentProcessItem = new MenuItem("Открыть в текущем процессе");
            MenuItem toggleExecutableItem = new MenuItem("Не запускать как программу");

            openItem.setOnAction(event -> {
                if (Files.isDirectory(path)) {
                    handleDirectoryChange(path.toString());
                } else {
                    openFile(path);
                }
            });

            openInCurrentProcessItem.setOnAction(event -> {
                if (!Files.isDirectory(path)) {
                    try {
                        openWindow(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            toggleExecutableItem.setOnAction(event -> {
                toggleExecutableStatus(path, toggleExecutableItem);
            });

            contextMenu.getItems().addAll(openItem, openInCurrentProcessItem, toggleExecutableItem, propertiesItem);
        } else {
            contextMenu.getItems().add(propertiesItem);
        }

        propertiesItem.setOnAction(event -> {
            try {
                openFileInfoWindow(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        vbox.setOnContextMenuRequested(event -> contextMenu.show(vbox, event.getScreenX(), event.getScreenY()));
        log("File item created for: " + path.toString() + " / Элемент файла создан для: " + path.toString());
        return vbox;
    }

    private boolean isExecutable(Path path) {
        return Files.isExecutable(path) && Files.isRegularFile(path);
    }

    public static void openWindow(Path path) throws IOException {
        String name = null;
        if (path.getFileName().toString().equals("System.sh")) {
            name = "SystemInfo";
        } else if (path.getFileName().toString().equals("Process.sh")) {
            name = "ProcessTracking";
        } else if (path.getFileName().toString().equals("Terminal.sh")) {
            name = "Terminal";
        }
        if (name != null) {
            FXMLLoader loader = new FXMLLoader(FileGridView.class.getResource(name + ".fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(FileGridView.class.getResource("style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(name);
            stage.setScene(scene);
            stage.show();
        }
    }

    private void openFile(Path path) {
        try {
            if (isExecutable(path)) {
                if (path.getFileName().toString().equals("System.sh")) {
                    launchApp(SystemInfoApp.class.getName());
                } else if (path.getFileName().toString().equals("Process.sh")) {
                    launchApp(ProcessTrackingApp.class.getName());
                } else if (path.getFileName().toString().equals("Terminal.sh")) {
                    launchApp(TerminalApp.class.getName());
                } else {
                    openTerminalWithCommand(path.toString());
                }
            } else {
                Desktop.getDesktop().open(path.toFile());
            }
            log("File opened: " + path.toString() + " / Файл открыт: " + path.toString());
        } catch (IOException e) {
            e.printStackTrace();
            log("Error opening file: " + path.toString() + " / Ошибка открытия файла: " + path.toString());
        }
    }

    private void launchApp(String appClassName) {
        try {
            String javafxPath = "/home/dmtrycreator/IdeaProjects/CM/KR_SuperApp/openjfx-22.0.1_linux-x64_bin-sdk/javafx-sdk-22.0.1/lib";
            new ProcessBuilder(
                    "java",
                    "--module-path", javafxPath,
                    "--add-modules", "javafx.controls,javafx.fxml",
                    "-cp", System.getProperty("java.class.path"),
                    appClassName
            ).start();
            log("Application launched: " + appClassName + " / Приложение запущено: " + appClassName);
        } catch (IOException e) {
            e.printStackTrace();
            log("Error launching application: " + appClassName + " / Ошибка запуска приложения: " + appClassName);
        }
    }

    private void openTerminalWithCommand(String command) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Terminal.fxml"));
            Parent root = loader.load();

            TerminalController terminalController = loader.getController();
            terminalController.executeCommand(command);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Терминал");
            stage.setScene(new Scene(root));
            stage.show();
            log("Terminal opened with command: " + command + " / Терминал открыт с командой: " + command);
        } catch (IOException e) {
            e.printStackTrace();
            log("Error opening terminal with command: " + command + " / Ошибка открытия терминала с командой: " + command);
        }
    }

    private void toggleExecutableStatus(Path path, MenuItem toggleItem) {
        try {
            boolean isExecutable = isExecutable(path);
            path.toFile().setExecutable(!isExecutable);
            toggleItem.setText(!isExecutable ? "Не запускать как программу" : "Запускать как программу");
            log("Executable status toggled for: " + path.toString() + " / Статус исполняемого файла изменен для: " + path.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log("Error toggling executable status for: " + path.toString() + " / Ошибка изменения статуса исполняемого файла для: " + path.toString());
        }
    }

    private void openFileInfoWindow(Path filePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FileInfo.fxml"));
        Parent root = loader.load();

        FileInfoController controller = loader.getController();
        controller.setFileInfo(filePath);

        Stage stage = new Stage();
        stage.setTitle("Информация о файле");
        stage.setScene(new Scene(root));
        stage.show();
        log("File info window opened for: " + filePath.toString() + " / Окно информации о файле открыто для: " + filePath.toString());
    }

    private void handleDirectoryChange(String directory) {
        if (directory.equals(HOME_DIRECTORY)) {
            updateHomeView();
        } else if (directory.equals(TRASH_DIRECTORY)) {
            updateTrashView();
        } else {
            updateView(directory);
        }
        log("Directory changed to: " + directory + " / Директория изменена на: " + directory);
    }

    private void handleFileItemClick(MouseEvent event, Path path, VBox vbox) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (event.getClickCount() == 2) {
                if (Files.isDirectory(path)) {
                    handleDirectoryChange(path.toString());
                    log("Директория открыта: " + path.toString());
                } else if (isExecutable(path)) {
                    openFile(path);
                }
            }
        }
    }

    /**
     * Логирует сообщение с отметкой времени.
     *
     * Logs a message with a timestamp.
     *
     * @param message сообщение для логирования / message to log
     */
    public static void log(String message) {
        Controller.log(message);
    }

    /**
     * Отображает результаты поиска по ключевым словам в системной директории.
     *
     * Displays the search results by keywords in the system directory.
     *
     * @param resultFiles список файлов с результатами поиска / list of result files
     * @param keywords ключевые слова для поиска / keywords for search
     */
    private void displaySearchResults(List<File> resultFiles, String keywords) {
        gridPane.getChildren().clear();
        int columnCount = 7;
        int index = 0;
        for (File file : resultFiles) {
            Path path = file.toPath();
            VBox vbox = createFileItem(path);
            int row = index / columnCount;
            int column = index % columnCount;
            gridPane.add(vbox, column, row);
            index++;
        }
        log("Результаты поиска в системе по: " + keywords + " / Search results in system for: " + keywords);
    }
}
