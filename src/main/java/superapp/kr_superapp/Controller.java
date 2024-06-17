package superapp.kr_superapp;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.nio.file.LinkOption;


/**
 * Этот класс управляет основным представлением приложения файлового менеджера.
 * Он инициализирует основные компоненты пользовательского интерфейса и обрабатывает взаимодействия с файловой системой,
 * включая операции с файлами, навигацию и поиск.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button aboutMeButton;

    @FXML
    private Label button_home_directory;

    @FXML
    private HBox directory;

    @FXML
    private GridPane fileBrowserGridView;

    @FXML
    private ScrollPane fileBrowserScrollPane;

    @FXML
    private TreeTableView<FileItem> fileBrowserTreeView;

    @FXML
    private Button helpButton;

    @FXML
    private Button mainButton;

    @FXML
    private Pane main_pane;

    @FXML
    private VBox main_vbox;

    @FXML
    private MenuItem menu_item_drivers;

    @FXML
    private MenuItem menu_item_network_connections;

    @FXML
    private MenuItem menu_item_processes;

    @FXML
    private MenuItem menu_item_report;

    @FXML
    private MenuItem menu_item_settings;

    @FXML
    private MenuItem menu_item_system_info;

    @FXML
    private MenuItem menu_item_system_monitor;

    @FXML
    private MenuItem menu_item_terminal;

    @FXML
    private MenuItem menu_item_terminal_linux;

    @FXML
    private Pane menu_pane;

    @FXML
    private Pane searchPane;

    @FXML
    private Label statusLabel;

    @FXML
    private HBox status_bar;

    @FXML
    private Pane systemPane;

    @FXML
    private Label trashLabel;

    @FXML
    private Pane trashPane;

    @FXML
    private MenuItem menu_item_create_file;

    @FXML
    private StackPane stackMain;

    private FileMappingHandler fileMappingHandler;
    private LinuxAppLauncher linuxAppLauncher;

    private FileTreeTable fileTreeTable;
    private FileGridView fileGridView;
    private SystemHandler systemHandler;
    private TrashHandler trashHandler;
    private DragAndDropHandler dragAndDropHandler;
    private MenuHandler menuHandler;
    private FileBrowserHandler fileBrowserHandler;
    private static final File rootDirectory = new File("system/home");
    private static StringBuilder logBuilder = new StringBuilder();

    private static Controller instance;

    private Comparator<Path> currentComparator = Comparator.comparing(path -> path.toFile().lastModified());

    /**
     * Конструктор для создания и инициализации Controller.
     *
     * Constructor to create and initialize the Controller.
     */
    private SearchHandler searchHandler;

    public Controller() {
        instance = this;
        linuxAppLauncher = new LinuxAppLauncher();
    }

    /**
     * Получает текущий экземпляр Controller.
     *
     * Gets the current instance of Controller.
     *
     * @return текущий экземпляр Controller / the current instance of Controller
     */
    public static Controller getInstance() {
        return instance;
    }

    /**
     * Инициализация контроллера. Устанавливает обработчики для различных компонентов
     * и инициализирует основные представления.
     *
     * Initializes the controller. Sets handlers for various components
     * and initializes the main views.
     */
    @FXML
    public void initialize() {
        try {
            fileMappingHandler = new FileMappingHandler();
            log("Обработчик сопоставления файлов инициализирован");
            setStatusMessage("Обработчик сопоставления файлов инициализирован");
        } catch (IOException e) {
            e.printStackTrace();
            log("Ошибка инициализации обработчика сопоставления файлов: " + e.getMessage());
            setStatusMessage("Ошибка инициализации обработчика сопоставления файлов");
        }
        fileTreeTable = new FileTreeTable(fileBrowserTreeView);
        fileGridView = new FileGridView(fileBrowserScrollPane, stackMain);
        systemHandler = new SystemHandler(fileBrowserScrollPane, stackMain);
        trashHandler = new TrashHandler(fileBrowserScrollPane, stackMain);
        dragAndDropHandler = new DragAndDropHandler(fileBrowserTreeView, fileBrowserScrollPane, fileTreeTable, fileGridView, trashPane);
        menuHandler = new MenuHandler(menu_item_system_info, menu_item_processes, menu_item_terminal);
        fileBrowserHandler = new FileBrowserHandler(fileTreeTable, fileGridView);

        searchHandler = new SearchHandler(fileGridView, directory, this, fileBrowserScrollPane);

        updateViews(rootDirectory.getPath());
        switchToFileGridView(rootDirectory.getPath());

        searchPane.setOnMouseClicked(event -> {
            showSearchOverlay();
            log("\"Поиск\" нажат");
            setStatusMessage("Поиск запущен");
        });

        systemPane.setOnMouseClicked(event -> {
            updateDirectoryHBox("system");
            switchToSystemHandler();
            log("\"Система\" нажата");
            setStatusMessage("Просмотр системных файлов");
        });

        trashPane.setOnMouseClicked(event -> {
            updateDirectoryHBox("Корзина");
            switchToTrashHandler();
            log("Панель корзины нажата");
            setStatusMessage("Просмотр корзины");
        });

        aboutMeButton.setOnAction(event -> {
            showAboutPane();
            log("Кнопка 'Обо мне' нажата");
            setStatusMessage("Просмотр информации обо мне");
        });
        helpButton.setOnAction(event -> {
            showHelpPane();
            log("Кнопка 'Помощь' нажата");
            setStatusMessage("Просмотр помощи");
        });
        mainButton.setOnAction(event -> {
            showMainPane();
            log("Основная кнопка нажата");
            setStatusMessage("Возврат к основному окну");
        });

        menu_item_system_monitor.setOnAction(event -> {
            linuxAppLauncher.openSystemMonitor();
            log("Пункт меню 'Системный монитор' нажат");
            setStatusMessage("Запуск системного монитора");
        });
        menu_item_terminal_linux.setOnAction(event -> {
            linuxAppLauncher.openTerminal();
            log("Пункт меню 'Терминал' нажат");
            setStatusMessage("Запуск терминала");
        });
        menu_item_settings.setOnAction(event -> {
            linuxAppLauncher.openSettings();
            log("Пункт меню 'Настройки' нажат");
            setStatusMessage("Открытие настроек");
        });
        menu_item_network_connections.setOnAction(event -> {
            linuxAppLauncher.openNetworkConnections();
            log("Пункт меню 'Сетевые подключения' нажат");
            setStatusMessage("Открытие сетевых подключений");
        });
        menu_item_drivers.setOnAction(event -> {
            linuxAppLauncher.openDrivers();
            log("Пункт меню 'Драйверы' нажат");
            setStatusMessage("Открытие драйверов");
        });

        menu_item_create_file.setOnAction(event -> {
            showFileCreatorOverlay();
            log("Пункт меню 'Создать файл' нажат");
            setStatusMessage("Создание нового файла");
        });

        menu_item_report.setOnAction(event -> {
            log("Пункт меню 'Отчет' нажат");
            setStatusMessage("Создание отчета");
            saveLogReport();
        });

        updateTrashLabel();
    }

    /**
     * Логирует сообщение с отметкой времени.
     *
     * Logs a message with a timestamp.
     *
     * @param message сообщение для логирования / message to log
     */
    public static void log(String message) {
        logBuilder.append(LocalTime.now()).append(" - ").append(message).append("\n");
    }

    /**
     * Устанавливает сообщение статуса.
     *
     * Sets the status message.
     *
     * @param message сообщение статуса / status message
     */
    public void setStatusMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        } else {
            System.err.println("statusLabel is not initialized");
        }
    }
    /**
     * Показывает оверлей для создания нового файла.
     *
     * Shows the file creator overlay.
     */
    public void showFileCreatorOverlay() {
        new FileCreatorOverlay(fileGridView);
        log("Оверлей создателя файла показан");
        setStatusMessage("Создание нового файла");
    }

    /**
     * Обрабатывает изменение директории.
     *
     * Handles the directory change.
     *
     * @param directoryPath путь к новой директории / new directory path
     */
    @FXML
    public void handleDirectoryChange(String directoryPath) {
        updateViews(directoryPath);
        updateDirectoryHBox(directoryPath);
        log("Директория изменена на: " + directoryPath);
        setStatusMessage("Переход в директорию: " + directoryPath);
    }

    /**
     * Показывает панель "Обо мне".
     *
     * Shows the "About me" pane.
     */
    private void showAboutPane() {
        main_pane.setVisible(false);
        status_bar.setVisible(false);

        AboutPane aboutPane = new AboutPane();
        main_vbox.getChildren().removeIf(node -> node instanceof Pane && node != menu_pane);
        main_vbox.getChildren().add(aboutPane);
        log("Панель 'Обо мне' показана");
        setStatusMessage("Просмотр информации обо мне");
    }

    /**
     * Показывает панель "Помощь".
     *
     * Shows the "Help" pane.
     */
    private void showHelpPane() {
        main_pane.setVisible(false);
        status_bar.setVisible(false);

        HelpPane helpPane = new HelpPane();
        main_vbox.getChildren().removeIf(node -> node instanceof Pane && node != menu_pane);
        main_vbox.getChildren().add(helpPane);
        log("Панель 'Помощь' показана");
        setStatusMessage("Просмотр помощи");
    }

    /**
     * Показывает основную панель.
     *
     * Shows the main pane.
     */
    private void showMainPane() {
        main_vbox.getChildren().removeIf(node -> node instanceof Pane && node != menu_pane);

        if (!main_vbox.getChildren().contains(main_pane)) {
            main_vbox.getChildren().add(1, main_pane);
        }
        if (!main_vbox.getChildren().contains(status_bar)) {
            main_vbox.getChildren().add(status_bar);
        }
        main_pane.setVisible(true);
        status_bar.setVisible(true);
        log("Основная панель показана");
        setStatusMessage("Возврат к основному окну");
    }

    /**
     * Показывает оверлей поиска.
     *
     * Shows the search overlay.
     */
    public void showSearchOverlay() {
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
                searchHandler.performSearch(keywords);
            }
            stackMain.getChildren().removeAll(background, searchOverlay);
            log("Поиск выполнен с ключевыми словами: " + keywords);
            setStatusMessage("Поиск выполнен: " + keywords);
        });

        searchOverlay.getChildren().addAll(searchField, searchButton);
        stackMain.getChildren().addAll(background, searchOverlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, searchOverlay));
        log("Оверлей поиска показан");
        setStatusMessage("Поиск запущен");
    }

    /**
     * Обновляет содержимое панели с текущей директорией.
     *
     * Updates the content of the directory panel.
     *
     * @param directoryPath путь к текущей директории / path to the current directory
     */
    public void updateDirectoryHBox(String directoryPath) {
        directory.getChildren().clear();

        if (directoryPath.equals("system")) {
            Label systemLabel = new Label("System: доступ ограничен");
            systemLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
            directory.getChildren().add(systemLabel);
            return;
        } else if (directoryPath.equals("Корзина")) {
            Label trashLabel = new Label("Корзина");
            trashLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
            directory.getChildren().add(trashLabel);
            return;
        }

        if (directoryPath.startsWith("/media/")) {
            String[] pathComponents = directoryPath.split(File.separator);
            StringBuilder fullPath = new StringBuilder("/media");

            for (String component : pathComponents) {
                if (!component.isEmpty() && !component.equals("media")) {
                    fullPath.append(File.separator).append(component);

                    directory.getChildren().add(new Label(File.separator));
                    Label pathLabel = new Label(component);
                    pathLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
                    String tempPath = fullPath.toString();
                    pathLabel.setOnMouseClicked(event -> navigateToDirectory(tempPath));
                    directory.getChildren().add(pathLabel);
                }
            }
        } else {
            int homeIndex = directoryPath.indexOf("home");
            if (homeIndex != -1) {
                String relativePath = directoryPath.substring(homeIndex).trim();

                Label homeLabel = new Label("home");
                homeLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
                homeLabel.setOnMouseClicked(event -> navigateToDirectory("system/home"));
                directory.getChildren().add(homeLabel);

                if (!relativePath.equals("home")) {
                    relativePath = relativePath.replaceFirst("^home", "");
                    if (relativePath.startsWith(File.separator)) {
                        relativePath = relativePath.substring(1);
                    }

                    if (!relativePath.isEmpty()) {
                        String[] pathComponents = relativePath.split(File.separator);
                        StringBuilder fullPath = new StringBuilder("system/home");

                        for (String component : pathComponents) {
                            if (!component.isEmpty()) {
                                fullPath.append(File.separator).append(component);

                                directory.getChildren().add(new Label(File.separator));
                                Label pathLabel = new Label(component);
                                pathLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
                                String tempPath = fullPath.toString();
                                pathLabel.setOnMouseClicked(event -> navigateToDirectory(tempPath));
                                directory.getChildren().add(pathLabel);
                            }
                        }
                    }
                }
            } else {
                Label pathLabel = new Label(directoryPath);
                pathLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
                directory.getChildren().add(pathLabel);
            }
        }

        log("Директория обновлена на: " + directoryPath);
        setStatusMessage("Текущая директория: " + directoryPath);
    }

    /**
     * Навигация к указанной директории.
     *
     * Navigates to the specified directory.
     *
     * @param path путь к директории / path to the directory
     */
    private void navigateToDirectory(String path) {
        updateViews(path);
        log("Изменена директория: " + path);
        setStatusMessage("Переход в директорию: " + path);
    }

    /**
     * Обновляет представления дерева файлов и сетки файлов.
     *
     * Updates the file tree and file grid views.
     *
     * @param path путь к директории / path to the directory
     */
    public void updateViews(String path) {
        fileTreeTable.updateTreeItems(rootDirectory.getPath());
        fileGridView.updateGridView(path);
        updateDirectoryHBox(path);
    }

    /**
     * Переключается на представление сетки файлов для указанной директории.
     *
     * Switches to the file grid view for the specified directory.
     *
     * @param path путь к директории / path to the directory
     */
    public void switchToFileGridView(String path) {
        fileGridView.updateGridView(path);
    }

    /**
     * Переключается на обработчик системных файлов.
     *
     * Switches to the system handler.
     */
    private void switchToSystemHandler() {
        systemHandler.updateSystemView();
    }

    /**
     * Переключается на обработчик корзины.
     *
     * Switches to the trash handler.
     */
    private void switchToTrashHandler() {
        trashHandler.updateTrashView();
    }

    /**
     * Обновляет метку корзины с количеством элементов.
     *
     * Updates the trash label with the number of items.
     */
    private void updateTrashLabel() {
        try (Stream<Path> files = Files.list(Paths.get("system/trash"))) {
            long itemCount = files.count();
            trashLabel.setText("Корзина (" + itemCount + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Сохраняет отчет с логами.
     *
     * Saves the log report.
     */
    private void saveLogReport() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("log_report.txt"))) {
            writer.write(logBuilder.toString());
            log("Отчет сохранен в log_report.txt");
            setStatusMessage("Отчет сохранен");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
