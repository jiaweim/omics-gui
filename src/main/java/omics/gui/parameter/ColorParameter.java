package omics.gui.parameter;

import javafx.scene.paint.Color;
import omics.gui.parameter.editor.ColorEditor;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 26 May 2020, 10:38 AM
 */
public class ColorParameter extends AbstractParameter<Color>
{
    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public ColorParameter(String name, String description, String category)
    {
        super(name, description, category, ColorEditor.class);
    }

    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public ColorParameter(String name, String description, String category, Color defaultColor)
    {
        super(name, description, category, ColorEditor.class);
        setValue(defaultColor);
    }
}
