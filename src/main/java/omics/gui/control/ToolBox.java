package omics.gui.control;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.controlsfx.control.TaskProgressView;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 24 May 2020, 9:40 AM
 */
public class ToolBox extends ScrollPane
{
    private static final String[] BACKGROUND_COLORS = new String[]{
            "#636EFA", "#EF553B", "#00CC96", "#AB63FA", "#FFA15A",
            "#19D3F3", "#FF6692", "#B6E880", "#FF97FF", "#FECB52"};

    private final TilePane rootPane;

    public ToolBox()
    {
        rootPane = new TilePane(6, 6);
        rootPane.setPadding(new Insets(6, 6, 6, 6));

        setContent(rootPane);
        initialize();
    }

    private TaskProgressView<Task<?>> progressView;

    public void setProgressView(TaskProgressView<Task<?>> progressView)
    {
        this.progressView = progressView;
    }

    private final double height = 72;
    private final double width = 86;

    private void initialize()
    {
        registerTool(new PSMViewer());
        registerTool(new DualPSMViewer());
    }

    /**
     * only access by javafx application thread, so it is safe
     */
    private static int count = 0;

    public void registerTool(ToolNode toolNode)
    {
        Button button = new Button(toolNode.getToolName());
        button.setPrefHeight(height);
        button.setPrefWidth(width);

        String color = BACKGROUND_COLORS[count % 10];
        count++;
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: " + color + ";");
        button.setTextAlignment(TextAlignment.CENTER);
        button.setWrapText(true);

        button.setOnAction(event -> {
            toolNode.setProgressView(progressView);
            Scene scene = toolNode.getRoot().getScene();
            if (scene == null) {
                scene = new Scene(toolNode.getRoot());
            }
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(toolNode.getToolName());
            stage.setOnCloseRequest(event1 -> {
                toolNode.clear();
                stage.close();
            });
            stage.show();
        });
        rootPane.getChildren().add(button);
    }
}
