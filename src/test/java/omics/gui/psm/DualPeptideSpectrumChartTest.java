package omics.gui.psm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import omics.msdk.io.MgfReader;
import omics.util.chem.Composition;
import omics.util.io.FileUtils;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.Tolerance;
import omics.util.protein.Peptide;
import omics.util.protein.mod.Modification;
import omics.util.protein.mod.PTM;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;
import omics.util.protein.ms.PeptideIon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 05 Jun 2020, 3:05 PM
 */
public class DualPeptideSpectrumChartTest extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        StackPane root = new StackPane();

        DualPeptideSpectrumChart chart = new DualPeptideSpectrumChart();

        Optional<MsnSpectrum> upperSpectrum = MgfReader.readFirst(FileUtils.getResourcePath("light.mgf"));
        Optional<MsnSpectrum> lowerSpectrum = MgfReader.readFirst(FileUtils.getResourcePath("heavy.mgf"));

        PeptideFragmenter fragmenter = new PeptideFragmenter();
        List<PeptideIon> peptideIonList = new ArrayList<>();
        peptideIonList.add(PeptideIon.b(1));
        peptideIonList.add(PeptideIon.b(2));
        peptideIonList.add(PeptideIon.y(1));
        peptideIonList.add(PeptideIon.y(2));
        fragmenter.setPeptideIonList(peptideIonList);
        PeptideFragmentAnnotator annotator = new PeptideFragmentAnnotator(fragmenter, Tolerance.abs(0.05));

        Peptide.Builder lightBuilder = new Peptide.Builder("VANTSTQTMGPRPAAAAAAATPAVR");
        PTM methyl = PTM.ofName("Methyl");
        lightBuilder.addModification(11, methyl);
        lightBuilder.addModification(11, methyl);

        Peptide lightPep = lightBuilder.build();
        MsnSpectrum lightSpectrum = upperSpectrum.get();

        annotator.annotate((PeakList) lightSpectrum, lightPep);

        Peptide.Builder heavyBuilder = new Peptide.Builder("VANTSTQTMGPRPAAAAAAATPAVR");
        Modification mod = new Modification("CD3", Composition.parseComposition("CH[2]3H-1"));
        heavyBuilder.addModification(11, mod);
        heavyBuilder.addModification(11, mod);
        heavyBuilder.addModification(8, new Modification("m_met_C13D",
                Composition.parseComposition("C[13]H[2]3C-1H-3")));
        Peptide heavyPep = heavyBuilder.build();

        MsnSpectrum heavySpectrum = lowerSpectrum.get();
        annotator.annotate((PeakList) heavySpectrum, heavyPep);
        chart.set(lightSpectrum, lightPep, heavySpectrum, heavyPep);
        chart.setTitle("Scan:21896;m/z:803.76;Charge:3", "Scan:21953;m/z:807.11;Charge:3");

        root.getChildren().add(chart);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}