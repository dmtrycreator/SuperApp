package superapp.kr_superapp;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Класс FileTreeTable управляет представлением дерева файлов и папок в приложении.
 * Он предоставляет методы для настройки и обновления элементов дерева.
 *
 * The FileTreeTable class manages the tree view representation of files and folders in the application.
 * It provides methods for setting up and updating tree items.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FileTreeTable {
    private final TreeTableView<FileItem> treeTableView;
    private static final Image folderImage = new Image(FileTreeTable.class.getResourceAsStream("icons/Folder.png"));
    private static final Image fileImage = new Image(FileTreeTable.class.getResourceAsStream("icons/File.png"));
    private static final Image storageImage = new Image(FileTreeTable.class.getResourceAsStream("icons/Storage.png"));
    private static final File rootDirectory = new File("src/main/home");
    private static final Path mediaDirectory = Paths.get("/media", System.getProperty("user.name"));

    /**
     * Конструктор FileTreeTable инициализирует дерево файлов с заданным представлением TreeTableView.
     *
     * The FileTreeTable constructor initializes the file tree with the given TreeTableView.
     *
     * @param treeTableView представление TreeTableView для отображения дерева файлов / TreeTableView for displaying the file tree
     */
    public FileTreeTable(TreeTableView<FileItem> treeTableView) {
        this.treeTableView = treeTableView;
        setupColumns();
        updateTreeItems(rootDirectory.getPath());
    }

    /**
     * Настраивает колонки дерева файлов.
     *
     * Sets up the file tree columns.
     */
    private void setupColumns() {
        TreeTableColumn<FileItem, String> nameColumn = new TreeTableColumn<>("Name");
        TreeTableColumn<FileItem, String> typeColumn = new TreeTableColumn<>("Type");
        TreeTableColumn<FileItem, String> sizeColumn = new TreeTableColumn<>("Size");

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
                        Image icon;
                        if (fileItem.getAbsolutePath().startsWith(mediaDirectory.toString())) {
                            icon = storageImage;
                        } else {
                            icon = fileItem.getType().equals("Folder") ? folderImage : fileImage;
                        }
                        ImageView iconView = new ImageView(icon);
                        iconView.setFitHeight(16);
                        iconView.setFitWidth(16);
                        setText(item);
                        setGraphic(iconView);

                        Tooltip tooltip = new Tooltip(item);
                        Tooltip.install(this, tooltip);
                    }
                }
            }
        });

        typeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(translateType(param.getValue().getValue().getType())));
        setupCellFactory(typeColumn, true);

        sizeColumn.setCellValueFactory(param -> {
            FileItem item = param.getValue().getValue();
            setupCellFactory(sizeColumn, true);
            return new ReadOnlyObjectWrapper<>(item.getType().equals("File") ? formatSize(item.getSize()) : "");
        });

        nameColumn.prefWidthProperty().bind(treeTableView.widthProperty().multiply(0.6));
        typeColumn.prefWidthProperty().bind(treeTableView.widthProperty().multiply(0.16));
        sizeColumn.prefWidthProperty().bind(treeTableView.widthProperty().multiply(0.16));
        treeTableView.getColumns().addAll(nameColumn, typeColumn, sizeColumn);
    }

    /**
     * Настраивает фабрику ячеек для указанных колонок.
     *
     * Sets up the cell factory for the specified columns.
     *
     * @param column колонка для настройки / column to set up
     * @param grayText флаг для использования серого текста / flag for using gray text
     */
    private void setupCellFactory(TreeTableColumn<FileItem, String> column, boolean grayText) {
        column.setCellFactory(param -> new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setGraphic(null);
                if (grayText) {
                    setStyle("-fx-text-fill: #83888B; -fx-padding: 0 5");
                }
            }
        });
    }

    /**
     * Форматирует размер файла в удобочитаемую строку.
     *
     * Formats the file size into a readable string.
     *
     * @param size размер файла / file size
     * @return форматированный размер / formatted size
     */
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), pre);
    }

    /**
     * Переводит тип файла на русский язык.
     *
     * Translates the file type to Russian.
     *
     * @param type тип файла / file type
     * @return переведенный тип / translated type
     */
    private String translateType(String type) {
        switch (type) {
            case "Folder":
                return "Папка";
            case "File":
                return "Файл";
            default:
                return type;
        }
    }

    /**
     * Обновляет элементы дерева файлов для указанной директории.
     *
     * Updates the file tree items for the specified directory.
     *
     * @param directoryPath путь к директории / path to the directory
     */
    public void updateTreeItems(String directoryPath) {
        String relativePath = directoryPath.replace(rootDirectory.getPath(), "").trim();
        if (relativePath.startsWith(File.separator)) {
            relativePath = relativePath.substring(1);
        }
        TreeItem<FileItem> root = createNode(Paths.get(rootDirectory.getPath() + File.separator + relativePath));
        treeTableView.setRoot(root);
        root.setExpanded(true);
        root.setValue(new FileItem("home", 0, "Folder", rootDirectory.getAbsolutePath()));

        if (Files.exists(mediaDirectory) && Files.isDirectory(mediaDirectory)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(mediaDirectory)) {
                for (Path path : stream) {
                    TreeItem<FileItem> mediaNode = createNode(path);
                    root.getChildren().add(0, mediaNode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Возвращает представление дерева файлов.
     *
     * Returns the file tree view.
     *
     * @return представление TreeTableView / TreeTableView instance
     */
    public TreeTableView<FileItem> getFileBrowserTreeView() {
        return treeTableView;
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
        String displayName = path.getFileName().toString();
        FileItem fileItem = new FileItem(displayName, path.toFile().length(), Files.isDirectory(path) ? "Folder" : "File", path.toAbsolutePath().toString());
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
