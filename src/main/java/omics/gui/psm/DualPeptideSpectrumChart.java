package omics.gui.psm;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import omics.util.ms.peaklist.PeakAnnotation;
import omics.util.ms.peaklist.PeakList;
import omics.util.protein.Peptide;
import omics.util.protein.ms.PeptideFragAnnotation;

import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 05 Jun 2020, 12:20 PM
 */
public class DualPeptideSpectrumChart extends BorderPane
{
    private final PeptideChart peptideChart;
    private final SpectrumChart upperSpectrumChart;
    private final SpectrumChart lowerSpectrumChart;

    public DualPeptideSpectrumChart()
    {
        this.peptideChart = new PeptideChart();
        this.upperSpectrumChart = new SpectrumChart();
        this.lowerSpectrumChart = new SpectrumChart();
        setTop(peptideChart);
        VBox box = new VBox();
        box.getChildren().addAll(upperSpectrumChart, lowerSpectrumChart);
        setCenter(box);

        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue();
            peptideChart.setPrefWidth(newWidth);
            upperSpectrumChart.setPrefWidth(newWidth);
            lowerSpectrumChart.setPrefWidth(newWidth);
        });
        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeight = newValue.doubleValue();
            lowerSpectrumChart.setPrefHeight(newHeight * 0.45);
            upperSpectrumChart.setPrefHeight(newHeight * 0.45);
            peptideChart.setPrefHeight(newHeight * 0.1);
        });
    }

    /**
     * Set the peptide to display
     *
     * @param peptide        {@link Peptide} to display
     * @param annotationList {@link PeptideFragAnnotation} to display
     */
    public void setPeptide(Peptide peptide, List<PeptideFragAnnotation> annotationList)
    {
        this.peptideChart.setPeptide(peptide, annotationList);
    }

    public void setLowerSpectrum(PeakList<PeakAnnotation> spectrum)
    {
        this.lowerSpectrumChart.setPeakList(spectrum);
    }

    public void setUpperSpectrum(PeakList<PeakAnnotation> spectrum)
    {
        this.upperSpectrumChart.setPeakList(spectrum);
    }
}
