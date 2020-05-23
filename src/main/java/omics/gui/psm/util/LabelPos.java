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
    TOP_LEFT4;

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

        penaltyMap.put(LabelPos.TOP_RIGHT4, 10 / 10.);
        penaltyMap.put(LabelPos.TOP_LEFT4, 11 / 10.);
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
