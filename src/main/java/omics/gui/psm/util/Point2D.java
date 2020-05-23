package omics.gui.psm.util;

import java.util.Objects;

/**
 * Point to be label.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 19 Dec 2019, 10:02 AM
 */
public class Point2D
{
    private final double x;
    private final double y;

    /**
     * Constructor.
     *
     * @param x x of the point to be label.
     * @param y y of the point to be label.
     */
    public Point2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString()
    {
        return x + ", " + y;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point2D point2D = (Point2D) o;
        return Double.compare(point2D.x, x) == 0 &&
                Double.compare(point2D.y, y) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public Point2D getLocation(double w, double h, LabelPos pos, double space)
    {
        switch (pos) {

            case TOP_CENTER1:
                return new Point2D(x - w * .5, y - h - space);
            case TOP_CENTER2:
                return new Point2D(x - w * .5, y - h * 2 - space);
            case TOP_CENTER3:
                return new Point2D(x - w * .5, y - h * 3 - space);
            case TOP_CENTER4:
                return new Point2D(x - w * .5, y - h * 4 - space);
            case TOP_RIGHT1:
                return new Point2D(x + space, y - h - space);
            case TOP_RIGHT2:
                return new Point2D(x + space, y - h * 2 - space);
            case TOP_RIGHT3:
                return new Point2D(x + space, y - h * 3 - space);
            case TOP_RIGHT4:
                return new Point2D(x + space, y - h * 4 - space);
            case TOP_LEFT1:
                return new Point2D(x - w - space, y - h - space);
            case TOP_LEFT2:
                return new Point2D(x - w - space, y - h * 2 - space);
            case TOP_LEFT3:
                return new Point2D(x - w - space, y - h * 3 - space);
            case TOP_LEFT4:
                return new Point2D(x - w - space, y - h * 4 - space);
            default:
                return new Point2D(x, y);
        }
    }
}
