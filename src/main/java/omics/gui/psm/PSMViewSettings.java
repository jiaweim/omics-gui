package omics.gui.psm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import omics.gui.glycan.SNFGShape;
import omics.util.ms.peaklist.Tolerance;
import omics.util.protein.mod.NeutralLoss;
import omics.util.protein.ms.Ion;
import omics.util.protein.ms.PeptideIon;
import omics.util.protein.ms.PeptideNeutralLoss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Style configuration of annotated spectrum.
 *
 * @author JiaweiMao
 * @version 1.1.0
 * @since 19 Jul 2018, 5:54 PM
 */
public final class PSMViewSettings
{
    /**
     * width of normal peaks
     */
    private double peakWidth = 1;
    /**
     * width of annotated peaks
     */
    private double annotatedPeakWidth = 2;
    private Font peakLabelFont = Font.font("Sans-Serif", FontWeight.NORMAL, 14);
    private Font aminoAcidLabelFont = Font.font("Sans-Serif", FontWeight.SEMI_BOLD, 12);
    private Font aminoAcidFont = Font.font("Sans-Serif", FontWeight.BOLD, 24);
    private Font modificationFont = Font.font("Sans-Serif", FontWeight.BOLD, 12);

    private Font titleFont = Font.font("Arial", FontWeight.SEMI_BOLD, 14);
    private int titleLeftSpace = 6;

    private Color frameColor = Color.rgb(127, 127, 127);
    /**
     * color for ticks
     */
    private Color tickColor = Color.rgb(127, 127, 127);
    /**
     * color of general peaks
     */
    private Color peakColor = Color.rgb(90, 90, 90);

    private int frameStrokeWidth = 2;

    private int yMajorTickCount = 5;
    private int yMinorTickCount = 5;

    private int xMajorRangeCount = -1;
    private int pixelForEachTick = 5;
    private int tickWidth = 1;
    /**
     * tick length
     */
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

    private int peptideAnnotationLineWidth = 2;
    private int aminoAcidPad = 6;
    private int peptideLabelLineSpace = 2;
    private int aminoAcidLineSpace = 4;
    private int aminoAcidModificationSpace = -2;

    private Tolerance fragmentTolerance = Tolerance.abs(0.05);
    private Tolerance deisotopeTolerance = Tolerance.abs(0.02);
    private boolean deisotope = false;
    private boolean matchMostIntense = true;
    private boolean filterPeaks = false;
    private int peakCount = 10;
    private double binWidth = 110;

    private final Map<Ion, Color> ionTypeColorMap = new HashMap<>();
    private final Table<Ion, NeutralLoss, Boolean> ionShowTable = HashBasedTable.create();
    private final Map<Ion, Integer> ionMinZMap = new HashMap<>();
    private final Map<Ion, Integer> ionMaxZMap = new HashMap<>();
    private boolean showNCore = false;
    private int nCoreMinCharge = 1;
    private int nCoreMaxCharge = Integer.MAX_VALUE;
    private Color ncoreColor = Color.rgb(118, 0, 161);

    public PSMViewSettings()
    {
        ionTypeColorMap.put(Ion.a, Color.rgb(0, 28, 127));
        ionTypeColorMap.put(Ion.b, Color.rgb(0, 99, 115));
        ionTypeColorMap.put(Ion.c, Color.rgb(1, 117, 23));
        ionTypeColorMap.put(Ion.x, Color.rgb(228, 114, 114));
        ionTypeColorMap.put(Ion.y, Color.rgb(190, 9, 0));
        ionTypeColorMap.put(Ion.z, Color.rgb(244, 121, 32));
        ionTypeColorMap.put(Ion.p, Color.rgb(118, 0, 161));
        ionTypeColorMap.put(Ion.ya, Color.web("#636EFA"));
        ionTypeColorMap.put(Ion.yb, Color.web("#FF6692"));

        ionTypeColorMap.put(Ion.im, Color.rgb(160, 160, 160));
        ionTypeColorMap.put(Ion.b_HexNAc, Color.rgb(0, 165, 191));
        ionTypeColorMap.put(Ion.y_HexNAc, Color.rgb(35, 149, 31));

        ionTypeColorMap.put(Ion.B, SNFGShape.ORANGE);
        ionTypeColorMap.put(Ion.Y, Color.rgb(118, 0, 161));

        initIon();
    }

    private void initIon()
    {
        Ion[] ions = new Ion[]{Ion.a, Ion.b, Ion.c, Ion.x, Ion.y, Ion.z, Ion.p, Ion.ya, Ion.yb, Ion.im,
                Ion.B, Ion.Y, Ion.b_HexNAc, Ion.y_HexNAc};

        NeutralLoss[] losses = new NeutralLoss[]{NeutralLoss.H2O_LOSS, NeutralLoss.NH3_LOSS, NeutralLoss.H3PO4_LOSS};

        for (Ion ion : ions) {
            ionShowTable.put(ion, NeutralLoss.EMPTY, false);
            ionMinZMap.put(ion, 1);
            ionMaxZMap.put(ion, 2);
        }
        ionMaxZMap.put(Ion.im, 1);
        ionMaxZMap.put(Ion.B, 1);
        ionMaxZMap.put(Ion.p, Integer.MAX_VALUE);
        ionMaxZMap.put(Ion.Y, Integer.MAX_VALUE);

        Ion[] ionLoss = new Ion[]{Ion.a, Ion.b, Ion.c, Ion.x, Ion.y, Ion.z, Ion.p, Ion.ya, Ion.yb};
        for (Ion ion : ionLoss) {
            for (NeutralLoss loss : losses) {
                ionShowTable.put(ion, loss, false);
            }
        }

        ionShowTable.put(Ion.b, NeutralLoss.EMPTY, true);
        ionShowTable.put(Ion.y, NeutralLoss.EMPTY, true);
        ionShowTable.put(Ion.p, NeutralLoss.EMPTY, true);
        ionShowTable.put(Ion.p, NeutralLoss.H2O_LOSS, true);
        ionShowTable.put(Ion.p, NeutralLoss.NH3_LOSS, true);
    }

    public int getMinCharge(Ion ion)
    {
        return ionMinZMap.get(ion);
    }

    public void setMinCharge(Ion ion, int z)
    {
        this.ionMinZMap.put(ion, z);
    }

    public int getMaxCharge(Ion ion)
    {
        return ionMaxZMap.get(ion);
    }

    public void setMaxCharge(Ion ion, int z)
    {
        this.ionMaxZMap.put(ion, z);
    }

    public Color getNcoreColor()
    {
        return ncoreColor;
    }

    public void setNcoreColor(Color ncoreColor)
    {
        this.ncoreColor = ncoreColor;
    }

    /**
     * @return true if show N-glycosylation core peaks.
     */
    public boolean isShowNCore()
    {
        return showNCore;
    }

    public void setShowNCore(boolean showNCore)
    {
        this.showNCore = showNCore;
    }

    public int getnCoreMinCharge()
    {
        return nCoreMinCharge;
    }

    public void setnCoreMinCharge(int nCoreMinCharge)
    {
        this.nCoreMinCharge = nCoreMinCharge;
    }

    public int getnCoreMaxCharge()
    {
        return nCoreMaxCharge;
    }

    public void setnCoreMaxCharge(int nCoreMaxCharge)
    {
        this.nCoreMaxCharge = nCoreMaxCharge;
    }

    /**
     * @return {@link PeptideIon} to be annotated with fixed max charge value
     */
    public List<PeptideIon> getFixedMaxChargePeptideIonList()
    {
        List<PeptideIon> peptideIonList = new ArrayList<>();

        test(Ion.im, peptideIonList);
//        test(Ion.B, peptideIonList);
        test(Ion.b_HexNAc, peptideIonList);
        test(Ion.y_HexNAc, peptideIonList);

        Ion[] ions = new Ion[]{Ion.a, Ion.b, Ion.c, Ion.x, Ion.y, Ion.z, Ion.ya, Ion.yb};

        PeptideNeutralLoss h2o = PeptideNeutralLoss.H2O();
        PeptideNeutralLoss nh3 = PeptideNeutralLoss.NH3();
        PeptideNeutralLoss h3po4 = PeptideNeutralLoss.H3PO4();
        for (Ion ion : ions) {
            test(ion, peptideIonList);
            test(ion, NeutralLoss.H2O_LOSS, h2o, peptideIonList);
            test(ion, NeutralLoss.NH3_LOSS, nh3, peptideIonList);
            test(ion, NeutralLoss.H3PO4_LOSS, h3po4, peptideIonList);
        }

        return peptideIonList;
    }

    private void test(Ion ion, List<PeptideIon> peptideIonList)
    {
        if (isShowIon(ion)) {
            for (int charge = getMinCharge(ion); charge <= getMaxCharge(ion); charge++) {
                PeptideIon peptideIon = new PeptideIon(ion, charge);
                peptideIonList.add(peptideIon);
            }
        }
    }

    private void test(Ion ion, NeutralLoss neutralLoss, PeptideNeutralLoss peptideNeutralLoss, List<PeptideIon> peptideIonList)
    {
        if (isShowIon(ion, neutralLoss)) {
            for (int charge = getMinCharge(ion); charge <= getMaxCharge(ion); charge++) {
                PeptideIon peptideIon = new PeptideIon(ion, peptideNeutralLoss, charge);
                peptideIonList.add(peptideIon);
            }
        }
    }

    /**
     * Return true if show the ion with given neutral loss.
     */
    public boolean isShowIon(Ion ion, NeutralLoss neutralLoss)
    {
        Boolean show = ionShowTable.get(ion, neutralLoss);
        if (show == null)
            return false;
        return show;
    }

    public void setShowIon(Ion ion, NeutralLoss neutralLoss, boolean show)
    {
        ionShowTable.put(ion, neutralLoss, show);
    }

    public void setShowIon(Ion ion, boolean show)
    {
        ionShowTable.put(ion, NeutralLoss.EMPTY, show);
    }


    /**
     * @return true if show the ion
     */
    public boolean isShowIon(Ion ion)
    {
        return isShowIon(ion, NeutralLoss.EMPTY);
    }

    /**
     * @return true if filter peaks before matching
     */
    public boolean isFilterPeaks()
    {
        return filterPeaks;
    }

    public void setFilterPeaks(boolean filterPeaks)
    {
        this.filterPeaks = filterPeaks;
    }

    /**
     * @return number of peaks retain for each bin
     */
    public int getPeakCount()
    {
        return peakCount;
    }

    public void setPeakCount(int peakCount)
    {
        this.peakCount = peakCount;
    }

    /**
     * @return m/z window width
     */
    public double getBinWidth()
    {
        return binWidth;
    }

    public void setBinWidth(double binWidth)
    {
        this.binWidth = binWidth;
    }

    /**
     * @return true if match the most intense peak within tolerance
     */
    public boolean isMatchMostIntense()
    {
        return matchMostIntense;
    }

    public void setMatchMostIntense(boolean matchMostIntense)
    {
        this.matchMostIntense = matchMostIntense;
    }

    /**
     * @return true if deisotope the spectrum
     */
    public boolean isDeisotope()
    {
        return deisotope;
    }

    public void setDeisotope(boolean deisotope)
    {
        this.deisotope = deisotope;
    }

    /**
     * @return Tolerance for deisotope spectrum
     */
    public Tolerance getDeisotopeTolerance()
    {
        return deisotopeTolerance;
    }

    public void setDeisotopeTolerance(Tolerance deisotopeTolerance)
    {
        this.deisotopeTolerance = deisotopeTolerance;
    }

    /**
     * @return fragment tolerance for peak matching
     */
    public Tolerance getFragmentTolerance()
    {
        return fragmentTolerance;
    }

    /**
     * @param fragmentTolerance fragment tolerance for peak matching
     */
    public void setFragmentTolerance(Tolerance fragmentTolerance)
    {
        this.fragmentTolerance = fragmentTolerance;
    }

    /**
     * @return the space between title and left drawing area bound
     */
    public int getTitleLeftSpace()
    {
        return titleLeftSpace;
    }

    /**
     * @param titleLeftSpace the space between title and left drawing area bound
     */
    public void setTitleLeftSpace(int titleLeftSpace)
    {
        this.titleLeftSpace = titleLeftSpace;
    }

    /**
     * @return title font
     */
    public Font getTitleFont()
    {
        return titleFont;
    }

    /**
     * @param titleFont title font
     */
    public void setTitleFont(Font titleFont)
    {
        this.titleFont = titleFont;
    }

    /**
     * @return the font for modification annotation
     */
    public Font getModificationFont()
    {
        return modificationFont;
    }

    /**
     * @param modificationFont font for modification annotation
     */
    public void setModificationFont(Font modificationFont)
    {
        this.modificationFont = modificationFont;
    }

    /**
     * @return vertical space between amino acid and modification
     */
    public int getAminoAcidModificationSpace()
    {
        return aminoAcidModificationSpace;
    }

    public void setAminoAcidModificationSpace(int aminoAcidModificationSpace)
    {
        this.aminoAcidModificationSpace = aminoAcidModificationSpace;
    }

    /**
     * @return space between amino acid and the annotation line
     */
    public int getAminoAcidLineSpace()
    {
        return aminoAcidLineSpace;
    }

    /**
     * @param aminoAcidLineSpace space between amino acid and the bottom and up annotation line
     */
    public void setAminoAcidLineSpace(int aminoAcidLineSpace)
    {
        this.aminoAcidLineSpace = aminoAcidLineSpace;
    }

    /**
     * @return left and right pad of amino acid
     */
    public int getAminoAcidPad()
    {
        return aminoAcidPad;
    }

    /**
     * @param aminoAcidPad left and right pad of amino acid
     */
    public void setAminoAcidPad(int aminoAcidPad)
    {
        this.aminoAcidPad = aminoAcidPad;
    }

    /**
     * The label-line space of peptide sequence annotation, default to be 2.
     *
     * @return space between label and horizontal line of peptide sequence annotation.
     */
    public int getPeptideLabelLineSpace()
    {
        return peptideLabelLineSpace;
    }

    /**
     * setter of the peptide label-line space.
     *
     * @param peptideLabelLineSpace label-line space
     */
    public void setPeptideLabelLineSpace(int peptideLabelLineSpace)
    {
        this.peptideLabelLineSpace = peptideLabelLineSpace;
    }

    /**
     * @return peptide annotation line width
     */
    public int getPeptideAnnotationLineWidth()
    {
        return peptideAnnotationLineWidth;
    }

    /**
     * setter of the peptide annotation line width
     */
    public void setPeptideAnnotationLineWidth(int peptideAnnotationLineWidth)
    {
        this.peptideAnnotationLineWidth = peptideAnnotationLineWidth;
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

    /**
     * @return the amino acid annotation font size of peptide sequence
     */
    public Font getAminoAcidLabelFont()
    {
        return aminoAcidLabelFont;
    }

    /**
     * @param aminoAcidLabelFont the amino acid annotation font size
     */
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
