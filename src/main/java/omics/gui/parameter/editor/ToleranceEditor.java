package omics.gui.parameter.editor;

import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import omics.gui.parameter.ToleranceParameter;
import omics.gui.util.DoubleStringConverter2;
import omics.util.ms.peaklist.Tolerance;
import org.controlsfx.control.PropertySheet;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 26 May 2020, 10:19 PM
 */
public class ToleranceEditor extends HBox implements ParameterEditor<Tolerance>
{
    private final ComboBox<Double> leftValue;
    private final ComboBox<Double> rightValue;
    private final ChoiceBox<String> unit;

    public ToleranceEditor(PropertySheet.Item item)
    {
        if (!(item instanceof ToleranceParameter))
            throw new IllegalArgumentException();
        setSpacing(6);
        leftValue = new ComboBox<>();
        rightValue = new ComboBox<>();
        leftValue.setEditable(true);
        rightValue.setEditable(true);
        DoubleStringConverter2 converter2 = new DoubleStringConverter2();
        leftValue.setConverter(converter2);
        rightValue.setConverter(converter2);

        unit = new ChoiceBox<>();

        leftValue.setPrefWidth(80);
        rightValue.setPrefWidth(80);
        unit.setPrefWidth(80);

        unit.getItems().addAll("ppm", "Da");
        getChildren().addAll(leftValue, rightValue, unit);
    }

    @Override
    public Node getEditor()
    {
        return this;
    }

    @Override
    public Tolerance getValue()
    {
        Double left = this.leftValue.getValue();
        Double right = rightValue.getValue();
        String unit = this.unit.getSelectionModel().getSelectedItem();
        if (unit.equals("ppm"))
            return Tolerance.ppm(left, right);
        else
            return Tolerance.abs(left, right);
    }

    @Override
    public void setValue(Tolerance value)
    {
        if (value == null)
            return;
        leftValue.setValue(value.getMinusError());
        rightValue.setValue(value.getPlusError());
        if (value.isAbsolute()) {
            unit.getSelectionModel().select(1);
        } else {
            unit.getSelectionModel().select(0);
        }
    }
}
