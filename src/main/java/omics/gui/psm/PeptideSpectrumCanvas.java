package omics.gui.psm;

import javafx.scene.control.Label;
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
public class PeptideSpectrumCanvas extends BorderPane
{
    private final PeptideCanvas peptideCanvas;
    private final SpectrumCanvas spectrumCanvas;
    private final Label label;

    public PeptideSpectrumCanvas()
    {
        this.peptideCanvas = new PeptideCanvas();
        this.spectrumCanvas = new SpectrumCanvas();
        this.label = new Label();

        setCenter(spectrumCanvas);
        setTop(peptideCanvas);
        setBottom(label);

        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            peptideCanvas.setWidth(newValue.doubleValue());
            spectrumCanvas.setWidth(newValue.doubleValue());
        });


        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            spectrumCanvas.setHeight(newValue.doubleValue() * 0.80);
            peptideCanvas.setHeight(newValue.doubleValue() * 0.15);
        });

        spectrumCanvas.currentMz().addListener((observable, oldValue, newValue) ->
                label.setText(String.format("Mass: %.2f, Intensity: %.2e", newValue.doubleValue(), spectrumCanvas.currentIntensity().doubleValue())));
        spectrumCanvas.currentIntensity().addListener((observable, oldValue, newValue) ->
                label.setText(String.format("Mass: %.2f, Intensity: %.2e", spectrumCanvas.currentMz().doubleValue(), newValue.doubleValue())));
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

        this.peptideCanvas.setPeptide(peptide, pepAnnoList);
        this.spectrumCanvas.setPeptideSpectrum(spectrum);
    }
}
