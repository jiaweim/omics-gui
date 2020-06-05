package omics.gui.util;

import omics.util.utils.Pair;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 8:44 AM
 */
public class DoubleStringConverter2 extends AbstractStringConverter<Double>
{
    private final NumberFormat numberFormat;

    public DoubleStringConverter2()
    {
        this(null);
    }


    public DoubleStringConverter2(NumberFormat numberFormat)
    {
        this(new ArrayList<>(), numberFormat);
    }

    public DoubleStringConverter2(String name, double value)
    {
        this(Collections.singletonList(Pair.create(name, value)), null);
    }

    public DoubleStringConverter2(List<Pair<String, Double>> pairList, NumberFormat numberFormat)
    {
        super(pairList);
        this.numberFormat = numberFormat;
    }

    @Override
    protected String repr(Double value)
    {
        if (numberFormat != null)
            return numberFormat.format(value);
        return value.toString();
    }

    @Override
    protected Double parse(String value)
    {
        return Double.valueOf(value);
    }
}
