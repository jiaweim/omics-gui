package omics.gui.psm;

import javafx.scene.layout.BorderPane;
import omics.util.ms.peaklist.PeakAnnotation;
import omics.util.ms.peaklist.PeakList;
import omics.util.protein.Peptide;
import omics.util.protein.ms.PeptideFragAnnotation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.1
 * @since 30 Dec 2017, 1:53 PM
 */
public class PeptideSpectrumChart extends BorderPane
{
    private final PeptideChart peptideChart;
    private final SpectrumChart spectrumChart;

    public PeptideSpectrumChart()
    {
        this.peptideChart = new PeptideChart();
        this.spectrumChart = new SpectrumChart();

        setCenter(spectrumChart);
        setTop(peptideChart);

        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            spectrumChart.setPrefWidth(newValue.doubleValue());
            peptideChart.setPrefWidth(newValue.doubleValue());
        });

        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            spectrumChart.setPrefHeight(newValue.doubleValue() * 0.80);
            peptideChart.setPrefHeight(newValue.doubleValue() * 0.2);
        });

//        spectrumPane.mzProperty().addListener((observable, oldValue, newValue) ->
//                label.setText(String.format("Mass: %.2f, Intensity: %.2e", newValue.doubleValue(), spectrumPane.intensityProperty().doubleValue())));
//        spectrumPane.intensityProperty().addListener((observable, oldValue, newValue) ->
//                label.setText(String.format("Mass: %.2f, Intensity: %.2e", spectrumPane.mzProperty().doubleValue(), newValue.doubleValue())));
    }

    public void setStyle(SpectrumViewStyle style)
    {
        this.peptideChart.setStyle(style);
        this.spectrumChart.setStyle(style);
    }

    public PeptideChart getPeptideChart()
    {
        return peptideChart;
    }

    /**
     * @return the {@link SpectrumChart}
     */
    public SpectrumChart getSpectrumChart()
    {
        return spectrumChart;
    }

    /**
     * set {@link Peptide} to be annotated
     *
     * @param peptide a {@link Peptide} instance.
     */
    public void setPeptide(Peptide peptide)
    {
        this.peptideChart.setPeptide(peptide, new ArrayList<>());
    }

    /**
     * Remove all peaks from view.
     */
    public void clearSpectrum()
    {
        this.spectrumChart.clearPeakList();
    }

    public void setPeptideSpectrum(Peptide peptide, PeakList<PeakAnnotation> spectrum)
    {
        List<PeptideFragAnnotation> pepAnnoList = new ArrayList<>();
        for (int i = 0; i < spectrum.size(); i++) {
            if (spectrum.hasAnnotationsAt(i)) {
                List<? extends PeakAnnotation> annotations = spectrum.getAnnotations(i);
                for (PeakAnnotation annotation : annotations) {
                    if (annotation instanceof PeptideFragAnnotation) {
                        PeptideFragAnnotation anno = (PeptideFragAnnotation) annotation;
                        pepAnnoList.add(anno);
                    }
                }
            }
        }

        this.peptideChart.setPeptide(peptide, pepAnnoList);
        this.spectrumChart.setPeakList(spectrum);
    }
}
