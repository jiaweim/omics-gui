package omics.gui;

import omics.pdk.ptm.glycosylation.ident.OxoniumDB;
import omics.pdk.ptm.oglycan.util.OxoniumFilter;
import omics.pdk.ptm.oglycan.util.OxoniumMarker;
import omics.util.ms.peaklist.Tolerance;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 27 May 2020, 4:02 PM
 */
public class DailyTest
{
    @org.junit.jupiter.api.Test
    void test()
    {
        OxoniumDB instance = OxoniumDB.getInstance();
        for (OxoniumMarker marker : instance.getMarkers()) {
            System.out.println(marker.getName() + "\t" + marker.getMz());
        }

        System.out.println();
        OxoniumFilter filter = OxoniumFilter.getDefaultOxoniumFilter(Tolerance.abs(0.05));
        for (OxoniumMarker oxoniumMarker : filter.getMarkerList()) {
            System.out.println(oxoniumMarker.getName() + "\t" + oxoniumMarker.getMz());
        }

    }
}
