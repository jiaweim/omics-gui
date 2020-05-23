package omics.gui.parameter;

import omics.gui.parameter.editor.BooleanEditor;

/**
 * Boolean parameter.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 12:28 PM
 */
public class BooleanParameter extends AbstractParameter<Boolean>
{
    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public BooleanParameter(String name, String description, String category)
    {
        super(name, description, category, BooleanEditor.class);
    }

    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public BooleanParameter(String name, String description, String category, Boolean defaultValue)
    {
        super(name, description, category, BooleanEditor.class);
        setValue(defaultValue);
    }
}
