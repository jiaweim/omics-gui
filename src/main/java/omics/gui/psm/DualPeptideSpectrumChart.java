package omics.gui.psm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import omics.gui.psm.util.LabelPos;
import omics.gui.psm.util.NodeUtils;
import omics.gui.psm.util.Point2D;
import omics.gui.psm.util.PointLabel;
import omics.util.ms.peaklist.PeakAnnotation;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.impl.DoublePeakList;
import omics.util.protein.Peptide;
import omics.util.protein.mod.ModAttachment;
import omics.util.protein.mod.Modification;
import omics.util.protein.mod.ModificationList;
import omics.util.protein.ms.FragmentType;
import omics.util.protein.ms.Ion;
import omics.util.protein.ms.PeptideFragmentAnnotation;
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.Pair;
import omics.util.utils.StringUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static omics.util.utils.ObjectUtils.checkNotNull;

/**
 * Dual spectrum viewer.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 05 Jun 2020, 12:20 PM
 */
public class DualPeptideSpectrumChart extends Pane
{
    private static final String ANNO_LINE = "-fx-stroke-dash-array: 2 2;";

    private String label_Y1 = "Relative Abundance (%)";
    private String label_Y2 = "Intensity";
    private String label_X = "m/z";

    private Insets axisLabelNumberPad = new Insets(2);
    private Insets axisTickLabelPad = new Insets(3);

    /**
     * padding around drawing area
     */
    private final Insets outerPad = new Insets(0, 6, 6, 6);

    /**
     * start x of peak area
     */
    private double leftXLoc;
    /**
     * end x of peak area
     */
    private double rightXLoc;

    private double topFrameYLoc;
    /**
     * lower bounds of the title area
     */
    private double topTitleYLoc;
    /**
     * top end of peak plotting area
     */
    private double topPeakYLoc;
    /**
     * y of middle line
     */
    private double middleYLoc;
    private double bottomPeakYLoc;
    private double bottomTitleYLoc;
    private double bottomFrameYLoc;

    private double titleRatio = 0.15;
    private double peptideRatio = 0.2;

    private double peakRatio = 0.85;
    private double peakAreaHeight;

    private PSMViewSettings settings = new PSMViewSettings();

    private int pixelForEachTick = 5;

    // m/z range
    private double minMz = 0;
    private double maxMz = 1000;

    /**
     * minimum m/z to display
     */
    private double scaleMinMz = 0;
    /**
     * maximum m/z to display
     */
    private double scaleMaxMz = 1000;
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
    /**
     * lower bound of top peptide area
     */
    private double topPeptideYLoc;
    private double bottomPeptideYLoc;

    private final DoubleProperty mzProperty = new SimpleDoubleProperty();
    private final DoubleProperty intensityProperty = new SimpleDoubleProperty();

    public DualPeptideSpectrumChart()
    {
        widthProperty().addListener((observable, oldValue, newValue) -> repaint());
        heightProperty().addListener((observable, oldValue, newValue) -> repaint());

        addEvent();
    }

    /**
     * setter of the left Y-axis title
     *
     * @param label_Y1 title for left Y-axis
     */
    public void setY1Title(String label_Y1)
    {
        this.label_Y1 = label_Y1;
    }

    /**
     * setter of the right Y-axis title
     *
     * @param label_Y2 title for the right Y-axis
     */
    public void setY2Title(String label_Y2)
    {
        this.label_Y2 = label_Y2;
    }

    /**
     * setter of X-axis title
     *
     * @param label_X title for the x-axis
     */
    public void setXTitle(String label_X)
    {
        this.label_X = label_X;
    }

    public void setViewSettings(PSMViewSettings settings)
    {
        this.settings = settings;
    }

    /**
     * @return settings for PSM view
     */
    public PSMViewSettings getViewSettings()
    {
        return settings;
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

        MenuItem saveItem = new MenuItem("Save to PNG");
        saveItem.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save image to png file");
            chooser.setInitialFileName("scan.png");
            File file = chooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                NodeUtils.saveNodeAsPng(this, file.getAbsolutePath());
            }
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(saveItem);
        menu.setAutoHide(true);
        setOnContextMenuRequested(event -> menu.show(getScene().getWindow(), event.getSceneX(), event.getSceneY()));
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

    /**
     * set the x and y scale
     */
    private void resetScale()
    {
        topMinIntensity = bottomMinIntensity = 0;
        if (topPeakList.isEmpty() && bottomPeakList.isEmpty()) {
            minMz = 0;
            maxMz = 1000;
            topBasePeakIntensity = bottomBasePeakIntensity = 0;
            topMaxIntensity = bottomMaxIntensity = 100;
        } else {
            if (!topPeakList.isEmpty() && !bottomPeakList.isEmpty()) {
                minMz = Double.min(topPeakList.getX(0), bottomPeakList.getX(0));
                maxMz = Double.max(topPeakList.getX(topPeakList.size() - 1),
                        bottomPeakList.getX(bottomPeakList.size() - 1));
                topMaxIntensity = topBasePeakIntensity = topPeakList.getBasePeakY();
                bottomMaxIntensity = bottomBasePeakIntensity = bottomPeakList.getBasePeakY();
            } else if (!topPeakList.isEmpty()) {
                minMz = topPeakList.getX(0);
                maxMz = topPeakList.getX(topPeakList.size() - 1);
                topMaxIntensity = topBasePeakIntensity = topPeakList.getBasePeakY();
                bottomBasePeakIntensity = 0;
                bottomMaxIntensity = 100;
            } else {
                minMz = bottomPeakList.getX(0);
                maxMz = bottomPeakList.getX(bottomPeakList.size() - 1);
                bottomMaxIntensity = bottomBasePeakIntensity = bottomPeakList.getBasePeakY();
                topBasePeakIntensity = 0;
                topMaxIntensity = 100;
            }
        }
        scaleMinMz = minMz;
        scaleMaxMz = maxMz;
    }

    private void repaint()
    {
        getChildren().clear();
        drawAxis();

        drawPeakList(topPeakList, true);
        drawAnnotation(topPeakList, true);

        drawPeakList(bottomPeakList, false);
        drawAnnotation(bottomPeakList, false);

        drawPeptide(topPeptide, topAnnotaions, topTitleYLoc, topPeptideYLoc, true);
        drawPeptide(bottomPeptide, bottomAnnotations, bottomTitleYLoc, bottomPeptideYLoc, false);

        double lineY = NodeUtils.snap(middleYLoc, settings.getFrameStrokeWidth());
        Line line = new Line(leftXLoc, lineY, rightXLoc, lineY);
        line.setStroke(settings.getFrameColor());
        line.setStrokeWidth(settings.getFrameStrokeWidth());
        getChildren().add(line);

        drawTitle();
        drawSubTitle();
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

        Font axisLabelFont = settings.getAxisLabelFont();
        Font tickLabelFont = settings.getTickLabelFont();

        textY1.setFont(axisLabelFont);
        textY2.setFont(axisLabelFont);
        textX.setFont(axisLabelFont);

        textTickY1.setFont(tickLabelFont);
        textTickY2.setFont(tickLabelFont);

        int frameStrokeWidth = settings.getFrameStrokeWidth();
        double majorTickLength = settings.getMajorTickLength();

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
        rectangle.setStroke(settings.getFrameColor());
        getChildren().add(rectangle);

        double lineY = NodeUtils.snap(middleYLoc, frameStrokeWidth);
        Line line = new Line(leftXLoc, lineY, rightXLoc, lineY);
        line.setStroke(settings.getFrameColor());
        line.setStrokeWidth(frameStrokeWidth);
        getChildren().add(line);

        drawXAxis();
        drawYAxis();
    }

    private void drawXAxis()
    {
        int frameStrokeWidth = settings.getFrameStrokeWidth();
        double majorTickLength = settings.getMajorTickLength();
        double middleTickLength = settings.getMiddleTickLength();
        double minorTickLength = settings.getMinorTickLength();
        int tickWidth = settings.getTickWidth();
        Color tickColor = settings.getTickColor();

        Pair<Integer, Integer> tickCount = SpectrumChartUtils.getTickCount(getWidth(), pixelForEachTick);
        int majorTickCount = tickCount.getFirst();
        int minorTickCount = tickCount.getSecond();

        double range = SpectrumChartUtils.calRange(scaleMinMz, scaleMaxMz, majorTickCount);
        this.scaleMinMz = range * Math.floor(scaleMinMz / range);
        this.scaleMaxMz = range * Math.ceil(scaleMaxMz / range);
        this.unitX = (scaleMaxMz - scaleMinMz) / (rightXLoc - leftXLoc);
        majorTickCount = (int) Math.round((scaleMaxMz - scaleMinMz) / range);

        Font tickLabelFont = settings.getTickLabelFont();
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
        Font tickLabelFont = settings.getTickLabelFont();
        int frameStrokeWidth = settings.getFrameStrokeWidth();
        double majorTickLength = settings.getMajorTickLength();
        double middleTickLength = settings.getMiddleTickLength();
        int yMajorTickCount = settings.getYMajorTickCount();

        int tickWidth = settings.getTickWidth();
        Color tickColor = settings.getTickColor();

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

        int yMinorTickCount = settings.getYMinorTickCount();
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

    private final PeakList<PeakAnnotation> topPeakList = new DoublePeakList<>();
    private final PeakList<PeakAnnotation> bottomPeakList = new DoublePeakList<>();

    private final List<PeptideFragmentAnnotation> topAnnotaions = new ArrayList<>();
    private final List<PeptideFragmentAnnotation> bottomAnnotations = new ArrayList<>();

    private final HashMap<String, String> annotatedPTMs = new HashMap<>();

    /**
     * @param ptmNameMap modifications to be annotated in the peptide view
     */
    public void setAnnotatedPTMs(Map<String, String> ptmNameMap)
    {
        annotatedPTMs.clear();
        annotatedPTMs.putAll(ptmNameMap);
    }

    private String cropModificationName(String name)
    {
        if (name.length() > 2)
            return name.substring(0, 2);
        return name;
    }

    private Peptide topPeptide;
    private Peptide bottomPeptide;

    private void drawPeptide(Peptide peptide, List<PeptideFragmentAnnotation> annotations,
                             double rangeTop, double rangeBottom, boolean isTop)
    {
        if (peptide == null)
            return;

        double width = rightXLoc - leftXLoc;
        int length = peptide.size();

        int pad_aa = settings.getAminoAcidPad();
        double pads = pad_aa * 2 * length;
        Font aaFont = settings.getAminoAcidFont();
        Font aaRealFont = aaFont;
        Text text = new Text("W");
        text.setFont(aaFont);
        double aaHeight = text.getLayoutBounds().getHeight();
        double aaWidth = text.getLayoutBounds().getWidth();
        if (aaWidth * length > (width - pads)) {
            int newSize = (int) (aaFont.getSize() * (width - pads) / aaWidth * length);
            aaRealFont = Font.font(aaFont.getFamily(), FontWeight.BOLD, newSize);
            text.setFont(aaRealFont);
            aaHeight = text.getLayoutBounds().getHeight();
            aaWidth = text.getLayoutBounds().getWidth();
        }

        int aminoAcidModificationSpace = settings.getAminoAcidModificationSpace();
        double y_aa = (rangeTop + rangeBottom - aaHeight) / 2;
        double x_start = leftXLoc + (width - (aaWidth + pad_aa * 2) * length) / 2;

        Font modificationFont = settings.getModificationFont();
        text.setFont(modificationFont);
        double height = text.getBoundsInLocal().getHeight();
        double y_mod = isTop ? y_aa - aminoAcidModificationSpace - height : y_aa + aaHeight + aminoAcidModificationSpace;

        for (int i = 0; i < peptide.size(); i++) {
            String symbol = peptide.getSymbol(i).getSymbol();
            Text aaText = new Text(symbol);
            aaText.setFont(aaRealFont);
            aaText.setTextOrigin(VPos.TOP);
            double x_aa = x_start + (aaWidth + pad_aa * 2) * i + pad_aa;
            aaText.relocate(x_aa, y_aa);
            getChildren().add(aaText);

            if (peptide.hasModificationAt(i)) {
                ModificationList modList = peptide.getModificationsAt(i, ModAttachment.all);
                for (Modification modification : modList) {
                    String title = modification.getTitle();
                    if (annotatedPTMs.containsKey(title)) {
                        String mod = cropModificationName(annotatedPTMs.get(title));
                        Text modText = new Text(mod);
                        modText.setFont(settings.getModificationFont());
                        modText.setTextOrigin(VPos.TOP);
                        double x_mod = x_aa + aaText.getBoundsInLocal().getWidth() / 2 - modText.getBoundsInLocal().getWidth() / 2;
                        modText.relocate(x_mod, y_mod);
                        getChildren().add(modText);
                        break;
                    }
                }

            }
        }

        if (annotations.isEmpty())
            return;

        ArrayListMultimap<Ion, PeptideFragmentAnnotation> annoMap = ArrayListMultimap.create();
        SetMultimap<FragmentType, Integer> annoSet = HashMultimap.create();
        for (PeptideFragmentAnnotation annotation : annotations) {
            annoMap.put(annotation.getIon(), annotation);
            FragmentType fragmentType = annotation.getIon().getFragmentType();
            int size = annotation.getFragment().size();
            annoSet.put(fragmentType, size);
        }

        int lineWidth = settings.getPeptideAnnotationLineWidth();
        int pad_aa_line = settings.getAminoAcidLineSpace();
        int peptideLabelLineSpace = settings.getPeptideLabelLineSpace();
        double y1 = NodeUtils.snap(y_aa - pad_aa_line, lineWidth);
        double y2 = y_aa + aaHeight / 2;
        double y3 = NodeUtils.snap(y_aa + aaHeight + pad_aa_line, lineWidth);
        Set<Integer> sizes = annoSet.get(FragmentType.FORWARD);
        for (Integer size : sizes) {
            double x1 = x_start + (aaWidth + pad_aa * 2) * (size - 1) + pad_aa + aaWidth;
            double x2 = NodeUtils.snap(x1 + pad_aa, lineWidth);

            Polyline polyline = new Polyline(x1, y3, x2, y3, x2, y2);
            polyline.setStroke(settings.getColor(Ion.b));
            polyline.setStrokeWidth(settings.getPeptideAnnotationLineWidth());

            getChildren().add(polyline);

            String label = String.valueOf(size);
            Text labelText = new Text(label);
            labelText.setFont(settings.getAminoAcidLabelFont());
            labelText.setTextOrigin(VPos.TOP);
            labelText.setFill(settings.getColor(Ion.b));
            labelText.relocate(x1, y3 + peptideLabelLineSpace);
            getChildren().add(labelText);
        }

        sizes = annoSet.get(FragmentType.REVERSE);
        for (Integer size : sizes) {
            double x1 = x_start + (aaWidth + pad_aa * 2) * (length - size);
            double x2 = x1 + pad_aa;
            x1 = NodeUtils.snap(x1, lineWidth);

            Color color = settings.getColor(Ion.y);

            Polyline polyline = new Polyline(x1, y2, x1, y1, x2, y1);
            polyline.setStroke(color);
            polyline.setStrokeWidth(settings.getPeptideAnnotationLineWidth());

            getChildren().add(polyline);

            String label = String.valueOf(size);
            Text labelText = new Text(label);
            labelText.setFont(settings.getAminoAcidLabelFont());
            labelText.setTextOrigin(VPos.TOP);
            labelText.setFill(color);
            labelText.relocate(x1 - labelText.getBoundsInLocal().getWidth() + pad_aa, y1 - peptideLabelLineSpace - labelText.getBoundsInLocal().getHeight());
            getChildren().add(labelText);
        }
    }

    private String topTitle;
    private String bottomTitle;

    public void setTitle(String topTitle, String bottomTitle)
    {
        this.topTitle = topTitle;
        this.bottomTitle = bottomTitle;
    }

    private void drawTitle()
    {
        int titleLeftSpace = settings.getTitleLeftSpace();

        double middleY = (topFrameYLoc + topTitleYLoc) / 2;
        Text topTitleText = new Text(topTitle);
        topTitleText.setTextOrigin(VPos.TOP);
        topTitleText.setFont(settings.getTitleFont());
        double height = topTitleText.getBoundsInLocal().getHeight();
        topTitleText.relocate(leftXLoc + titleLeftSpace, middleY - height / 2);
        getChildren().add(topTitleText);

        middleY = (bottomFrameYLoc + bottomTitleYLoc) / 2;
        Text bottomTitleText = new Text(bottomTitle);
        bottomTitleText.setTextOrigin(VPos.TOP);
        bottomTitleText.setFont(settings.getTitleFont());
        bottomTitleText.relocate(leftXLoc + titleLeftSpace, middleY - height / 2);
        getChildren().add(bottomTitleText);
    }

    private String topSubTitle;
    private String bottomSubTitle;

    public void setTopSubTitle(String topSubTitle)
    {
        this.topSubTitle = topSubTitle;
    }

    public void setBottomSubTitle(String bottomSubTitle)
    {
        this.bottomSubTitle = bottomSubTitle;
    }

    private void drawSubTitle()
    {
        int rightSpace = settings.getTitleLeftSpace();

        if (StringUtils.isNotEmpty(topSubTitle)) {
            double middleY = (topFrameYLoc + topTitleYLoc) / 2;
            Text topTitleText = new Text(topSubTitle);
            topTitleText.setTextOrigin(VPos.TOP);
            topTitleText.setFont(settings.getTitleFont());
            double height = topTitleText.getBoundsInLocal().getHeight();
            topTitleText.relocate(rightXLoc - rightSpace - topTitleText.getBoundsInLocal().getWidth(), middleY - height / 2);
            getChildren().add(topTitleText);
        }

        if (StringUtils.isNotEmpty(bottomSubTitle)) {
            double middleY = (bottomFrameYLoc + bottomTitleYLoc) / 2;
            Text bottomTitleText = new Text(bottomSubTitle);
            bottomTitleText.setTextOrigin(VPos.TOP);
            bottomTitleText.setFont(settings.getTitleFont());
            double height = bottomTitleText.getBoundsInLocal().getHeight();
            double width = bottomTitleText.getBoundsInLocal().getWidth();
            bottomTitleText.relocate(rightXLoc - width - rightSpace, middleY - height / 2);
            getChildren().add(bottomTitleText);
        }
    }

    /**
     * clear peak list and peptide
     */
    public void clear()
    {
        topPeakList.clear();
        bottomPeakList.clear();
        topPeptide = null;
        bottomPeptide = null;
        topTitle = null;
        bottomTitle = null;
        topSubTitle = null;
        bottomSubTitle = null;
        resetScale();
        repaint();
    }

    /**
     * set the peak list to be drawn.
     */
    public void set(PeakList<PeakAnnotation> topPeakList, Peptide topPeptide,
                    PeakList<PeakAnnotation> bottomPeakList, Peptide bottomPeptide)
    {
        checkNotNull(topPeakList);
        checkNotNull(bottomPeakList);
        checkNotNull(topPeptide);
        checkNotNull(bottomPeptide);

        if (topPeakList.isEmpty() || bottomPeakList.isEmpty())
            return;

        this.topPeptide = topPeptide;
        this.bottomPeptide = bottomPeptide;

        this.topPeakList.clear();
        this.bottomPeakList.clear();

        this.topPeakList.addPeaks(topPeakList);
        this.bottomPeakList.addPeaks(bottomPeakList);

        this.topAnnotaions.clear();
        getPeptideAnnotations(topPeakList, topAnnotaions);
        this.bottomAnnotations.clear();
        getPeptideAnnotations(bottomPeakList, bottomAnnotations);

        resetScale();
        repaint();
    }

    private void getPeptideAnnotations(PeakList<PeakAnnotation> peakList, List<PeptideFragmentAnnotation> annotations)
    {
        for (int i = 0; i < peakList.size(); i++) {
            if (!peakList.hasAnnotationsAt(i))
                continue;
            List<PeakAnnotation> annoList = peakList.getAnnotations(i);
            for (PeakAnnotation peakAnnotation : annoList) {
                if (peakAnnotation instanceof PeptideFragmentAnnotation) {
                    PeptideFragmentAnnotation anno = (PeptideFragmentAnnotation) peakAnnotation;
                    annotations.add(anno);
                }
            }
        }
    }

    private void drawPeakList(PeakList<PeakAnnotation> peakList, boolean isTop)
    {
        double minIntensity = isTop ? topMinIntensity : bottomMinIntensity;
        double peakWidth = settings.getPeakWidth();
        int intWidth = (int) peakWidth;
        Color peakColor = settings.getPeakColor();
        for (int i = 0; i < peakList.size(); i++) {
            if (peakList.hasAnnotationsAt(i))
                continue;

            double mz = peakList.getX(i);
            double in = peakList.getY(i);

            if (mz <= scaleMinMz || mz >= scaleMaxMz)
                continue;
            if (in <= minIntensity)
                continue;

            double xPos = getX(mz);
            double yPos = isTop ? getTopY(in) : getBottomY(in);

            double x = NodeUtils.snap(xPos, intWidth);
            Line line = new Line(x, yPos, x, middleYLoc);
            line.setStrokeWidth(peakWidth);
            line.setStroke(peakColor);
            getChildren().add(line);
        }
    }

    private void drawAnnotation(PeakList<PeakAnnotation> peakList, boolean isTop)
    {
        double minIntensity = isTop ? topMinIntensity : bottomMinIntensity;
        double annotatedPeakWidth = settings.getAnnotatedPeakWidth();
        int intWidth = (int) annotatedPeakWidth;
        Color peakColor = settings.getPeakColor();

        HashMap<Point2D, Text> textMap = new HashMap<>();
        for (int i = 0; i < peakList.size(); i++) {
            if (!peakList.hasAnnotationsAt(i))
                continue;

            double mz = peakList.getX(i);
            double in = peakList.getY(i);
            if (mz <= scaleMinMz || mz >= scaleMaxMz)
                continue;
            if (in <= minIntensity)
                continue;

            double xPos = getX(mz);
            double yPos = isTop ? getTopY(in) : getBottomY(in);

            List<PeakAnnotation> annotations = peakList.getAnnotations(i);
            PeakAnnotation peakAnnotation = annotations.get(0);
            Ion ion = peakAnnotation.getIon();
            Color color = settings.getColor(ion);
            if (color == null)
                color = peakColor;

            double annoX = NodeUtils.snap(xPos, intWidth);
            Line line = new Line(annoX, middleYLoc, annoX, yPos);
            line.setStrokeWidth(annotatedPeakWidth);
            line.setStroke(color);
            getChildren().add(line);

            String label = SpectrumChartUtils.getAnnotationsLabel(annotations);
            Text labelText = new Text(label);
            labelText.setFont(settings.getPeakLabelFont());
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

        HashMap<Point2D, PointLabel> labelMap = isTop ?
                PointLabel.placeLabelGrasp(pointMap, settings.getPeakLabelSpace(),
                        leftXLoc, topPeptideYLoc, rightXLoc, middleYLoc) :
                PointLabel.placeLabelGrasp(pointMap, LabelPos.getBottomPoses(), settings.getPeakLabelSpace(),
                        leftXLoc, middleYLoc, rightXLoc, bottomPeptideYLoc);
        for (Map.Entry<Point2D, PointLabel> entry : labelMap.entrySet()) {
            Point2D key = entry.getKey();
            PointLabel pointLabel = entry.getValue();
            Point2D minLoc = pointLabel.getMinLoc();
            LabelPos pos = pointLabel.getPos();
            Text text = textMap.get(key);
            text.relocate(minLoc.getX(), minLoc.getY());
            getChildren().add(text);

            // add line
            if ((isTop && pos != LabelPos.TOP_CENTER1) || (!isTop && pos != LabelPos.BOTTOM_CENTER1)) {
                Point2D linkPoint = pointLabel.getLinkPoint();
                Line line = new Line(linkPoint.getX(), linkPoint.getY(), key.getX(), key.getY());
                line.setStroke(text.getFill());
                line.setStyle(ANNO_LINE);

                getChildren().add(line);
            }
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
