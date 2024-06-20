package superapp.kr_superapp;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.nio.file.*;
import java.io.IOException;

/**
 * Класс для отображения оверлея, который позволяет пользователю создавать файлы и папки.
 * Включает функционал для ввода имени файла или папки и их создания в текущей директории.
 *
 * Class for displaying an overlay that allows the user to create files and folders.
 * Includes functionality for entering a file or folder name and creating them in the current directory.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FileCreatorOverlay {

    private FileGridView fileGridView;
    private HBox overlay;

    /**
     * Конструктор класса FileCreatorOverlay.
     * Создает экземпляр оверлея для создания файлов и папок.
     *
     * Constructor of the FileCreatorOverlay class.
     * Creates an instance of the overlay for creating files and folders.
     *
     * @param fileGridView Сетка файлового представления / File grid view
     */
    public FileCreatorOverlay(FileGridView fileGridView) {
        this.fileGridView = fileGridView;
        createOverlay();
    }

    /**
     * Создает и отображает оверлей для ввода имени и расширения файла или папки.
     *
     * Creates and displays an overlay for entering the name and extension of a file or folder.
     */
    private void createOverlay() {
        overlay = new HBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setMaxHeight(138);
        overlay.setMaxWidth(538);
        overlay.setSpacing(10);
        overlay.setStyle("-fx-background-color: #1E1E1E; -fx-background-radius: 18; -fx-padding: 30 40 30 40;");

        TextField nameField = new TextField();
        nameField.setPromptText("Имя файла или папки");
        nameField.setPrefWidth(200);
        nameField.setStyle("-fx-background-color: #272727; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12; -fx-padding: 16 20 16 20;");

        TextField extensionField = new TextField();
        extensionField.setPromptText("Расширение");
        extensionField.setPrefWidth(130);
        extensionField.setStyle("-fx-background-color: #272727; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12; -fx-padding: 16 20 16 20;");

        Button createButton = new Button("Создать");
        createButton.setPrefHeight(46);
        createButton.setPrefWidth(107);
        createButton.setStyle("-fx-background-color: #469EE9; -fx-text-fill: #F5FAFF; -fx-font-size: 14; -fx-background-radius: 12;");

        Rectangle background = new Rectangle(
                fileGridView.getStackMain().getScene().getWidth(),
                fileGridView.getStackMain().getScene().getHeight(),
                Color.rgb(0, 0, 0, 0.7)
        );

        createButton.setOnAction(event -> {
            String name = nameField.getText();
            String extension = extensionField.getText();
            if (name != null && !name.trim().isEmpty()) {
                createFileOrFolder(name, extension);
            }
            fileGridView.getStackMain().getChildren().removeAll(background, overlay);
        });

        overlay.getChildren().addAll(nameField, extensionField, createButton);
        fileGridView.getStackMain().getChildren().addAll(background, overlay);

        background.setOnMouseClicked(event -> fileGridView.getStackMain().getChildren().removeAll(background, overlay));
    }

    /**
     * Создает файл или папку в текущей директории с указанным именем и расширением.
     * Если расширение не указано, создается папка.
     *
     * Creates a file or folder in the current directory with the specified name and extension.
     * If no extension is specified, a folder is created.
     *
     * @param name Имя файла или папки / Name of the file or folder
     * @param extension Расширение файла (опционально) / File extension (optional)
     */
    private void createFileOrFolder(String name, String extension) {
        Path newPath;
        try {
            if (extension == null || extension.trim().isEmpty()) {
                newPath = Paths.get(fileGridView.getCurrentDirectory(), name);
                Files.createDirectory(newPath);
            } else {
                newPath = Paths.get(fileGridView.getCurrentDirectory(), name + "." + extension);
                Files.createFile(newPath);
                byte[] data;
                if (extension.equalsIgnoreCase("png")) {
                    data = "Это заполнители для файла изображения.".getBytes();
                } else if (extension.equalsIgnoreCase("jpg")) {
                    data = "Это заполнители для файла изображения.".getBytes();
                } else if (extension.equalsIgnoreCase("txt")) {
                    data = "Это заполнители для текстового файла.".getBytes();
                } else {
                    data = "Это заполнители для файла.".getBytes();
                }
                Files.write(newPath, data, StandardOpenOption.WRITE);
            }
            fileGridView.updateGridView(fileGridView.getCurrentDirectory());
            Controller.getInstance().getFileTreeTable().updateTreeItems(Controller.getInstance().getRootDirectory().getPath());
        } catch (AccessDeniedException e) {
            showError("Отказано в доступе: " + e.getFile());
            e.printStackTrace();
        } catch (IOException e) {
            showError("Ошибка создания файла или папки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Отображает сообщение об ошибке пользователю.
     *
     * Displays an error message to the user.
     *
     * @param message Сообщение об ошибке / Error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
