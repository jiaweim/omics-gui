package omics.gui.psm;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import omics.gui.psm.util.NodeUtils;
import omics.util.ms.peaklist.PeakAnnotation;
import omics.util.ms.peaklist.PeakList;
import omics.util.protein.Peptide;
import omics.util.protein.ms.PeptideFragAnnotation;

import java.io.File;
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

        Label label = new Label();
        setBottom(label);
        BorderPane.setAlignment(label, Pos.CENTER_RIGHT);
        BorderPane.setMargin(label, new Insets(0, 10, 0, 0));

        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            spectrumChart.setPrefWidth(newValue.doubleValue());
            peptideChart.setPrefWidth(newValue.doubleValue());
        });

        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            spectrumChart.setPrefHeight(newValue.doubleValue() * 0.8);
            peptideChart.setPrefHeight(newValue.doubleValue() * 0.2);
        });

        spectrumChart.mzProperty().addListener((observable, oldValue, newValue) ->
                label.setText(String.format("Mass: %.4f, Intensity: %.4e", newValue.doubleValue(),
                        spectrumChart.intensityProperty().doubleValue())));
        spectrumChart.intensityProperty().addListener((observable, oldValue, newValue) ->
                label.setText(String.format("Mass: %.4f, Intensity: %.4e",
                        spectrumChart.mzProperty().doubleValue(), newValue.doubleValue())));

        ContextMenu menu = new ContextMenu();
        MenuItem item = new Menu("Save to PNG");
        item.setOnAction(event1 -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("spectrum.png");
            File file = chooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                NodeUtils.saveNodeAsPng(this, 2, file.getAbsolutePath());
            }
        });
        menu.getItems().add(item);
        menu.setAutoHide(true);

        setOnContextMenuRequested(event -> menu.show(getScene().getWindow(), event.getScreenX(), event.getScreenY()));
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
