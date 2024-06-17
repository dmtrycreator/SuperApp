package superapp.kr_superapp;

import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Этот класс предназначен для обработки операций перетаскивания файлов и папок в приложении файлового менеджера.
 * Он управляет поведением Drag and Drop для элементов файловой системы, представленных в виде таблицы и сетки,
 * а также для корзины.

 * This class is designed to handle drag-and-drop operations for files and folders in the file manager application.
 * It manages the drag-and-drop behavior for file system elements presented in a table and grid view,
 * as well as for the trash bin.

 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class DragAndDropHandler {

    private final TreeTableView<FileItem> treeTableView;
    private final ScrollPane fileBrowserScrollPane;
    private final FileTreeTable fileTreeTable;
    private final FileGridView fileGridView;
    private final Pane trashPane;

    public DragAndDropHandler(TreeTableView<FileItem> treeTableView, ScrollPane fileBrowserScrollPane,
                              FileTreeTable fileTreeTable, FileGridView fileGridView, Pane trashPane) {
        this.treeTableView = treeTableView;
        this.fileBrowserScrollPane = fileBrowserScrollPane;
        this.fileTreeTable = fileTreeTable;
        this.fileGridView = fileGridView;
        this.trashPane = trashPane;
        enableDragAndDrop();
    }

    private void enableDragAndDrop() {
        treeTableView.setOnDragDetected(event -> {
            TreeItem<FileItem> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Dragboard db = treeTableView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                List<File> selectedFiles = treeTableView.getSelectionModel().getSelectedItems().stream()
                        .map(item -> new File(item.getValue().getAbsolutePath()))
                        .collect(Collectors.toList());
                content.putFiles(selectedFiles);
                db.setContent(content);
                SystemInfo.log("Drag detected for files: " + selectedFiles + " / Обнаружено перетаскивание файлов: " + selectedFiles);
                event.consume();
            }
        });

        treeTableView.setOnDragOver(event -> {
            if (event.getGestureSource() != treeTableView && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                SystemInfo.log("Drag over detected / Обнаружено перетаскивание");
            }
            event.consume();
        });

        treeTableView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                TreeItem<FileItem> targetItem = treeTableView.getSelectionModel().getSelectedItem();
                if (targetItem != null) {
                    Path targetPath = Paths.get(targetItem.getValue().getAbsolutePath());
                    for (File file : files) {
                        Path sourcePath = file.toPath();
                        Path destinationPath = targetPath.resolve(file.getName());
                        try {
                            Files.move(sourcePath, destinationPath);
                            fileTreeTable.updateTreeItems(targetPath.toString());
                            SystemInfo.log("File moved: " + sourcePath + " to " + destinationPath + " / Файл перемещен: " + sourcePath + " в " + destinationPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                            SystemInfo.log("Error moving file: " + e.getMessage() + " / Ошибка перемещения файла: " + e.getMessage());
                        }
                    }
                    success = true;
                }
            }
            event.setDropCompleted(success);
            SystemInfo.log("Drag drop completed with success: " + success + " / Перетаскивание завершено с успехом: " + success);
            event.consume();
        });

        treeTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<FileItem> selectedItem = treeTableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String originalPath = selectedItem.getValue().getAbsolutePath();
                    String rootDirPath = Controller.getInstance().getRootDirectory().getAbsolutePath();
                    String correctedPath;
                    if (originalPath.startsWith("/media/")) {
                        correctedPath = originalPath;
                    } else {
                        correctedPath = originalPath.replace(rootDirPath, "home");
                    }
                    if (Files.isDirectory(Paths.get(originalPath))) {
                        Controller.getInstance().handleDirectoryChange(correctedPath);
                        SystemInfo.log("Директория открыта: " + originalPath);
                    }
                }
            }
        });



        trashPane.setOnDragOver(event -> {
            if (event.getGestureSource() != trashPane && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
                SystemInfo.log("Trash drag over detected / Обнаружено перетаскивание на корзину");
            }
            Controller.getInstance().updateTrashLabel();
            event.consume();
        });

        trashPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                for (File file : files) {
                    try {
                        TrashHandler.moveToTrash(file.toPath());
                        SystemInfo.log("File deleted: " + file.getAbsolutePath() + " / Файл удален: " + file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        SystemInfo.log("Error deleting file: " + e.getMessage() + " / Ошибка удаления файла: " + e.getMessage());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            Controller.getInstance().updateTrashLabel();
            SystemInfo.log("Trash drag drop completed with success: " + success + " / Перетаскивание на корзину завершено с успехом: " + success);
            event.consume();

        });
    }
}
