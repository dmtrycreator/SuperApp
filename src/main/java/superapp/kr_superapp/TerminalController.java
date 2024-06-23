package superapp.kr_superapp;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

/**
 * Класс для управления терминалом в приложении.
 * Позволяет выполнять команды операционной системы асинхронно, обновлять интерфейс с результатами выполнения и обрабатывать ввод пользователя.
 *
 * Class for managing the terminal within the application.
 * Allows for asynchronous execution of operating system commands, updating the interface with execution results, and handling user input.
 *
 * <p>Author: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */

public class TerminalController {

    @FXML
    private VBox TerminalV;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button clearButton;

    @FXML
    private TextArea commandInputArea;

    @FXML
    private VBox commandResultVBox;

    @FXML
    private VBox footer_content_vbox;

    @FXML
    private HBox footer_hbox;

    @FXML
    private VBox pastCommand;

    @FXML
    private Label pathLabel;

    @FXML
    private Label placeholder_label;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private MenuBar settingsMenu;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox terminalVBox;

    @FXML
    private HBox tipOneHBox;

    @FXML
    private HBox tipTwoHBox;

    @FXML
    private HBox tipThreeHBox;

    private boolean tipsRemoved = false; // Флаг для отслеживания удаления HBox

    private static final String FILE_PATH = "shared_memory.dat";
    private static final int FILE_SIZE = 1024; // Размер файла для отображения
    private static final Semaphore semaphore = new Semaphore(1); // Семафор для синхронизации

    private String currentDirectory;
    private String userName;

    @FXML
    void initialize() {
        // Инициализируем текущую директорию и имя пользователя
        currentDirectory = System.getProperty("user.home");
        userName = System.getProperty("user.name");

        commandInputArea.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                String command = commandInputArea.getText().trim();
                commandInputArea.clear();
                executeCommandAsync(command);
            }
        });

        // Обработчик для кнопки очистки
        clearButton.setOnAction(event -> pastCommand.getChildren().clear());

        // Отображаем текущий путь
        updatePathLabel();

        TerminalV.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.H) {
                handleHelpShortcut();
            } else if (event.isControlDown() && event.getCode() == KeyCode.L) {
                handleClearShortcut();
            } else if (event.isControlDown() && event.getCode() == KeyCode.T) {
                handleTerminalShortcut();
            }
        });
    }

    public void executeCommandAsync(String command) {
        new Thread(() -> executeCommand(command)).start();
    }

    public void executeCommand(String command) {
        try {
            // Проверяем, если команда "help", выводим справку по командам
            if (command.equals("help")) {
                updateLabels(command, getHelpMessage(), new Date());
                removeTips(); // Удаляем подсказки при выводе справки
                return;
            }

            // Обрабатываем команду cd отдельно для изменения директории
            if (command.startsWith("cd")) {
                changeDirectory(command);
                return;
            }

            // Создаем процесс для выполнения команды в текущей директории
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            builder.directory(new File(currentDirectory));
            builder.redirectErrorStream(true); // Перенаправляем stderr в stdout
            Process process = builder.start();

            // Создаем StringBuilder для хранения результата выполнения команды
            StringBuilder resultBuilder = new StringBuilder();

            // Читаем вывод команды
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Добавляем каждую строку результата в StringBuilder
                    resultBuilder.append(line).append("\n");
                }
            }

            // Ожидаем завершения выполнения команды
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Если выполнение завершено успешно, обновляем метки
                Date executionTime = new Date();
                updateLabels(command, resultBuilder.toString(), executionTime);
                removeTips();

                // Записываем результат команды в отображаемый файл
                writeToFile(resultBuilder.toString());
            } else {
                // В случае ошибки выводим сообщение об ошибке
                System.err.println("Ошибка выполнения команды: " + command);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        // Обновляем текущий путь после выполнения команды
        updatePathLabel();
    }

    private void changeDirectory(String command) {
        String[] parts = command.split(" ", 2); // Разделяем команду только на два элемента, чтобы поддерживать пути с пробелами
        if (parts.length < 2 || parts[1].equals("~")) {
            currentDirectory = System.getProperty("user.home");
        } else if (parts[1].equals("-")) {
            // Implement switching to previous directory if needed
        } else {
            File newDir;
            if (parts[1].startsWith("/")) {
                // Если путь абсолютный, используем его напрямую
                newDir = new File(parts[1]);
            } else {
                // Если путь относительный, создаем его относительно текущей директории
                newDir = new File(currentDirectory, parts[1]);
            }

            if (newDir.exists() && newDir.isDirectory()) {
                currentDirectory = newDir.getAbsolutePath();
            } else {
                updateLabels(command, "No such directory: " + newDir.getAbsolutePath(), new Date());
                return;
            }
        }
        updatePathLabel();
    }

    private void updatePathLabel() {
        pathLabel.setText(userName + ":" + currentDirectory + "$");
    }

    // Метод для обновления меток результатов и времени выполнения
    private void updateLabels(String command, String result, Date executionStartTime) {
        // Создаем новую запись команды
        VBox commandEntry = new VBox();
        commandEntry.setSpacing(10.0);
        commandEntry.setPadding(new Insets(0, 26, 0, 0)); // Добавляем отступ слева

        // Создаем HBox для текста с пользователем и директорией
        HBox userTimeBox = new HBox();
        userTimeBox.setSpacing(20);
        userTimeBox.setPadding(new Insets(0, 26, 0, 26));

        // Создаем метку текста с пользователем и директорией
        Label userLabel = new Label(userName + " ~ Terminal File Manager");
        userLabel.setStyle("-fx-opacity: 0.6; -fx-text-fill: #469ee9; -fx-font-family: Inter Medium; -fx-font-size: 16;");
        userTimeBox.getChildren().add(userLabel);

        // Вычисляем время выполнения команды
        Date executionEndTime = new Date();
        long executionTime = executionEndTime.getTime() - executionStartTime.getTime();

        // Создаем и настраиваем время выполнения команды
        Label timeLabel = new Label("(" + executionTime + " ms)");
        timeLabel.setStyle("-fx-opacity: 0.4; -fx-text-fill: #83888b;");
        userTimeBox.getChildren().add(timeLabel);

        // Добавляем HBox в запись команды
        commandEntry.getChildren().add(userTimeBox);

        // Создаем и настраиваем название команды
        Label commandLabel = new Label(command);
        commandLabel.setStyle("-fx-text-fill: #83888b; -fx-font-family: Ubuntu Mono; -fx-font-size: 16; -fx-opacity: 0.6;"); // Устанавливаем прозрачность в 0.6
        commandLabel.setPadding(new Insets(0, 26, 0, 26));
        commandEntry.getChildren().add(commandLabel);

        TextFlow resultTextFlow = new TextFlow();
        resultTextFlow.setPrefWidth(888.0); // Ширина может быть фиксированной
        resultTextFlow.setStyle("-fx-font-family: Ubuntu Mono; -fx-font-size: 16;");
        resultTextFlow.setLineSpacing(5.0); // Устанавливаем промежуток между строками
        resultTextFlow.setPadding(new Insets(0, 26, 0, 26)); // Уменьшаем отступ сверху до 5 пикселей

        // Создаем текст для отображения в TextFlow
        Text resultText = new Text(result);
        resultText.setFill(Color.web("#83888b")); // Задаем цвет текста

        // Добавляем текст в TextFlow
        resultTextFlow.getChildren().add(resultText);
        resultTextFlow.setPadding(new Insets(0, 26, 0, 26));
        commandEntry.getChildren().add(resultTextFlow);

        // Добавляем созданную запись команды в конец списка предыдущих записей
        pastCommand.getChildren().add(commandEntry);

        // Создаем и добавляем линию-разделитель
        Line separatorLine = new Line();
        separatorLine.setStartX(0);
        separatorLine.setEndX(940);
        separatorLine.setStroke(Color.web("#1E1E1E")); // Задаем цвет линии
        separatorLine.setStrokeWidth(3); // Задаем толщину линии
        commandEntry.getChildren().add(separatorLine);
    }

    // Метод для удаления подсказок
    private void removeTips() {
        if (!tipsRemoved) {
            if (terminalVBox.getChildren().contains(tipOneHBox)) {
                terminalVBox.getChildren().remove(tipOneHBox);
            }
            if (terminalVBox.getChildren().contains(tipTwoHBox)) {
                terminalVBox.getChildren().remove(tipTwoHBox);
            }
            if (terminalVBox.getChildren().contains(tipThreeHBox)) {
                terminalVBox.getChildren().remove(tipThreeHBox);
            }
            tipsRemoved = true; // Устанавливаем флаг, чтобы избежать повторного удаления
        }
    }

    // Метод для записи данных в файл отображения
    private void writeToFile(String data) {
        byte[] bytes = data.getBytes();
        if (bytes.length > FILE_SIZE) {
            throw new BufferOverflowException();
        }
        try (RandomAccessFile file = new RandomAccessFile(FILE_PATH, "rw");
             FileChannel fileChannel = file.getChannel()) {
            semaphore.acquire(); // Захватываем семафор для синхронизации
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
            buffer.put(bytes); // Записываем данные в файл
            semaphore.release(); // Освобождаем семафор
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Метод для чтения данных из файла отображения (может быть использован другим процессом)
    private String readFromFile() {
        try (RandomAccessFile file = new RandomAccessFile(FILE_PATH, "r");
             FileChannel fileChannel = file.getChannel()) {
            semaphore.acquire(); // Захватываем семафор для синхронизации
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, FILE_SIZE);
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data); // Читаем данные из файла
            semaphore.release(); // Освобождаем семафор
            return new String(data).trim();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Метод для получения справки по командам
    private String getHelpMessage() {
        return """
                Доступные команды:
                help - Выводит эту справку.
                ls - Выводит список файлов в текущей директории.
                cd <dir> - Изменяет текущую директорию.
                mkdir <dir> - Создает новую директорию.
                rm <file> - Удаляет указанный файл.
                rmdir <dir> - Удаляет указанную директорию.
                ping <host> - Проверяет доступность хоста.
                hostname - Выводит имя текущего хоста.
                ip - Выводит IP-адрес текущего хоста.
                touch <file> - Создает новый файл.
                cp <src> <dest> - Копирует файл или директорию.
                mv <src> <dest> - Перемещает файл или директорию.
                cat <file> - Выводит содержимое файла.
            """;
    }

    @FXML
    private void handleHelpShortcut() {
        commandInputArea.setText("help");
        String command = commandInputArea.getText().trim();
        executeCommand(command);
        commandInputArea.clear();
    }

    @FXML
    private void handleClearShortcut() {
        pastCommand.getChildren().clear();
    }

    @FXML
    private void handleTerminalShortcut() {
        statusLabel.setText("Клавиши нажаты"); // Подтверждение нажатия клавиш
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "gnome-terminal",
                    "--",
                    "/bin/bash",
                    "--rcfile",
                    System.getProperty("user.home") + "/.superapp/scripts/custom_bashrc"
            );
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
