package omics.gui.psm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 19 Dec 2019, 9:54 AM
 */
public enum LabelPos
{
    TOP_CENTER1,
    TOP_CENTER2,
    TOP_CENTER3,
    TOP_RIGHT1,
    TOP_LEFT1,
    TOP_CENTER4,
    TOP_RIGHT2,
    TOP_LEFT2,
    TOP_RIGHT3,
    TOP_LEFT3,
    TOP_RIGHT4,
    TOP_LEFT4,
    BOTTOM_CENTER1,
    BOTTOM_CENTER2,
    BOTTOM_CENTER3,
    BOTTOM_RIGHT1,
    BOTTOM_LEFT1,
    BOTTOM_CENTER4,
    BOTTOM_RIGHT2,
    BOTTOM_LEFT2,
    BOTTOM_RIGHT3,
    BOTTOM_LEFT3,
    BOTTOM_RIGHT4,
    BOTTOM_LEFT4;

    public static LabelPos[] getTopPoses()
    {
        return new LabelPos[]{TOP_CENTER1, TOP_CENTER2, TOP_CENTER3,
                TOP_RIGHT1, TOP_LEFT1, TOP_CENTER4,
                TOP_RIGHT2, TOP_LEFT2, TOP_RIGHT3,
                TOP_LEFT3, TOP_RIGHT4, TOP_LEFT4};
    }

    public static LabelPos[] getBottomPoses()
    {
        return new LabelPos[]{BOTTOM_CENTER1, BOTTOM_CENTER2, BOTTOM_CENTER3,
                BOTTOM_RIGHT1, BOTTOM_LEFT1, BOTTOM_CENTER4,
                BOTTOM_RIGHT2, BOTTOM_LEFT2, BOTTOM_RIGHT3,
                BOTTOM_LEFT3, BOTTOM_RIGHT4, BOTTOM_LEFT4};
    }

    private static final Map<LabelPos, Double> penaltyMap = new HashMap<>();

    static {
        penaltyMap.put(LabelPos.TOP_CENTER1, 0.);
        penaltyMap.put(LabelPos.TOP_CENTER2, 1 / 12.);
        penaltyMap.put(LabelPos.TOP_CENTER3, 2 / 12.);
        penaltyMap.put(LabelPos.TOP_CENTER4, 5 / 12.);

        penaltyMap.put(LabelPos.TOP_RIGHT1, 3 / 12.);
        penaltyMap.put(LabelPos.TOP_LEFT1, 4 / 12.);

        penaltyMap.put(LabelPos.TOP_RIGHT2, 6 / 12.);
        penaltyMap.put(LabelPos.TOP_LEFT2, 7 / 12.);

        penaltyMap.put(LabelPos.TOP_RIGHT3, 8 / 12.);
        penaltyMap.put(LabelPos.TOP_LEFT3, 9 / 12.);

        penaltyMap.put(LabelPos.TOP_RIGHT4, 10 / 12.);
        penaltyMap.put(LabelPos.TOP_LEFT4, 11 / 12.);

        penaltyMap.put(LabelPos.BOTTOM_CENTER1, 0.);
        penaltyMap.put(LabelPos.BOTTOM_CENTER2, 1 / 12.);
        penaltyMap.put(LabelPos.BOTTOM_CENTER3, 2 / 12.);
        penaltyMap.put(LabelPos.BOTTOM_CENTER4, 5 / 12.);

        penaltyMap.put(LabelPos.BOTTOM_RIGHT1, 3 / 12.);
        penaltyMap.put(LabelPos.BOTTOM_LEFT1, 4 / 12.);

        penaltyMap.put(LabelPos.BOTTOM_RIGHT2, 6 / 12.);
        penaltyMap.put(LabelPos.BOTTOM_LEFT2, 7 / 12.);

        penaltyMap.put(LabelPos.BOTTOM_RIGHT3, 8 / 12.);
        penaltyMap.put(LabelPos.BOTTOM_LEFT3, 9 / 12.);

        penaltyMap.put(LabelPos.BOTTOM_RIGHT4, 10 / 12.);
        penaltyMap.put(LabelPos.BOTTOM_LEFT4, 11 / 12.);
    }


    /**
     * set the penalty of this pos
     *
     * @param penalty penalty value.
     */
    public void setPenalty(double penalty)
    {
        penaltyMap.put(this, penalty);
    }

    /**
     * @return penalty of the position.
     */
    public double getPenalty()
    {
        return penaltyMap.get(this);
    }
}
