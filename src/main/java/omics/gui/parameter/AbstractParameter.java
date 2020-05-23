package omics.gui.parameter;

import javafx.beans.value.ObservableValue;
import omics.gui.parameter.editor.ParameterEditor;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.Optional;

/**
 * Parameter abstract implementation
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 20 May 2020, 11:18 PM
 */
public abstract class AbstractParameter<T> implements Parameter<T>
{
    private final String name;
    private final String description;
    private final String category;
    private T value;

    private final Class<? extends ParameterEditor<T>> editorClass;

    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     * @param editorClass {@link ParameterEditor} of this parameter
     */
    public AbstractParameter(String name, String description, String category,
            Class<? extends ParameterEditor<T>> editorClass)
    {
        this.name = name;
        this.description = description;
        this.category = category;
        this.editorClass = editorClass;
    }

    /**
     * set the default value
     *
     * @param value parameter value
     * @return this
     */
    public AbstractParameter<T> defaultValue(T value)
    {
        setValue(value);
        return this;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getCategory()
    {
        return category;
    }

    @Override
    public Class<?> getType()
    {
        return Object.class;
    }

    @Override
    public T getValue()
    {
        return value;
    }

    @Override
    public void setValue(Object value)
    {
        this.value = (T) value;
    }

    @Override
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass()
    {
        return Optional.of(editorClass);
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue()
    {
        return Optional.empty();
    }
}
