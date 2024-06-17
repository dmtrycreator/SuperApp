package superapp.kr_superapp;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

/**
 * Этот класс представляет собой панель, которая отображает информацию об авторе
 * и приложении, когда пользователь нажимает на "aboutMeButton" в классе Controller.

 * This class represents the pane that displays information about the author
 * and the application when the user clicks on the "aboutMeButton" in the Controller class.

 * <p>Автор: Дмитрий Задисенцев</p>
 * <p>Version: 1.0</p>
 */
public class AboutPane extends VBox {

    /**
     * Конструктор для создания и инициализации AboutPane.
     * Настраивает макет и стиль панели и добавляет необходимую информацию.

     * Constructor to create and initialize the AboutPane.
     * Sets up the layout and style of the pane and adds the necessary information.
     */
    public AboutPane() {
        super(60);
        setPadding(new Insets(60, 120, 60, 120));
        setStyle("-fx-background-color: #272727; -fx-background-radius: 12;");
        setPrefSize(1132, 558);

        // Дисциплина, для которой выполнялось создание файлового менеджера SuperApp
        // The discipline for which the SuperApp file manager was created.
        Label headerLabel = new Label("Операционные системы и оболочки");
        headerLabel.setFont(new Font("Inter Medium", 16));
        headerLabel.setTextFill(Color.web("#83888B"));

        // Информация
        // Information sections
        VBox programmingLanguageVBox = createInfoVBox("Язык программирования", "Java");
        VBox authorVBox = createInfoVBox("Автор", "Задисенцев Дмитрий Андреевич, ИВТ-24");
        VBox githubVBox = createInfoVBox("GitHub", "https://github.com/dmtrycreator");

        getChildren().addAll(headerLabel, programmingLanguageVBox, authorVBox, githubVBox);
    }

    /**
     * Создает VBox с информацией, содержащей заголовок и значение.

     * Creates a VBox containing information with a title and a value.

     * @param title Заголовок информации / Information title
     * @param value Значение информации / Information value
     * @return VBox с заголовком и значением / VBox with title and value
     */
    private VBox createInfoVBox(String title, String value) {
        VBox vbox = new VBox(8);

        // Заголовок для информации
        // Title for the information
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font("Inter", 16));
        titleLabel.setTextFill(Color.web("#83888B"));

        // Значение информации
        // Value for the information
        Label valueLabel = new Label(value);
        valueLabel.setFont(new Font("Inter", 18));
        valueLabel.setTextFill(Color.web("#F5FAFF"));

        vbox.getChildren().addAll(titleLabel, valueLabel);
        return vbox;
    }
}
