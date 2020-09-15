package omics.gui.controller;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import omics.msdk.io.MgfWriter;
import omics.msdk.io.MsDataAccessor;
import omics.msdk.model.MsDataFile;
import omics.util.io.FilenameUtils;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.SpectrumFilter;
import omics.util.ms.peaklist.PeakProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 31 Oct 2018, 12:16 AM
 */
public class MainProcessorController implements Initializable
{
    public ListView<Object> peakProcessorListNode;
    public ProgressBar msProgressNode;
    public ListView<Path> msFileListNode;
    public Label statusLabel;
    private Stage mainWindow;

    /**
     * set the main window
     */
    public void setMainWindow(Stage mainWindow)
    {
        this.mainWindow = mainWindow;
    }

    public ListView<Object> getPeakProcessorListNode()
    {
        return peakProcessorListNode;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        peakProcessorListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        msFileListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * show the spectrum filter pane
     */
    @FXML
    public void showFilterPane(ActionEvent actionEvent) throws IOException
    {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("spectrum_processors.fxml"));
        ScrollPane filterPane = loader.load();
        SpectrumProcessorController controller = loader.getController();
        controller.setMainProcessorController(this);

        Scene scene = new Scene(filterPane);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Spectrum Processors");
        stage.show();
    }

    /**
     * remove selected spectrum processors
     */
    public void onRemoveProcessor(ActionEvent actionEvent)
    {
        ObservableList<Object> processors = peakProcessorListNode.getSelectionModel().getSelectedItems();
        peakProcessorListNode.getItems().removeAll(processors);
    }

    /**
     * clear all processors
     */
    public void onClearProcessors(ActionEvent actionEvent)
    {
        peakProcessorListNode.getItems().clear();
    }


    /**
     * start processing ms files
     */
    public void onStartMSProcessing(ActionEvent actionEvent)
    {
        ObservableList<Path> items = msFileListNode.getItems();
        if (items.isEmpty())
            return;


        Task<Void> task = new Task<Void>()
        {
            @Override
            protected Void call() throws Exception
            {
                for (Path file : items) {

                    MsDataAccessor accessor = new MsDataAccessor(file);
                    accessor.titleProperty().addListener(evt -> updateTitle((String) evt.getNewValue()));
                    accessor.progressProperty().addListener(evt -> updateProgress((Double) evt.getNewValue(), 1.));

                    accessor.go();
                    MsDataFile msDataFile = accessor.getValue();
                    if (msDataFile == null)
                        continue;

                    String fileName;
                    int count = 1;
                    while (true) {
                        fileName = FilenameUtils.removeExtension(file.toString()) + "_filter_" + count + ".mgf";
                        if (Files.notExists(Paths.get(fileName)))
                            break;
                        count++;
                    }
                    MgfWriter writer = new MgfWriter(Paths.get(fileName));

                    for (MsnSpectrum spectrum : msDataFile.getSpectrumList()) {
                        MsnSpectrum msnSpectrum = spectrum.asMsnSpectrum();
                        boolean pass = true;
                        for (Object processor : peakProcessorListNode.getItems()) {
                            if (processor instanceof PeakProcessor) {
                                msnSpectrum.apply((PeakProcessor) processor);
                            } else if (processor instanceof SpectrumFilter) {
                                SpectrumFilter filter = (SpectrumFilter) processor;
                                if (!filter.test(msnSpectrum)) {
                                    pass = false;
                                    break;
                                }
                            }
                        }
                        if (!pass)
                            continue;

                        writer.writeSpectrum(msnSpectrum);
                    }

                    writer.close();

                    updateTitle(file.getFileName() + " Finished");
                }
                return null;
            }
        };

        statusLabel.textProperty().bind(task.titleProperty());
        msProgressNode.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void onAddMSFile(ActionEvent actionEvent)
    {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All", "*.*"),
                new FileChooser.ExtensionFilter("MGF Files", "*.mgf"),
                new FileChooser.ExtensionFilter("MzXML Files", "*.mzXML"),
                new FileChooser.ExtensionFilter("MzML Files", "*.mzML"));
        chooser.setTitle("Choose Mass spectrum data for processing");
        List<File> msFiles = chooser.showOpenMultipleDialog(mainWindow);
        if (msFiles != null) {
            for (File msFile : msFiles) {
                msFileListNode.getItems().add(msFile.toPath());
            }
        }
    }

    public void onRemoveMSFile(ActionEvent actionEvent)
    {
        ObservableList<Path> selectedItems = msFileListNode.getSelectionModel().getSelectedItems();
        msFileListNode.getItems().removeAll(selectedItems);
    }

    public void onClearMSFiles(ActionEvent actionEvent)
    {
        msFileListNode.getItems().clear();
    }
}
