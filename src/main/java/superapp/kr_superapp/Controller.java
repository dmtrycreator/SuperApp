package superapp.kr_superapp;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

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
    private static final File rootDirectory = new File("src/main/home");
    private static StringBuilder logBuilder = new StringBuilder();

    private static Controller instance;

    private Comparator<Path> currentComparator = Comparator.comparing(path -> path.toFile().lastModified());

    private static final String HOME_DIR = "src/main/home";
    private static final String TRASH_DIR = "src/main/trash";
    private static final String FONTS_DIR = "src/main/fonts";
    private static final String JAVA_FX_LIB = "src/main/javafx/lib";
    private static final String INSTALL_FLAG = "src/main/.installed";

    private static final String GITHUB_REPO_URL = "https://github.com/dmtrycreator/SuperApp.git";
    private static final String TEMP_DIR = "src/main/temp";

    private SearchHandler searchHandler;

    public Controller() {
        instance = this;
        linuxAppLauncher = new LinuxAppLauncher();
    }

    public static Controller getInstance() {
        return instance;
    }

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
            updateDirectoryHBox("src/main");
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

    public static void log(String message) {
        logBuilder.append(LocalTime.now()).append(" - ").append(message).append("\n");
    }

    public void showFileCreatorOverlay() {
        new FileCreatorOverlay(fileGridView);
        log("Оверлей создателя файла показан");
        setStatusMessage("Создание нового файла");
    }

    public void writeFilePathToSharedMemory(String filePath) {
        try {
            FileMappingHandler fileMappingHandler = new FileMappingHandler();
            fileMappingHandler.writeData(filePath.getBytes());
            log("Путь к файлу записан в общую память: " + filePath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            log("Ошибка записи пути к файлу в общую память: " + e.getMessage());
        }
    }

    @FXML
    public void handleDirectoryChange(String directoryPath) {
        updateViews(directoryPath);
        updateDirectoryHBox(directoryPath);
        log("Директория изменена на: " + directoryPath);
        setStatusMessage("Переход в директорию: " + directoryPath);
        writeFilePathToSharedMemory(directoryPath);
    }

    private void showAboutPane() {
        main_pane.setVisible(false);
        status_bar.setVisible(false);

        AboutPane aboutPane = new AboutPane();
        main_vbox.getChildren().removeIf(node -> node instanceof Pane && node != menu_pane);
        main_vbox.getChildren().add(aboutPane);
        log("Панель 'Обо мне' показана");
        setStatusMessage("Просмотр информации обо мне");
    }

    private void showHelpPane() {
        main_pane.setVisible(false);
        status_bar.setVisible(false);

        HelpPane helpPane = new HelpPane();
        main_vbox.getChildren().removeIf(node -> node instanceof Pane && node != menu_pane);
        main_vbox.getChildren().add(helpPane);
        log("Панель 'Помощь' показана");
        setStatusMessage("Просмотр помощи");
    }

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

    public void updateDirectoryHBox(String directoryPath) {
        directory.getChildren().clear();

        if (directoryPath.equals("src/main")) {
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
                homeLabel.setOnMouseClicked(event -> navigateToDirectory("src/main/home"));
                directory.getChildren().add(homeLabel);

                if (!relativePath.equals("home")) {
                    relativePath = relativePath.replaceFirst("^home", "");
                    if (relativePath.startsWith(File.separator)) {
                        relativePath = relativePath.substring(1);
                    }

                    if (!relativePath.isEmpty()) {
                        String[] pathComponents = relativePath.split(File.separator);
                        StringBuilder fullPath = new StringBuilder("src/main/home");

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

    private void navigateToDirectory(String path) {
        updateViews(path);
        log("Изменена директория: " + path);
        setStatusMessage("Переход в директорию: " + path);
    }

    public void updateViews(String path) {
        fileTreeTable.updateTreeItems(rootDirectory.getPath());
        fileGridView.updateGridView(path);
        updateDirectoryHBox(path);
    }

    private void switchToFileGridView(String directoryPath) {
        fileGridView.updateGridView(directoryPath);
        fileBrowserScrollPane.setContent(fileGridView.getGridPane());
        log("Переключено на представление сетки файлов для директории: " + directoryPath);
        setStatusMessage("Просмотр файлов в директории: " + directoryPath);
    }

    private void switchToSystemHandler() {
        systemHandler.updateSystemView();
        fileBrowserScrollPane.setContent(systemHandler.getGridPane());
        fileBrowserScrollPane.setUserData(systemHandler);
        log("Переключено на обработчик системы");
        setStatusMessage("Просмотр системных файлов");
    }

    public void switchToTrashHandler() {
        trashHandler.updateTrashView();
        fileBrowserScrollPane.setContent(trashHandler.getGridPane());
        fileBrowserScrollPane.setUserData(trashHandler);
        log("Переключено на обработчик корзины");
        setStatusMessage("Просмотр корзины");
    }

    public FileGridView getFileGridView() {
        return fileGridView;
    }

    public FileTreeTable getFileTreeTable() {
        return fileTreeTable;
    }

    public File getRootDirectory() {
        return rootDirectory;
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

    public void updateTrashLabel() {
        Path trashPath = Paths.get("src/main/trash");
        try (Stream<Path> elements = Files.walk(trashPath)) {
            long elementCount = elements.filter(Files::isRegularFile).count();
            trashLabel.setText(elementCount + " элемент" + getCorrectRussianEnding(elementCount));
            trashLabel.setStyle("-fx-text-fill: #83888B");
        } catch (IOException e) {
            e.printStackTrace();
            trashLabel.setText("Ошибка чтения файлов");
        }
    }

    public String getCorrectRussianEnding(long count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return "а";
        } else {
            return "ов";
        }
    }

    private void handleInstallation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите директорию для установки");
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
            try {
                setupDirectories(selectedDirectory.toPath());
                cloneGitHubRepo(selectedDirectory.toPath());
                installComponents(selectedDirectory.toPath());
                Files.createFile(Paths.get(selectedDirectory.toPath().toString(), INSTALL_FLAG));
                setStatusMessage("Установка завершена. Перезапустите приложение.");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                setStatusMessage("Ошибка установки: " + e.getMessage());
            }
        } else {
            setStatusMessage("Директория не выбрана.");
        }
    }

    private void setupDirectories(Path installPath) throws IOException {
        Files.createDirectories(installPath.resolve(HOME_DIR));
        Files.createDirectories(installPath.resolve(TRASH_DIR));
        Files.createDirectories(installPath.resolve(FONTS_DIR));
        Files.createDirectories(installPath.resolve(JAVA_FX_LIB));
        Files.createDirectories(installPath.resolve(TEMP_DIR));
    }

    private void cloneGitHubRepo(Path installPath) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("git", "clone", GITHUB_REPO_URL, installPath.resolve(TEMP_DIR).toString());
        builder.directory(new File(System.getProperty("user.home")));
        Process process = builder.start();
        process.waitFor();
    }

    private void installComponents(Path installPath) throws IOException {
        Files.move(installPath.resolve(TEMP_DIR).resolve("path/to/javafx"), installPath.resolve(JAVA_FX_LIB));
        Files.move(installPath.resolve(TEMP_DIR).resolve("path/to/fonts"), installPath.resolve(FONTS_DIR));
        deleteDirectory(installPath.resolve(TEMP_DIR).toFile());

        createDesktopEntry(installPath);
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    private void createDesktopEntry(Path installPath) throws IOException {
        String desktopEntry = "[Desktop Entry]\n" +
                "Version=1.0\n" +
                "Name=SuperApp\n" +
                "Exec=java -jar " + installPath.resolve("build/libs/SuperApp.jar").toString() + "\n" +
                "Icon=" + installPath.resolve("icon.png").toString() + "\n" +
                "Type=Application\n" +
                "Categories=Utility;\n";

        Path desktopEntryPath = Paths.get(System.getProperty("user.home"), ".local/share/applications/SuperApp.desktop");
        Files.write(desktopEntryPath, desktopEntry.getBytes());
    }

    public void setStatusMessage(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        } else {
            System.err.println("statusLabel is not initialized");
        }
    }
}
