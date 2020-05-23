package omics.gui.parameter;

import javafx.util.StringConverter;

/**
 * Interface for control need a {@link StringConverter}
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 12:02 PM
 */
public interface Convertable<T>
{
    StringConverter<T> getConverter();
}
