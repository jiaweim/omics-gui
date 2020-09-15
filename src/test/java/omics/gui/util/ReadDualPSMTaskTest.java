package omics.gui.util;

import omics.util.io.MonitorableFileInputStream;
import omics.util.io.csv.CsvReader;
import omics.util.protein.Peptide;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 10 Sep 2020, 1:48 PM
 */
class ReadDualPSMTaskTest
{

    @Test
    void call() throws IOException
    {
        MonitorableFileInputStream inputStream = new MonitorableFileInputStream(Paths.get("Z:\\MaoJiawei\\dataset\\methyl\\ident result\\hek293_scx_removeMod_f2s2_aa8_view.csv"));
        CsvReader reader = new CsvReader(inputStream, StandardCharsets.UTF_8);
        reader.readHeaders();
        List<DualPSM> dualPSMList = new ArrayList<>();
        int count = 0;
        while (reader.readRecord()) {
            String topPeptide = reader.get("TopPeptide");
            String bottomPeptide = reader.get("BottomPeptide");
            String topFile = reader.get("TopFile");
            String bottomFile = reader.get("BottomFile");
            String topScan = reader.get("TopScan");
            String bottomScan = reader.get("BottomScan");
            String topTitle = reader.get("TopTitle");
            String bottomTitle = reader.get("BottomTitle");
            String topSubTitle = reader.get("TopSubTitle");
            String bottomSubTitle = reader.get("BottomSubTitle");

            DualPSM psm = new DualPSM();

            Peptide topPep = Peptide.parse(topPeptide);
            Peptide bottomPep = Peptide.parse(bottomPeptide);

            psm.setTopPeptide(topPep);
            psm.setBottomPeptide(bottomPep);

            psm.setTopFile(topFile);
            psm.setBottomFile(bottomFile);

            psm.setTopScan(Integer.parseInt(topScan));
            psm.setBottomScan(Integer.parseInt(bottomScan));

            psm.setTopTitle(topTitle);
            psm.setBottomTitle(bottomTitle);

            psm.setTopSubTitle(topSubTitle);
            psm.setBottomSubTitle(bottomSubTitle);

            dualPSMList.add(psm);

            count++;
            System.out.println(count);
        }
        reader.close();
        System.out.println(dualPSMList.size());
    }
}