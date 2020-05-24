package omics.gui.control;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.TilePane;
import javafx.scene.text.TextAlignment;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 24 May 2020, 9:40 AM
 */
public class ToolBox extends TilePane
{
    public ToolBox()
    {
        setPadding(new Insets(6, 6, 6, 6));
        setVgap(6);
        setHgap(6);
        initialize();
    }

    private final double height = 72;
    private final double width = 86;

    private void initialize()
    {
        getChildren().add(createButton("Format Conversion", "#B22222"));
    }

    private Button createButton(String text, String color)
    {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + ";" +
                "-fx-text-fill: #FFFFFF");
        button.setPrefHeight(height);
        button.setPrefWidth(width);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setWrapText(true);
        return button;
    }
}
