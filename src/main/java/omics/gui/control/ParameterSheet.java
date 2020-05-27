package omics.gui.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import omics.gui.parameter.Parameter;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Parameters Sheet
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 20 May 2020, 11:03 PM
 */
public class ParameterSheet extends PropertySheet
{
    private final List<Parameter<?>> parameters;
    private final ParameterEditorFactory editorFactory;

    public ParameterSheet()
    {
        this(new ArrayList<>());
    }

    public ParameterSheet(List<Parameter<?>> parameters)
    {
        super((ObservableList) FXCollections.observableList(parameters));

        this.parameters = parameters;
        setSearchBoxVisible(false);
        setModeSwitcherVisible(false);
        setMode(Mode.NAME);

        // Set editor factory to keep track of which editing component belongs
        // to which parameter
        this.editorFactory = new ParameterEditorFactory();
        setPropertyEditorFactory(editorFactory);
    }

    public void add(Parameter<?> parameter)
    {
        this.parameters.add(parameter);
    }

    public <ValueType> PropertyEditor<ValueType> getEditorForParameter(Parameter<ValueType> parameter)
    {
        // Let's lookup the parameter by name, because the actual instance may
        // differ due to the cloning of parameter sets
        Parameter<?> actualParameter = null;
        for (Parameter<?> p : parameters) {
            if (p.getName().equals(parameter.getName()))
                actualParameter = p;
        }

        if (actualParameter == null)
            throw new IllegalArgumentException(
                    "Parameter " + parameter.getName() + " not found in this component");

        return editorFactory.getEditorForItem(actualParameter);
    }
}
