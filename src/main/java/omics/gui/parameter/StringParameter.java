package omics.gui.parameter;

import omics.gui.parameter.editor.StringEditor;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 1:11 PM
 */
public class StringParameter extends AbstractParameter<String>
{
    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public StringParameter(String name, String description, String category)
    {
        super(name, description, category, StringEditor.class);
    }

    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public StringParameter(String name, String description, String category, String defaultValue)
    {
        super(name, description, category, StringEditor.class);
        setValue(defaultValue);
    }
}
