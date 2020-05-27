package omics.gui.util;

import omics.util.utils.Pair;

import java.util.Collections;
import java.util.List;

/**
 * Converter for integer value, for {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE}, display "No Limit"
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 01 Nov 2018, 12:52 PM
 */
public class IntegerStringConverterV2 extends AbstractStringConverter<Integer>
{
    /**
     * A general converter
     */
    public IntegerStringConverterV2()
    {
        super();
    }

    public IntegerStringConverterV2(String name, Integer value)
    {
        this(Collections.singletonList(Pair.create(name, value)));
    }

    /**
     * Create a converter with given name value map
     *
     * @param nameValueList name value map list.
     */
    public IntegerStringConverterV2(List<Pair<String, Integer>> nameValueList)
    {
        super(nameValueList);
    }

    @Override
    protected String repr(Integer value)
    {
        return value.toString();
    }

    @Override
    protected Integer parse(String value)
    {
        return Integer.parseInt(value);
    }
}
