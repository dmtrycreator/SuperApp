package superapp.kr_superapp;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Класс FolderController управляет представлением папок в приложении.
 * Он предоставляет методы для отображения и навигации по папкам, а также для обработки операций перетаскивания.
 *
 * The FolderController class manages the folder view in the application.
 * It provides methods for displaying and navigating through folders, as well as handling drag-and-drop operations.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FolderController {

    private static FolderController instance;
    @FXML
    private HBox directory;

    @FXML
    private ScrollPane fileBrowserScrollPane;

    @FXML
    private TreeTableView<FileItem> fileBrowserTreeView;

    @FXML
    private Label folderALabel;

    @FXML
    private Label homeDirectoryLabel;

    @FXML
    private Button mainButton;

    @FXML
    private Pane main_pane;

    @FXML
    private VBox main_vbox;

    @FXML
    private Pane menu_pane;

    @FXML
    private Label nestedFoldersLabel;

    @FXML
    private Label noFilesLabel;

    @FXML
    private Pane right_pane;

    @FXML
    private Label searchLabel;

    @FXML
    private Pane searchPane;

    @FXML
    private StackPane stackMain;

    @FXML
    private Label statusLabel;

    @FXML
    private Pane statusPane;

    @FXML
    private HBox status_bar;

    @FXML
    private Label trashLabel;

    @FXML
    private Pane trashPane;

    @FXML
    private HBox vbox_menu_1;

    @FXML
    private Pane window;

    private GridViewManager gridViewManager;
    private static final File rootDirectory = new File("src/main/home");

    /**
     * Возвращает текущий экземпляр FolderController.
     *
     * Returns the current instance of FolderController.
     *
     * @return текущий экземпляр FolderController / the current instance of FolderController
     */
    public static FolderController getInstance() {
        return instance;
    }

    /**
     * Устанавливает текущий экземпляр FolderController.
     *
     * Sets the current instance of FolderController.
     *
     * @param instance экземпляр FolderController / the instance of FolderController
     */
    public static void setInstance(FolderController instance) {
        FolderController.instance = instance;
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
        instance = this;
        gridViewManager = new GridViewManager(fileBrowserScrollPane, stackMain);
        setupTreeView();
        updateViews(rootDirectory.getPath());
        applyStyles();
        enableDragAndDrop();

        fileBrowserTreeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<FileItem> selectedItem = fileBrowserTreeView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String originalPath = selectedItem.getValue().getAbsolutePath();
                    String rootDirPath = Controller.getInstance().getRootDirectory().getAbsolutePath();
                    String path = originalPath.replace(rootDirPath, "home");
                    FolderController.getInstance().handleDirectoryChange("src/main/" + path);
                }
            }
        });

        searchPane.setOnMouseClicked(event -> {
            showSearchOverlay();
            log("\"Поиск\" нажат");
            setStatusMessage("Поиск запущен");
        });
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
                performSearch(keywords);
            }
            stackMain.getChildren().removeAll(background, searchOverlay);
            log("Поиск выполнен с ключевыми словами: " + keywords);
        });

        searchOverlay.getChildren().addAll(searchField, searchButton);
        stackMain.getChildren().addAll(background, searchOverlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, searchOverlay));
        log("Оверлей поиска показан");
    }

    /**
     * Применяет стили к компонентам.
     *
     * Applies styles to the components.
     */
    private void applyStyles() {
        fileBrowserTreeView.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        fileBrowserScrollPane.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    }

    /**
     * Открывает новое окно для указанной директории.
     *
     * Opens a new window for the specified directory.
     *
     * @param directoryPath путь к директории / the path to the directory
     */
    public static void openFolderWindow(String directoryPath) {
        try {
            FXMLLoader loader = new FXMLLoader(FolderController.class.getResource("Folder.fxml"));
            Parent root = loader.load();
            FolderController controller = loader.getController();
            controller.updateViews(directoryPath);

            Stage stage = new Stage();
            stage.setTitle(directoryPath);
            stage.setScene(new Scene(root));
            stage.show();
            log("Folder window opened for directory: " + directoryPath + " / Окно папки открыто для директории: " + directoryPath);
        } catch (IOException e) {
            e.printStackTrace();
            log("Error opening folder window: " + e.getMessage() + " / Ошибка при открытии окна папки: " + e.getMessage());
        }
    }

    /**
     * Обновляет представления для указанной директории.
     *
     * Updates the views for the specified directory.
     *
     * @param directoryPath путь к директории / path to the directory
     */
    private void updateViews(String directoryPath) {
        gridViewManager.updateGridView(directoryPath);
        updateDirectoryHBox(directoryPath);
    }

    /**
     * Настраивает дерево файлов.
     *
     * Sets up the file tree.
     */
    private void setupTreeView() {
        TreeTableColumn<FileItem, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue().getName()));
        nameColumn.setCellFactory(param -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    TreeItem<FileItem> treeItem = getTreeTableRow().getTreeItem();
                    if (treeItem != null) {
                        FileItem fileItem = treeItem.getValue();
                        Image icon = fileItem.getType().equals("Folder") ? GridViewManager.getFolderImage() : GridViewManager.getFileImage();
                        ImageView iconView = new ImageView(icon);
                        iconView.setFitHeight(16);
                        iconView.setFitWidth(16);
                        setText(item);
                        setGraphic(iconView);
                    }
                }
            }
        });

        nameColumn.prefWidthProperty().bind(fileBrowserTreeView.widthProperty().multiply(0.9));
        fileBrowserTreeView.getColumns().addAll(nameColumn);
        updateTreeItems(rootDirectory.getPath());
    }

    /**
     * Выполняет поиск файлов по ключевым словам.
     *
     * Performs a search for files by keywords.
     *
     * @param keywords ключевые слова для поиска / keywords for the search
     */
    public void performSearch(String keywords) {
        try {
            Path directoryPath = Paths.get(gridViewManager.getCurrentDirectory());
            List<File> resultFiles = Files.walk(directoryPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().contains(keywords.toLowerCase()))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            gridViewManager.updateGridViewWithFiles(resultFiles);
            updateDirectoryHBox(directoryPath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обновляет элементы дерева файлов для указанной директории.
     *
     * Updates the file tree items for the specified directory.
     *
     * @param directoryPath путь к директории / path to the directory
     */
    private void updateTreeItems(String directoryPath) {
        TreeItem<FileItem> root = createNode(Paths.get(directoryPath));
        fileBrowserTreeView.setRoot(root);
        root.setExpanded(true);
        root.setValue(new FileItem("home", 0, "Folder", rootDirectory.getAbsolutePath()));
    }

    /**
     * Создает узел дерева для указанного пути.
     *
     * Creates a tree node for the specified path.
     *
     * @param path путь к файлу или папке / path to the file or folder
     * @return узел TreeItem / TreeItem node
     */
    private TreeItem<FileItem> createNode(Path path) {
        FileItem fileItem = new FileItem(path.getFileName().toString(), path.toFile().length(), Files.isDirectory(path) ? "Folder" : "File", path.toAbsolutePath().toString());
        TreeItem<FileItem> node = new TreeItem<>(fileItem);

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
                for (Path p : directoryStream) {
                    node.getChildren().add(createNode(p));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return node;
    }

    /**
     * Включает поддержку drag and drop для дерева файлов.
     *
     * Enables drag and drop support for the file tree.
     */
    private void enableDragAndDrop() {
        fileBrowserTreeView.setOnDragDetected(event -> {
            if (!gridViewManager.getSelectedFiles().isEmpty()) {
                Dragboard db = fileBrowserTreeView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putFiles(gridViewManager.getSelectedFiles().stream().map(Path::toFile).collect(Collectors.toList()));
                db.setContent(content);
                event.consume();
                log("Drag detected on tree view / Обнаружено перетаскивание на представлении дерева");
            }
        });

        fileBrowserTreeView.setOnDragOver(event -> {
            if (event.getGestureSource() != fileBrowserTreeView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
            log("Drag over tree view / Перетаскивание над представлением дерева");
        });

        fileBrowserTreeView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                TreeItem<FileItem> selectedItem = fileBrowserTreeView.getSelectionModel().getSelectedItem();
                Path targetPath = selectedItem == null ? Paths.get(gridViewManager.getCurrentDirectory()) : Paths.get(selectedItem.getValue().getAbsolutePath());

                for (File file : files) {
                    Path sourcePath = file.toPath();
                    Path destinationPath = targetPath.resolve(file.getName());
                    try {
                        Files.move(sourcePath, destinationPath);
                        gridViewManager.updateGridView(gridViewManager.getCurrentDirectory());
                        updateTreeItems(rootDirectory.getPath());
                        log("File moved from " + sourcePath + " to " + destinationPath + " / Файл перемещен с " + sourcePath + " на " + destinationPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log("Error moving file: " + e.getMessage() + " / Ошибка перемещения файла: " + e.getMessage());
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        trashPane.setOnDragOver(event -> {
            if (event.getGestureSource() != trashPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
            log("Drag over trash pane / Перетаскивание над панелью корзины");
        });

        trashPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                for (File file : files) {
                    try {
                        TrashHandler.moveToTrash(file.toPath());
                        updateTreeItems(rootDirectory.getPath());
                        log("File moved to trash: " + file.getPath() + " / Файл перемещен в корзину: " + file.getPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        log("Error moving file to trash: " + e.getMessage() + " / Ошибка перемещения файла в корзину: " + e.getMessage());
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Обрабатывает изменение директории.
     *
     * Handles the directory change.
     *
     * @param directoryPath путь к новой директории / new directory path
     */
    public void handleDirectoryChange(String directoryPath) {
        updateViews(directoryPath);
        updateDirectoryHBox(directoryPath);
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

        int homeIndex = directoryPath.indexOf("home");
        if (homeIndex != -1) {
            String relativePath = directoryPath.substring(homeIndex + "home".length()).trim();
            if (!relativePath.isEmpty() && relativePath.startsWith(File.separator)) {
                relativePath = relativePath.substring(1);
            }

            Label homeLabel = new Label("home");
            homeLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
            homeLabel.setOnMouseClicked(event -> navigateToDirectory("src/main/home"));
            directory.getChildren().add(homeLabel);

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
        } else {
            // Если "home" не найден, добавляем полный путь
            Label pathLabel = new Label(directoryPath);
            pathLabel.setStyle("-fx-text-fill: #83888B; -fx-font-size: 16; -fx-font-family: Inter; -fx-font-weight: 500;");
            directory.getChildren().add(pathLabel);
        }

        log("Directory updated to: " + directoryPath + " / Директория обновлена на: " + directoryPath);
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
     * Навигация к указанной директории.
     *
     * Navigates to the specified directory.
     *
     * @param directoryPath путь к директории / path to the directory
     */
    private void navigateToDirectory(String directoryPath) {
        updateViews(directoryPath);
        updateDirectoryHBox(directoryPath);
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
}
