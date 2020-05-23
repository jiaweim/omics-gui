package omics.gui.parameter;

import javafx.util.StringConverter;
import omics.gui.parameter.editor.IntegerEditor;
import omics.gui.util.IntegerStringConverterV2;

/**
 * Integer parameter
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 8:06 AM
 */
public class IntegerParameter extends CandidateParameter<Integer> implements Convertable<Integer>
{
    /**
     * Construct an integer parameter.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    category to show
     */
    public IntegerParameter(String name, String description, String category)
    {
        super(name, description, category, IntegerEditor.class);
    }

    /**
     * Construct an integer parameter.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    category to show
     */
    public IntegerParameter(String name, String description, String category, int defaultValue)
    {
        super(name, description, category, IntegerEditor.class);
        setValue(defaultValue);
    }

    /**
     * @return converter for this parameter.
     */
    public StringConverter<Integer> getConverter()
    {
        return new IntegerStringConverterV2(getNameValueList());
    }
}
