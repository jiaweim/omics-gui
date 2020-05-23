package omics.gui.parameter;

import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

/**
 * Parameter GUI control
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 20 May 2020, 11:04 PM
 */
public interface Parameter<T> extends PropertySheet.Item
{
    @Override
    String getName();

    @Override
    String getDescription();

    @Override
    String getCategory();

    @Override
    T getValue();

    @Override
    void setValue(Object value);

    @Override
    Class<?> getType();

    @Override
    default Optional<ObservableValue<? extends Object>> getObservableValue()
    {
        return Optional.empty();
    }
}
