package superapp.kr_superapp;

import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Утилитарный класс для настройки функциональности Drag-and-Drop.
 * Этот класс предоставляет методы для настройки элементов управления как источников и целей операций перетаскивания и сброса.

 * Utility class for setting up drag-and-drop functionality.
 * This class provides methods to configure controls as sources and targets of drag-and-drop operations.

 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class DragAndDropUtil {

    /**
     * Настраивает узел в качестве источника перетаскивания.
     *
     * Sets up a node as a drag source.
     *
     * @param node Узел, который будет источником перетаскивания / Node to be set up as a drag source
     * @param path Путь к файлу, который будет перетаскиваться / Path to the file to be dragged
     */
    public static void setupDragAndDropSource(Node node, Path path) {
        node.setOnDragDetected(event -> {
            Dragboard db = node.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putFiles(List.of(path.toFile()));
            db.setContent(content);
            SystemInfo.log("Drag detected for file: " + path.toString() + " / Обнаружено перетаскивание файла: " + path.toString());
            event.consume();
        });
    }

    /**
     * Настраивает узел в качестве цели перетаскивания.
     *
     * Sets up a node as a drag target.
     *
     * @param node Узел, который будет целью перетаскивания / Node to be set up as a drag target
     * @param onDrop Действие, которое будет выполнено при сбросе файла / Action to be performed on file drop
     */
    public static void setupDragAndDropTarget(Node node, Consumer<Path> onDrop) {
        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                SystemInfo.log("Drag over detected on node / Обнаружено перетаскивание на узел");
            }
            event.consume();
        });

        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    Path sourcePath = file.toPath();
                    onDrop.accept(sourcePath);
                    SystemInfo.log("File dropped: " + sourcePath.toString() + " / Файл сброшен: " + sourcePath.toString());
                }
                success = true;
                Controller.getInstance().updateTrashLabel(); 
            }
            event.setDropCompleted(success);
            SystemInfo.log("Drag drop completed with success: " + success + " / Перетаскивание завершено с успехом: " + success);
            event.consume();
        });
    }
}
