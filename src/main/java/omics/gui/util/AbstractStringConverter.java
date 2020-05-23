package omics.gui.util;

import javafx.util.StringConverter;
import omics.util.utils.Pair;

import java.util.ArrayList;
import java.util.List;

import static omics.util.utils.ObjectUtils.checkNotNull;

/**
 * abstract StringConverter, custom for some value.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 10:57 AM
 */
public abstract class AbstractStringConverter<T> extends StringConverter<T>
{
    private final List<Pair<String, T>> nameValueList = new ArrayList<>(1);

    public AbstractStringConverter()
    {
        this(new ArrayList<>());
    }

    public AbstractStringConverter(List<Pair<String, T>> nameValueList)
    {
        checkNotNull(nameValueList);
        this.nameValueList.addAll(nameValueList);
    }

    @Override
    public String toString(T value)
    {
        if (value == null)
            return "";

        if (!nameValueList.isEmpty()) {
            for (Pair<String, T> pair : nameValueList) {
                if (value.equals(pair.getValue()))
                    return pair.getKey();
            }

        }
        return repr(value);
    }

    /**
     * Return String representation for the value
     *
     * @param value value to display
     */
    protected abstract String repr(T value);

    @Override
    public T fromString(String value)
    {
        if (value == null)
            return null;
        value = value.trim();
        if (value.isEmpty())
            return null;

        if (!nameValueList.isEmpty()) {
            for (Pair<String, T> pair : nameValueList) {
                if (value.equals(pair.getKey()))
                    return pair.getValue();
            }
        }

        return parse(value);
    }

    /**
     * Parse given value.
     */
    protected abstract T parse(String value);
}
