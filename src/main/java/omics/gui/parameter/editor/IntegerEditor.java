package omics.gui.parameter.editor;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import omics.gui.parameter.IntegerParameter;
import org.controlsfx.control.PropertySheet;

import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 8:05 AM
 */
public class IntegerEditor extends HBox implements ParameterEditor<Integer>
{
    private final ComboBox<Integer> control;

    public IntegerEditor(PropertySheet.Item parameter)
    {
        if (!(parameter instanceof IntegerParameter))
            throw new IllegalArgumentException();
        IntegerParameter integerParameter = (IntegerParameter) parameter;
        this.control = new ComboBox<>();
        this.control.setEditable(true);
        control.setConverter(integerParameter.getConverter());
        List<Integer> candidateValues = integerParameter.getCandidateValues();
        if (!candidateValues.isEmpty()) {
            control.getItems().addAll(candidateValues);
        }

        getChildren().add(control);
    }

    @Override
    public Node getEditor()
    {
        return this;
    }

    @Override
    public Integer getValue()
    {
        return control.getValue();
    }

    @Override
    public void setValue(Integer value)
    {
        this.control.setValue(value);
    }
}
