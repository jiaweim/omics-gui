package omics.gui.psm;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import omics.gui.psm.util.NodeUtils;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.impl.DoublePeakList;
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.Pair;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static omics.util.utils.ObjectUtils.checkNotNull;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 05 Jun 2020, 12:20 PM
 */
public class DualPeptideSpectrumChart extends Pane
{
    private String label_Y1 = "Relative Abundance (%)";
    private String label_Y2 = "Intensity";
    private String label_X = "m/z";
    private Font peakLabelFont = Font.font("Sans-Serif", FontWeight.NORMAL, 14);
    private Font axisLabelFont = Font.font("Arial", FontWeight.SEMI_BOLD, 14);
    private Font tickLabelFont = Font.font("Arial", FontWeight.SEMI_BOLD, 12);
    private int tickWidth = 1;
    private Color tickColor = Color.rgb(128, 128, 128);

    private double majorTickLength = 8;
    private double middleTickLength = 5;
    private double minorTickLength = 2;

    private int yMajorTickCount = 5;
    private int yMinorTickCount = 5;

    private Insets axisLabelNumberPad = new Insets(2);
    private Insets axisTickLabelPad = new Insets(3);

    private int frameStrokeWidth = 2;
    private Color frameColor = Color.rgb(128, 128, 128);
    /**
     * padding around drawing area
     */
    private Insets outerPad = new Insets(0, 6, 6, 6);

    /**
     * start x of peak area
     */
    private double leftXLoc;
    /**
     * end x of peak area
     */
    private double rightXLoc;

    private double topFrameYLoc;
    private double topTitleYLoc;
    private double topPeptideYLoc;
    /**
     * top end of peak plotting area
     */
    private double topPeakYLoc;
    /**
     * y of middle line
     */
    private double middleYLoc;
    private double bottomPeptideYLoc;
    private double bottomPeakYLoc;
    private double bottomTitleYLoc;
    private double bottomFrameYLoc;

    private double titleRatio = 0.15;
    private double peptideRatio = 0.2;

    private double peakRatio = 0.9;
    private double peakAreaHeight;
    private int peakWidth = 1;
    private Color peakColor = Color.rgb(90, 90, 90);

    private int pixelForEachTick = 5;

    // m/z range
    private double minMz = 0;
    private double maxMz = 1000;

    /**
     * minimum m/z to display
     */
    private double scaleMinMz;
    /**
     * maximum m/z to display
     */
    private double scaleMaxMz;
    /**
     * base peak intensity of top peak list
     */
    private double topBasePeakIntensity;
    /**
     * base peak intensity of bottom peak list
     */
    private double bottomBasePeakIntensity;

    /**
     * min intensity to display in top panel
     */
    private double topMinIntensity = 0;
    /**
     * max intensity to display in top panel
     */
    private double topMaxIntensity = 100;
    /**
     * min intensity to display in bottom panel
     */
    private double bottomMinIntensity = 0;
    /**
     * max intensity to display in bottom panel
     */
    private double bottomMaxIntensity = 100;
    /**
     * mz per pixel
     */
    private double unitX;
    /**
     * intensity per pixel of top panel
     */
    private double topUnitY;
    /**
     * intensity per pixel of bottom panel
     */
    private double bottomUnitY;

    private final DoubleProperty mzProperty = new SimpleDoubleProperty();
    private final DoubleProperty intensityProperty = new SimpleDoubleProperty();

    public DualPeptideSpectrumChart()
    {
        widthProperty().addListener((observable, oldValue, newValue) -> repaint());
        heightProperty().addListener((observable, oldValue, newValue) -> repaint());

        addEvent();
    }

    /**
     * Return the m/z of given x location
     *
     * @param x x coordinate
     * @return m/z of given x
     */
    private double getMz(double x)
    {
        return (x - leftXLoc) * unitX + scaleMinMz;
    }

    private double getIntensity(double y)
    {
        if (y < middleYLoc) {
            return topMinIntensity + (middleYLoc - y) * topUnitY;
        } else if (y > middleYLoc) {
            return bottomMinIntensity + (y - middleYLoc) * bottomUnitY;
        } else {
            return 0;
        }
    }

    private Rectangle rectangle;
    private boolean newRectangleIsDrawing = false;
    private double startX;
    private double startY;

    private void addEvent()
    {
        setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();

            if (x > leftXLoc && x < rightXLoc && y < topPeakYLoc && y > bottomPeakYLoc) {
                double mz = getMz(x);
                double in = getIntensity(y);

                mzProperty.set(mz);
                intensityProperty.set(in);
                setCursor(Cursor.CROSSHAIR);
            } else {
                mzProperty.set(0);
                intensityProperty.set(0);
                setCursor(Cursor.DEFAULT);
            }
        });

        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                scaleMinMz = minMz;
                scaleMaxMz = maxMz;
                double y = event.getY();
                if (y < middleYLoc) {
                    topMinIntensity = 0;
                    topMaxIntensity = topBasePeakIntensity;
                } else if (y > middleYLoc) {
                    bottomMinIntensity = 0;
                    bottomMaxIntensity = bottomBasePeakIntensity;
                }
                repaint();
            }
        });

        setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY && !newRectangleIsDrawing) {
                rectangle = new Rectangle();
                startX = event.getX();
                startY = event.getY();
                rectangle.setFill(null);
                rectangle.setStroke(Color.BLACK);
                rectangle.setStrokeWidth(1.);
                getChildren().add(rectangle);
                newRectangleIsDrawing = true;
            }
        });

        setOnMouseDragged(event -> {
            if (newRectangleIsDrawing) {
                double currentX = event.getX();
                double currentY = event.getY();

                adjustRectangle(startX, startY, currentX, currentY);
            }
        });

        setOnMouseReleased(event -> {
            if (newRectangleIsDrawing) {
                newRectangleIsDrawing = false;
                double x = rectangle.getX();
                double y = rectangle.getY();
                double maxX = x + rectangle.getWidth();
                double maxY = y + rectangle.getHeight();

                getChildren().remove(rectangle);

                if (maxX <= leftXLoc || x >= rightXLoc || y > bottomFrameYLoc || maxY < topFrameYLoc)
                    return;

                double newMinMz = scaleMinMz;
                if (x > leftXLoc)
                    newMinMz = getMz(x);
                double newMaxMz = scaleMaxMz;
                if (maxX < rightXLoc)
                    newMaxMz = getMz(maxX);

                double newTopMaxIntensity = topMaxIntensity;
                double newBottomMinIntensity = bottomMinIntensity;
                if (y < middleYLoc) {
                    newTopMaxIntensity = getIntensity(y);
                } else if (y > middleYLoc) {
                    newBottomMinIntensity = getIntensity(y);
                }

                double newBottomMaxIntensity = bottomMaxIntensity;
                double newTopMinIntensity = topMinIntensity;
                if (maxY > middleYLoc) {
                    newBottomMaxIntensity = getIntensity(maxY);
                } else if (maxY < middleYLoc) {
                    newTopMinIntensity = getIntensity(maxY);
                }

                this.scaleMinMz = newMinMz;
                this.scaleMaxMz = newMaxMz;
                this.topMinIntensity = newTopMinIntensity;
                this.topMaxIntensity = newTopMaxIntensity;
                this.bottomMinIntensity = newBottomMinIntensity;
                this.bottomMaxIntensity = newBottomMaxIntensity;

                repaint();
            }
        });
    }

    private void adjustRectangle(double startX, double startY, double endX, double endY)
    {
        rectangle.setX(startX);
        rectangle.setY(startY);
        rectangle.setWidth(endX - startX);
        rectangle.setHeight(endY - startY);

        if (rectangle.getWidth() < 0) {
            rectangle.setWidth(-rectangle.getWidth());
            rectangle.setX(rectangle.getX() - rectangle.getWidth());
        }
        if (rectangle.getHeight() < 0) {
            rectangle.setHeight(-rectangle.getHeight());
            rectangle.setY(rectangle.getY() - rectangle.getHeight());
        }
    }


    private void repaint()
    {
        getChildren().clear();
        drawAxis();
        drawTopPeakList();
        drawBottomPeakList();
    }

    private void drawAxis()
    {
        double width = getWidth();
        double height = getHeight();

        Text textY1 = new Text(label_Y1);
        Text textY2 = new Text(label_Y2);
        Text textX = new Text(label_X);
        Text textTickY1 = new Text("100");
        Text textTickY2 = new Text("9.9E99");

        textY1.setFont(axisLabelFont);
        textY2.setFont(axisLabelFont);
        textX.setFont(axisLabelFont);

        textTickY1.setFont(tickLabelFont);
        textTickY2.setFont(tickLabelFont);

        double areaHeight = height - outerPad.getBottom() - outerPad.getTop()
                - frameStrokeWidth * 2
                - textX.getLayoutBounds().getHeight() - axisLabelNumberPad.getBottom()
                - textTickY1.getLayoutBounds().getHeight() - axisTickLabelPad.getBottom()
                - majorTickLength;
        double titleHeight = areaHeight * titleRatio / 2;
        double peptideHeight = areaHeight * peptideRatio / 2;

        topFrameYLoc = outerPad.getTop() + frameStrokeWidth;
        topTitleYLoc = topFrameYLoc + titleHeight;
        topPeptideYLoc = topTitleYLoc + peptideHeight;
        middleYLoc = topFrameYLoc + areaHeight / 2;
        peakAreaHeight = areaHeight * (1 - titleRatio - peptideRatio) * peakRatio / 2;
        topPeakYLoc = middleYLoc - peakAreaHeight;

        bottomFrameYLoc = height - outerPad.getBottom()
                - textX.getLayoutBounds().getHeight() - axisLabelNumberPad.getBottom()
                - textTickY1.getLayoutBounds().getHeight() - axisTickLabelPad.getBottom()
                - majorTickLength - frameStrokeWidth;
        bottomPeakYLoc = middleYLoc + peakAreaHeight;
        bottomTitleYLoc = bottomFrameYLoc - titleHeight;
        bottomPeptideYLoc = bottomTitleYLoc - peptideHeight;

        leftXLoc = outerPad.getLeft()
                + textY1.getLayoutBounds().getHeight() + axisLabelNumberPad.getLeft()
                + textTickY1.getLayoutBounds().getWidth() + axisTickLabelPad.getLeft()
                + majorTickLength + frameStrokeWidth;

        rightXLoc = width - outerPad.getRight()
                - textY2.getLayoutBounds().getHeight() - axisLabelNumberPad.getRight()
                - textTickY2.getLayoutBounds().getWidth() - axisTickLabelPad.getRight()
                - majorTickLength - frameStrokeWidth;

        // x label
        double xLabel_x = (leftXLoc + rightXLoc - textX.getLayoutBounds().getWidth()) / 2;
        double xLabel_y = height - outerPad.getBottom() - textX.getLayoutBounds().getHeight();
        textX.setTextOrigin(VPos.TOP);
        textX.relocate(xLabel_x, xLabel_y);
        getChildren().add(textX);

        // left y label
        double y1Label_x = outerPad.getLeft();
        double y1Label_y = middleYLoc + textY1.getLayoutBounds().getWidth() / 2;
        textY1.relocate(y1Label_x, y1Label_y);
        textY1.setTextOrigin(VPos.TOP);
        textY1.getTransforms().add(new Rotate(-90));
        getChildren().add(textY1);

        // right y label
        textY2.setTextOrigin(VPos.TOP);
        double y2Label_y = middleYLoc - textY2.getBoundsInLocal().getWidth() / 2;
        double y2Label_x = width - outerPad.getRight();
        textY2.relocate(y2Label_x, y2Label_y);
        textY2.getTransforms().add(new Rotate(90));
        getChildren().add(textY2);

        // frame
        double frameLeftXLoc = NodeUtils.snap(leftXLoc - frameStrokeWidth * .5, frameStrokeWidth);
        double frameRightXLoc = NodeUtils.snap(rightXLoc + frameStrokeWidth * .5, frameStrokeWidth);
        double frameBottomYLoc = NodeUtils.snap(bottomFrameYLoc + frameStrokeWidth * .5, frameStrokeWidth);
        double frameTopYLoc = NodeUtils.snap(topFrameYLoc - frameStrokeWidth * .5, frameStrokeWidth);

        // frame
        Rectangle rectangle = new Rectangle(frameLeftXLoc, frameTopYLoc,
                frameRightXLoc - frameLeftXLoc, frameBottomYLoc - frameTopYLoc);
        rectangle.setStrokeWidth(frameStrokeWidth);
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(frameColor);
        getChildren().add(rectangle);

        double lineY = NodeUtils.snap(middleYLoc, frameStrokeWidth);
        Line line = new Line(leftXLoc, lineY, rightXLoc, lineY);
        line.setStroke(frameColor);
        line.setStrokeWidth(frameStrokeWidth);
        getChildren().add(line);

        drawXAxis();
        drawYAxis();
    }

    private void drawXAxis()
    {
        Pair<Integer, Integer> tickCount = SpectrumChartUtils.getTickCount(getWidth(), pixelForEachTick);
        int majorTickCount = tickCount.getFirst();
        int minorTickCount = tickCount.getSecond();

        double range = SpectrumChartUtils.calRange(scaleMinMz, scaleMaxMz, majorTickCount);
        this.scaleMinMz = range * Math.floor(scaleMinMz / range);
        this.scaleMaxMz = range * Math.ceil(scaleMaxMz / range);
        this.unitX = (scaleMaxMz - scaleMinMz) / (rightXLoc - leftXLoc);
        majorTickCount = (int) Math.round((scaleMaxMz - scaleMinMz) / range);

        int precision = -(int) Math.floor(Math.log10(range));
        if (precision < 0)
            precision = 0;
        DecimalFormat format = NumberFormatFactory.valueOf(precision);
        double space = (rightXLoc - leftXLoc) / majorTickCount;
        double yLoc = bottomFrameYLoc + frameStrokeWidth + majorTickLength + axisTickLabelPad.getBottom();
        for (int i = 0; i <= majorTickCount; i++) {
            double x = leftXLoc + i * space;
            double mz = scaleMinMz + i * range;
            Text text = new Text(format.format(mz));
            text.setFont(tickLabelFont);
            text.setTextOrigin(VPos.TOP);
            text.relocate(x - text.getBoundsInLocal().getWidth() / 2, yLoc);
            getChildren().add(text);
        }

        double tickUnit = (rightXLoc - leftXLoc) / majorTickCount;
        double yLoc1 = bottomFrameYLoc + frameStrokeWidth + majorTickLength;
        double yLoc2 = bottomFrameYLoc + frameStrokeWidth;
        double xLoc;
        for (int i = 0; i <= majorTickCount; i++) {
            xLoc = NodeUtils.snap(leftXLoc + tickUnit * i, tickWidth);

            Line tickLine = new Line(xLoc, yLoc1, xLoc, yLoc2);
            tickLine.setStroke(tickColor);
            tickLine.setStrokeWidth(tickWidth);

            getChildren().add(tickLine);
        }

        // x minor ticks
        tickUnit = tickUnit / minorTickCount;
        if (minorTickCount % 10 == 0) {
            yLoc1 = bottomFrameYLoc + frameStrokeWidth + minorTickLength;
            double yLoc3 = bottomFrameYLoc + frameStrokeWidth + middleTickLength;
            for (int i = 1; i < majorTickCount * minorTickCount; i++) {
                xLoc = NodeUtils.snap(leftXLoc + tickUnit * i, tickWidth);
                if (i % 10 == 0) {
                    continue;
                }
                Line line;
                if (i % 5 == 0) {
                    line = new Line(xLoc, yLoc3, xLoc, yLoc2);
                } else {
                    line = new Line(xLoc, yLoc1, xLoc, yLoc2);
                }
                line.setStroke(tickColor);
                line.setStrokeWidth(tickWidth);

                getChildren().add(line);
            }
        } else {
            yLoc1 = bottomFrameYLoc + frameStrokeWidth + middleTickLength;
            for (int i = 1; i < majorTickCount * minorTickCount; i++) {
                if (i % 5 == 0)
                    continue;
                xLoc = NodeUtils.snap(leftXLoc + tickUnit * i, tickWidth);
                Line line = new Line(xLoc, yLoc1, xLoc, yLoc2);
                line.setStroke(tickColor);
                line.setStrokeWidth(tickWidth);

                getChildren().add(line);
            }
        }
    }

    private void drawYAxis()
    {
        // y major tick
        double xLoc1 = leftXLoc - majorTickLength - frameStrokeWidth;
        double xLoc2 = leftXLoc - frameStrokeWidth;
        double xLoc3 = rightXLoc + frameStrokeWidth;
        double xLoc4 = rightXLoc + majorTickLength + frameStrokeWidth;
        double xLoc5 = xLoc4 + axisTickLabelPad.getRight();

        this.topUnitY = (topMaxIntensity - topMinIntensity) / (middleYLoc - topPeakYLoc);
        this.bottomUnitY = (bottomMaxIntensity - bottomMinIntensity) / (bottomPeakYLoc - middleYLoc);

        double topInUnit = (topMaxIntensity - topMinIntensity) / yMajorTickCount;
        double bottomInUnit = (bottomMaxIntensity - bottomMinIntensity) / yMajorTickCount;

        double topTickUnit = (middleYLoc - topPeakYLoc) / yMajorTickCount;
        double bottomTickUnit = (bottomPeakYLoc - middleYLoc) / yMajorTickCount;

        // major ticks
        NumberFormat inFormat = SpectrumChartUtils.getFormat(topMaxIntensity);
        for (int i = 0; i <= yMajorTickCount; i++) {
            double yLocTop = NodeUtils.snap(middleYLoc - topTickUnit * i, tickWidth);
            double yLocBottom = NodeUtils.snap(middleYLoc + bottomTickUnit * i, tickWidth);

            Line tickTopL = new Line(xLoc1, yLocTop, xLoc2, yLocTop);
            Line tickTopR = new Line(xLoc3, yLocTop, xLoc4, yLocTop);
            Line tickBottomL = new Line(xLoc1, yLocBottom, xLoc2, yLocBottom);
            Line tickBottomR = new Line(xLoc3, yLocBottom, xLoc4, yLocBottom);

            tickTopL.setStroke(tickColor);
            tickTopR.setStroke(tickColor);
            tickBottomL.setStroke(tickColor);
            tickBottomR.setStroke(tickColor);

            tickTopL.setStrokeWidth(tickWidth);
            tickTopR.setStrokeWidth(tickWidth);
            tickBottomL.setStrokeWidth(tickWidth);
            tickBottomR.setStrokeWidth(tickWidth);

            getChildren().addAll(tickTopL, tickTopR, tickBottomL, tickBottomR);

            String tickLabelL = String.valueOf(100 * i / yMajorTickCount);

            Text textY1Top = new Text(tickLabelL);
            textY1Top.setFont(tickLabelFont);
            textY1Top.setTextOrigin(VPos.TOP);
            textY1Top.relocate(xLoc1 - axisTickLabelPad.getLeft() - textY1Top.getLayoutBounds().getWidth(),
                    yLocTop - textY1Top.getLayoutBounds().getHeight() / 2);
            getChildren().add(textY1Top);

            if (i != 0) {
                Text textY1Bottom = new Text(tickLabelL);
                textY1Bottom.setFont(tickLabelFont);
                textY1Bottom.setTextOrigin(VPos.TOP);
                textY1Bottom.relocate(xLoc1 - axisTickLabelPad.getLeft() - textY1Bottom.getLayoutBounds().getWidth(),
                        yLocBottom - textY1Bottom.getLayoutBounds().getHeight() / 2);
                getChildren().add(textY1Bottom);
            }

            Text textY2Top = new Text(inFormat.format(topMinIntensity + topInUnit * i));
            textY2Top.setFont(tickLabelFont);
            textY2Top.setTextOrigin(VPos.TOP);
            if (i != 0)
                textY2Top.relocate(xLoc5, yLocTop - textY2Top.getLayoutBounds().getHeight() / 2);
            else
                textY2Top.relocate(xLoc5, yLocTop - textY2Top.getLayoutBounds().getHeight());

            getChildren().add(textY2Top);

            Text textY2Bottom = new Text(inFormat.format(bottomMinIntensity + bottomInUnit * i));
            textY2Bottom.setFont(tickLabelFont);
            textY2Bottom.setTextOrigin(VPos.TOP);

            if (i != 0)
                textY2Bottom.relocate(xLoc5, yLocBottom - textY2Bottom.getLayoutBounds().getHeight() / 2);
            else
                textY2Bottom.relocate(xLoc5, yLocBottom);
            getChildren().add(textY2Bottom);
        }

        int minorTickCount = yMajorTickCount * yMinorTickCount;
        topTickUnit = (middleYLoc - topPeakYLoc) / minorTickCount;
        bottomTickUnit = (bottomPeakYLoc - middleYLoc) / minorTickCount;

        xLoc1 = leftXLoc - middleTickLength - frameStrokeWidth;
        xLoc2 = leftXLoc - frameStrokeWidth;
        xLoc3 = rightXLoc + frameStrokeWidth;
        xLoc4 = rightXLoc + middleTickLength + frameStrokeWidth;
        for (int i = 1; i <= minorTickCount; i++) {
            if (i % yMinorTickCount == 0)
                continue;
            double yLocTop = NodeUtils.snap(middleYLoc - topTickUnit * i, tickWidth);
            double yLocBottom = NodeUtils.snap(middleYLoc + bottomTickUnit * i, tickWidth);

            Line tickLineTopL = new Line(xLoc1, yLocTop, xLoc2, yLocTop);
            tickLineTopL.setStrokeWidth(tickWidth);
            tickLineTopL.setStroke(tickColor);

            Line tickLineBottomL = new Line(xLoc1, yLocBottom, xLoc2, yLocBottom);
            tickLineBottomL.setStrokeWidth(tickWidth);
            tickLineBottomL.setStroke(tickColor);

            Line tickLineTopR = new Line(xLoc3, yLocTop, xLoc4, yLocTop);
            tickLineTopR.setStrokeWidth(tickWidth);
            tickLineTopR.setStroke(tickColor);

            Line tickLineBottomR = new Line(xLoc3, yLocBottom, xLoc4, yLocBottom);
            tickLineBottomR.setStrokeWidth(tickWidth);
            tickLineBottomR.setStroke(tickColor);

            getChildren().addAll(tickLineTopL, tickLineTopR, tickLineBottomL, tickLineBottomR);
        }
    }

    private final PeakList<?> topPeakList = new DoublePeakList<>();
    private final PeakList<?> bottomPeakList = new DoublePeakList<>();

    /**
     * set the peak list to be drawn.
     */
    public void setPeakList(PeakList topPeakList, PeakList bottomPeakList)
    {
        checkNotNull(topPeakList);
        checkNotNull(bottomPeakList);

        if (topPeakList.isEmpty() || bottomPeakList.isEmpty())
            return;

        this.topPeakList.clear();
        this.bottomPeakList.clear();

        this.topPeakList.addPeaks(topPeakList);
        this.bottomPeakList.addPeaks(bottomPeakList);

        this.minMz = Double.min(topPeakList.getX(0), bottomPeakList.getX(0));
        this.maxMz = Double.max(topPeakList.getX(topPeakList.size() - 1),
                bottomPeakList.getX(bottomPeakList.size() - 1));

        this.topBasePeakIntensity = topPeakList.getBasePeakY();
        this.bottomBasePeakIntensity = bottomPeakList.getBasePeakY();

        this.topMinIntensity = 0;
        this.bottomMinIntensity = 0;

        this.topMaxIntensity = topBasePeakIntensity;
        this.bottomMaxIntensity = bottomBasePeakIntensity;

        this.scaleMinMz = minMz;
        this.scaleMaxMz = maxMz;

        repaint();
    }


    private void drawTopPeakList()
    {
        for (int i = 0; i < topPeakList.size(); i++) {
            if (topPeakList.hasAnnotationsAt(i))
                continue;
            double mz = topPeakList.getX(i);
            double in = topPeakList.getY(i);

            if (mz <= scaleMinMz || mz >= scaleMaxMz)
                continue;
            if (in <= topMinIntensity)
                continue;
            double xPos = getX(mz);
            double yPos = getTopY(in);

            double x = NodeUtils.snap(xPos, peakWidth);
            Line line = new Line(x, yPos, x, middleYLoc);
            line.setStrokeWidth(peakWidth);
            line.setStroke(peakColor);
            getChildren().add(line);
        }
    }

    private void drawBottomPeakList()
    {
        for (int i = 0; i < bottomPeakList.size(); i++) {
            if (bottomPeakList.hasAnnotationsAt(i))
                continue;
            double mz = bottomPeakList.getX(i);
            double in = bottomPeakList.getY(i);

            if (mz <= scaleMinMz || mz >= scaleMaxMz)
                continue;
            if (in <= bottomMinIntensity)
                continue;
            double xPos = getX(mz);
            double yPos = getBottomY(in);

            double x = NodeUtils.snap(xPos, peakWidth);
            Line line = new Line(x, yPos, x, middleYLoc);
            line.setStrokeWidth(peakWidth);
            line.setStroke(peakColor);
            getChildren().add(line);
        }
    }

    private double getX(double mz)
    {
        return leftXLoc + (mz - scaleMinMz) / unitX;
    }

    private double getTopY(double intensity)
    {
        double y = middleYLoc - (intensity - topMinIntensity) / topUnitY;
        if (y < topPeakYLoc)
            y = topPeakYLoc;
        return y;
    }

    private double getBottomY(double in)
    {
        double y = middleYLoc + (in - bottomMinIntensity) / bottomUnitY;
        if (y > bottomPeakYLoc)
            y = bottomPeakYLoc;
        return y;
    }
}
