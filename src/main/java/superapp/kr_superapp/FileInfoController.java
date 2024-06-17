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
 * Класс FileInfoController отвечает за отображение подробной информации о файлах, процессах
 * и процессорах. Он предоставляет методы для установки информации о файлах, процессах и процессорах,
 * и отображает эту информацию в удобном для пользователя формате.
 *
 * The FileInfoController class is responsible for displaying detailed information about files, processes,
 * and processors. It provides methods for setting file, process, and processor information, and displays
 * this information in a user-friendly format.
 *
 *  * <p>Автор: Дмитрий Задисенцев</p>
 *  * <p>Version: 1.0</p>
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
     * Устанавливает и отображает информацию о процессе.
     *
     * Sets and displays information about a process.
     *
     * @param process Информация о процессе / The process information
     */
    public void setProcessInfo(ProcessInfo process) {

        ImageView imageView = new ImageView();
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        Image icon = new Image(getClass().getResourceAsStream("icons/Process.png"));
        imageView.setImage(icon);

        Text processNameText = new Text(process.getName());
        processNameText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        processNameText.setWrappingWidth(240);

        fileNameAndIconHBox.getChildren().addAll(imageView, processNameText);

        // Загрузка информации о процессе
        Text pidTextTitle = new Text("PID");
        pidTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
        Text pidText = new Text(String.valueOf(process.getPid()));
        pidText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        pidText.setWrappingWidth(240);
        VBox pidVBox = new VBox(pidTextTitle, pidText);
        secondVBox.getChildren().add(pidVBox);

        Text cpuTextTitle = new Text("CPU Usage (%)");
        cpuTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
        Text cpuText = new Text(String.format("%.2f%%", process.getCpuUsage()));
        cpuText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        cpuText.setWrappingWidth(240);
        VBox cpuVBox = new VBox(cpuTextTitle, cpuText);
        secondVBox.getChildren().add(cpuVBox);

        Text memoryTextTitle = new Text("Memory Usage (MB)");
        memoryTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
        Text memoryText = new Text(String.format("%.2f MB", process.getMemoryUsage()));
        memoryText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        memoryText.setWrappingWidth(240);
        VBox memoryVBox = new VBox(memoryTextTitle, memoryText);
        secondVBox.getChildren().add(memoryVBox);

        // Загрузка состояния процесса
        Text stateTextTitle = new Text("Состояние");
        stateTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
        Text stateText = new Text(process.getState());
        stateText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        stateText.setWrappingWidth(240);
        VBox stateVBox = new VBox(stateTextTitle, stateText);
        thirdVBox.getChildren().add(stateVBox);

        // Загрузка приоритета процесса
        Text priorityTextTitle = new Text("Приоритет");
        priorityTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
        Text priorityText = new Text(String.valueOf(process.getPriority()));
        priorityText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        priorityText.setWrappingWidth(240);
        VBox priorityVBox = new VBox(priorityTextTitle, priorityText);
        thirdVBox.getChildren().add(priorityVBox);

        // Загрузка времени запуска процесса
        Text startedTextTitle = new Text("Запущен");
        startedTextTitle.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
        Text startedText = new Text(Util.formatDate(process.getStartTime()));
        startedText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        startedText.setWrappingWidth(240);
        VBox startedVBox = new VBox(startedTextTitle, startedText);
        thirdVBox.getChildren().add(startedVBox);
    }

    /**
     * Устанавливает и отображает информацию о процессоре.
     *
     * Sets and displays information about a processor.
     *
     * @param details Информация о процессоре / The processor details
     */
    public void setProcessorInfo(ProcessorDetails details) {

        // Загрузка иконки и имени процессора
        ImageView imageView = new ImageView();
        imageView.setFitWidth(24);
        imageView.setFitHeight(24);
        Image icon = new Image(getClass().getResourceAsStream("icons/Processor.png"));
        imageView.setImage(icon);

        Text processorInfoText = new Text("Информация о процессоре");
        processorInfoText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        processorInfoText.setWrappingWidth(240);

        fileNameAndIconHBox.getChildren().addAll(imageView, processorInfoText);

        // Заполнение secondVBox
        addProcessorInfoToVBox(secondVBox, "Имя", details.getName());
        addProcessorInfoToVBox(secondVBox, "Производитель", details.getVendor());
        addProcessorInfoToVBox(secondVBox, "Семейство", details.getFamily());
        addProcessorInfoToVBox(secondVBox, "Модель", details.getModel());
        addProcessorInfoToVBox(secondVBox, "Степпинг", details.getStepping());
        addProcessorInfoToVBox(secondVBox, "Идентификатор процессора", details.getProcessorID());

        // Заполнение thirdVBox
        addProcessorInfoToVBox(thirdVBox, "Идентификатор", details.getIdentifier());
        addProcessorInfoToVBox(thirdVBox, "Микроархитектура", details.getMicroarchitecture());
        addProcessorInfoToVBox(thirdVBox, "Логические процессоры", String.valueOf(details.getLogicalProcessorCount()));
        addProcessorInfoToVBox(thirdVBox, "Физические процессоры", String.valueOf(details.getPhysicalProcessorCount()));
        addProcessorInfoToVBox(thirdVBox, "Максимальная частота", String.valueOf(details.getMaxFreq()) + " Hz");
        addProcessorInfoToVBox(thirdVBox, "Текущая частота", String.valueOf(details.getCurrentFreq()) + " Hz");
    }

    /**
     * Добавляет информацию о процессоре в указанный VBox.
     *
     * Adds processor information to the specified VBox.
     *
     * @param vbox VBox для добавления информации / VBox to add the information to
     * @param title Название информации / The title of the information
     * @param value Значение информации / The value of the information
     */
    private void addProcessorInfoToVBox(VBox vbox, String title, String value) {
        Text titleText = new Text(title);
        titleText.setStyle("-fx-font-size: 12; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #83888B;");
        titleText.setWrappingWidth(240);

        Text valueText = new Text(value);
        valueText.setStyle("-fx-font-size: 14; -fx-font-family: Inter; -fx-font-weight: 500; -fx-fill: #F5FAFF;");
        valueText.setWrappingWidth(240);

        VBox infoVBox = new VBox(titleText, valueText);
        vbox.getChildren().add(infoVBox);
    }

    public String getCorrectRussianEnding(long count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return "";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return "а";
        } else {
            return "ов";
        }
    }
}
