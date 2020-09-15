package omics.gui.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import omics.msdk.model.MsDataFile;
import omics.pdk.IdentResult;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.Tolerance;
import omics.util.ms.peaklist.impl.DoublePeakList;
import omics.util.protein.Peptide;
import omics.util.protein.ms.PeptideFragmentAnnotation;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;

import java.io.File;

/**
 * Controller extends by others.
 *
 * @author JiaweiMao
 * @version 1.2.0
 * @since 21 Jan 2018, 1:20 PM
 */
public abstract class OmicsController
{
    protected IdentResult ident_result;
    protected MsDataFile raw_data_file;
    protected ObjectProperty<MsDataFile> raw_data_property = new SimpleObjectProperty<>();
    private Stage window_main;
    /**
     * previous working directory
     */
    private File previousDir;

    private PeptideFragmentAnnotator peptide_annotator;

    public OmicsController()
    {
        peptide_annotator = new PeptideFragmentAnnotator(PeptideFragmenter.EsiTrapFragmenter(), Tolerance.abs(0.05));
    }

    /**
     * @return the main window
     */
    public Stage getMainWindow()
    {
        return window_main;
    }

    public void setMainWindow(Stage window)
    {
        this.window_main = window;
    }

    /**
     * @return previous working directory.
     */
    public File getPreviousDir()
    {
        return previousDir;
    }

    /**
     * set the previous working directory, it is saved for the initial directory of next openFileDialog.
     *
     * @param previousDir a directory path
     */
    public void setPreviousDir(File previousDir)
    {
        this.previousDir = previousDir;
    }

    /**
     * @return MS RawDataFile.
     */
    public MsDataFile getRawDataFile()
    {
        return raw_data_file;
    }

    public void setRawDataFile(MsDataFile rawDataFile)
    {
        raw_data_property.set(rawDataFile);
        this.raw_data_file = rawDataFile;
    }

    public void setIdentResult(IdentResult iResult)
    {
        this.ident_result = iResult;
        if (ident_result.getRawDataFile() != null) {
            setRawDataFile(ident_result.getRawDataFile());
        }
    }

    protected Task<PeakList<PeptideFragmentAnnotation>> createAnnotationTask(MsnSpectrum spectrum, Peptide peptide)
    {
        return new Task<PeakList<PeptideFragmentAnnotation>>()
        {
            @Override
            protected PeakList<PeptideFragmentAnnotation> call()
            {
                PeakList<PeptideFragmentAnnotation> peakList = new DoublePeakList<>(spectrum.size());
                peakList.addPeaksNoAnnotations(spectrum);
                peakList.setPrecursor(spectrum.getPrecursor());

                peptide_annotator.annotate(peakList, peptide);
                updateValue(peakList);

                return peakList;
            }
        };
    }

    protected void showAlert(Alert.AlertType type, String msg)
    {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }
}
