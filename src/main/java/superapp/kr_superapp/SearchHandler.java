package superapp.kr_superapp;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SearchHandler {
    private FileGridView fileGridView;
    private HBox directory;
    private Controller controller;
    private ScrollPane fileBrowserScrollPane;

    public SearchHandler(FileGridView fileGridView, HBox directory, Controller controller, ScrollPane fileBrowserScrollPane) {
        this.fileGridView = fileGridView;
        this.directory = directory;
        this.controller = controller;
        this.fileBrowserScrollPane = fileBrowserScrollPane;
    }

    /**
     * Выполняет поиск файлов по ключевым словам.
     *
     * Performs a search for files by keywords.
     *
     * @param keywords ключевые слова для поиска / keywords for search
     */
    public void performSearch(String keywords) {
        directory.getChildren().clear();
        Label searchResultLabel = new Label("Результаты поиска по \"" + keywords + "\"");
        searchResultLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #83888B;");
        directory.getChildren().add(searchResultLabel);

        List<File> resultFiles = new ArrayList<>();
        Object userData = fileBrowserScrollPane.getUserData();

        if (userData instanceof SystemHandler) {
            searchFiles(new File(SystemHandler.SYSTEM_DIRECTORY), keywords.toLowerCase(), resultFiles);
        } else if (userData instanceof TrashHandler) {
            searchFiles(new File(TrashHandler.TRASH_DIRECTORY), keywords.toLowerCase(), resultFiles);
        } else {
            String currentDirectory = fileGridView.getCurrentDirectory();
            searchFiles(new File(currentDirectory), keywords.toLowerCase(), resultFiles);
        }

        displaySearchResults(resultFiles, keywords);
    }

    private void displaySearchResults(List<File> resultFiles, String keywords) {
        fileGridView.getGridPane().getChildren().clear();
        fileBrowserScrollPane.setContent(null);

        Object userData = fileBrowserScrollPane.getUserData();
        if (userData instanceof SystemHandler) {
            displayAsSystemView(resultFiles, keywords);
        } else if (userData instanceof TrashHandler) {
            displayAsTrashView(resultFiles, keywords);
        } else {
            displayAsFileGridView(resultFiles, keywords);
        }
    }

    private void displayAsSystemView(List<File> resultFiles, String keywords) {
        SystemHandler systemHandler = (SystemHandler) fileBrowserScrollPane.getUserData();
        GridPane gridPane = systemHandler.getGridPane();
        gridPane.getChildren().clear();

        int columnCount = 7;
        int index = 0;
        for (File file : resultFiles) {
            Path path = file.toPath();
            VBox vbox = systemHandler.createFileItem(path);
            int row = index / columnCount;
            int column = index % columnCount;
            gridPane.add(vbox, column, row);
            index++;
        }

        fileBrowserScrollPane.setContent(gridPane);
        controller.log("Результаты поиска в системе отображены");
        controller.setStatusMessage("Результаты поиска в системе по \"" + keywords + "\"");
    }

    private void displayAsTrashView(List<File> resultFiles, String keywords) {
        TrashHandler trashHandler = (TrashHandler) fileBrowserScrollPane.getUserData();
        GridPane gridPane = trashHandler.getGridPane();
        gridPane.getChildren().clear();

        int columnCount = 7;
        int index = 0;
        for (File file : resultFiles) {
            Path path = file.toPath();
            VBox vbox = trashHandler.createFileItem(path);
            int row = index / columnCount;
            int column = index % columnCount;
            gridPane.add(vbox, column, row);
            index++;
        }

        fileBrowserScrollPane.setContent(gridPane);
        controller.log("Результаты поиска в корзине отображены");
        controller.setStatusMessage("Результаты поиска в корзине по \"" + keywords + "\"");
    }

    private void displayAsFileGridView(List<File> resultFiles, String keywords) {
        GridPane gridPane = fileGridView.getGridPane();
        gridPane.getChildren().clear();

        int columnCount = 7;
        int index = 0;
        for (File file : resultFiles) {
            Path path = file.toPath();
            VBox vbox = fileGridView.createFileItem(path);
            int row = index / columnCount;
            int column = index % columnCount;
            gridPane.add(vbox, column, row);
            index++;
        }

        fileBrowserScrollPane.setContent(gridPane);
        controller.log("Результаты обычного поиска отображены");
        controller.setStatusMessage("Результаты поиска по \"" + keywords + "\"");
    }

    /**
     * Рекурсивно ищет файлы, содержащие ключевые слова в именах.
     *
     * Recursively searches for files containing keywords in their names.
     *
     * @param directory директория для поиска / directory to search
     * @param searchText текст для поиска / text to search
     * @param resultFiles список файлов с результатами поиска / list of result files
     */
    private void searchFiles(File directory, String searchText, List<File> resultFiles) {
        for (File file : directory.listFiles()) {
            if (file.getName().toLowerCase().contains(searchText)) {
                resultFiles.add(file);
            }
            if (file.isDirectory()) {
                searchFiles(file, searchText, resultFiles);
            }
        }
    }
}
