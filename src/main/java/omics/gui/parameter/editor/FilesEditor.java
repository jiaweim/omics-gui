package omics.gui.parameter.editor;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import omics.gui.parameter.FilesParameter;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 2:01 PM
 */
public class FilesEditor extends BorderPane implements ParameterEditor<List<File>>
{
    private final ListView<File> listView;

    public FilesEditor(PropertySheet.Item item)
    {
        if (!(item instanceof FilesParameter))
            throw new IllegalArgumentException();
        listView = new ListView<>();
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setCenter(listView);
        FilesParameter parameter = (FilesParameter) item;

        Button openButton = new Button();
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        openButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN_ALT));
        openButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            File lastOpenFolder = parameter.getLastOpenFolder();
            if (lastOpenFolder != null) {
                chooser.setInitialDirectory(lastOpenFolder);
            }
            chooser.setTitle(parameter.getName());
            chooser.getExtensionFilters().addAll(parameter.getExtensionFilters());
            List<File> selectedFiles = chooser.showOpenMultipleDialog(getScene().getWindow());
            if (selectedFiles != null) {
                listView.getItems().addAll(selectedFiles);
                parameter.setLastOpenFolder(selectedFiles.get(0).getParentFile());
            }
        });

        Button clearButton = new Button();
        clearButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.TRASH_ALT));
        clearButton.setOnAction(event -> listView.getItems().removeAll(listView.getSelectionModel().getSelectedItems()));

        VBox box = new VBox(10);
        box.getChildren().addAll(openButton, clearButton);
        setRight(box);
        setMargin(box, new Insets(0, 0, 0, 6));
    }

    @Override
    public Node getEditor()
    {
        return this;
    }

    @Override
    public List<File> getValue()
    {
        return new ArrayList<>(listView.getItems());
    }

    @Override
    public void setValue(List<File> value)
    {
        if (value == null)
            return;
        listView.getItems().clear();
        listView.getItems().addAll(value);
    }
}
