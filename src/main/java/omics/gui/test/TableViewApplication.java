package omics.gui.test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 8:35 AM
 */
public class TableViewApplication extends Application
{

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        TableView<Person> table = new TableView<>(PersonTableUtil.getPersonList());

        table.getColumns().addAll(PersonTableUtil.getIdColumn(),
                PersonTableUtil.getFirstNameColumn(),
                PersonTableUtil.getLastNameColumn(),
                PersonTableUtil.getBirthDateColumn());

        VBox root = new VBox(table);
        root.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
