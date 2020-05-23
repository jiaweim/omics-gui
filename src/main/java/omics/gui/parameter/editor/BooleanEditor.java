package omics.gui.parameter.editor;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import omics.gui.parameter.BooleanParameter;
import org.controlsfx.control.PropertySheet;

/**
 * Boolean parameter editor.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 12:34 PM
 */
public class BooleanEditor extends CheckBox implements ParameterEditor<Boolean>
{
    public BooleanEditor(PropertySheet.Item item)
    {
        if (!(item instanceof BooleanParameter))
            throw new IllegalArgumentException();
    }

    @Override
    public Node getEditor()
    {
        return this;
    }

    @Override
    public Boolean getValue()
    {
        return isSelected();
    }

    @Override
    public void setValue(Boolean value)
    {
        if (value != null)
            setSelected(value);
    }
}
