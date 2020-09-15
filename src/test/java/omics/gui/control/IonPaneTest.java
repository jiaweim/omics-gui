package omics.gui.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 01 Sep 2020, 3:25 PM
 */
public class IonPaneTest extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        IonTypePane pane = new IonTypePane();
        ScrollPane root = new ScrollPane();
        root.setPrefHeight(600);
        root.setPrefWidth(800);
        root.setContent(pane);
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}