package omics.gui.parameter;

import omics.gui.parameter.editor.ParameterEditor;
import omics.util.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameter with candidate values.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 12:31 PM
 */
public abstract class CandidateParameter<T> extends AbstractParameter<T>
{
    private final List<T> candidateValues = new ArrayList<>();
    private final List<Pair<String, T>> nameValueList = new ArrayList<>(1);

    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     * @param editorClass {@link ParameterEditor} of this parameter
     */
    public CandidateParameter(String name, String description, String category, Class<? extends ParameterEditor<T>> editorClass)
    {
        super(name, description, category, editorClass);
    }

    /**
     * set the default value
     *
     * @param value parameter value
     * @return this
     */
    public CandidateParameter<T> defaultValue(T value)
    {
        setValue(value);
        return this;
    }

    /**
     * Add an name-value pair
     *
     * @param name  parameter name
     * @param value parameter value
     * @return this
     */
    public CandidateParameter<T> addPair(String name, T value)
    {
        this.nameValueList.add(Pair.create(name, value));
        return this;
    }

    /**
     * @return Predefined name-value pairs.
     */
    public List<Pair<String, T>> getNameValueList()
    {
        return nameValueList;
    }

    /**
     * Set candidate values.
     *
     * @param values candidate values
     * @return this
     */
    public CandidateParameter<T> candidateValues(List<T> values)
    {
        this.candidateValues.clear();
        this.candidateValues.addAll(values);
        return this;
    }

    /**
     * @return candidate values.
     */
    public List<T> getCandidateValues()
    {
        return candidateValues;
    }
}
