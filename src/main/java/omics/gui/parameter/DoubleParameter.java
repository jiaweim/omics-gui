package omics.gui.parameter;

import javafx.util.StringConverter;
import omics.gui.parameter.editor.DoubleEditor;
import omics.gui.util.DoubleStringConverter2;

import java.text.NumberFormat;

/**
 * Double parameter
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 8:22 AM
 */
public class DoubleParameter extends CandidateParameter<Double> implements Convertable<Double>
{
    private NumberFormat numberFormat;

    public DoubleParameter(String name, String description, String category)
    {
        super(name, description, category, DoubleEditor.class);
    }

    public DoubleParameter(String name, String description, String category, double defaultValue)
    {
        super(name, description, category, DoubleEditor.class);
        setValue(defaultValue);
    }

    /**
     * Set of {@link NumberFormat} to format value
     */
    public DoubleParameter numberFormat(NumberFormat numberFormat)
    {
        this.numberFormat = numberFormat;
        return this;
    }

    public StringConverter<Double> getConverter()
    {
        return new DoubleStringConverter2(getNameValueList(), numberFormat);
    }
}
