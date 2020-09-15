package omics.gui.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 01 Sep 2020, 12:33 PM
 */
public class ToolBoxTest extends Application
{

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setScene(new Scene(new ToolBox()));
        primaryStage.show();
    }
}