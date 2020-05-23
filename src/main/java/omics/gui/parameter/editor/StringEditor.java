package omics.gui.parameter.editor;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import omics.gui.parameter.StringParameter;
import org.controlsfx.control.PropertySheet;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 1:12 PM
 */
public class StringEditor extends TextField implements ParameterEditor<String>
{
    public StringEditor(PropertySheet.Item item)
    {
        if (!(item instanceof StringParameter))
            throw new IllegalArgumentException();
    }

    @Override
    public Node getEditor()
    {
        return this;
    }

    @Override
    public String getValue()
    {
        return getText();
    }

    @Override
    public void setValue(String value)
    {
        setText(value);
    }
}
