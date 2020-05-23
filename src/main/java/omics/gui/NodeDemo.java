package omics.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import omics.gui.control.FormulaEditor;
import omics.gui.control.PTMEditor;
import omics.gui.control.ParameterPane;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 12:13 PM
 */
public class NodeDemo extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        BorderPane root = new BorderPane();
        root.setCenter(new ParameterPane());
        HBox bottom = new HBox(1);
        root.setBottom(bottom);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
