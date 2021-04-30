package omics.gui.control;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import omics.gui.ShowAlert;
import omics.gui.TaskType;
import omics.gui.psm.DualPeptideSpectrumChart;
import omics.gui.psm.IonAnnotator;
import omics.gui.psm.PSMViewSettings;
import omics.gui.psm.util.NodeUtils;
import omics.gui.util.DualPSM;
import omics.gui.util.ExceptionAlert;
import omics.gui.util.ReadDualPSMTask;
import omics.msdk.io.MsDataAccessor;
import omics.msdk.model.MsDataFile;
import omics.msdk.model.SpectrumKeyFunc;
import omics.msdk.processors.ZscoreDeisotoper;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakAnnotation;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.PeakProcessorChain;
import omics.util.ms.peaklist.Tolerance;
import omics.util.ms.peaklist.filter.NPeaksPerBinFilter;
import omics.util.ms.peaklist.sim.impl.PreferCloserMzDuplicateAligner;
import omics.util.ms.peaklist.sim.impl.PreferLargerIntensityDuplicateAligner;
import omics.util.protein.Peptide;
import omics.util.protein.mod.NeutralLoss;
import omics.util.protein.ms.Ion;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;
import omics.util.protein.ms.PeptideIon;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.TaskProgressView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 01 Sep 2020, 1:03 PM
 */
public class DualPSMViewer extends BorderPane implements ToolNode, ShowAlert
{
    private static final String BUILD_TIME = "Sep 10, 2020";

    @FXML
    private MenuItem openPSMMenu;
    @FXML
    private MenuItem openMSMenu;
    @FXML
    private TableView<DualPSM> psmTableView;
    @FXML
    private StackPane viewPane;
    @FXML
    private MenuItem settingMenu;
    @FXML
    private MenuItem showPTMMenu;
    @FXML
    private MenuItem aboutMenu;

    private TaskProgressView<Task<?>> progressView = null;

    private DualPeptideSpectrumChart chart;
    private final PSMViewSettings settings = new PSMViewSettings();
    private final HashMap<String, String> ptmNameMap = new HashMap<>();

    public DualPSMViewer()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/dual_psm_viewer.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(fxmlUrl);
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize()
    {
        initView();
        initEvent();
        updateSetting();
    }

    private void initView()
    {
        openPSMMenu.setGraphic(TaskType.READ_PSM.getIcon());
        openMSMenu.setGraphic(TaskType.READ_MS.getIcon());

        NodeUtils.addIndexColumn(psmTableView);
        TableColumn<DualPSM, Peptide> topPeptideCol = new TableColumn<>("TopPeptide");
        TableColumn<DualPSM, Peptide> bottomPeptideCol = new TableColumn<>("BottomPeptide");
        TableColumn<DualPSM, String> topFileCol = new TableColumn<>("TopFile");
        TableColumn<DualPSM, String> bottomFileCol = new TableColumn<>("BottomFile");
        TableColumn<DualPSM, Integer> topScanCol = new TableColumn<>("TopScan");
        TableColumn<DualPSM, Integer> bottomScanCol = new TableColumn<>("BottomScan");
        TableColumn<DualPSM, String> topTitleCol = new TableColumn<>("TopTitle");
        TableColumn<DualPSM, String> bottomTitleCol = new TableColumn<>("BottomTitle");
        TableColumn<DualPSM, String> topSubTitleCol = new TableColumn<>("TopSubTitle");
        TableColumn<DualPSM, String> bottomSubTitleCol = new TableColumn<>("BottomSubTitle");

        topPeptideCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTopPeptide()));
        bottomPeptideCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getBottomPeptide()));

        topFileCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getTopFile()));
        bottomFileCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getBottomFile()));

        topScanCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTopScan()));
        bottomScanCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getBottomScan()));

        topTitleCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTopTitle()));
        bottomTitleCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getBottomTitle()));

        topSubTitleCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTopSubTitle()));
        bottomSubTitleCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getBottomSubTitle()));

        psmTableView.getColumns().addAll(topPeptideCol, topFileCol, topScanCol, topTitleCol, topSubTitleCol,
                bottomPeptideCol, bottomFileCol, bottomScanCol, bottomTitleCol, bottomSubTitleCol);

        chart = new DualPeptideSpectrumChart();
        viewPane.getChildren().add(chart);

        aboutMenu.setOnAction(event -> {

            AboutWindow aboutWindow = new AboutWindow(getToolName(), "1.0", BUILD_TIME,
                    "Dual PSM Viewer provides a top and bottom contrast view of spectra, which was originally " +
                            "designed for the comparison visualization of light and heavy labeled methylated peptide " +
                            "spectrum. " +
                            "It can also be used for the comparison view of any other type of spectrum");

            PopOver popOver = new PopOver(aboutWindow);
            popOver.setTitle(getToolName());
            popOver.show(getScene().getWindow());
        });
    }


    private void initEvent()
    {
        openPSMMenu.setOnAction(event -> selectPSMFile());
        openMSMenu.setOnAction(event -> selectMSFile());

        psmTableView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        repaint(newValue);
                    }
                });

        settingMenu.setOnAction(event -> {
            IonTypePane ionTypePane = new IonTypePane();
            ionTypePane.updateView(settings);

            PopOver popOver = new PopOver(ionTypePane);
            popOver.setTitle("Setting ion types to show");
            popOver.setOnCloseRequest(event1 -> {
                ionTypePane.updateSettings(settings);
                updateSetting();
                DualPSM selectedItem = psmTableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    repaint(selectedItem);
                }
            });
            popOver.show(getScene().getWindow());
        });

        showPTMMenu.setOnAction(event -> {
            PTMShowNameEditor nameEditor = new PTMShowNameEditor();
            nameEditor.updateView(ptmNameMap);

            PopOver popOver = new PopOver(nameEditor);
            popOver.setTitle("PTM to Show");
            popOver.setOnHiding(event1 -> {
                ptmNameMap.clear();
                ptmNameMap.putAll(nameEditor.getMap());
                chart.setAnnotatedPTMs(ptmNameMap);
                DualPSM selectedItem = psmTableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    repaint(selectedItem);
                }
            });
            popOver.setDetached(true);
            popOver.show(getScene().getWindow());
        });
    }

    private PeptideFragmentAnnotator annotator;
    private final PeakProcessorChain<PeakAnnotation> processorChain = new PeakProcessorChain<>();

    private void updateSetting()
    {
        chart.setViewSettings(settings);

        Tolerance fragmentTolerance = settings.getFragmentTolerance();
        if (settings.isMatchMostIntense()) {
            annotator = new PeptideFragmentAnnotator(new PeptideFragmenter(),
                    new PreferLargerIntensityDuplicateAligner<>(fragmentTolerance));
        } else {
            annotator = new PeptideFragmentAnnotator(new PeptideFragmenter(),
                    new PreferCloserMzDuplicateAligner<>(fragmentTolerance));
        }

        processorChain.getProcessorList().clear();
        if (settings.isDeisotope()) {
            Tolerance deisotopeTolerance = settings.getDeisotopeTolerance();
            ZscoreDeisotoper deisotoper = new ZscoreDeisotoper(deisotopeTolerance);
            deisotoper.setConvert2One(true);
            processorChain.add(deisotoper);
        }
        if (settings.isFilterPeaks()) {
            int peakCount = settings.getPeakCount();
            double binWidth = settings.getBinWidth();
            NPeaksPerBinFilter<PeakAnnotation> filter = new NPeaksPerBinFilter<>(peakCount, binWidth);
            processorChain.add(filter);
        }

        List<PeptideIon> peptideIons = settings.getFixedMaxChargePeptideIonList();
        annotator.setPeptideIonList(peptideIons);
    }

    private void repaint(DualPSM dualPSM)
    {
        String topKey = dualPSM.getTopKey();
        String bottomKey = dualPSM.getBottomKey();
        if (!spectrumMap.containsKey(topKey)) {
            showAlert(Alert.AlertType.ERROR, "Spectrum '" + topKey + "'is not found");
            return;
        }
        if (!spectrumMap.containsKey(bottomKey)) {
            showAlert(Alert.AlertType.ERROR, "Spectrum '" + bottomKey + "' is not found");
            return;
        }

        MsnSpectrum topSpectrum = spectrumMap.get(topKey);
        MsnSpectrum bottomSpectrum = spectrumMap.get(bottomKey);

        topSpectrum.clearAnnotations();
        bottomSpectrum.clearAnnotations();

        if (!processorChain.isEmpty()) {
            topSpectrum = topSpectrum.copy(processorChain);
            bottomSpectrum = bottomSpectrum.copy(processorChain);
        }

        Peptide topPeptide = dualPSM.getTopPeptide();
        Peptide bottomPeptide = dualPSM.getBottomPeptide();

        annotator.annotate((PeakList) topSpectrum, topPeptide);
        annotator.annotate((PeakList) bottomSpectrum, bottomPeptide);

        if (settings.isShowIon(Ion.p)) {
            IonAnnotator.annotatePrecursor(topSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
            IonAnnotator.annotatePrecursor(bottomSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }
        if (settings.isShowIon(Ion.p, NeutralLoss.H2O_LOSS)) {
            IonAnnotator.annotatePrecursorH2O(topSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
            IonAnnotator.annotatePrecursorH2O(bottomSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }
        if (settings.isShowIon(Ion.p, NeutralLoss.NH3_LOSS)) {
            IonAnnotator.annotatePrecursorNH3(topSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
            IonAnnotator.annotatePrecursorNH3(bottomSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }
        if (settings.isShowIon(Ion.p, NeutralLoss.H3PO4_LOSS)) {
            IonAnnotator.annotatePrecursorH3PO4(topSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
            IonAnnotator.annotatePrecursorH3PO4(bottomSpectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }
        chart.setTopSubTitle(dualPSM.getTopSubTitle());
        chart.setBottomSubTitle(dualPSM.getBottomSubTitle());
        chart.setTitle(dualPSM.getTopTitle(), dualPSM.getBottomTitle());
        chart.set(topSpectrum, topPeptide, bottomSpectrum, bottomPeptide);
    }

    /**
     * choose the csv file to load top and bottom PSM
     */
    private void selectPSMFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Dual PSM File");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All", "*.*"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Csv", "*.csv"));
        chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(1));

        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file == null)
            return;

        ReadDualPSMTask task = new ReadDualPSMTask(file);
        viewProgress(task);
        task.setOnSucceeded(event -> {
            List<DualPSM> dualPSMList = task.getValue();
            if (!dualPSMList.isEmpty()) {
                psmTableView.getItems().setAll(dualPSMList);
            }
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private final HashMap<String, MsnSpectrum> spectrumMap = new HashMap<>();

    private void selectMSFile()
    {
        FileChooser msFileChooser = new FileChooser();
        msFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All", "*.*"),
                new FileChooser.ExtensionFilter("Mascot Generic File", "*.mgf"),
                new FileChooser.ExtensionFilter("mzML", "*.mzML"),
                new FileChooser.ExtensionFilter("mzXML", "*.mzXML"));
        msFileChooser.setSelectedExtensionFilter(msFileChooser.getExtensionFilters().get(1));
        File file = msFileChooser.showOpenDialog(getScene().getWindow());
        if (file == null)
            return;

        MsDataAccessor accessor = new MsDataAccessor(file.toPath());
        Task<MsDataFile> task = NodeUtils.createTask(accessor);
        viewProgress(task);
        task.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            ExceptionAlert alert = new ExceptionAlert(newValue);
            alert.showAndWait();
        });
        task.setOnSucceeded(event -> {
            MsDataFile msDataFile = task.getValue();
            spectrumMap.clear();
            SpectrumKeyFunc keyFunc = SpectrumKeyFunc.FILE_SCAN;
            for (MsnSpectrum spectrum : msDataFile.getSpectrumList()) {
                spectrumMap.put(keyFunc.getKey(spectrum), spectrum);
            }
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public String getToolName()
    {
        return "Dual PSM Viewer";
    }

    @Override
    public Parent getRoot()
    {
        return this;
    }

    @Override
    public void clear()
    {
        spectrumMap.clear();
        processorChain.getProcessorList().clear();
        chart.clear();
        psmTableView.getItems().clear();
    }

    @Override
    public TaskProgressView<Task<?>> getTaskProgressView()
    {
        return this.progressView;
    }

    @Override
    public void setProgressView(TaskProgressView<Task<?>> progressView)
    {
        this.progressView = progressView;
    }
}
