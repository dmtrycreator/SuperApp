package superapp.kr_superapp;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class HelpPane extends VBox {

    public HelpPane() {
        super(20);
        setPadding(new Insets(60, 30, 30, 120));
        setStyle("-fx-background-color: #272727; -fx-background-radius: 12;");
        setPrefSize(1132, 558);

        Label shortcutText = new Label("Горячие клавиши");
        shortcutText.setFont(new Font("Inter Medium", 16));
        shortcutText.setTextFill(Color.web("#83888B"));

        VBox vbox1 = new VBox(20);
        VBox vbox2 = new VBox(20);

        vbox1.getChildren().addAll(
                createShortcutHBox("Ctrl", "C", "Копировать"),
                createShortcutHBox("Ctrl", "V", "Вставить"),
                createShortcutHBox("Ctrl", "X", "Вырезать"),
                createShortcutHBox("Ctrl", "P", "Свойства")
        );

        vbox2.getChildren().addAll(
                createShortcutHBox("Ctrl", "H", "Выполнить HELP"),
                createShortcutHBox("Ctrl", "L", "Очистить терминал"),
                createShortcutHBox("Ctrl", "T", "Открыть консольный терминал"),
                createShortcutHBox("Ctrl", "S", "Прервать процесс")
                );

        HBox hbox = new HBox(0);
        hbox.setSpacing(120);
        hbox.getChildren().addAll(vbox1, vbox2);
        hbox.setAlignment(Pos.CENTER_LEFT);

        VBox containerVBox = new VBox(20);
        containerVBox.getChildren().addAll(shortcutText, hbox);
        containerVBox.setAlignment(Pos.CENTER_LEFT);

        getChildren().add(containerVBox);
    }

    private HBox createShortcutHBox(String key1, String key2, String description) {
        HBox shortcutHBox = new HBox(30);
        shortcutHBox.setAlignment(Pos.CENTER_LEFT);

        HBox keysHBox = new HBox(12);
        keysHBox.setAlignment(Pos.CENTER_LEFT);

        Pane key1Pane = createKeyPane(key1);
        Text plusText = new Text("+");
        plusText.setFont(new Font("Inter Medium", 16));
        plusText.setFill(Color.web("#469ee9"));

        Pane key2Pane = createKeyPane(key2);

        keysHBox.getChildren().addAll(key1Pane, plusText, key2Pane);

        Text descriptionText = new Text(description);
        descriptionText.setFont(new Font("Inter Medium", 14));
        descriptionText.setFill(Color.web("#F5FAFF"));

        shortcutHBox.getChildren().addAll(keysHBox, descriptionText);
        return shortcutHBox;
    }

    private Pane createKeyPane(String key) {
        StackPane keyPane = new StackPane();
        keyPane.setPrefSize(58, 32);
        keyPane.setMaxSize(58, 32);
        keyPane.setStyle("-fx-background-color: #313131; -fx-background-radius: 8;");

        Text keyText = new Text(key);
        keyText.setFont(new Font("Inter Medium", 16));
        keyText.setFill(Color.web("#469ee9"));

        keyPane.getChildren().add(keyText);
        StackPane.setAlignment(keyText, Pos.CENTER);

        return keyPane;
    }
}
