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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс для отображения и управления файлами и папками в виде сетки.
 * Включает в себя функционал для обновления представления, создания элементов файлов, обработки кликов,
 * управления выделением, а также поддержки операций drag and drop и копирования/вставки.
 *
 * Class for displaying and managing files and folders in a grid view.
 * Includes functionality for updating the view, creating file items, handling clicks,
 * managing selection, and supporting drag and drop and copy/paste operations.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FileGridView {
    private ScrollPane scrollPane;
    private GridPane gridPane;
    private StackPane stackMain;
    private String currentDirectory;
    private final Image folderImage = new Image(getClass().getResourceAsStream("icons/Folder.png"));
    private final Image fileImage = new Image(getClass().getResourceAsStream("icons/File.png"));
    private List<Path> selectedFiles = new ArrayList<>();
    private Path lastSelectedPath = null;
    private Path clipboardFile = null;
    private boolean isCutOperation = false;

    /**
     * Конструктор класса FileGridView.
     *
     * Constructor of the FileGridView class.
     *
     * @param scrollPane Прокручиваемая панель / Scroll pane
     * @param stackMain Основная стековая панель / Main stack pane
     */
    public FileGridView(ScrollPane scrollPane, StackPane stackMain) {
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
        enableDragAndDrop();
        enableCopyPaste();
    }

    /**
     * Обновляет представление сетки для указанной директории.
     *
     * Updates the grid view for the specified directory.
     *
     * @param directoryPath Путь к директории / Path to the directory
     */
    public void updateGridView(String directoryPath) {
        currentDirectory = directoryPath;
        writeCurrentDirectoryToSharedMemory();
        gridPane.getChildren().clear();
        try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Обрабатывает клик по элементу файла.
     *
     * Handles a click on a file item.
     *
     * @param event Событие клика / Mouse event
     * @param path Путь к файлу или папке / Path to the file or folder
     * @param vbox Контейнер элемента / VBox container of the item
     */
    private void handleFileItemClick(MouseEvent event, Path path, VBox vbox) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (event.isControlDown()) {
                toggleSelection(path, vbox);
            } else {
                clearSelection();
                selectItem(path, vbox);
            }
            lastSelectedPath = path;

            if (event.getClickCount() == 2) {
                if (Files.isDirectory(path)) {
                    updateGridView(path.toString());
                    String correctedPath = path.toString().startsWith("/media/") ? path.toString() : "home" + path.toString().replace(Controller.getInstance().getRootDirectory().getPath(), "");
                    Controller.getInstance().updateDirectoryHBox(correctedPath);
                }
            }
        }
    }

    /**
     * Создает элемент файла для отображения в сетке.
     *
     * Creates a file item for display in the grid.
     *
     * @param path Путь к файлу или папке / Path to the file or folder
     * @return Контейнер VBox с элементом файла / VBox container with the file item
     */
    public VBox createFileItem(Path path) {
        String itemName = path.getFileName().toString();
        ImageView imageView = new ImageView(Files.isDirectory(path) ? folderImage : fileImage);
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);

        Label label = new Label(itemName, imageView);
        label.setFont(new Font("Inter Medium", 12));
        label.setTextFill(Color.web("#F5FAFF"));
        label.setContentDisplay(ContentDisplay.TOP);
        label.setAlignment(Pos.CENTER);

        Tooltip tooltip = new Tooltip(itemName);
        Tooltip.install(label, tooltip);

        VBox vbox = new VBox(imageView, label);
        vbox.setAlignment(Pos.CENTER);
        vbox.prefWidth(74);
        vbox.setMaxWidth(74);
        vbox.setPadding(new Insets(8, 5, 10, 5));
        label.setMaxWidth(64);
        label.setWrapText(false);

        vbox.setOnMouseEntered(event -> vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;"));
        vbox.setOnMouseExited(event -> {
            if (!selectedFiles.contains(path)) {
                vbox.setStyle("-fx-background-color: transparent;");
            }
        });

        vbox.setOnMouseClicked(event -> handleFileItemClick(event, path, vbox));

        ContextMenu contextMenu = new ContextMenu();
        MenuItem openItem = new MenuItem("Открыть");
        MenuItem openNewWindowItem = new MenuItem("Открыть в новом окне");
        MenuItem cutItem = new MenuItem("Вырезать");
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        MenuItem copyItem = new MenuItem("Копировать");
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        MenuItem pasteItem = new MenuItem("Вставить");
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
        MenuItem deleteItem = new MenuItem("Удалить");
        deleteItem.getStyleClass().add("menu-item-delete");
        MenuItem renameItem = new MenuItem("Переименовать");
        MenuItem propertiesItem = new MenuItem("Свойства");
        propertiesItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

        if (Files.isDirectory(path)) {
            contextMenu.getItems().addAll(openItem, openNewWindowItem, cutItem, copyItem, pasteItem, renameItem, deleteItem, propertiesItem);
        } else {
            contextMenu.getItems().addAll(cutItem, copyItem, pasteItem, renameItem, deleteItem, propertiesItem);
        }

        vbox.setOnContextMenuRequested(event -> contextMenu.show(vbox, event.getScreenX(), event.getScreenY()));

        openItem.setOnAction(event -> {
            if (Files.isDirectory(path)) {
                updateGridView(path.toString());
                Controller.getInstance().updateDirectoryHBox("home" + path.toString().replace(Controller.getInstance().getRootDirectory().getPath(), ""));
            }
        });

        openNewWindowItem.setOnAction(event -> {
            if (Files.isDirectory(path)) {
                try {
                    FileMappingHandler fileMappingHandler = new FileMappingHandler();
                    fileMappingHandler.writeData(path.toString().getBytes());
                    Controller.log("Текущая директория записана в общую память: " + path.toString());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    Controller.log("Ошибка записи текущей директории в общую память: " + e.getMessage());
                }
                FolderController.openFolderWindow(path.toString());
            }
        });


        propertiesItem.setOnAction(event -> {
            try {
                openFileInfoWindow(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        cutItem.setOnAction(event -> cutFile(path));
        copyItem.setOnAction(event -> copyFile(path));
        pasteItem.setOnAction(event -> pasteFile());
        deleteItem.setOnAction(event -> {
            try {
                TrashHandler.moveToTrash(path);
                updateGridView(path.getParent().toString());
                Controller.getInstance().getFileTreeTable().updateTreeItems(Controller.getInstance().getRootDirectory().getPath());
                Controller.getInstance().updateTrashLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        renameItem.setOnAction(event -> showRenameOverlay(path));

        DragAndDropUtil.setupDragAndDropSource(vbox, path);
        DragAndDropUtil.setupDragAndDropTarget(vbox, sourcePath -> {
            Path targetPath = path.resolve(sourcePath.getFileName());
            try {
                Files.move(sourcePath, targetPath);
                updateGridView(currentDirectory);
                Controller.getInstance().getFileTreeTable().updateTreeItems(Controller.getInstance().getRootDirectory().getPath());
                Controller.getInstance().updateTrashLabel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return vbox;
    }

    /**
     * Открывает окно информации о файле.
     *
     * Opens the file information window.
     *
     * @param filePath Путь к файлу / Path to the file
     * @throws IOException Если возникает ошибка ввода-вывода / If an I/O error occurs
     */
    public static void openFileInfoWindow(Path filePath) throws IOException {
        FXMLLoader loader = new FXMLLoader(FileGridView.class.getResource("FileInfo.fxml"));
        Parent root = loader.load();

        FileInfoController controller = loader.getController();
        controller.setFileInfo(filePath);

        Stage stage = new Stage();
        stage.setTitle("Информация о файле");
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Переключает состояние выделения элемента.
     *
     * Toggles the selection state of an item.
     *
     * @param path Путь к файлу или папке / Path to the file or folder
     * @param vbox Контейнер элемента / VBox container of the item
     */
    private void toggleSelection(Path path, VBox vbox) {
        if (selectedFiles.contains(path)) {
            selectedFiles.remove(path);
            vbox.setStyle("-fx-background-color: transparent;");
        } else {
            selectedFiles.add(path);
            vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;");
        }
    }

    /**
     * Выделяет элемент.
     *
     * Selects an item.
     *
     * @param path Путь к файлу или папке / Path to the file or folder
     * @param vbox Контейнер элемента / VBox container of the item
     */
    private void selectItem(Path path, VBox vbox) {
        selectedFiles.add(path);
        vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;");
    }

    /**
     * Снимает выделение со всех элементов.
     *
     * Clears the selection of all items.
     */
    private void clearSelection() {
        selectedFiles.clear();
        gridPane.getChildren().forEach(node -> node.setStyle("-fx-background-color: transparent;"));
    }

    /**
     * Включает поддержку drag and drop для сетки.
     *
     * Enables drag and drop support for the grid.
     */
    private void enableDragAndDrop() {
        gridPane.setOnDragDetected(event -> {
            if (!selectedFiles.isEmpty()) {
                Dragboard db = gridPane.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putFiles(selectedFiles.stream().map(Path::toFile).collect(Collectors.toList()));
                db.setContent(content);
                event.consume();
            }
        });

        gridPane.setOnDragOver(event -> {
            if (event.getGestureSource() != gridPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        gridPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                for (File file : files) {
                    Path sourcePath = file.toPath();
                    Path targetPath = Paths.get(currentDirectory).resolve(file.getName());
                    try {
                        Files.move(sourcePath, targetPath);
                        updateGridView(currentDirectory);
                        Controller.getInstance().getFileTreeTable().updateTreeItems(Controller.getInstance().getRootDirectory().getPath());
                        Controller.getInstance().updateTrashLabel(); // Добавляем вызов здесь
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Включает поддержку копирования и вставки.
     *
     * Enables copy and paste support.
     */
    private void enableCopyPaste() {
        stackMain.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                copyFile(selectedFiles.isEmpty() ? lastSelectedPath : selectedFiles.get(0));
            } else if (event.isControlDown() && event.getCode() == KeyCode.X) {
                cutFile(selectedFiles.isEmpty() ? lastSelectedPath : selectedFiles.get(0));
            } else if (event.isControlDown() && event.getCode() == KeyCode.V) {
                pasteFile();
            }
        });
    }

    /**
     * Копирует файл в буфер обмена.
     *
     * Copies a file to the clipboard.
     *
     * @param source Путь к исходному файлу / Path to the source file
     */
    public void copyFile(Path source) {
        clipboardFile = source;
        isCutOperation = false;
    }

    /**
     * Вырезает файл в буфер обмена.
     *
     * Cuts a file to the clipboard.
     *
     * @param source Путь к исходному файлу / Path to the source file
     */
    public void cutFile(Path source) {
        clipboardFile = source;
        isCutOperation = true;
    }

    /**
     * Вставляет файл из буфера обмена.
     *
     * Pastes a file from the clipboard.
     */
    public void pasteFile() {
        if (clipboardFile != null) {
            Path targetPath = Paths.get(currentDirectory).resolve(clipboardFile.getFileName());
            try {
                if (isCutOperation) {
                    Files.move(clipboardFile, targetPath);
                } else {
                    Files.copy(clipboardFile, targetPath);
                }
                updateGridView(currentDirectory);
                Controller.getInstance().getFileTreeTable().updateTreeItems(Controller.getInstance().getRootDirectory().getPath());
                Controller.getInstance().updateTrashLabel(); // Добавляем вызов здесь
                clipboardFile = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Возвращает текущую директорию.
     *
     * Returns the current directory.
     *
     * @return Текущая директория / Current directory
     */
    public String getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Возвращает сетку файлового представления.
     *
     * Returns the file grid pane.
     *
     * @return Сетка файлового представления / File grid pane
     */
    public GridPane getGridPane() {
        return gridPane;
    }

    /**
     * Возвращает список выделенных файлов.
     *
     * Returns the list of selected files.
     *
     * @return Список выделенных файлов / List of selected files
     */
    public List<Path> getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * Показывает окно переименования файла.
     *
     * Shows the rename file overlay.
     *
     * @param path Путь к файлу или папке / Path to the file or folder
     */
    private void showRenameOverlay(Path path) {
        HBox renameOverlay = new HBox();
        renameOverlay.setAlignment(Pos.CENTER);
        renameOverlay.setMaxHeight(138);
        renameOverlay.setMaxWidth(538);
        renameOverlay.setSpacing(10);
        renameOverlay.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 18; -fx-padding: 30 40 30 40;");

        TextField renameField = new TextField(path.getFileName().toString());
        renameField.setPrefWidth(300);
        renameField.setStyle("-fx-background-color: #272727; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12; -fx-padding: 16 20 16 20;");
        Button renameButton = new Button("Переименовать");
        renameButton.setPrefHeight(46);
        renameButton.setPrefWidth(150);
        renameButton.setStyle("-fx-background-color: #469EE9; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");

        Rectangle background = new Rectangle(
                stackMain.getScene().getWidth(),
                stackMain.getScene().getHeight(),
                Color.rgb(0, 0, 0, 0.7)
        );

        renameButton.setOnAction(event -> {
            String newName = renameField.getText();
            if (newName != null && !newName.trim().isEmpty()) {
                try {
                    Files.move(path, path.resolveSibling(newName));
                    updateGridView(currentDirectory);
                    Controller.getInstance().updateTrashLabel(); // Добавляем вызов здесь
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            stackMain.getChildren().removeAll(background, renameOverlay);
        });

        renameOverlay.getChildren().addAll(renameField, renameButton);
        stackMain.getChildren().addAll(background, renameOverlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, renameOverlay));
    }

    private List<Path> getPaths() {
        return Arrays.asList(new File(currentDirectory).listFiles())
                .stream()
                .map(File::toPath)
                .collect(Collectors.toList());
    }

    private void updateGridView(List<Path> paths) {
        gridPane.getChildren().clear();
        int index = 0;
        int columnCount = 7;
        for (Path path : paths) {
            VBox vbox = createFileItem(path);
            int row = index / columnCount;
            int column = index % columnCount;
            gridPane.add(vbox, column, row);
            index++;
        }
    }

    public StackPane getStackMain() {
        return stackMain;
    }

    public void writeCurrentDirectoryToSharedMemory() {
        try {
            FileMappingHandler fileMappingHandler = new FileMappingHandler();
            fileMappingHandler.writeData(currentDirectory.getBytes());
            Controller.log("Текущая директория записана в общую память: " + currentDirectory);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Controller.log("Ошибка записи текущей директории в общую память: " + e.getMessage());
        }
    }

}
