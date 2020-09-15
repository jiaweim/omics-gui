package omics.gui.util;

import javafx.concurrent.Task;
import omics.util.io.MonitorableFileInputStream;
import omics.util.io.csv.CsvReader;
import omics.util.protein.Peptide;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * read dual PSM list.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 03 Sep 2020, 12:23 PM
 */
public class ReadDualPSMTask extends Task<List<DualPSM>>
{
    private final File file;

    public ReadDualPSMTask(File file)
    {
        this.file = file;
    }

    @Override
    protected List<DualPSM> call() throws IOException
    {
        updateTitle("Reading dual PSM csv file");
        MonitorableFileInputStream inputStream = new MonitorableFileInputStream(file.toPath());
        CsvReader reader = new CsvReader(inputStream, StandardCharsets.UTF_8);
        reader.readHeaders();
        List<DualPSM> dualPSMList = new ArrayList<>();
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

            try {
                Peptide topPep = Peptide.parse(topPeptide);
                psm.setTopPeptide(topPep);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Peptide bottomPep = Peptide.parse(bottomPeptide);
                psm.setBottomPeptide(bottomPep);
            } catch (Exception e) {
                e.printStackTrace();
            }

            psm.setTopFile(topFile);
            psm.setBottomFile(bottomFile);

            psm.setTopScan(Integer.parseInt(topScan));
            psm.setBottomScan(Integer.parseInt(bottomScan));

            psm.setTopTitle(topTitle);
            psm.setBottomTitle(bottomTitle);

            psm.setTopSubTitle(topSubTitle);
            psm.setBottomSubTitle(bottomSubTitle);

            dualPSMList.add(psm);

            updateProgress(inputStream.getProgress(), inputStream.getMaximum());
        }
        reader.close();
        updateValue(dualPSMList);
        return dualPSMList;
    }
}
