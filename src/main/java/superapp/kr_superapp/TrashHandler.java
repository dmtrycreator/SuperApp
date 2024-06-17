package superapp.kr_superapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

/**
 * Класс TrashHandler отвечает за управление содержимым корзины,
 * включая перемещение файлов в корзину и удаление файлов навсегда.
 *
 * The TrashHandler class is responsible for managing the contents of the trash,
 * including moving files to the trash and permanently deleting files.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class TrashHandler {
    public static final String TRASH_DIRECTORY = "src/main/trash";

    private ScrollPane scrollPane;
    private StackPane stackMain;
    private GridPane gridPane;
    private final Image folderImage = new Image(getClass().getResourceAsStream("icons/Folder_trash.png"));
    private final Image fileImage = new Image(getClass().getResourceAsStream("icons/File_trash.png"));

    /**
     * Конструктор для создания и инициализации TrashHandler.
     *
     * Constructor to create and initialize the TrashHandler.
     */
    public TrashHandler(ScrollPane scrollPane, StackPane stackMain) {
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
        log("TrashHandler initialized / TrashHandler инициализирован");
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public void updateTrashView() {
        updateView(TRASH_DIRECTORY);
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

    public static void moveToTrash(Path source) throws Exception {
        Path target = Paths.get(TRASH_DIRECTORY).resolve(source.getFileName());
        Files.move(source, target);
        log("File moved to trash: " + source.toString() + " / Файл перемещен в корзину: " + source.toString());
    }

    public static void deleteForever(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                for (Path entry : entries.collect(Collectors.toList())) {
                    deleteForever(entry);
                }
            }
        }
        Files.delete(path);
        Controller.getInstance().updateTrashLabel();
        log("File permanently deleted: " + path.toString() + " / Файл удален навсегда: " + path.toString());
    }

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

        VBox vbox = new VBox(imageView, label);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefWidth(74);
        vbox.setMaxWidth(74);
        vbox.setPadding(new Insets(8, 5, 10, 5));
        label.setMaxWidth(64);
        label.setWrapText(false);

        Tooltip tooltip = new Tooltip(path.toString());
        Tooltip.install(vbox, tooltip);

        vbox.setOnMouseEntered(event -> vbox.setStyle("-fx-background-color: #3E3434; -fx-background-radius: 5px;"));
        vbox.setOnMouseExited(event -> vbox.setStyle("-fx-background-color: transparent;"));

        vbox.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && Files.isDirectory(path)) {
                updateView(path.toString());
                log("Директория открыта: " + path.toString());
            }
            vbox.setStyle("-fx-background-color: #3E3434; -fx-background-radius: 5px;");
        });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem openItem = new MenuItem("Открыть");
        MenuItem propertiesItem = new MenuItem("Свойства");
        MenuItem deleteForeverItem = new MenuItem("Удалить навсегда");
        deleteForeverItem.getStyleClass().add("menu-item-delete");
        propertiesItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

        contextMenu.getItems().addAll(openItem, propertiesItem, deleteForeverItem);

        vbox.setOnContextMenuRequested(event -> contextMenu.show(vbox, event.getScreenX(), event.getScreenY()));

        propertiesItem.setOnAction(event -> {
            try {
                FileGridView.openFileInfoWindow(path);
            } catch (IOException e) {
                e.printStackTrace();
                log("Error opening file info window for: " + path.toString() + " / Ошибка открытия окна информации о файле для: " + path.toString());
            }
        });

        openItem.setOnAction(event -> {
            if (Files.isDirectory(path)) {
                updateView(path.toString());
            }
        });

        deleteForeverItem.setOnAction(event -> {
            try {
                deleteForever(path);
                updateTrashView();
            } catch (IOException e) {
                e.printStackTrace();
                log("Error deleting file permanently: " + path.toString() + " / Ошибка удаления файла: " + path.toString());
                e.printStackTrace();
                log("Error deleting file permanently: " + path.toString() + " / Ошибка удаления файла: " + path.toString());
            }
        });

        return vbox;
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
