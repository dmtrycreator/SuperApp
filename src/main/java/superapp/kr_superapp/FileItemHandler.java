package superapp.kr_superapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileItemHandler {
    private final Image folderImage;
    private final Image fileImage;
    private final List<Path> selectedFiles;
    private final GridViewManager gridViewManager;

    public FileItemHandler(Image folderImage, Image fileImage, GridViewManager gridViewManager, List<Path> selectedFiles) {
        this.folderImage = folderImage;
        this.fileImage = fileImage;
        this.gridViewManager = gridViewManager;
        this.selectedFiles = selectedFiles;
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

        contextMenu.getItems().addAll(openItem, openNewWindowItem, cutItem, copyItem, pasteItem, renameItem, deleteItem, propertiesItem);

        vbox.setOnContextMenuRequested(event -> contextMenu.show(vbox, event.getScreenX(), event.getScreenY()));

        openItem.setOnAction(event -> {
            if (Files.isDirectory(path)) {
                if (Files.exists(path)) {
                    gridViewManager.updateGridView(path.toString());
                    String relativePath = path.toString().replace(GridViewManager.getRootDirectory().getPath(), "home");
                    FolderController.getInstance().updateDirectoryHBox(relativePath);
                    FolderController.getInstance().setStatusMessage("Открыта директория: " + path);
                } else {
                    System.out.println("Директория не найдена: " + path);
                    FolderController.getInstance().setStatusMessage("Директория не найдена: " + path);
                }
            }
        });

        openNewWindowItem.setOnAction(event -> {
            if (Files.isDirectory(path)) {
                if (Files.exists(path)) {
                    FolderController.openFolderWindow(path.toString());
                    FolderController.getInstance().setStatusMessage("Открыта новая папка для директории: " + path);
                } else {
                    System.out.println("Directory does not exist: " + path);
                    FolderController.getInstance().setStatusMessage("Директория не существует: " + path);
                }
            }
        });

        propertiesItem.setOnAction(event -> {
            try {
                FileGridView.openFileInfoWindow(path);
                FolderController.getInstance().setStatusMessage("Показаны свойства для: " + path);
            } catch (IOException e) {
                e.printStackTrace();
                FolderController.getInstance().setStatusMessage("Ошибка при показе свойств: " + e.getMessage());
            }
        });

        cutItem.setOnAction(event -> {
            gridViewManager.cutFile(path);
            FolderController.getInstance().setStatusMessage("Вырезан файл: " + path);
        });
        copyItem.setOnAction(event -> {
            gridViewManager.copyFile(path);
            FolderController.getInstance().setStatusMessage("Скопирован файл: " + path);
        });
        pasteItem.setOnAction(event -> {
            gridViewManager.pasteFile();
            FolderController.getInstance().setStatusMessage("Вставлен файл");
        });

        deleteItem.setOnAction(event -> {
            try {
                TrashHandler.moveToTrash(path);
                gridViewManager.updateGridView(path.getParent().toString());
                gridViewManager.updateTreeItems(GridViewManager.getRootDirectory().getPath());
                Controller.getInstance().updateTrashLabel();
                FolderController.getInstance().setStatusMessage("Удален файл: " + path);
            } catch (Exception e) {
                e.printStackTrace();
                FolderController.getInstance().setStatusMessage("Ошибка при удалении файла: " + e.getMessage());
            }
        });

        renameItem.setOnAction(event -> {
            gridViewManager.showRenameOverlay(path);
            FolderController.getInstance().setStatusMessage("Переименован файл: " + path);
        });

        DragAndDropUtil.setupDragAndDropSource(vbox, path);
        DragAndDropUtil.setupDragAndDropTarget(vbox, sourcePath -> {
            Path targetPath = path.resolve(sourcePath.getFileName());
            try {
                Files.move(sourcePath, targetPath);
                gridViewManager.updateGridView(gridViewManager.getCurrentDirectory());
                gridViewManager.updateTreeItems(GridViewManager.getRootDirectory().getPath());
                Controller.getInstance().updateTrashLabel();
                FolderController.getInstance().setStatusMessage("Перемещен файл с " + sourcePath + " на " + targetPath);
            } catch (IOException e) {
                e.printStackTrace();
                FolderController.getInstance().setStatusMessage("Ошибка при перемещении файла: " + e.getMessage());
            }
        });

        return vbox;
    }

    public void handleFileItemClick(MouseEvent event, Path path, VBox vbox) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (event.isControlDown()) {
                toggleSelection(path, vbox);
            } else {
                gridViewManager.clearSelection();
                gridViewManager.selectItem(path, vbox);
            }

            if (event.getClickCount() == 2) {
                if (Files.isDirectory(path)) {
                    gridViewManager.updateGridView(path.toString());
                    String relativePath = path.toString().replace(GridViewManager.getRootDirectory().getPath(), "home");
                    FolderController.getInstance().updateDirectoryHBox(relativePath);
                    FolderController.getInstance().setStatusMessage("Открыта директория: " + path);
                }
            }
        }
    }

    private void toggleSelection(Path path, VBox vbox) {
        if (selectedFiles.contains(path)) {
            selectedFiles.remove(path);
            vbox.setStyle("-fx-background-color: transparent;");
        } else {
            selectedFiles.add(path);
            vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;");
        }
    }
}
