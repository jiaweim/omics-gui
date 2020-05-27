package omics.gui.parameter.editor;

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import omics.gui.parameter.ColorParameter;
import org.controlsfx.control.PropertySheet;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 26 May 2020, 10:38 AM
 */
public class ColorEditor extends HBox implements ParameterEditor<Color>
{
    private final ColorPicker colorPicker;

    public ColorEditor(PropertySheet.Item item)
    {
        if (!(item instanceof ColorParameter))
            throw new IllegalArgumentException();
        this.colorPicker = new ColorPicker();
        colorPicker.getStyleClass().add(ColorPicker.STYLE_CLASS_BUTTON);
        getChildren().add(colorPicker);
    }

    @Override
    public Node getEditor()
    {
        return this;
    }

    @Override
    public Color getValue()
    {
        return colorPicker.getValue();
    }

    @Override
    public void setValue(Color value)
    {
        colorPicker.setValue(value);
    }
}
