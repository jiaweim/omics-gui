package omics.gui.psm.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import omics.util.utils.Pair;

import java.util.*;

/**
 * Candidate label positions.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 19 Dec 2019, 10:37 AM
 */
public class PointLabel
{
    private final Point2D point;
    private final Point2D minLoc;
    private final Point2D maxLoc;
    private final LabelPos pos;
    private final double width;
    private final double height;
    private final double area;

    /**
     * Constructor
     *
     * @param point  point to be annotated
     * @param pos    candidate {@link LabelPos}
     * @param width  width of the label
     * @param height height of the label
     * @param space  space between label and the point
     */
    public PointLabel(Point2D point, LabelPos pos, double width, double height, double space)
    {
        this.point = point;
        this.pos = pos;
        this.width = width;
        this.height = height;

        this.minLoc = point.getLocation(width, height, pos, space);
        this.maxLoc = new Point2D(minLoc.getX() + width, minLoc.getY() + height);
        this.area = width * height;
        this.penalty = pos.getPenalty();
    }

    public double getArea()
    {
        return area;
    }

    /**
     * @return the bottom-right location of the point label.
     */
    public Point2D getMaxLoc()
    {
        return maxLoc;
    }

    /**
     * the point to be labeled.
     *
     * @return the point to be label.
     */
    public Point2D getPoint()
    {
        return point;
    }

    public LabelPos getPos()
    {
        return pos;
    }

    /**
     * @return the location for this candidate label.
     */
    public Point2D getMinLoc()
    {
        return minLoc;
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointLabel label = (PointLabel) o;
        return point.equals(label.point) &&
                pos == label.pos;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(point, pos);
    }

    private double penalty;

    /**
     * @return penalty for this
     */
    public double getPenalty()
    {
        return penalty;
    }

    public void setPenalty(double penalty)
    {
        this.penalty = penalty;
    }

    /**
     * Indicates whether any of the dimensions (width or height) of this bounds is less than zero.
     *
     * @return true if any of the dimension (width or height) of this bounds is less than zero.
     */
    public boolean isEmpty()
    {
        return width < 0 || height < 0;
    }

    public Point2D getLinkPoint()
    {
        double linkX;
        double linkY;
        switch (pos) {
            case TOP_RIGHT1:
            case TOP_RIGHT2:
            case TOP_RIGHT3:
            case TOP_RIGHT4:
                linkX = minLoc.getX();
                linkY = maxLoc.getY();
                break;
            case TOP_LEFT1:
            case TOP_LEFT2:
            case TOP_LEFT3:
            case TOP_LEFT4:
                linkX = maxLoc.getX();
                linkY = maxLoc.getY();
                break;
            case BOTTOM_LEFT1:
            case BOTTOM_LEFT2:
            case BOTTOM_LEFT3:
            case BOTTOM_LEFT4:
                linkX = maxLoc.getX();
                linkY = minLoc.getY();
                break;
            case BOTTOM_RIGHT1:
            case BOTTOM_RIGHT2:
            case BOTTOM_RIGHT3:
            case BOTTOM_RIGHT4:
                linkX = minLoc.getX();
                linkY = minLoc.getY();
                break;
            case TOP_CENTER1:
            case TOP_CENTER2:
            case TOP_CENTER3:
            case TOP_CENTER4:
                linkX = point.getX();
                linkY = maxLoc.getY();
                break;
            case BOTTOM_CENTER1:
            case BOTTOM_CENTER2:
            case BOTTOM_CENTER3:
            case BOTTOM_CENTER4:
                linkX = point.getX();
                linkY = minLoc.getY();
                break;
            default:
                linkX = point.getX();
                linkY = point.getY();
                break;
        }
        return new Point2D(linkX, linkY);
    }

    /**
     * Test if the interior of this {@code Bounds} intersects the interior of a specified rectangular area.
     *
     * @param pointLabel a specified point label
     * @return true if the interior of this {@code Bounds} and the interior
     * of the rectangular area intersect.
     */
    public boolean intersects(PointLabel pointLabel)
    {
        if (isEmpty() || pointLabel.isEmpty())
            return false;
        return pointLabel.maxLoc.getX() >= minLoc.getX() &&
                pointLabel.maxLoc.getY() >= minLoc.getY() &&
                pointLabel.minLoc.getX() <= maxLoc.getX() &&
                pointLabel.minLoc.getY() <= maxLoc.getY();
    }

    /**
     * Return the intersect area between this and a specified rectangular area.
     *
     * @param pointLabel a specified {@link PointLabel}
     * @return the intersect area of this {@code Bounds} and the specified bounds.
     */
    public double intersection(PointLabel pointLabel)
    {
        if (!intersects(pointLabel))
            return 0.;

        double areaX1 = Double.max(minLoc.getX(), pointLabel.minLoc.getX());
        double areaX2 = Double.min(maxLoc.getX(), pointLabel.maxLoc.getX());
        double areaY1 = Double.max(minLoc.getY(), pointLabel.minLoc.getY());
        double areaY2 = Double.min(maxLoc.getY(), pointLabel.maxLoc.getY());

        return (areaX2 - areaX1) * (areaY2 - areaY1);
    }

    private static final int N = 3;


    /**
     * find suitable positions for all labels with Greedy Randomized Adaptive Search Procedure (GRASP) algorithm
     *
     * @param point2BoundsMap map from point to be label and the label size (width, height)
     * @param space           space between labels and the labeled point.
     * @param minX            minimum X allowed
     * @param minY            minimum Y allowed
     * @param maxX            maximum X allowed
     * @param maxY            maximum Y allowed
     */
    public static HashMap<Point2D, PointLabel> placeLabelGrasp(Map<Point2D, Pair<Double, Double>> point2BoundsMap,
                                                               LabelPos[] candidatePoses, double space,
                                                               double minX, double minY, double maxX, double maxY)
    {
        List<PointLabel> candidateList = new ArrayList<>();
        ArrayListMultimap<Point2D, PointLabel> candidateMap = ArrayListMultimap.create();
        for (Map.Entry<Point2D, Pair<Double, Double>> entry : point2BoundsMap.entrySet()) {
            Point2D key = entry.getKey();
            Pair<Double, Double> value = entry.getValue();

            for (LabelPos pos : candidatePoses) {
                PointLabel label = new PointLabel(key, pos, value.getFirst(), value.getSecond(), space);
                Point2D minLoc = label.getMinLoc();
                Point2D maxLoc = label.getMaxLoc();
                if (minLoc.getX() < minX || minLoc.getY() < minY || maxLoc.getX() > maxX || maxLoc.getY() > maxY)
                    continue;
                PointLabel pointLabel = new PointLabel(key, pos, value.getFirst(), value.getSecond(), space);
                candidateList.add(pointLabel);
                candidateMap.put(key, pointLabel);
            }
        }

        for (PointLabel c1 : candidateList) {
            for (PointLabel c2 : candidateList) {
                if (!c1.getPoint().equals(c2.getPoint())) {
                    double intersection = c1.intersection(c2);
                    c1.penalty += (intersection / c1.area);
                }
            }
        }

        List<PointLabel> unassignedList = new ArrayList<>(candidateList);
        HashMap<Point2D, PointLabel> map = new HashMap<>(point2BoundsMap.size());
        while (!unassignedList.isEmpty()) {
            PointLabel best = getBest(unassignedList);
            map.put(best.getPoint(), best);

            List<PointLabel> toRemoved = new ArrayList<>();
            Point2D point = best.getPoint();
            for (PointLabel label : candidateList) {
                if (label.getPoint().equals(point) && !label.equals(best)) {
                    toRemoved.add(label);
                }
            }

            unassignedList.removeAll(toRemoved);
            unassignedList.remove(best);
            if (unassignedList.isEmpty())
                break;
            if (toRemoved.isEmpty())
                continue;
            for (PointLabel label : unassignedList) {
                for (PointLabel removed : toRemoved) {
                    label.penalty -= (label.intersection(removed) / label.area);
                }
            }
        }

        boolean changed = false;
        Set<Point2D> labels = map.keySet();
        for (int i = 0; i < N; i++) {
            for (Point2D label : labels) {
                PointLabel best = null;

                List<PointLabel> pointLabels = candidateMap.get(label);
                for (PointLabel candidate1 : pointLabels) {
                    candidate1.penalty = 0;

                    for (Point2D label2 : labels) {
                        if (label2.equals(label))
                            continue;

                        PointLabel candidate2 = map.get(label2);
                        candidate1.penalty += candidate1.intersection(candidate2);
                    }

                    if (best == null || candidate1.penalty < best.penalty) {
                        best = candidate1;
                    }
                }

                if (!map.get(label).equals(best)) {
                    changed = true;
                }
                map.put(label, best);
            }
            if (!changed) {
                break;
            }
        }
        return map;
    }

    /**
     * find suitable positions for all labels with Greedy Randomized Adaptive Search Procedure (GRASP) algorithm
     *
     * @param point2BoundsMap map from point to be label and the label size (width, height)
     * @param space           space between labels and the labeled point.
     * @param minX            minimum X allowed
     * @param minY            minimum Y allowed
     * @param maxX            maximum X allowed
     * @param maxY            maximum Y allowed
     */
    public static HashMap<Point2D, PointLabel> placeLabelGrasp(Map<Point2D, Pair<Double, Double>> point2BoundsMap, double space,
                                                               double minX, double minY, double maxX, double maxY)
    {
        return placeLabelGrasp(point2BoundsMap, LabelPos.getTopPoses(), space, minX, minY, maxX, maxY);
    }


    /**
     * find suitable positions for all labels, basic greedy algorithm.
     *
     * @param point2BoundsMap map from point to be label and the label size (width, height)
     * @param space           space between labels and the to be labeled point.
     * @param minX            minimum X allowed
     * @param minY            minimum Y allowed
     * @param maxX            maximum X allowed
     * @param maxY            maximum Y allowed
     */
    public static HashMap<Point2D, PointLabel> placeLabelAdvancedGreedy(Map<Point2D, Pair<Double, Double>> point2BoundsMap, double space,
                                                                        double minX, double minY, double maxX, double maxY)
    {
        List<PointLabel> candidateList = new ArrayList<>();
        LabelPos[] posArray = LabelPos.values();
        for (Map.Entry<Point2D, Pair<Double, Double>> entry : point2BoundsMap.entrySet()) {
            Point2D key = entry.getKey();
            Pair<Double, Double> value = entry.getValue();

            for (LabelPos pos : posArray) {
                PointLabel label = new PointLabel(key, pos, value.getFirst(), value.getSecond(), space);
                Point2D minLoc = label.getMinLoc();
                Point2D maxLoc = label.getMaxLoc();
                if (minLoc.getX() < minX || minLoc.getY() < minY || maxLoc.getX() > maxX || maxLoc.getY() > maxY)
                    continue;
                candidateList.add(new PointLabel(key, pos, value.getFirst(), value.getSecond(), space));
            }
        }

        for (PointLabel c1 : candidateList) {
            for (PointLabel c2 : candidateList) {
                if (!c1.getPoint().equals(c2.getPoint())) {
                    double intersection = c1.intersection(c2);
                    c1.penalty += (intersection / c1.area);
                }
            }
        }

        List<PointLabel> unassignedList = new ArrayList<>(candidateList);
        HashMap<Point2D, PointLabel> map = new HashMap<>(point2BoundsMap.size());
        while (!unassignedList.isEmpty()) {
            PointLabel best = getBest(unassignedList);
            map.put(best.getPoint(), best);

            List<PointLabel> toRemoved = new ArrayList<>();
            Point2D point = best.getPoint();
            for (PointLabel label : candidateList) {
                if (label.getPoint().equals(point) && !label.equals(best)) {
                    toRemoved.add(label);
                }
            }

            unassignedList.removeAll(toRemoved);
            unassignedList.remove(best);
            if (unassignedList.isEmpty())
                break;
            if (toRemoved.isEmpty())
                continue;
            for (PointLabel label : unassignedList) {
                for (PointLabel removed : toRemoved) {
                    label.penalty -= (label.intersection(removed) / label.area);
                }
            }
        }
        return map;
    }

    private static PointLabel getBest(List<PointLabel> labelList)
    {
        PointLabel best = null;
        for (PointLabel label : labelList) {
            if (best == null || label.getPenalty() < best.getPenalty())
                best = label;
        }

        return best;
    }


    /**
     * find suitable positions for all labels, basic greedy algorithm.
     *
     * @param point2BoundsMap map from point to be label and the label size (width, height)
     * @param space           space between labels and the to be labeled point.
     * @param minX            minimum X allowed
     * @param minY            minimum Y allowed
     * @param maxX            maximum X allowed
     * @param maxY            maximum Y allowed
     */
    public static HashMap<Point2D, PointLabel> placeLabelSimpleGreedy(Map<Point2D, Pair<Double, Double>> point2BoundsMap, double space,
                                                                      double minX, double minY, double maxX, double maxY)
    {
        List<PointLabel> candidateList = new ArrayList<>();
        LabelPos[] posArray = LabelPos.values();
        for (Map.Entry<Point2D, Pair<Double, Double>> entry : point2BoundsMap.entrySet()) {
            Point2D key = entry.getKey();
            Pair<Double, Double> value = entry.getValue();
            for (LabelPos pos : posArray) {
                PointLabel label = new PointLabel(key, pos, value.getFirst(), value.getSecond(), space);
                Point2D minLoc = label.getMinLoc();
                Point2D maxLoc = label.getMaxLoc();
                if (minLoc.getX() < minX || minLoc.getY() < minY || maxLoc.getX() > maxX || maxLoc.getY() > maxY)
                    continue;
                candidateList.add(new PointLabel(key, pos, value.getFirst(), value.getSecond(), space));
            }
        }

        for (PointLabel c1 : candidateList) {
            for (PointLabel c2 : candidateList) {
                if (c1 != c2) {
                    double intersection = c1.intersection(c2);
                    c1.penalty += intersection;
                }
            }
        }

        candidateList.sort((o1, o2) -> ComparisonChain.start()
                .compare(o1.penalty, o2.penalty)
                .compare(o1.pos.getPenalty(), o2.pos.getPenalty()).result());
        HashMap<Point2D, PointLabel> map = new HashMap<>(point2BoundsMap.size());
        for (PointLabel candidate : candidateList) {
            if (map.containsKey(candidate.getPoint()))
                continue;
            map.put(candidate.getPoint(), candidate);
        }
        return map;
    }
}
