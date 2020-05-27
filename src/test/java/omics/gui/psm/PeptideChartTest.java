package omics.gui.psm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import omics.util.protein.Peptide;
import omics.util.protein.PeptideFragment;
import omics.util.protein.mod.PTM;
import omics.util.protein.ms.FragmentType;
import omics.util.protein.ms.Ion;
import omics.util.protein.ms.PeptideFragAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 22 May 2020, 12:14 PM
 */
class PeptideChartTest extends Application
{

    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        PTM phospho = PTM.Phospho();
        Peptide peptide = new Peptide.Builder("AAAAAATAPPSPGPAQPGPR")
                .addModification(10, phospho).build();
        List<PeptideFragAnnotation> annotations = new ArrayList<>();
        annotations.add(new PeptideFragAnnotation(Ion.b, 1, PeptideFragment.parse("A", FragmentType.FORWARD)));
        annotations.add(new PeptideFragAnnotation(Ion.b, 1, PeptideFragment.parse("AA", FragmentType.FORWARD)));
        annotations.add(new PeptideFragAnnotation(Ion.b, 1, PeptideFragment.parse("AAA", FragmentType.FORWARD)));
        annotations.add(new PeptideFragAnnotation(Ion.b, 1, PeptideFragment.parse("AAAA", FragmentType.FORWARD)));
        annotations.add(new PeptideFragAnnotation(Ion.b, 1, PeptideFragment.parse("AAAAAATAPPSPGPAQPGP", FragmentType.FORWARD)));

        annotations.add(new PeptideFragAnnotation(Ion.y, 1, PeptideFragment.parse("R", FragmentType.REVERSE)));
        annotations.add(new PeptideFragAnnotation(Ion.y, 1, PeptideFragment.parse("AAAAATAPPSPGPAQPGPR", FragmentType.REVERSE)));

//        peptide = Peptide.parse("SGRGGNFGFGDSRGGGGNFGPGPGSNFRWWWWWWWWWWW");

        PeptideChart root = new PeptideChart();
        root.setPeptide(peptide, annotations);

        Scene scene = new Scene(root, 1000, 100);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}