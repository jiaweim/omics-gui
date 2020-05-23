package omics.gui.psm.util;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Callback;
import omics.util.OmicsTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * Utilities for javafx gui.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 30 Sep 2018, 9:14 PM
 */
public class NodeUtils
{
    /**
     * for line with odd width, to have sharp border, its position should be half.
     *
     * @param value position
     * @return modified position.
     */
    public static double snapOdd(double value)
    {
        return ((int) value) + .5;
    }

    /**
     * for line with even width, to have sharp border, its position should be integer.
     *
     * @param value position
     * @return modified position.
     */
    public static double snapEven(double value)
    {
        return Math.round(value);
    }

    /**
     * modified the position of given line width to make it have a sharp border.
     *
     * @param value position
     * @param width line width
     * @return modified position.
     */
    public static double snap(double value, int width)
    {
        if (width % 2 == 0) {
            return snapEven(value);
        } else
            return snapOdd(value);
    }

    /**
     * Calculate the font size that can fit given width.
     *
     * @param font     {@link Font}.
     * @param value    String to draw
     * @param maxWidth max width allowed.
     * @return suitable font size for the String and width.
     */
    public static double calcFontSizeFit(Font font, String value, double maxWidth)
    {
        double fontSize = font.getSize();
        Bounds bounds = textSize(font, VPos.CENTER, value);
        if (bounds.getWidth() > maxWidth) {
            return fontSize * maxWidth / bounds.getWidth();
        }
        return fontSize;
    }

    public static void saveNodeAsPng(Node node, double scale, String path)
    {
//        Bounds bounds = node.getLayoutBounds();
//        final WritableImage image = new WritableImage((int) Math.round(bounds.getWidth() * scale),
//                (int) Math.round(bounds.getHeight() * scale));
        final SnapshotParameters spa = new SnapshotParameters();
        spa.setTransform(new Scale(scale, scale));
//        ImageView view = new ImageView(node.snapshot(spa, image));
//        view.setFitWidth(bounds.getWidth());
//        view.setFitHeight(bounds.getHeight());
//        view.resize(bounds.getWidth(), bounds.getHeight());
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(node.snapshot(spa, null), null), "png", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save JavaFX Node to png.
     *
     * @param node    a javafx Node.
     * @param pngPath output path of the png file.
     */
    public static void saveNodeAsPng(Node node, int width, int height, String pngPath)
    {
        WritableImage image = new WritableImage(width, height);
        node.snapshot(null, image);

        BufferedImage bImg = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImg, "png", new File(pngPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save JavaFX Node to png.
     *
     * @param node    a javafx Node.
     * @param pngPath output path of the png file.
     */
    public static void saveNodeAsPng(Node node, String pngPath)
    {
        WritableImage image = node.snapshot(null, null);
        BufferedImage bImg = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bImg, "png", new File(pngPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * calculate the text size of given Font.
     *
     * @param font  {@link Font}
     * @param pos   {@link VPos} will have impact on the minY, maxY value.
     * @param value text to mearusing
     * @return {@link Bounds} of text
     */
    public static Bounds textSize(Font font, VPos pos, String value)
    {
        Text text = new Text(value);
        text.setFont(font);
        text.setTextOrigin(pos);

        return text.getBoundsInLocal();
    }

    /**
     * Add index column to the TableView.
     *
     * @param tableView a {@link TableView}
     */
    public static <P> void addIndexColumn(TableView<P> tableView)
    {
        TableColumn<P, Object> indexCol = new TableColumn<>("#");
        indexCol.setSortable(false);
        indexCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        indexCol.setCellFactory(new Callback<TableColumn<P, Object>, TableCell<P, Object>>()
        {
            @Override
            public TableCell<P, Object> call(TableColumn<P, Object> param)
            {
                return new TableCell<P, Object>()
                {
                    @Override
                    protected void updateItem(Object item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (getTableRow() != null && item != null) {
                            setText(String.valueOf(getTableRow().getIndex() + 1));
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        tableView.getColumns().add(indexCol);
    }

    /**
     * Create a {@link Task} instance from {@link OmicsTask}
     *
     * @param infoTask an {@link OmicsTask} instance
     * @return a {@link Task}
     */
    public static <T> Task<T> createTask(OmicsTask<T> infoTask)
    {
        return new Task<T>()
        {
            @Override
            protected T call()
            {
                infoTask.progressProperty().addListener(evt -> updateProgress((Double) evt.getNewValue(), 1.0));
                infoTask.titleProperty().addListener(evt -> updateTitle((String) evt.getNewValue()));
                infoTask.messageProperty().addListener(evt -> updateMessage((String) evt.getNewValue()));
                infoTask.exceptionProperty().addListener(evt -> setException((Throwable) evt.getNewValue()));
                try {
                    infoTask.go();
                    updateValue(infoTask.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return infoTask.getValue();
            }
        };
    }

    /**
     * Format double value in a {@link TableColumn}
     *
     * @param column a {@link TableColumn}
     * @param format {@link NumberFormat} used to format double value
     * @param <P>    parent type
     * @param <S>    child type
     */
    public static <P, S> void formatDoubleColumn(TableColumn<P, S> column, NumberFormat format)
    {
        column.setCellFactory(new Callback<TableColumn<P, S>, TableCell<P, S>>()
        {
            @Override
            public TableCell<P, S> call(TableColumn<P, S> param)
            {
                return new TableCell<P, S>()
                {
                    @Override
                    protected void updateItem(S item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        setText(null);
                        if (empty)
                            return;
                        if (item instanceof Double) {
                            Double value = (Double) item;
                            setText(format.format(value));
                        }
                    }
                };
            }
        });
    }

    /**
     * Return the intersect area between this and a specified rectangular area.
     *
     * @param x the x coordinate of the upper-left corner of the specified
     *          rectangular volume
     * @param y the y coordinate of the upper-left corner of the specified
     *          rectangular volume
     * @param w the width of the specified rectangular volume
     * @param h the height of the specified rectangular volume
     * @return the intersect area of this {@code Bounds} and the specified bounds.
     */
    public static double intersection(Bounds bounds, double x, double y, double w, double h)
    {
        if (!bounds.intersects(x, y, w, h))
            return 0.;

        double areaX1 = Double.max(x, bounds.getMinX());
        double areaX2 = Double.min(x + w, bounds.getMaxX());
        double areaY2 = Double.min(bounds.getMaxY(), y + h);
        double areaY1 = Double.max(bounds.getMinY(), y);

        return (areaX2 - areaX1) * (areaY2 - areaY1);
    }

    /**
     * Return the intersect area between this and a specified rectangular area.
     *
     * @param bounds1 a specified bounds
     * @param bounds2 a specified bounds
     * @return the intersect area of this {@code Bounds} and the specified bounds.
     */
    public static double intersection(Bounds bounds1, Bounds bounds2)
    {
        return intersection(bounds1, bounds2.getMinX(), bounds2.getMinY(), bounds2.getWidth(), bounds2.getHeight());
    }


    public static void createScreenCapture(String path) throws AWTException, IOException
    {
        Robot robot = new Robot();
        Rectangle rectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        BufferedImage screenCapture = robot.createScreenCapture(rectangle);
        ImageIO.write(screenCapture, "jpg", new File(path));
    }
}
