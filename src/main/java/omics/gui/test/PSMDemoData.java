package omics.gui.test;

import omics.msdk.io.MgfWriter;
import omics.msdk.model.MsDataFile;
import omics.pdk.IdentResult;
import omics.pdk.ident.model.PeptideSpectrumMatch;
import omics.pdk.ident.model.ProteinMatch;
import omics.util.OmicsException;
import omics.util.interfaces.Filter;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.ScanNumber;
import omics.util.protein.database.Protein;
import omics.util.protein.database.ProteinDB;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 14 Sep 2020, 5:26 PM
 */
public class PSMDemoData
{
    public static void extractProteins() throws OmicsException, FileNotFoundException
    {
        ProteinDB proteinDB = ProteinDB.read("Z:\\MaoJiawei\\fasta\\homo_20191210.fasta");
        IdentResult result = IdentResult.read("Z:\\MaoJiawei\\test\\omics\\psm_view.xlsx");
        PrintWriter writer = new PrintWriter("Z:\\MaoJiawei\\test\\omics\\");
        for (ProteinMatch proteinMatch : result.getProteinSet()) {
            String accession = proteinMatch.getAccession();
            Protein protein = proteinDB.get(accession);
            protein.writeToFasta(writer);
        }
        writer.close();
        proteinDB.clear();
    }

    static void getSample()
    {
        //        IdentResult result = IdentResult.read("D:\\igd\\results\\kidney_offset14.xlsx");
//        result.apply((Filter<PeptideSpectrumMatch>) filterable -> filterable.getMSFileId().equals("180222_KidneyNormal_F1"));
//
//        result.buildReference();
//        result.write("Z:\\MaoJiawei\\test\\omics\\psm_view_data.xlsx");
    }

    static void sampleToSearch() throws IOException
    {
        IdentResult result = IdentResult.read("Z:\\MaoJiawei\\test\\omics\\psm_view.xlsx");
        result.apply((Filter<PeptideSpectrumMatch>) filterable -> {
            String deltaName = filterable.getDeltaName();
            return deltaName.equals("Hex(1)HexNAc(3)");
        });
        result.buildReference();

        Set<ScanNumber> scanSet = new HashSet<>();
        for (PeptideSpectrumMatch psm : result) {
            scanSet.add(psm.getScanNumber());
        }

        MsDataFile dataFile = MsDataFile.read("Z:\\MaoJiawei\\dataset\\zhanghui\\raw_mgf\\180222_KidneyNormal_F1.mgf");
        MgfWriter writer = new MgfWriter(Paths.get("Z:\\MaoJiawei\\test\\omics\\s.mgf"));

        for (MsnSpectrum spectrum : dataFile) {
            if (scanSet.contains(spectrum.getScanNumber())) {
                writer.writeSpectrum(spectrum);
            }
        }

        writer.close();

        result.write("Z:\\MaoJiawei\\test\\omics\\psm.xlsx");

    }

    public static void main(String[] args) throws IOException
    {
        sampleToSearch();
//        IdentResult result = IdentResult.read("Z:\\MaoJiawei\\test\\omics\\psm_view.xlsx");


    }
}
