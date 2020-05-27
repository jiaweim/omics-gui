package omics.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 27 May 2020, 8:07 AM
 */
class TaskTypeTest extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        System.out.println(TaskType.REFRESH.getIcon(Color.GREEN).getFontSize());
        Scene scene = new Scene(new StackPane());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}