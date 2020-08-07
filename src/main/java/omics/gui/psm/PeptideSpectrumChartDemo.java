package omics.gui.psm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import omics.msdk.model.MsDataFile;
import omics.msdk.model.SpectrumKeyFunc;
import omics.pdk.ident.util.IonAnnotator;
import omics.pdk.ptm.glycosylation.ident.OxoniumDB;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.Tolerance;
import omics.util.protein.Peptide;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;
import omics.util.protein.ms.PeptideIon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 16 Dec 2019, 11:42 AM
 */
public class PeptideSpectrumChartDemo extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        PeptideSpectrumChart root = new PeptideSpectrumChart();
//        MsDataFile dataFile = MsDataFile.read("Z:\\MaoJiawei\\data\\liujing\\20200520_S1_CG_Glycopeptides_1ug_HCD-1.mgf");
        MsDataFile dataFile = MsDataFile.read("Z:\\MaoJiawei\\data\\liujing\\20200520_S1_CG_Glycopeptides_1ug_193nmUVPD_1_5mJ-1.mgf");
        HashMap<String, MsnSpectrum> map = dataFile.map(SpectrumKeyFunc.SCAN);
        int scan = 13056;
        MsnSpectrum spectrum = map.get(String.valueOf(scan));

//        Peptide peptide = Peptide.parse("SSANNC(Carbamidomethyl)TF");
//        Peptide peptide = Peptide.parse("FSNVTWF");
//        Peptide peptide = Peptide.parse("IVNNATNVVIKVC(Carbamidomethyl)EF");
//        Peptide peptide = Peptide.parse("QDVNC(Carbamidomethyl)TEVPVAIHADQLTPTW");
//        Peptide peptide = Peptide.parse("QDVNC(Carbamidomethyl)TEVPVAIHADQL");
        Peptide peptide = Peptide.parse("QDVNC(Carbamidomethyl)TEVPVAIHADQLTPTW");

        Tolerance ms2 = Tolerance.abs(0.05);

        PeptideFragmentAnnotator annotator = new PeptideFragmentAnnotator(new PeptideFragmenter(), ms2);
        List<PeptideIon> ions = new ArrayList<>();
        ions.add(PeptideIon.b(1));
        ions.add(PeptideIon.b(2));
        ions.add(PeptideIon.y(1));
        ions.add(PeptideIon.y(2));

        ions.add(PeptideIon.a(1));
        ions.add(PeptideIon.c(1));
        ions.add(PeptideIon.x(1));

        annotator.setPeptideIonList(ions);

        annotator.annotate((PeakList) spectrum, peptide);
        IonAnnotator.annoNCore(spectrum, peptide.getMolecularMass(), ms2, 1, spectrum.getPrecursorCharge());

        IonAnnotator.annotateOxonium(spectrum, ms2, OxoniumDB.getInstance().getMarkers(), 1, 1);
//        IonAnnotator.annotatePrecursor(msnSpectrum, ms2, 1, Integer.MAX_VALUE);

        Scene scene = new Scene(root, 1000, 500);
        root.setPeptideSpectrum(peptide, spectrum);
        stage.setScene(scene);
        stage.setTitle("Using FXMl Custom Control");
        stage.show();
    }
}
