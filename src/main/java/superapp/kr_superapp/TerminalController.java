package superapp.kr_superapp;

import javafx.application.Platform;
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
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalTime;
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

    private static StringBuilder logBuilder = new StringBuilder();

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
                updateLabels(command, getHelpMessage(), new Date(), false);
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

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder resultBuilder = new StringBuilder();
            Date startTime = new Date();
            VBox commandEntry = createCommandEntry(command, startTime);

            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                resultBuilder.append(line).append("\n");
                Platform.runLater(() -> updateResultText(commandEntry, finalLine));
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Date endTime = new Date();
                updateLabels(command, resultBuilder.toString(), endTime, true);
                removeTips();
                writeToFile(resultBuilder.toString());
            } else {
                System.err.println("Ошибка выполнения команды: " + command);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
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
                updateLabels(command, "No such directory: " + newDir.getAbsolutePath(), new Date(), false);
                return;
            }
        }
        updatePathLabel();
    }

    private void updatePathLabel() {
        Platform.runLater(() -> pathLabel.setText(userName + ":" + currentDirectory + "$"));
    }

    private VBox createCommandEntry(String command, Date executionStartTime) {
        VBox commandEntry = new VBox();
        commandEntry.setSpacing(10.0);
        commandEntry.setPadding(new Insets(0, 26, 0, 0));

        HBox userTimeBox = new HBox();
        userTimeBox.setSpacing(20);
        userTimeBox.setPadding(new Insets(0, 26, 0, 26));

        Label userLabel = new Label(userName + " ~ Terminal File Manager");
        userLabel.setStyle("-fx-opacity: 0.6; -fx-text-fill: #469ee9; -fx-font-family: Inter Medium; -fx-font-size: 16;");
        userTimeBox.getChildren().add(userLabel);

        Label timeLabel = new Label();
        timeLabel.setStyle("-fx-opacity: 0.4; -fx-text-fill: #83888b;");
        userTimeBox.getChildren().add(timeLabel);

        commandEntry.getChildren().add(userTimeBox);

        Label commandLabel = new Label(command);
        commandLabel.setStyle("-fx-text-fill: #83888b; -fx-font-family: Ubuntu Mono; -fx-font-size: 16; -fx-opacity: 0.6;");
        commandLabel.setPadding(new Insets(0, 26, 0, 26));
        commandEntry.getChildren().add(commandLabel);

        TextFlow resultTextFlow = new TextFlow();
        resultTextFlow.setPrefWidth(888.0);
        resultTextFlow.setStyle("-fx-font-family: Ubuntu Mono; -fx-font-size: 16;");
        resultTextFlow.setLineSpacing(5.0);
        resultTextFlow.setPadding(new Insets(0, 26, 0, 26));

        commandEntry.getChildren().add(resultTextFlow);
        pastCommand.getChildren().add(commandEntry);

        Line separatorLine = new Line();
        separatorLine.setStartX(0);
        separatorLine.setEndX(940);
        separatorLine.setStroke(Color.web("#1E1E1E"));
        separatorLine.setStrokeWidth(3);
        commandEntry.getChildren().add(separatorLine);

        commandEntry.setUserData(new Object[]{resultTextFlow, timeLabel});

        return commandEntry;
    }

    private void updateResultText(VBox commandEntry, String result) {
        Object[] userData = (Object[]) commandEntry.getUserData();
        TextFlow resultTextFlow = (TextFlow) userData[0];
        Text resultText = new Text(result + "\n");
        resultText.setFill(Color.web("#83888b"));
        resultTextFlow.getChildren().add(resultText);
    }

    private void updateLabels(String command, String result, Date executionStartTime, boolean updateTime) {
        VBox commandEntry = createCommandEntry(command, executionStartTime);
        updateResultText(commandEntry, result);

        if (updateTime) {
            Date executionEndTime = new Date();
            long executionTime = executionEndTime.getTime() - executionStartTime.getTime();
            Object[] userData = (Object[]) commandEntry.getUserData();
            Label timeLabel = (Label) userData[1];
            timeLabel.setText("(" + executionTime + " ms)");
        }
    }

    private void removeTips() {
        if (!tipsRemoved) {
            Platform.runLater(() -> {
                if (terminalVBox.getChildren().contains(tipOneHBox)) {
                    terminalVBox.getChildren().remove(tipOneHBox);
                }
                if (terminalVBox.getChildren().contains(tipTwoHBox)) {
                    terminalVBox.getChildren().remove(tipTwoHBox);
                }
                if (terminalVBox.getChildren().contains(tipThreeHBox)) {
                    terminalVBox.getChildren().remove(tipThreeHBox);
                }
                tipsRemoved = true;
            });
        }
    }

    private void writeToFile(String data) {
        byte[] bytes = data.getBytes();
        if (bytes.length > FILE_SIZE) {
            throw new BufferOverflowException();
        }
        try (RandomAccessFile file = new RandomAccessFile(FILE_PATH, "rw");
             FileChannel fileChannel = file.getChannel()) {
            semaphore.acquire();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);
            buffer.put(bytes);
            semaphore.release();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String readFromFile() {
        try (RandomAccessFile file = new RandomAccessFile(FILE_PATH, "r");
             FileChannel fileChannel = file.getChannel()) {
            semaphore.acquire();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, FILE_SIZE);
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            semaphore.release();
            return new String(data).trim();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

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
        executeCommandAsync(command);
        commandInputArea.clear();
    }

    @FXML
    private void handleClearShortcut() {
        pastCommand.getChildren().clear();
    }

    @FXML
    private void handleTerminalShortcut() {
        statusLabel.setText("Клавиши нажаты");
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "gnome-terminal",
                    "--",
                    "/bin/bash",
                    "--rcfile",
                    System.getProperty("user.home") + "/SuperApp/scripts/custom_bashrc"
            );
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        logBuilder.append(LocalTime.now()).append(" - ").append(message).append("\n");
    }

    private void saveLogReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет журнала");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Log Files", "*.log"));
        fileChooser.setInitialFileName("log.log");

        File initialDirectory = new File("src/main/log");
        if (!initialDirectory.exists()) {
            initialDirectory.mkdirs();
        }
        fileChooser.setInitialDirectory(initialDirectory);

        File file = fileChooser.showSaveDialog(TerminalV.getScene().getWindow());
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(logBuilder.toString());
                log("Отчет журнала сохранен в: " + file.getAbsolutePath());
                setStatusMessage("Отчет журнала сохранен");

                if (file.setReadOnly()) {
                    log("Файл установлен в режим только для чтения");
                } else {
                    log("Не удалось установить файл в режим только для чтения");
                }
            } catch (IOException e) {
                log("Ошибка сохранения отчета журнала: " + e.getMessage());
                setStatusMessage("Ошибка сохранения отчета журнала");
            }
        } else {
            log("Сохранение отчета журнала отменено");
            setStatusMessage("Сохранение отчета журнала отменено");
        }
    }

    public void setStatusMessage(String message) {
        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText(message);
            } else {
                System.err.println("statusLabel is not initialized");
            }
        });
    }
}
