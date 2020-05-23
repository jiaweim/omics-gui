package omics.gui.psm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import omics.msdk.io.MgfReader;
import omics.pdk.ident.util.IonAnnotator;
import omics.pdk.ptm.glycosylation.ident.OxoniumDB;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.Tolerance;
import omics.util.protein.Peptide;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;
import omics.util.protein.ms.PeptideIon;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
//        PeptideSpectrumChart root = new PeptideSpectrumChart();

        SpectrumChart chart = new SpectrumChart();
        Peptide peptide = Peptide.parse("IEETTM(Oxidation)TTQTPAPIQAPSAILPLPGQSVER");
        Optional<MsnSpectrum> first = MgfReader.readFirst(Paths.get("D:\\data\\oglycan\\18609.mgf"));

        Tolerance ms2 = Tolerance.abs(0.05);
        MsnSpectrum msnSpectrum = first.get();
        PeptideFragmentAnnotator annotator = new PeptideFragmentAnnotator(new PeptideFragmenter(), ms2);
        List<PeptideIon> ions = new ArrayList<>();
        ions.add(PeptideIon.b(1));
        ions.add(PeptideIon.b(2));
        ions.add(PeptideIon.y(1));
        ions.add(PeptideIon.y(2));
        annotator.setPeptideIonList(ions);

        annotator.annotate((PeakList) msnSpectrum, peptide);
        IonAnnotator.annotateOxonium(msnSpectrum, ms2, OxoniumDB.getInstance().getMarkers(), 1, 1);
        IonAnnotator.annotatePrecursor(msnSpectrum, ms2, 1, Integer.MAX_VALUE);

        Scene scene = new Scene(chart, 800, 550);
        chart.setPeakList(msnSpectrum);
        stage.setScene(scene);
        stage.setTitle("Using FXMl Custom Control");
        stage.show();
    }
}
