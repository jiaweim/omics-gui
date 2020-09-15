package omics.gui;

import omics.pdk.ptm.glyco.OxoniumMarker;
import omics.pdk.ptm.glyco.ident.OxoniumDB;
import omics.pdk.ptm.glyco.o_glyco.util.OxoniumFilter;
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
        for (OxoniumMarker marker : instance.getItemList()) {
            System.out.println(marker.getName() + "\t" + marker.getMz());
        }

        System.out.println();
        OxoniumFilter filter = OxoniumFilter.getDefaultOxoniumFilter(Tolerance.abs(0.05));
        for (OxoniumMarker oxoniumMarker : filter.getMarkerList()) {
            System.out.println(oxoniumMarker.getName() + "\t" + oxoniumMarker.getMz());
        }

    }
}
