package omics.gui.util;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

public class FontAwesomeDemo extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        StackPane root = new StackPane();
        ListView<Button> listView = new ListView<>();
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        for (FontAwesome.Glyph glyph : FontAwesome.Glyph.values()) {
            Button button = new Button();
            button.setText(glyph.name());
            Glyph icon = fontAwesome.create(glyph);
            System.out.println(icon.getFontSize());
            button.setGraphic(icon);
//            button.setPrefHeight(20);
            listView.getItems().add(button);
        }
        root.getChildren().add(listView);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}