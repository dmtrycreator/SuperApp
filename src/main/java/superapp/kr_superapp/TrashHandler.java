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
import javafx.scene.shape.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final Map<Path, Path> originalPaths = new HashMap<>();

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
        originalPaths.put(target, source);
        Files.move(source, target);
        log("File moved to trash: " + source.toString() + " / Файл перемещен в корзину: " + source.toString());
    }

    public static void restoreFromTrash(Path path) throws IOException {
        Path target = originalPaths.remove(path);
        if (target != null) {
            Files.move(path, target);
            Controller.getInstance().updateTrashLabel();
            log("File restored from trash: " + path.toString() + " / Файл восстановлен из корзины: " + path.toString());
        }
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
        MenuItem restoreItem = new MenuItem("Восстановить");
        MenuItem propertiesItem = new MenuItem("Свойства");
        MenuItem deleteForeverItem = new MenuItem("Удалить навсегда");
        deleteForeverItem.getStyleClass().add("menu-item-delete");
        propertiesItem.setAccelerator(new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN));

        contextMenu.getItems().addAll(restoreItem, propertiesItem, deleteForeverItem);

        vbox.setOnContextMenuRequested(event -> contextMenu.show(vbox, event.getScreenX(), event.getScreenY()));

        propertiesItem.setOnAction(event -> {
            try {
                FileGridView.openFileInfoWindow(path);
            } catch (IOException e) {
                e.printStackTrace();
                log("Error opening file info window for: " + path.toString() + " / Ошибка открытия окна информации о файле для: " + path.toString());
            }
        });

        restoreItem.setOnAction(event -> {
            try {
                restoreFromTrash(path);
                updateTrashView();
            } catch (IOException e) {
                e.printStackTrace();
                log("Error restoring file: " + path.toString() + " / Ошибка восстановления файла: " + path.toString());
            }
        });

        deleteForeverItem.setOnAction(event -> {
            showDeleteConfirmationOverlay(path);
        });

        return vbox;
    }

    private void showDeleteConfirmationOverlay(Path path) {
        HBox deleteOverlay = new HBox();
        deleteOverlay.setAlignment(Pos.CENTER);
        deleteOverlay.setMaxHeight(138);
        deleteOverlay.setMaxWidth(538);
        deleteOverlay.setSpacing(10);
        deleteOverlay.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 18; -fx-padding: 30 40 30 40;");

        Label confirmationLabel = new Label("Вы действительно хотите удалить файл навсегда?");
        confirmationLabel.setTextFill(Color.web("#F5FAFF"));
        confirmationLabel.setFont(new Font("Inter Medium", 14));

        Button deleteButton = new Button("Удалить");
        deleteButton.setPrefHeight(46);
        deleteButton.setPrefWidth(107);
        deleteButton.setStyle("-fx-background-color: #E94545; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");

        Button cancelButton = new Button("Отмена");
        cancelButton.setPrefHeight(46);
        cancelButton.setPrefWidth(107);
        cancelButton.setStyle("-fx-background-color: #469EE9; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");

        Rectangle background = new Rectangle(
                stackMain.getScene().getWidth(),
                stackMain.getScene().getHeight(),
                Color.rgb(0, 0, 0, 0.7)
        );

        deleteButton.setOnAction(event -> {
            try {
                deleteForever(path);
                updateTrashView();
                stackMain.getChildren().removeAll(background, deleteOverlay);
            } catch (IOException e) {
                e.printStackTrace();
                log("Error deleting file permanently: " + path.toString() + " / Ошибка удаления файла: " + path.toString());
            }
        });

        cancelButton.setOnAction(event -> {
            stackMain.getChildren().removeAll(background, deleteOverlay);
        });

        deleteOverlay.getChildren().addAll(confirmationLabel, deleteButton, cancelButton);
        stackMain.getChildren().addAll(background, deleteOverlay);

        background.setOnMouseClicked(event -> stackMain.getChildren().removeAll(background, deleteOverlay));
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
