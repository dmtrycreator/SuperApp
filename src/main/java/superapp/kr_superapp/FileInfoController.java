package superapp.kr_superapp;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;

/**
 * Класс FileInfoController отвечает за отображение подробной информации о файлах.
 * Он предоставляет методы для установки информации о файлах и отображает
 * эту информацию в удобном для пользователя формате.
 *
 * The FileInfoController class is responsible for displaying detailed information about files.
 * It provides methods for setting file information and displays this information
 * in a user-friendly format.
 *
 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class FileInfoController {

    @FXML
    private HBox fileNameAndIconHBox;

    @FXML
    private HBox firstHBox;

    @FXML
    private VBox firstVBox;

    @FXML
    private VBox secondVBox;

    @FXML
    private VBox thirdVBox;

    /**
     * Устанавливает и отображает информацию о файле.
     *
     * Sets and displays information about a file.
     *
     * @param filePath Путь к файлу / The path to the file
     */
    public void setFileInfo(Path filePath) {
        try {
            // Загрузка иконки и имени файла
            ImageView imageView = new ImageView();
            imageView.setFitWidth(24);
            imageView.setFitHeight(24);
            Image icon = new Image(getClass().getResourceAsStream(Files.isDirectory(filePath) ? "icons/Folder.png" : "icons/File.png"));
            imageView.setImage(icon);

            Text fileNameText = new Text(filePath.getFileName().toString());
            fileNameText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
            fileNameText.setWrappingWidth(240);

            fileNameAndIconHBox.getChildren().addAll(imageView, fileNameText);

            // Загрузка пути
            Text pathTextTitle = new Text("Путь");
            pathTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
            Text pathText = new Text(filePath.toString().replace("src/main", ""));
            pathText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
            pathText.setWrappingWidth(240);
            VBox pathVBox = new VBox(pathTextTitle, pathText);
            firstVBox.getChildren().add(pathVBox);

            // Загрузка информации о файле
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(filePath, FileOwnerAttributeView.class);

            // Загрузка типа, времени создания и последнего изменения
            Text typeTextTitle = new Text("Тип");
            typeTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
            Text typeText = new Text(Files.isDirectory(filePath) ? "Папка" : "Файл");
            typeText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
            typeText.setWrappingWidth(240);

            Text modifiedTextTitle = new Text("Последнее изменение");
            modifiedTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
            Text modifiedText = new Text(attrs.lastModifiedTime().toString());
            modifiedText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
            modifiedText.setWrappingWidth(240);

            Text createdTextTitle = new Text("Создано");
            createdTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
            Text createdText = new Text(attrs.creationTime().toString());
            createdText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
            createdText.setWrappingWidth(240);

            VBox typeVBox = new VBox(typeTextTitle, typeText);
            VBox modifiedVBox = new VBox(modifiedTextTitle, modifiedText);
            VBox createdVBox = new VBox(createdTextTitle, createdText);

            secondVBox.getChildren().addAll(typeVBox, modifiedVBox, createdVBox);

            // Загрузка размера, содержимого и владельца
            Text sizeTextTitle = new Text("Размер");
            sizeTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
            Text sizeText = new Text(String.valueOf(attrs.size()) + " bytes");
            sizeText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
            sizeText.setWrappingWidth(240);

            VBox sizeVBox = new VBox(sizeTextTitle, sizeText);

            if (Files.isDirectory(filePath)) {
                Text contentTextTitle = new Text("Содержание");
                contentTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
                Text contentText = new Text(String.valueOf(Files.list(filePath).count()) + " Элемент" + getCorrectRussianEnding((Files.list(filePath).count())));
                contentText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
                contentText.setWrappingWidth(240);

                VBox contentVBox = new VBox(contentTextTitle, contentText);
                thirdVBox.getChildren().addAll(sizeVBox, contentVBox);
            } else {
                thirdVBox.getChildren().add(sizeVBox);
            }

            Text ownerTextTitle = new Text("Владелец");
            ownerTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
            Text ownerText = new Text(ownerAttributeView.getOwner().getName());
            ownerText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
            ownerText.setWrappingWidth(240);

            VBox ownerVBox = new VBox(ownerTextTitle, ownerText);
            thirdVBox.getChildren().add(ownerVBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает правильное русское окончание для слова "элемент".
     *
     * Returns the correct Russian ending for the word "элемент".
     *
     * @param count количество элементов / number of items
     * @return правильное окончание / correct ending
     */
    public String getCorrectRussianEnding(long count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return "а";
        } else {
            return "ов";
        }
    }

    /**
     * Устанавливает информацию о процессоре и отображает ее.
     *
     * Sets and displays processor information.
     *
     * @param processorDetails Информация о процессоре / Processor details
     */
    public void setProcessorInfo(ProcessorDetails processorDetails) {
        // Очистка текущей информации
        secondVBox.getChildren().clear();

        // Добавление информации о процессоре
        Text processorTitle = new Text("Информация о процессоре");
        processorTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");

        Text processorInfo = new Text(processorDetails.toString());
        processorInfo.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        processorInfo.setWrappingWidth(240);

        VBox processorVBox = new VBox(processorTitle, processorInfo);
        secondVBox.getChildren().add(processorVBox);
    }

    /**
     * Устанавливает информацию о процессе и отображает ее.
     *
     * Sets and displays process information.
     *
     * @param processInfo Информация о процессе / Process information
     */
    public void setProcessInfo(ProcessInfo processInfo) {
        // Очистка текущей информации
        secondVBox.getChildren().clear();

        // Добавление информации о процессе
        Text processTitle = new Text("Информация о процессе");
        processTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");

        Text processInfoText = new Text(processInfo.toString());
        processInfoText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        processInfoText.setWrappingWidth(240);

        VBox processVBox = new VBox(processTitle, processInfoText);
        secondVBox.getChildren().add(processVBox);
    }
}
