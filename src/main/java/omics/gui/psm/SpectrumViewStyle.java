package omics.gui.psm;

import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import omics.pdk.ptm.glycosylation.util.SNFGShape;
import omics.util.protein.ms.Ion;

import java.util.HashMap;
import java.util.Map;

/**
 * Style configuration of annotated spectrum.
 *
 * @author JiaweiMao
 * @version 1.1.0
 * @since 19 Jul 2018, 5:54 PM
 */
public final class SpectrumViewStyle
{
    private final Map<Ion, Color> ionTypeColorMap;

    private double peakWidth = 1;
    private double annotatedPeakWidth = 2;
    private Font peakLabelFont = Font.font("Sans-Serif", FontWeight.NORMAL, 14);
    private Font aminoAcidLabelFont = Font.font("Sans-Serif", FontWeight.BOLD, 14);
    private Font aminoAcidFont = Font.font("Sans-Serif", FontWeight.BOLD, 18);

    private Color frameColor = Color.rgb(127, 127, 127);
    private Color tickColor = Color.rgb(127, 127, 127);
    private Color peakColor = Color.rgb(90, 90, 90);

    private int frameStrokeWidth = 2;
    private int yMajorTickCount = 10;
    private int xMajorRangeCount = -1;
    private int pixelForEachTick = 5;
    private int yMinorTickCount = 5;
    private int tickWidth = 1;
    private double majorTickLength = 8;
    private double middleTickLength = 5;
    private double minorTickLength = 2;

    private Font axisLabelFont = Font.font("Arial", FontWeight.SEMI_BOLD, 14);
    private Font tickLabelFont = Font.font("Arial", FontWeight.SEMI_BOLD, 12);

    private Insets padding = new Insets(0, 6, 6, 6); // padding around drawing area
    private double axisLabelTickNumberSpace = 2; // space between axis label and its tick number
    private double tickLabelSpace = 3; // padding between tick and its number

    private double peakLabelSpace = 6;
    private double blankAreaRatio = 1 / 10.;

    public SpectrumViewStyle()
    {
        this.ionTypeColorMap = new HashMap<>();

        this.ionTypeColorMap.put(Ion.a, Color.rgb(0, 28, 127));
//        this.ionTypeColorMap.put(FragmentIonType.B_ION, Color.rgb(0, 10, 255));
        this.ionTypeColorMap.put(Ion.b, Color.rgb(0, 99, 115));
        this.ionTypeColorMap.put(Ion.c, Color.rgb(1, 117, 23));
        this.ionTypeColorMap.put(Ion.b_HexNAc, Color.rgb(0, 165, 191));
        this.ionTypeColorMap.put(Ion.x, Color.rgb(228, 114, 114));
//        this.ionTypeColorMap.put(FragmentIonType.Y_ION, Color.rgb(196, 20, 36));
        this.ionTypeColorMap.put(Ion.y, Color.rgb(190, 9, 0));
        this.ionTypeColorMap.put(Ion.y_HexNAc, Color.rgb(35, 149, 31));
        this.ionTypeColorMap.put(Ion.z, Color.rgb(244, 121, 32));

        this.ionTypeColorMap.put(Ion.im, Color.rgb(160, 160, 160));
        this.ionTypeColorMap.put(Ion.B, SNFGShape.ORANGE);

        this.ionTypeColorMap.put(Ion.p, Color.rgb(118, 0, 161));
        this.ionTypeColorMap.put(Ion.p_H2O, Color.rgb(118, 0, 161));
        this.ionTypeColorMap.put(Ion.p_NH3, Color.rgb(118, 0, 161));
        this.ionTypeColorMap.put(Ion.Y, Color.rgb(118, 0, 161));
    }

    /**
     * @return the blank area ratio above the peaks
     */
    public double getBlankAreaRatio()
    {
        return blankAreaRatio;
    }

    public void setBlankAreaRatio(double blankAreaRatio)
    {
        this.blankAreaRatio = blankAreaRatio;
    }

    /**
     * @return the space between peak and its label.
     */
    public double getPeakLabelSpace()
    {
        return peakLabelSpace;
    }

    public void setPeakLabelSpace(double peakLabelSpace)
    {
        this.peakLabelSpace = peakLabelSpace;
    }

    /**
     * @return default {@link Color} to draw peaks.
     */
    public Color getPeakColor()
    {
        return peakColor;
    }

    /**
     * set the color to draw peaks.
     *
     * @param peakColor {@link Color} instance.
     */
    public void setPeakColor(Color peakColor)
    {
        this.peakColor = peakColor;
    }

    /**
     * @return {@link Color} for tick.
     */
    public Color getTickColor()
    {
        return tickColor;
    }

    public void setTickColor(Color tickColor)
    {
        this.tickColor = tickColor;
    }

    /**
     * @return space between tick and its label.
     */
    public double getTickLabelSpace()
    {
        return tickLabelSpace;
    }

    public void setTickLabelSpace(double tickLabelSpace)
    {
        this.tickLabelSpace = tickLabelSpace;
    }

    /**
     * @return the {@link Font} for axis tick label.
     */
    public Font getTickLabelFont()
    {
        return tickLabelFont;
    }

    public void setTickLabelFont(Font tickLabelFont)
    {
        this.tickLabelFont = tickLabelFont;
    }

    /**
     * @return space between axis label and the tick number
     */
    public double getAxisLabelTickNumberSpace()
    {
        return axisLabelTickNumberSpace;
    }

    public void setAxisLabelTickNumberSpace(double axisLabelTickNumberSpace)
    {
        this.axisLabelTickNumberSpace = axisLabelTickNumberSpace;
    }

    /**
     * @return padding around drawing area.
     */
    public Insets getPadding()
    {
        return padding;
    }

    public void setPadding(Insets padding)
    {
        this.padding = padding;
    }

    /**
     * @return {@link Font} for axis label.
     */
    public Font getAxisLabelFont()
    {
        return axisLabelFont;
    }

    public void setAxisLabelFont(Font axisLabelFont)
    {
        this.axisLabelFont = axisLabelFont;
    }

    /**
     * @return length of major tick
     */
    public double getMajorTickLength()
    {
        return majorTickLength;
    }

    public void setMajorTickLength(double majorTickLength)
    {
        this.majorTickLength = majorTickLength;
    }

    /**
     * @return length of middle tick
     */
    public double getMiddleTickLength()
    {
        return middleTickLength;
    }

    public void setMiddleTickLength(double middleTickLength)
    {
        this.middleTickLength = middleTickLength;
    }

    /**
     * @return length of minor tick.
     */
    public double getMinorTickLength()
    {
        return minorTickLength;
    }

    public void setMinorTickLength(double minorTickLength)
    {
        this.minorTickLength = minorTickLength;
    }

    /**
     * @return width of axis tick.
     */
    public int getTickWidth()
    {
        return tickWidth;
    }

    public void setTickWidth(int tickWidth)
    {
        this.tickWidth = tickWidth;
    }

    /**
     * @return the number of units between each major tick pair.
     */
    public int getYMinorTickCount()
    {
        return yMinorTickCount;
    }

    public void setYMinorTickCount(int yMinorTickCount)
    {
        this.yMinorTickCount = yMinorTickCount;
    }

    /**
     * @return the number of major ticks in Y-axis.
     */
    public int getYMajorTickCount()
    {
        return yMajorTickCount;
    }

    public void setYMajorTickCount(int yMajorTickCount)
    {
        this.yMajorTickCount = yMajorTickCount;
    }

    /**
     * @return the number of major ticks in X-axis.
     */
    public int getXMajorRangeCount()
    {
        return xMajorRangeCount;
    }

    public void setXMajorRangeCount(int xMajorTickCount)
    {
        this.xMajorRangeCount = xMajorTickCount;
    }

    /**
     * @return number of pixel distance for each minor tick.
     */
    public int getPixelForEachTick()
    {
        return pixelForEachTick;
    }

    public void setPixelForEachTick(int pixelForEachTick)
    {
        this.pixelForEachTick = pixelForEachTick;
    }

    /**
     * @return Color for the frame rectangle
     */
    public Color getFrameColor()
    {
        return frameColor;
    }

    /**
     * set the color for
     *
     * @param frameColor {@link Color} for frame.
     */
    public void setFrameColor(Color frameColor)
    {
        this.frameColor = frameColor;
    }

    /**
     * @return the stroke width of frame rectangle
     */
    public int getFrameStrokeWidth()
    {
        return frameStrokeWidth;
    }

    public void setFrameStrokeWidth(int frameStrokeWidth)
    {
        this.frameStrokeWidth = frameStrokeWidth;
    }

    public Font getAminoAcidLabelFont()
    {
        return aminoAcidLabelFont;
    }

    public void setAminoAcidLabelFont(Font aminoAcidLabelFont)
    {
        this.aminoAcidLabelFont = aminoAcidLabelFont;
    }

    public Font getAminoAcidFont()
    {
        return aminoAcidFont;
    }

    /**
     * set the {@link Font} to render amino acid.
     *
     * @param aminoAcidFont a {@link Font}
     */
    public void setAminoAcidFont(Font aminoAcidFont)
    {
        this.aminoAcidFont = aminoAcidFont;
    }

    public Font getPeakLabelFont()
    {
        return peakLabelFont;
    }

    public void setPeakLabelFont(Font peakLabelFont)
    {
        this.peakLabelFont = peakLabelFont;
    }

    /**
     * Set the color for given {@link Ion}
     *
     * @param ionType {@link Ion}
     * @param color   {@link Color}
     */
    public void setColor(Ion ionType, Color color)
    {
        ionTypeColorMap.put(ionType, color);
    }

    /**
     * Return the display {@link Color} of the given {@link Ion}.
     * <p>
     * Supported type: a, b, c, x, y, z, immonium, precursor.
     *
     * @param ionType {@link Ion}.
     * @return {@link Color} of the {@link Ion}, null for absent.
     */
    public Color getColor(Ion ionType)
    {
        return ionTypeColorMap.get(ionType);
    }

    public double getPeakWidth()
    {
        return peakWidth;
    }

    public void setPeakWidth(double peakWidth)
    {
        this.peakWidth = peakWidth;
    }

    /**
     * @return the peak width for annotated peak
     */
    public double getAnnotatedPeakWidth()
    {
        return annotatedPeakWidth;
    }

    public void setAnnotatedPeakWidth(double annotatedPeakWidth)
    {
        this.annotatedPeakWidth = annotatedPeakWidth;
    }
}
