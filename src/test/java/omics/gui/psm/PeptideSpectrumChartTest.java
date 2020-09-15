package omics.gui.psm;

import com.google.common.collect.ArrayListMultimap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import omics.gui.psm.util.NodeUtils;
import omics.msdk.io.MgfReader;
import omics.pdk.IdentResult;
import omics.pdk.ident.model.PeptideSpectrumMatch;
import omics.pdk.io.MaxQuantEvidenceReader;
import omics.pdk.psm.filter.ModificationFilter;
import omics.util.OmicsException;
import omics.util.chem.Composition;
import omics.util.io.FilenameUtils;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.Tolerance;
import omics.util.ms.peaklist.filter.NPeaksPerBinFilter;
import omics.util.ms.peaklist.impl.FloatPeakList;
import omics.util.protein.AminoAcid;
import omics.util.protein.Peptide;
import omics.util.protein.mod.PTM;
import omics.util.protein.mod.PTMResolver;
import omics.util.protein.mod.Specificity;
import omics.util.protein.ms.PeptideFragmentAnnotation;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;
import omics.util.protein.ms.PeptideIon;
import omics.util.utils.Pair;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 22 May 2020, 12:16 PM
 */
class PeptideSpectrumChartTest extends Application
{
    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
////        PeptideIonType.setColor(PeptideIonType.b, Color.rgb(0, 139, 139));
////        PeptideIonType.setColor(PeptideIonType.b, Color.rgb(0, 144,188));
////        PeptideIonType.setColor(PeptideIonType.b, Color.rgb(67, 83,150));
////        PeptideIonType.setColor(PeptideIonType.y, Color.rgb(172, 22, 28));
//
        List<Pair<PeptideIon, Double>> ionTypes = new ArrayList<>();
        ionTypes.add(Pair.create(PeptideIon.b(1), 100.0));
        ionTypes.add(Pair.create(PeptideIon.b(2), 100.0));
        ionTypes.add(Pair.create(PeptideIon.y(1), 100.0));
        ionTypes.add(Pair.create(PeptideIon.y(2), 100.0));
//        ionTypes.add(Pair.create(PeptideIon.y_H2O(1), 100.0));
//        ionTypes.add(Pair.create(PeptideIon.y_H2O(2), 100.0));
//        ionTypes.add(Pair.create(PeptideIon.y_NH3(1), 100.0));
//        ionTypes.add(Pair.create(PeptideIon.y_NH3(2), 100.0));
        ionTypes.add(Pair.create(PeptideIon.p(1), 50.0));
        ionTypes.add(Pair.create(PeptideIon.p(2), 50.0));
        ionTypes.add(Pair.create(PeptideIon.p(3), 50.0));
        ionTypes.add(Pair.create(PeptideIon.p(4), 50.0));

        PeptideFragmenter fragmenter = new PeptideFragmenter(ionTypes);
        Specificity k = new Specificity(AminoAcid.K);
        Specificity r = new Specificity(AminoAcid.R);
        Specificity m = new Specificity(AminoAcid.M);

        PTM methylPTM = new PTM("hM_monomethyl(KR)", Composition.parseComposition("H-1C[13]H[2]3"), Arrays.asList(k, r));
        PTM methyl2PTM = new PTM("hM_dimethyl(KR)", Composition.parseComposition("H-2C[13]2H[2]6"), Arrays.asList(k, r));
        PTM methyl3PTM = new PTM("hM_trimethyl(K)", Composition.parseComposition("H-3C[13]3H[2]9"), Collections.singletonList(k));
        PTM hMPTM = new PTM("hM_M", Composition.parseComposition("H-3C-1C[13]H[2]3"));

        PTMResolver resolver = new PTMResolver();
        resolver.putOverrideUnimod("Oxidation (M)", PTM.Oxidation());
        resolver.putOverrideUnimod("Acetyl (Protein N-term)", PTM.Acetyl());
        resolver.putOverrideUnimod(methylPTM.getTitle(), methylPTM);
        resolver.putOverrideUnimod(methyl2PTM.getTitle(), methyl2PTM);
        resolver.putOverrideUnimod(methyl3PTM.getTitle(), methyl3PTM);
        resolver.putOverrideUnimod(hMPTM.getTitle(), hMPTM);

        PeptideFragmentAnnotator annotator = new PeptideFragmentAnnotator(fragmenter, Tolerance.abs(0.05));

        PeptideSpectrumChart newPane = new PeptideSpectrumChart();

        Scene scene = new Scene(newPane, 1200, 620);

        MaxQuantEvidenceReader accessor = new MaxQuantEvidenceReader(Paths.get("Z:\\MaoJiawei\\Liuzhen-methylation results\\evidence-Bel-5-Fu1.csv"), resolver, null);
        accessor.go();
        Set<String> modSet = new HashSet<>();
        modSet.add("hM_monomethyl(KR)");
        modSet.add("hM_dimethyl(KR)");
        modSet.add("hM_trimethyl(K)");
        ModificationFilter filter = new ModificationFilter(modSet);

        IdentResult result = accessor.getValue();
        result.apply(filter);

        ArrayListMultimap<String, PeptideSpectrumMatch> psmMap = ArrayListMultimap.create();
        for (PeptideSpectrumMatch psm : result) {
            psmMap.put(psm.getMsDataId().getName(), psm);
        }

        String imgs = "Z:\\MaoJiawei\\Liuzhen-methylation results\\ben5Imgs";
        String folder = "Z:\\MaoJiawei\\Liuzhen-methylation results\\mgf-Bel-5-Fu";
        for (String name : psmMap.keySet()) {
            HashMap<Integer, MsnSpectrum> spectrumMap = new HashMap<>();
            MgfReader reader = new MgfReader(Paths.get(folder, name));
            while (reader.hasNext()) {
                MsnSpectrum spectrum = reader.next();
                int scan = spectrum.getScanNumber().getValue();
                spectrumMap.put(scan, spectrum);
            }
            reader.close();

            List<PeptideSpectrumMatch> psmList = psmMap.get(name);
            for (PeptideSpectrumMatch peptideSpectrumMatch : psmList) {
                int value = peptideSpectrumMatch.getScanNumber().getValue();
                MsnSpectrum spectrum = spectrumMap.get(value);

                if (spectrum == null)
                    throw new OmicsException("Cannot find spectrum for " + name + " with scan " + value);

                Peptide peptide = peptideSpectrumMatch.getPeptide();

                spectrum.apply(new NPeaksPerBinFilter<>(12, 100));
                PeakList<PeptideFragmentAnnotation> peakList = new FloatPeakList<>();
                peakList.addPeaksNoAnnotations(spectrum);

                annotator.annotate(peakList, peptide, spectrum.getPrecursor().getCharge());

                newPane.setPeptideSpectrum(peptide, (PeakList) peakList);

                String pngName = FilenameUtils.removeExtension(name) + "_" + value + ".png";

//                if (pngName.equals("BEL_H_SCX_F2_40mM_1_22477.png")) {
//                    NodeUtils.saveNodeAsPng(newPane, new File(imgs, pngName).getAbsolutePath());
//                    System.out.println("pass");
//                } else {
//                    continue;
//                }

                try {
                    NodeUtils.saveNodeAsPng(newPane, new File(imgs, pngName).getAbsolutePath());
                } catch (Exception e) {
                    System.out.println(pngName);
                }
            }
        }

        primaryStage.centerOnScreen();
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("Peptide Spectrum Canvas");
    }
}