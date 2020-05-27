package omics.gui.parameter;

import omics.gui.parameter.editor.ToleranceEditor;
import omics.util.ms.peaklist.Tolerance;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 26 May 2020, 10:19 PM
 */
public class ToleranceParameter extends AbstractParameter<Tolerance>
{
    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public ToleranceParameter(String name, String description, String category)
    {
        super(name, description, category, ToleranceEditor.class);
    }
}
