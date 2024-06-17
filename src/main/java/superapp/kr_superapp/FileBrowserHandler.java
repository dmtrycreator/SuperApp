package superapp.kr_superapp;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Класс для обработки действий пользователя с элементами файлового браузера.
 * Включает в себя логику обработки кликов на файлы и папки, а также управление выделением элементов.

 * Class for handling user actions with file browser items.
 * Includes logic for handling clicks on files and folders, as well as managing item selection.

 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FileBrowserHandler {

    private FileTreeTable fileTreeTable;
    private FileGridView fileGridView;
    private Path lastSelectedPath;

    /**
     * Конструктор класса FileBrowserHandler.

     * Constructor of the FileBrowserHandler class.

     * @param fileTreeTable Таблица файлового дерева / File tree table
     * @param fileGridView Сетка файлового браузера / File grid view
     */
    public FileBrowserHandler(FileTreeTable fileTreeTable, FileGridView fileGridView) {
        this.fileTreeTable = fileTreeTable;
        this.fileGridView = fileGridView;
    }

    /**
     * Обрабатывает клик по элементу файлового браузера.

     * Handles a click on a file browser item.

     * @param event Событие клика / Mouse event
     * @param path Путь к файлу или папке / Path to the file or folder
     * @param vbox Контейнер элемента / VBox container of the item
     */
    public void handleFileItemClick(MouseEvent event, Path path, VBox vbox) {
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
                    fileGridView.updateGridView(path.toString());
                    Controller.getInstance().updateDirectoryHBox(path.toString().replace("src/main/home", ""));
                }
            }
        }
    }

    /**
     * Переключает состояние выделения элемента.

     * Toggles the selection state of an item.

     * @param path Путь к файлу или папке / Path to the file or folder
     * @param vbox Контейнер элемента / VBox container of the item
     */
    private void toggleSelection(Path path, VBox vbox) {
        if (fileGridView.getSelectedFiles().contains(path)) {
            fileGridView.getSelectedFiles().remove(path);
            vbox.setStyle("-fx-background-color: transparent;");
        } else {
            fileGridView.getSelectedFiles().add(path);
            vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;");
        }
    }

    /**
     * Выделяет элемент.

     * Selects an item.

     * @param path Путь к файлу или папке / Path to the file or folder
     * @param vbox Контейнер элемента / VBox container of the item
     */
    private void selectItem(Path path, VBox vbox) {
        fileGridView.getSelectedFiles().add(path);
        vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;");
    }

    /**
     * Снимает выделение со всех элементов.

     * Clears the selection of all items.
     */
    private void clearSelection() {
        fileGridView.getSelectedFiles().clear();
        fileGridView.getGridPane().getChildren().forEach(node -> node.setStyle("-fx-background-color: transparent;"));
    }
}
