package omics.gui.psm;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import omics.gui.psm.util.LabelPos;
import omics.gui.psm.util.NodeUtils;
import omics.gui.psm.util.Point2D;
import omics.gui.psm.util.PointLabel;
import omics.util.ms.peaklist.PeakAnnotation;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.impl.DoublePeakList;
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.Pair;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static omics.util.utils.ObjectUtils.checkNotNull;

/**
 * This class is designed to have more features of event handlers.
 *
 * @author JiaweiMao
 * @version 1.2.1
 * @since 16 Apr 2018, 10:47 AM
 */
public class SpectrumChart extends Pane
{
    /**
     * labels
     */
    private static final String label_X = "m/z";
    private static final String label_Y1 = "Relative Abundance (%)";
    private static final String label_Y2 = "Ion Current";

    /**
     * Location of points in the Pane
     */
    private double leftXLoc; // left X of the x-axis
    private double rightXLoc; // right X of the x-axis
    private double bottomYLoc; // bottom Y of the y-axis
    private double topYLoc; // top Y of the drawing area
    private double middleYLoc; // top Y of the y-axis.

    private PeakList<? extends PeakAnnotation> peakList = new DoublePeakList<>();
    // minimum m/z to display
    private double scaleMinMz;
    // maximum m/z to display
    private double scaleMaxMz;
    // minimum intensity to display
    private double minIntensity;
    // maximum intensity to display
    private double maxIntensity;

    private double basePeakIntensity;
    private double minMz = 0;
    private double maxMz = 1000;
    // mz per pixel
    private double unitX;
    // intensity value per pixel
    private double unitY;

    private final DoubleProperty mzProperty = new SimpleDoubleProperty();
    private final DoubleProperty intensityProperty = new SimpleDoubleProperty();

    private PSMViewSettings config;

    public SpectrumChart(PSMViewSettings config)
    {
        this.config = config;
        widthProperty().addListener((observable, oldValue, newValue) -> repaint());
        heightProperty().addListener((observable, oldValue, newValue) -> repaint());
        addEvent();
    }

    public SpectrumChart()
    {
        this(new PSMViewSettings());
    }

    public void setStyle(PSMViewSettings style)
    {
        checkNotNull(style);
        this.config = style;
    }

    public PSMViewSettings getConfig()
    {
        return config;
    }

    /**
     * Property of current mz value under cursor
     *
     * @return Property of current mz value under mouse cursor
     */
    public DoubleProperty mzProperty()
    {
        return mzProperty;
    }

    /**
     * Property of current intensity value under mouse cursor.
     *
     * @return Property of current intensity value under mouse cursor.
     */
    public DoubleProperty intensityProperty()
    {
        return intensityProperty;
    }

    private boolean newRectangleIsDrawing = false;
    private double startX;
    private double startY;
    private Rectangle rectangle;

    private void addEvent()
    {
        setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();

            if (x > leftXLoc && x < rightXLoc && y < bottomYLoc && y > middleYLoc) {
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
                minIntensity = 0;
                maxIntensity = basePeakIntensity;
                scaleMinMz = minMz;
                scaleMaxMz = maxMz;
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

                if (maxX <= leftXLoc || x >= rightXLoc || y >= bottomYLoc || maxY <= middleYLoc)
                    return;

                double newMinMz = scaleMinMz;
                if (x > leftXLoc)
                    newMinMz = getMz(x);
                double newMaxMz = scaleMaxMz;
                if (maxX < rightXLoc)
                    newMaxMz = getMz(maxX);

                double newMaxIntensity = maxIntensity;
                if (y > middleYLoc)
                    newMaxIntensity = getIntensity(y);

                double newMinIntensity = minIntensity;
                if (maxY < bottomYLoc)
                    newMinIntensity = getIntensity(maxY);

                this.scaleMinMz = newMinMz;
                this.scaleMaxMz = newMaxMz;
                this.minIntensity = newMinIntensity;
                this.maxIntensity = newMaxIntensity;

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

    private void resetScale()
    {
        minIntensity = 0;
        if (peakList.isEmpty()) {
            minMz = 0;
            maxMz = 1000;
            basePeakIntensity = 0;
            maxIntensity = 100;
        } else {
            minMz = peakList.getX(0);
            maxMz = peakList.getX(peakList.size() - 1);
            maxIntensity = basePeakIntensity = peakList.getBasePeakY();
        }
        scaleMinMz = minMz;
        scaleMaxMz = maxMz;
    }


    /**
     * set the peak list to be drawn.
     *
     * @param peakList a {@link PeakList} instance.
     */
    public void setPeakList(PeakList<? extends PeakAnnotation> peakList)
    {
        checkNotNull(peakList);

        if (peakList.isEmpty())
            return;
        this.peakList = peakList;
        resetScale();
        repaint();
    }

    /**
     * Clear all peaks in the pane.
     */
    public void clear()
    {
        if (peakList.isEmpty())
            return;
        this.peakList.clear();
        resetScale();
        repaint();
    }

    private void repaint()
    {
        getChildren().clear();
        if (peakList == null || peakList.isEmpty()) {
            return;
        }

        drawAxis();
        drawPeaks();
        addAnnotation();
    }

    private void drawAxis()
    {
        double width = getWidth();
        double height = getHeight();

        Text textY1 = new Text(label_Y1);
        Text textY2 = new Text(label_Y2);
        Text textX = new Text(label_X);
        Text textTickY1 = new Text("100");
        Text textTickY2 = new Text("1.0E10");

        Font axisLabelFont = config.getAxisLabelFont();
        Font tickLabelFont = config.getTickLabelFont();

        textY1.setFont(axisLabelFont);
        textY2.setFont(axisLabelFont);
        textX.setFont(axisLabelFont);
        textTickY1.setFont(tickLabelFont);
        textTickY2.setFont(tickLabelFont);

        double majorTickLength = config.getMajorTickLength();
        Insets padding = config.getPadding();
        double labelNumSpace = config.getAxisLabelTickNumberSpace();
        double tickLabelSpace = config.getTickLabelSpace();
        int frameStrokeWidth = config.getFrameStrokeWidth();

        leftXLoc = padding.getLeft() + textY1.getLayoutBounds().getHeight() + labelNumSpace
                + textTickY1.getLayoutBounds().getWidth() + tickLabelSpace + majorTickLength + frameStrokeWidth;
        // left x of the frame
        double frameLeftXLoc = NodeUtils.snap(leftXLoc - frameStrokeWidth * .5, frameStrokeWidth);
        rightXLoc = width - padding.getRight() - textY2.getLayoutBounds().getHeight()
                - labelNumSpace - textTickY2.getLayoutBounds().getWidth() - tickLabelSpace - majorTickLength - frameStrokeWidth;
        // right x of the frame
        double frameRightXLoc = NodeUtils.snap(rightXLoc + frameStrokeWidth * .5, frameStrokeWidth);
        bottomYLoc = height - padding.getBottom() - textX.getLayoutBounds().getHeight() - labelNumSpace
                - textTickY1.getLayoutBounds().getHeight() - tickLabelSpace - majorTickLength - frameStrokeWidth;
        // bottom Y of the frame
        double frameBottomYLoc = NodeUtils.snap(bottomYLoc + frameStrokeWidth * .5, frameStrokeWidth);
        topYLoc = padding.getTop() + frameStrokeWidth;
        // top Y of the frame
        double frameTopYLoc = NodeUtils.snap(padding.getTop() + frameStrokeWidth * .5, frameStrokeWidth);
        // only draw values in 5/6, the remain 1/6 is used for possible labels.
        middleYLoc = topYLoc + (bottomYLoc - topYLoc) * config.getBlankAreaRatio();

        // x label
        double xLabel_x = (leftXLoc + rightXLoc - textX.getLayoutBounds().getWidth()) / 2;
        double xLabel_y = height - padding.getBottom() - textX.getLayoutBounds().getHeight();
        textX.setTextOrigin(VPos.TOP);
        textX.relocate(xLabel_x, xLabel_y);
        getChildren().add(textX);

        // left y label
        double y1Label_x = padding.getLeft();
        double y1Label_y = (bottomYLoc + middleYLoc + textY1.getLayoutBounds().getWidth()) / 2;
        textY1.relocate(y1Label_x, y1Label_y);
        textY1.setTextOrigin(VPos.TOP);
        textY1.getTransforms().add(new Rotate(-90));
        getChildren().add(textY1);

        // right y label
        textY2.setTextOrigin(VPos.TOP);
        double y2Label_y = (bottomYLoc + middleYLoc - textY2.getLayoutBounds().getWidth()) / 2;
        double y2Label_x = width - padding.getRight();
        textY2.relocate(y2Label_x, y2Label_y);
        textY2.getTransforms().add(new Rotate(90));
        getChildren().add(textY2);

        // frame
        Rectangle rectangle = new Rectangle(frameLeftXLoc, frameTopYLoc,
                frameRightXLoc - frameLeftXLoc, frameBottomYLoc - frameTopYLoc);
        rectangle.setStrokeWidth(config.getFrameStrokeWidth());
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(config.getFrameColor());
        getChildren().add(rectangle);

        drawXAxis();
        drawYAxis();
    }

    private void drawXAxis()
    {
        double majorTickLength = config.getMajorTickLength();
        double minorTickLength = config.getMinorTickLength();
        double middleTickLength = config.getMiddleTickLength();
        int frameStrokeWidth = config.getFrameStrokeWidth();
        double tickLabelSpace = config.getTickLabelSpace();
        int tickWidth = config.getTickWidth();
        Color tickColor = config.getTickColor();
        Font tickLabelFont = config.getTickLabelFont();

        Pair<Integer, Integer> tickCount = SpectrumChartUtils.getTickCount(getWidth(), config.getPixelForEachTick());
        int majorTickCount = tickCount.getFirst();
        int minorTickCount = tickCount.getSecond();

        double range = SpectrumChartUtils.calRange(scaleMinMz, scaleMaxMz, majorTickCount);
//        System.out.println(minMz + "\t" + maxMz + "\t" + majorTickCount + "\t" + range);
        this.scaleMinMz = range * Math.floor(scaleMinMz / range);
        this.scaleMaxMz = range * Math.ceil(scaleMaxMz / range);
        this.unitX = (scaleMaxMz - scaleMinMz) / (rightXLoc - leftXLoc);
        majorTickCount = (int) Math.round((scaleMaxMz - scaleMinMz) / range);

//        System.out.println(minMz + "\t" + maxMz + "\t" + majorTickCount + "\t" + range);

        int precision = -(int) Math.floor(Math.log10(range));
        if (precision < 0)
            precision = 0;
        DecimalFormat format = NumberFormatFactory.valueOf(precision);
        double space = (rightXLoc - leftXLoc) / majorTickCount;
        double yLoc = bottomYLoc + frameStrokeWidth + majorTickLength + tickLabelSpace;
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
        double yLoc1 = bottomYLoc + frameStrokeWidth + majorTickLength;
        double yLoc2 = bottomYLoc + frameStrokeWidth;
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
        if (minorTickCount == 10) {
            yLoc1 = bottomYLoc + frameStrokeWidth + minorTickLength;
            double yLoc3 = bottomYLoc + frameStrokeWidth + middleTickLength;
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
            yLoc1 = bottomYLoc + frameStrokeWidth + middleTickLength;
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
        int yMajorTickCount = config.getYMajorTickCount();
        int yMinorTickCount = config.getYMinorTickCount();
        int frameStrokeWidth = config.getFrameStrokeWidth();
        int tickWidth = config.getTickWidth();
        Color tickColor = config.getTickColor();
        Font tickLabelFont = config.getTickLabelFont();
        double tickLabelSpace = config.getTickLabelSpace();

        double majorTickLength = config.getMajorTickLength();
        double middleTickLength = config.getMiddleTickLength();

        // y major tick
        double xLoc1 = leftXLoc - majorTickLength - frameStrokeWidth;
        double xLoc2 = leftXLoc - frameStrokeWidth;
        double xLoc3 = rightXLoc + frameStrokeWidth;
        double xLoc4 = rightXLoc + majorTickLength + frameStrokeWidth;
        double xLocY2 = xLoc4 + tickLabelSpace;

        this.unitY = (maxIntensity - minIntensity) / (bottomYLoc - middleYLoc);
        double inUnit = (maxIntensity - minIntensity) / yMajorTickCount;
        double tickUnit = (bottomYLoc - middleYLoc) / yMajorTickCount;
        NumberFormat inFormat = SpectrumChartUtils.getFormat(maxIntensity);

        for (int i = 0; i <= yMajorTickCount; i++) {
            double yLoc = NodeUtils.snap(bottomYLoc - tickUnit * i, tickWidth);

            Line tickLine1 = new Line(xLoc1, yLoc, xLoc2, yLoc);
            Line tickLine2 = new Line(xLoc3, yLoc, xLoc4, yLoc);
            tickLine1.setStroke(tickColor);
            tickLine2.setStroke(tickColor);
            tickLine1.setStrokeWidth(tickWidth);
            tickLine2.setStrokeWidth(tickWidth);

            String tickLabel = String.valueOf(100 * i / yMajorTickCount);

            Text textY1 = new Text(tickLabel);
            textY1.setFont(tickLabelFont);
            textY1.setTextOrigin(VPos.TOP);
            textY1.relocate(xLoc1 - tickLabelSpace - textY1.getLayoutBounds().getWidth(),
                    yLoc - textY1.getLayoutBounds().getHeight() / 2);

            double inValue = minIntensity + inUnit * i;
            Text textY2 = new Text(inFormat.format(inValue));
            textY2.setFont(tickLabelFont);
            textY2.setTextOrigin(VPos.TOP);
            textY2.relocate(xLocY2, yLoc - textY2.getLayoutBounds().getHeight() / 2);

            getChildren().addAll(tickLine1, tickLine2, textY1, textY2);
        }


        int minorTickCount = yMajorTickCount * yMinorTickCount;
        tickUnit = (bottomYLoc - middleYLoc) / minorTickCount;

        xLoc1 = leftXLoc - middleTickLength - frameStrokeWidth;
        xLoc2 = leftXLoc - frameStrokeWidth;
        xLoc3 = rightXLoc + frameStrokeWidth;
        xLoc4 = rightXLoc + middleTickLength + frameStrokeWidth;
        for (int i = 1; i <= minorTickCount; i++) {
            if (i % yMinorTickCount == 0)
                continue;
            double yLoc = NodeUtils.snap(bottomYLoc - tickUnit * i, tickWidth);

            Line tickLine = new Line(xLoc1, yLoc, xLoc2, yLoc);
            tickLine.setStrokeWidth(tickWidth);
            tickLine.setStroke(tickColor);

            Line tickLine2 = new Line(xLoc3, yLoc, xLoc4, yLoc);
            tickLine2.setStrokeWidth(tickWidth);
            tickLine2.setStroke(tickColor);

            getChildren().addAll(tickLine, tickLine2);
        }
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
        return minIntensity + (bottomYLoc - y) * unitY;
    }

    /**
     * Return the x coordinate of given m/z
     *
     * @param mz m/z
     */
    private double getX(double mz)
    {
        return leftXLoc + (mz - scaleMinMz) / unitX;
    }

    private double getY(double intensity)
    {
        double y = bottomYLoc - (intensity - minIntensity) / unitY;
        if (y < middleYLoc)
            y = middleYLoc;
        return y;
    }

    private void drawPeaks()
    {
        int peakWidth = (int) Math.round(config.getPeakWidth());
        Color peakColor = config.getPeakColor();
        for (int i = 0; i < peakList.size(); i++) {
            if (peakList.hasAnnotationsAt(i))
                continue;

            double mz = peakList.getX(i);
            double in = peakList.getY(i);

            if (mz < scaleMinMz || mz > scaleMaxMz)
                continue;
            if (in < minIntensity)
                continue;

            double xPos = getX(mz);
            double yPos = getY(in);

            double x1 = NodeUtils.snap(xPos, peakWidth);
            Line peakLine = new Line(x1, bottomYLoc, x1, yPos);
            peakLine.setStrokeWidth(config.getPeakWidth());
            peakLine.setStroke(peakColor);
            getChildren().add(peakLine);
        }
    }

    private boolean drawRectangle = false;

    private void addAnnotation()
    {
        String dashStyle = "-fx-stroke-dash-array: 2 2;";
        int peakWidth = (int) Math.round(config.getAnnotatedPeakWidth());
        HashMap<Point2D, Text> textMap = new HashMap<>();
        for (int i = 0; i < peakList.size(); i++) {
            if (!peakList.hasAnnotationsAt(i))
                continue;

            double mz = peakList.getX(i);
            double in = peakList.getY(i);
            if (mz < scaleMinMz || mz > scaleMaxMz)
                continue;
            if (in < minIntensity)
                continue;

            double xPos = getX(mz);
            double yPos = getY(in);

            List<? extends PeakAnnotation> annotations = peakList.getAnnotations(i);
            PeakAnnotation annotation = annotations.get(0);
            Color color = config.getColor(annotation.getIon());
            if (color == null)
                color = config.getPeakColor();

            double annoX = NodeUtils.snap(xPos, peakWidth);
            Line line = new Line(annoX, bottomYLoc, annoX, yPos);
            line.setStrokeWidth(config.getAnnotatedPeakWidth());
            line.setStroke(color);
            getChildren().add(line);

            String label = SpectrumChartUtils.getAnnotationsLabel(annotations);
            Text labelText = new Text(label);
            labelText.setFont(config.getPeakLabelFont());
            labelText.setFill(color);
            labelText.setTextOrigin(VPos.CENTER);
            textMap.put(new Point2D(xPos, yPos), labelText);
        }

        Map<Point2D, Pair<Double, Double>> pointMap = new HashMap<>(textMap.size());
        for (Map.Entry<Point2D, Text> entry : textMap.entrySet()) {
            Text text = entry.getValue();
            Bounds bounds = text.getLayoutBounds();
            pointMap.put(entry.getKey(), Pair.create(bounds.getWidth(), bounds.getHeight()));
        }

        HashMap<Point2D, PointLabel> labelMap = PointLabel.placeLabelGrasp(pointMap, config.getPeakLabelSpace(), leftXLoc, topYLoc, rightXLoc, bottomYLoc);
        for (Map.Entry<Point2D, PointLabel> entry : labelMap.entrySet()) {
            Point2D key = entry.getKey();
            PointLabel pointLabel = entry.getValue();
            Point2D minLoc = pointLabel.getMinLoc();
            LabelPos pos = pointLabel.getPos();
            Text text = textMap.get(key);
            text.relocate(minLoc.getX(), minLoc.getY());
            getChildren().add(text);

            if (drawRectangle) {
                Bounds layoutBounds = text.getLayoutBounds();
                Rectangle rectangle = new Rectangle(minLoc.getX(), minLoc.getY(), layoutBounds.getWidth(), layoutBounds.getHeight());
                rectangle.setStroke(text.getFill());
                rectangle.setFill(null);
                getChildren().add(rectangle);
            }

            // add line
            if (pos != LabelPos.TOP_CENTER1) {
                Point2D linkPoint = pointLabel.getLinkPoint();
                Line line = new Line(linkPoint.getX(), linkPoint.getY(), key.getX(), key.getY());
                line.setStroke(text.getFill());
                line.setStyle(dashStyle);

                getChildren().add(line);
            }
        }
    }
}
