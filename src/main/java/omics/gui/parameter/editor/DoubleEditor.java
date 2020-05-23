package omics.gui.parameter.editor;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import omics.gui.parameter.DoubleParameter;
import org.controlsfx.control.PropertySheet;

import java.util.List;

/**
 * Double parameter editor.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 8:21 AM
 */
public class DoubleEditor extends HBox implements ParameterEditor<Double>
{
    private final ComboBox<Double> control;

    public DoubleEditor(PropertySheet.Item parameter)
    {
        if (!(parameter instanceof DoubleParameter))
            throw new IllegalArgumentException();
        DoubleParameter dp = (DoubleParameter) parameter;
        control = new ComboBox<>();
        control.setEditable(true);
        control.setConverter(dp.getConverter());
        List<Double> candidateValues = dp.getCandidateValues();
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
    public Double getValue()
    {
        return control.getValue();
    }

    @Override
    public void setValue(Double value)
    {
        control.setValue(value);
    }
}
