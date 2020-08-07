package omics.gui.psm;

import omics.util.utils.NumberFormatFactory;
import omics.util.utils.Pair;

import java.text.NumberFormat;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 07 Aug 2020, 10:26 AM
 */
public class SpectrumChartUtils
{
    /**
     * suitable tick count for the width
     *
     * @return major and minor tick count
     */
    public static Pair<Integer, Integer> getTickCount(double width, int pixelForEachTick)
    {
        int count = (int) width / pixelForEachTick;
        int major = (int) Math.floor(count / 10.);
        if (count < 100) {
            return Pair.create(major, 5);
        } else {
            return Pair.create(major, 10);
        }
    }

    /**
     * Calculate a suitable range for the given data.
     *
     * @param minMz      minimum m/z
     * @param maxMz      maximum m/z
     * @param rangeCount number of major tick count (in fact, it is the number of major range count)
     * @return major tick range
     */
    public static double calRange(double minMz, double maxMz, int rangeCount)
    {
        double size = (maxMz - minMz) / rangeCount;

        double x = Math.floor(Math.log10(size));
        double powx = Math.pow(10, x);
        return Math.ceil(size / powx) * powx;
    }

    private static final NumberFormat SCI = NumberFormatFactory.valueOf("0.0E0");
    private static final NumberFormat DIGIT2 = NumberFormatFactory.valueOf("0.00");
    private static final NumberFormat DIGIT4 = NumberFormatFactory.valueOf("0.0000");

    public static NumberFormat getFormat(double maxValue)
    {
        if (maxValue >= 1E3) {
            return SCI;
        } else if (maxValue > 1) {
            return DIGIT2;
        } else if (maxValue > 1E-3) {
            return DIGIT4;
        } else {
            return SCI;
        }
    }
}
