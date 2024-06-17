package superapp.kr_superapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GridViewManager {
    private ScrollPane scrollPane;
    private GridPane gridPane;
    private StackPane stackMain;
    private String currentDirectory;
    private final Image folderImage;
    private final Image fileImage;
    private List<Path> selectedFiles;
    private Path lastSelectedPath;
    private FileItemHandler fileItemHandler;
    private static final File rootDirectory = new File("src/main/home");
    private Path clipboardFile = null;
    private boolean isCutOperation = false;
    private FileTreeTable fileTreeTable;

    public GridViewManager(ScrollPane scrollPane, StackPane stackMain) {
        this.scrollPane = scrollPane;
        this.stackMain = stackMain;
        this.gridPane = new GridPane();
        this.scrollPane.setContent(gridPane);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setPrefHeight(287);
        this.scrollPane.setMinWidth(560);
        this.scrollPane.setMaxWidth(560);
        this.gridPane.prefWidth(560);
        this.gridPane.setMaxWidth(560);
        this.gridPane.setVgap(15);
        this.gridPane.setHgap(15);

        this.folderImage = new Image(getClass().getResourceAsStream("icons/Folder.png"));
        this.fileImage = new Image(getClass().getResourceAsStream("icons/File.png"));
        this.selectedFiles = new ArrayList<>();
        this.fileItemHandler = new FileItemHandler(folderImage, fileImage, this, selectedFiles);
        enableCopyPaste();
    }

    public void updateGridView(String directoryPath) {
        currentDirectory = directoryPath;
        gridPane.getChildren().clear();
        try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
            List<Path> fileList = paths.collect(Collectors.toList());
            int fileCount = fileList.size();
            int rowCount = (fileCount + 5) / 6;
            int columnCount = Math.min(fileCount, 6);
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
                        VBox vbox = fileItemHandler.createFileItem(path);
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

    public void updateGridViewWithFiles(List<File> files) {
        gridPane.getChildren().clear();
        int columnCount = Math.min(files.size(), 6);

        for (int i = 0; i < columnCount; i++) {
            gridPane.getColumnConstraints().add(new ColumnConstraints(100.0 / columnCount));
        }

        int index = 0;
        for (File file : files) {
            Path path = file.toPath();
            VBox vbox = fileItemHandler.createFileItem(path);
            int row = index / columnCount;
            int column = index % columnCount;
            gridPane.add(vbox, column, row);
            index++;
        }
    }

    public void clearSelection() {
        selectedFiles.clear();
        gridPane.getChildren().forEach(node -> node.setStyle("-fx-background-color: transparent;"));
    }

    public void selectItem(Path path, VBox vbox) {
        selectedFiles.add(path);
        vbox.setStyle("-fx-background-color: #34393E; -fx-background-radius: 5px;");
    }

    public List<Path> getSelectedFiles() {
        return selectedFiles;
    }

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public static File getRootDirectory() {
        return rootDirectory;
    }

    public void setLastSelectedPath(Path path) {
        this.lastSelectedPath = path;
    }

    public void showRenameOverlay(Path path) {
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
                clipboardFile = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    public static Image getFolderImage() {
        return new Image(GridViewManager.class.getResourceAsStream("icons/Folder.png"));
    }

    public static Image getFileImage() {
        return new Image(GridViewManager.class.getResourceAsStream("icons/File.png"));
    }

    public void updateTreeItems(String path) {
     //   fileTreeTable.updateTreeItems(path);
    }
}
