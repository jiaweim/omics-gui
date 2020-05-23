package omics.gui.parameter.editor;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import omics.gui.parameter.FileParameter;
import omics.util.utils.StringUtils;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 1:14 PM
 */
public class FileEditor extends BorderPane implements ParameterEditor<File>
{
    private final TextField textField;

    public FileEditor(PropertySheet.Item item)
    {
        if ((!(item instanceof FileParameter)))
            throw new IllegalArgumentException();
        this.textField = new TextField();
        setCenter(textField);
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        Button openButton = new Button();
        openButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN));
        setRight(openButton);
        setMargin(openButton, new Insets(0, 0, 0, 6));

        FileParameter parameter = (FileParameter) item;
        openButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            File lastOpenPath = parameter.getLastOpenFolder();
            if (lastOpenPath != null) {
                chooser.setInitialDirectory(lastOpenPath);
            }
            chooser.setTitle(parameter.getName());
            List<FileChooser.ExtensionFilter> extensionFilters = parameter.getExtensionFilters();
            if (extensionFilters != null) {
                chooser.getExtensionFilters().addAll(extensionFilters);
            }

            Window window = getScene().getWindow();
            File file;
            if (parameter.getChooseType() == FileParameter.ChooseType.OPEN) {
                file = chooser.showOpenDialog(window);
            } else
                file = chooser.showSaveDialog(window);
            if (file != null) {
                textField.setText(file.getPath());
                File parentFile = file.getParentFile();
                parameter.setLastOpenFolder(parentFile);
            }
        });
    }

    @Override
    public Node getEditor()
    {
        return this;
    }

    @Override
    public File getValue()
    {
        String text = textField.getText();
        if (StringUtils.isNotEmpty(text))
            return new File(text);
        return null;
    }

    @Override
    public void setValue(File value)
    {
        if (value != null)
            textField.setText(value.getPath());
        else
            textField.clear();
    }
}
