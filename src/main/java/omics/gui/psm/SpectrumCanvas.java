package omics.gui.psm;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import omics.gui.psm.util.NodeUtils;
import omics.util.ms.peaklist.PeakAnnotation;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.impl.DoublePeakList;
import omics.util.protein.ms.PeptideFragmentAnnotation;
import omics.util.utils.NumberFormatFactory;

import java.text.NumberFormat;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author JiaweiMao
 * @version 2.0.0
 * @since 25 Dec 2017, 8:12 AM
 */
public class SpectrumCanvas extends Canvas
{
    private static final NumberFormat FORMAT_IONCURRENT = NumberFormatFactory.valueOf("0.0E0");
    private static final double[] DeltaMzs = new double[]{100, 50, 20, 10, 5, 2, 1};
    private static final Color Color_DefaultAnno = Color.GRAY;

    // axis label font
    private final Font axisFont = Font.font("Arial", FontWeight.SEMI_BOLD, 14);
    // peak annotation font
    private final Font peakLabelFont = Font.font("Calibri", FontWeight.BOLD, 14);
    // The padding (distance between the axes and the border of the panel).
    private double iPadding = 4;
    private double iOriginX;
    private double iOriginY;
    private double iMaxX;
    /**
     * minimum y value in the canvas
     */
    private double iMinY;
    /**
     * The Y value of 100 relative intensity
     */
    private double iMinMeanY;
    /**
     * width of the outer rectangle
     */
    private double iAxisWidth = 2;
    private double iMajorTickLength = 8;
    /**
     * length of the minor tick.
     */
    private double iMinorTickLength = 5;
    private int iYMinorTickCount = 50;
    /**
     * count of major tick
     */
    private int iYMajorTickCount = 10;

    /**
     * padding between tick and its number
     */
    private int iTickPadding = 3;
    /**
     * line width of the tick.
     */
    private double iTickWidth = 1;
    private String iLabelX = "m/z";
    private String iLabelY1 = "Relative Abundance";
    private String iLabelY2 = "Ion Current";
    private Font iFontTick = Font.font("Arial", FontWeight.SEMI_BOLD, 12);
    private double iPeakWidth = 1.0;
    private double iPeakWidthAnnotation = 2;
    private double iPeakLabelPadding = 5;

    private PeakList<? extends PeakAnnotation> peakList = new DoublePeakList<>();
    // minimum m/z to display
    private double iMinMz;
    // maximum m/z to display
    private double iMaxMz;
    // mz per pixel
    private double iUnitX;
    // intensity value per pixel
    private double iUnitY;
    private PSMViewSettings config = new PSMViewSettings();

    private DoubleProperty iCurrentMz = new SimpleDoubleProperty();
    private DoubleProperty iCurrentIntensity = new SimpleDoubleProperty();

    public SpectrumCanvas()
    {
        this(0.0, 0.0);
    }

    public SpectrumCanvas(double width, double height)
    {
        super(width, height);

        this.widthProperty().addListener(evt -> draw());
        this.heightProperty().addListener(evt -> draw());

        addEvent();
        draw();
    }

    public PSMViewSettings getConfig()
    {
        return config;
    }

    public void setPeptideSpectrum(PeakList<? extends PeakAnnotation> spectrum)
    {
        this.peakList = spectrum;
        draw();
    }

    private void addEvent()
    {
        setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();

            if (x > iOriginX && x < iMaxX && y < iOriginY && y > iMinY) {
                double mz = (x - iOriginX) * iUnitX + iMinMz;
                double in = (iOriginY - y) * iUnitY;

                iCurrentMz.set(mz);
                iCurrentIntensity.set(in);
                setCursor(Cursor.CROSSHAIR);
            } else {
                iCurrentMz.set(0);
                iCurrentIntensity.set(0);
                setCursor(Cursor.DEFAULT);
            }
        });
    }

    private void draw()
    {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (peakList == null || peakList.isEmpty())
            return;

        drawFrame(gc);
        drawPeaks(gc);
    }

    /**
     * Draw the axis part which is not affected by peak list range.
     *
     * @param gc {@link GraphicsContext}.
     */
    private void drawFrame(GraphicsContext gc)
    {
        Bounds canvasBounds = getBoundsInLocal();

        Bounds labelY1Bounds = NodeUtils.textSize(axisFont, VPos.TOP, iLabelY1);
        Bounds tickY1Bounds = NodeUtils.textSize(iFontTick, VPos.TOP, "120");
        Bounds labelY2Bounds = NodeUtils.textSize(axisFont, VPos.TOP, iLabelY2);
        Bounds tickY2Bounds = NodeUtils.textSize(iFontTick, VPos.TOP, "1.0E10");
        Bounds labelXBounds = NodeUtils.textSize(axisFont, VPos.TOP, iLabelX);

        // as the rectangle has even width, its better to have interger position for sharp border
        iOriginX = (int) (canvasBounds.getMinX() + labelY1Bounds.getHeight() + iPadding + tickY1Bounds.getWidth() + iTickPadding + iMajorTickLength);
        iMaxX = (int) (canvasBounds.getMaxX() - labelY2Bounds.getHeight() - iPadding - tickY2Bounds.getWidth() - iTickPadding - iMajorTickLength);
        iOriginY = (int) (canvasBounds.getMaxY() - labelXBounds.getHeight() - iPadding - tickY1Bounds.getHeight() - iTickPadding - iMajorTickLength);
        iMinY = (int) (canvasBounds.getMinY() + iPadding);

        iMinMeanY = iMinY + (iOriginY - iMinY) / 6;

        gc.save();

        // draw the rectangle
        gc.setLineWidth(iAxisWidth);
        gc.strokeRect(iOriginX, iMinY, iMaxX - iOriginX, iOriginY - iMinY);

        // draw tick
        gc.setLineWidth(iTickWidth);

        // y minor tick
        double tickWidth = (iOriginY - iMinMeanY) / iYMinorTickCount;
        double xLoc = iOriginX - iMinorTickLength;
        for (int i = 0; i < iYMinorTickCount; i++) {
            int count = i + 1;
            if (count % (iYMinorTickCount / iYMajorTickCount) == 0)
                continue;
            double yLoc = snapOdd(iOriginY - tickWidth * count);
            gc.strokeLine(iOriginX, yLoc, xLoc, yLoc);
        }

        // y major tick and its tick number
        gc.setFont(iFontTick);
        gc.setTextBaseline(VPos.CENTER);

        tickWidth = (iOriginY - iMinMeanY) / iYMajorTickCount;
        xLoc = iOriginX - iMajorTickLength;
        double xLocY2 = iMaxX + iMajorTickLength;
        for (int i = 0; i < iYMajorTickCount + 1; i++) { // +1 for the end major tick

            double yLoc = snapOdd(iOriginY - tickWidth * i);

            gc.strokeLine(iOriginX, yLoc, xLoc, yLoc);
            gc.strokeLine(iMaxX, yLoc, xLocY2, yLoc);

            String tickLabel = String.valueOf(100 * i / iYMajorTickCount);
            Bounds tickLabelBounds = NodeUtils.textSize(iFontTick, VPos.TOP, tickLabel);

            double textX = xLoc - iTickPadding - tickLabelBounds.getWidth();
            gc.fillText(tickLabel, textX, yLoc);
        }

        gc.setTextAlign(TextAlignment.CENTER);

        double deltaMz = DeltaMzs[0];
        int deltaIdx = 1;
        while (peakList.getX(peakList.size() - 1) < (peakList.getX(0) + 10 * deltaMz)) {
            deltaMz = DeltaMzs[deltaIdx];
            deltaIdx++;
            if (deltaIdx == DeltaMzs.length)
                break;
        }

        iMinMz = ((int) (peakList.getX(0) / deltaMz)) * deltaMz;
        int xMajorTickCount = (int) ((peakList.getX(peakList.size() - 1) - iMinMz) / deltaMz) + 1;
        iMaxMz = iMinMz + xMajorTickCount * deltaMz;

        int precision = -(int) Math.log10(deltaMz);
        if (precision < 0)
            precision = 0;

        NumberFormat format = NumberFormatFactory.valueOf(precision);
        double space = (iMaxX - iOriginX) / xMajorTickCount;

        // x m/z labels
        double yLoc = iOriginY + iMajorTickLength + iPadding;
        for (int i = 0; i < xMajorTickCount + 1; i++) {
            double x = iOriginX + space * i;
            double value = iMinMz + i * deltaMz;
            gc.fillText(format.format(value), x, yLoc);
        }

        // y2-axis ion current tick labels.
        gc.setTextAlign(TextAlignment.LEFT);
        double inUnit = peakList.getBasePeakX() / 10;
        double tickUnit = (iOriginY - iMinMeanY) / iYMajorTickCount;
        xLoc = iMaxX + iMajorTickLength + iTickPadding;
        for (int i = 0; i <= iYMajorTickCount; i++) {
            yLoc = iOriginY - i * tickUnit;
            double inValue = inUnit * i;
            gc.fillText(FORMAT_IONCURRENT.format(inValue), xLoc, yLoc);
        }

        // x minor tick
        tickWidth = (iMaxX - iOriginX) / (xMajorTickCount * 10);
        yLoc = iOriginY + iMinorTickLength;
        for (int i = 0; i < xMajorTickCount * 10; i++) {
            int count = i + 1;
            if (count % 10 == 0)
                continue;
            double xTickxLoc = snapOdd(iOriginX + tickWidth * count);
            gc.strokeLine(xTickxLoc, iOriginY, xTickxLoc, yLoc);
        }

        // x major tick
        tickWidth = (iMaxX - iOriginX) / xMajorTickCount;
        yLoc = iOriginY + iMajorTickLength;
        for (int i = 0; i < xMajorTickCount + 1; i++) {
            xLoc = snapOdd(iOriginX + tickWidth * i);
            gc.strokeLine(xLoc, iOriginY, xLoc, yLoc);
        }

        // x label
        double xLabelCornerX = (iOriginX + iMaxX - labelXBounds.getWidth()) / 2;
        double xLabelCornerY = (canvasBounds.getMaxY() - labelXBounds.getHeight());
        gc.setFont(axisFont);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(iLabelX, xLabelCornerX, xLabelCornerY);

        // y1 label left bottom point
        double y1LabelCornerY = (iMinY + iOriginY + +(iOriginY - iMinY) / 6 + labelY1Bounds.getWidth()) / 2;
        double y1LabelCornerX = iOriginX - iMajorTickLength - iTickPadding - tickY1Bounds.getHeight() - iPadding;

        gc.setTextBaseline(VPos.BOTTOM);
        gc.rotate(-90);

        gc.fillText(iLabelY1, -y1LabelCornerY, y1LabelCornerX);

        // y2 label left top point
        double y2LabelY = (iMinY + iOriginY + (iOriginY - iMinY) / 6 - labelY2Bounds.getWidth()) / 2;
        double y2LabelX = iMaxX + iMajorTickLength + iTickPadding + tickY2Bounds.getHeight() + iPadding + labelY2Bounds.getHeight();
        gc.rotate(180);
        gc.fillText(iLabelY2, y2LabelY, -y2LabelX);

        gc.restore();
    }

    private void drawPeaks(GraphicsContext gc)
    {
        gc.save();
        gc.setLineWidth(iPeakWidth);
        gc.setFont(peakLabelFont);
        gc.setTextBaseline(VPos.CENTER);

        double baseIn = peakList.getBasePeakY();

        iUnitX = (iMaxMz - iMinMz) / (iMaxX - iOriginX);
        iUnitY = baseIn / (iOriginY - iMinMeanY);

        for (int i = 0; i < peakList.size(); i++) {

            double mz = peakList.getX(i);
            double intensity = peakList.getY(i);

            double xPos = iOriginX + (mz - iMinMz) / iUnitX;
            double yPos = iOriginY - intensity / iUnitY;

            double oddX = snapOdd(xPos);
            gc.strokeLine(oddX, iOriginY, oddX, yPos);

            if (peakList.hasAnnotationsAt(i)) {
                gc.save();
                gc.setLineWidth(iPeakWidthAnnotation);

                List<? extends PeakAnnotation> annotations = peakList.getAnnotations(i);
                PeakAnnotation annotation = annotations.get(0);
                Color color = Color_DefaultAnno;
                if (annotation instanceof PeptideFragmentAnnotation) {
                    PeptideFragmentAnnotation fragAnnotation = (PeptideFragmentAnnotation) annotation;
                    color = config.getColor(fragAnnotation.getIon());
                }

                gc.setStroke(color);
                int evenX = (int) xPos;
                gc.strokeLine(evenX, iOriginY, evenX, yPos);

                gc.setFill(color);

                String label = getAnnotationsLabel(annotations);

                gc.rotate(-90);
                gc.fillText(label, -(yPos - iPeakLabelPadding), xPos, yPos - iPeakLabelPadding);
                gc.restore();
            }
        }

        gc.restore();
    }

    private String getAnnotationsLabel(List<? extends PeakAnnotation> annotations)
    {
        if (annotations.size() == 1) {
            return annotations.get(0).getSymbol();
        } else {
            StringJoiner joiner = new StringJoiner("|");
            for (PeakAnnotation annotation : annotations) {
                joiner.add(annotation.getSymbol());
            }
            return joiner.toString();
        }
    }

    /**
     * for line with odd width, to have sharp border, its position should be half.
     *
     * @param y position
     * @return modified position.
     */
    private double snapOdd(double y)
    {
        return ((int) y) + .5;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public double prefWidth(double height)
    {
        return getWidth();
    }

    public DoubleProperty currentMz()
    {
        return iCurrentMz;
    }

    public DoubleProperty currentIntensity()
    {
        return iCurrentIntensity;
    }

    @Override
    public double prefHeight(double width)
    {
        return getHeight();
    }
}
