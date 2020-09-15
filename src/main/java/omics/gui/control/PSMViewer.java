package omics.gui.control;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import omics.gui.ShowAlert;
import omics.gui.TaskType;
import omics.gui.psm.IonAnnotator;
import omics.gui.psm.PSMViewSettings;
import omics.gui.psm.PeptideSpectrumChart;
import omics.gui.psm.util.NodeUtils;
import omics.gui.util.ExceptionAlert;
import omics.gui.util.IdentFileUIUtils;
import omics.msdk.io.MsDataAccessor;
import omics.msdk.model.MsDataFile;
import omics.msdk.model.SpectrumKeyFunc;
import omics.msdk.processors.ZscoreDeisotoper;
import omics.pdk.IdentResult;
import omics.pdk.PSMColumn;
import omics.pdk.ident.model.Delta;
import omics.pdk.ident.model.PeptideProteinMatch;
import omics.pdk.ident.model.PeptideSpectrumMatch;
import omics.pdk.ident.model.Score;
import omics.pdk.io.IdentResultReadingTask;
import omics.pdk.psm.PSMKeyFunc;
import omics.pdk.ptm.glyco.GlycanComposition;
import omics.pdk.ptm.glyco.ident.OxoniumDB;
import omics.pdk.util.DoubleScoreFormatter;
import omics.pdk.util.ScoreFormatter;
import omics.util.MetaKey;
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
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.TaskProgressView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Pane for PSM.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 16 Dec 2019, 12:29 PM
 */
public class PSMViewer extends BorderPane implements ToolNode, ShowAlert
{
    private static final NumberFormat DELTA_FORMAT_DA = NumberFormatFactory.MASS_PRECISION;
    private static final NumberFormat DELTA_FORMAT_PPM = NumberFormatFactory.DIGIT2;
    private static final NumberFormat MASS_FORMAT = NumberFormatFactory.MASS_PRECISION;
    private static final DecimalFormat INTENSITY_FORMAT = NumberFormatFactory.valueOf("0.####E0");

    private static final String BUILD_TIME = "Sep 14, 2020";

    @FXML
    private MenuItem openPSMMenu;
    @FXML
    private MenuItem openMSMenu;
    @FXML
    private MenuItem closeMenu;
    @FXML
    private MenuItem settingMenu;
    @FXML
    private MenuItem showPTMMenu;
    @FXML
    private CheckMenuItem showSpectrumMenu;
    @FXML
    private MenuItem aboutMenu;
    @FXML
    private SplitPane splitPane;

    @FXML
    private TableView<PeptideSpectrumMatch> psmTableView;
    @FXML
    private SplitPane viewPane;

    private TaskProgressView<Task<?>> progressView = null;

    private final PeptideSpectrumChart chart = new PeptideSpectrumChart();
    private final PSMViewSettings settings = new PSMViewSettings();
    private final HashMap<String, String> ptmNameMap = new HashMap<>();

    private PeptideFragmentAnnotator annotator;
    private final PeakProcessorChain<PeakAnnotation> processorChain = new PeakProcessorChain<>();

    private final OxoniumDB oxoniumDB = OxoniumDB.getInstance();
    private final HashMap<String, MsnSpectrum> spectrumMap = new HashMap<>();

    public PSMViewer()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/psm_viewer.fxml");
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
        initMenu();
        updateSetting();

        psmTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (showSpectrumMenu.isSelected() && newValue != null) {
                repaint(newValue);
            }
        });
    }

    private void initMenu()
    {
        openPSMMenu.setGraphic(TaskType.READ_PSM.getIcon());
        openPSMMenu.setOnAction(event -> selectPSMFile());

        openMSMenu.setGraphic(TaskType.READ_MS.getIcon());
        openMSMenu.setOnAction(event -> selectMSFile());

        closeMenu.setOnAction(event -> getScene().getWindow().hide());

        settingMenu.setOnAction(event -> {
            IonTypePane ionTypePane = new IonTypePane();
            ionTypePane.updateView(settings);

            PopOver popOver = new PopOver(ionTypePane);
            popOver.setTitle("Setting ion types to show");
            popOver.setOnHiding(event1 -> {
                ionTypePane.updateSettings(settings);
                updateSetting();
                PeptideSpectrumMatch selectedItem = psmTableView.getSelectionModel().getSelectedItem();
                if (showSpectrumMenu.isSelected() && selectedItem != null) {
                    repaint(selectedItem);
                }
            });
            popOver.show(getScene().getWindow());
        });

        showSpectrumMenu.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                viewPane.getItems().addAll(chart, new StackPane());
                viewPane.setDividerPositions(1.0);
                PeptideSpectrumMatch selectedItem = psmTableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    repaint(selectedItem);
                }
            } else {
                viewPane.getItems().clear();
                splitPane.setDividerPositions(1.0);
            }
        });

        aboutMenu.setOnAction(event -> {
            AboutWindow aboutWindow = new AboutWindow(getToolName(), "1.0", BUILD_TIME, "");
            PopOver popOver = new PopOver(aboutWindow);
            popOver.show(getScene().getWindow());
        });
    }

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

    private void repaint(PeptideSpectrumMatch psm)
    {
        String key = PSMKeyFunc.FILE_SCAN.getKey(psm);
        Peptide peptide = psm.getPeptide();
        if (!spectrumMap.containsKey(key)) {
            chart.clearSpectrum();
            chart.setPeptide(peptide);
            showAlert(Alert.AlertType.ERROR, "Spectrum '" + key + "' not exists!");
            return;
        }
        MsnSpectrum spectrum = spectrumMap.get(key);
        spectrum.clearAnnotations();

        if (!processorChain.isEmpty()) {
            spectrum = spectrum.copy(processorChain);
        }

        annotator.annotate((PeakList) spectrum, peptide);

        Tolerance fragmentTolerance = settings.getFragmentTolerance();
        if (settings.isShowIon(Ion.B)) {
            IonAnnotator.annotateOxonium(spectrum, fragmentTolerance, oxoniumDB.getItemList(),
                    settings.getMinCharge(Ion.B), settings.getMaxCharge(Ion.B));
        }

        if (settings.isShowIon(Ion.p)) {
            IonAnnotator.annotatePrecursor(spectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }
        if (settings.isShowIon(Ion.p, NeutralLoss.H2O_LOSS)) {
            IonAnnotator.annotatePrecursorH2O(spectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }
        if (settings.isShowIon(Ion.p, NeutralLoss.NH3_LOSS)) {
            IonAnnotator.annotatePrecursorNH3(spectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }
        if (settings.isShowIon(Ion.p, NeutralLoss.H3PO4_LOSS)) {
            IonAnnotator.annotatePrecursorH3PO4(spectrum, settings.getFragmentTolerance(),
                    settings.getMinCharge(Ion.p), settings.getMaxCharge(Ion.p));
        }

        if (settings.isShowIon(Ion.Y)) {
            String composition = psm.getMetaString(Delta.NAME);
            if (StringUtils.isNotEmpty(composition)) {
                GlycanComposition glycanComposition = GlycanComposition.fromName(composition);
                IonAnnotator.annotateGlycanY(spectrum, fragmentTolerance, peptide.getMolecularMass(),
                        settings.getMinCharge(Ion.Y), settings.getMaxCharge(Ion.Y), glycanComposition);
            }
        }

        if (settings.isShowNCore()) {
            int ncore_minz = settings.getnCoreMinCharge();
            int maxz = settings.getnCoreMaxCharge();
            if (maxz == Integer.MAX_VALUE)
                maxz = spectrum.getPrecursorCharge();
            IonAnnotator.annoNCore(spectrum, peptide.getMolecularMass(), fragmentTolerance, ncore_minz, maxz);
        }

        chart.setPeptideSpectrum(peptide, spectrum);
    }

    @Override
    public String getToolName()
    {
        return "PSM Viewer";
    }

    @Override
    public Parent getRoot()
    {
        return this;
    }

    @Override
    public void clear()
    {
        psmTableView.getItems().clear();
        psmTableView.getColumns().clear();
        spectrumMap.clear();
        chart.clearSpectrum();
        chart.setPeptide(null);
    }

    @Override
    public TaskProgressView<Task<?>> getTaskProgressView()
    {
        return progressView;
    }

    /**
     * set the {@link TaskProgressView} to show progress.
     *
     * @param progressView a {@link TaskProgressView} instance.
     */
    public void setProgressView(TaskProgressView<Task<?>> progressView)
    {
        this.progressView = progressView;
    }

    /**
     * update {@link IdentResult}, {@link TaskProgressView} and {@link Tolerance}
     */
    private void selectPSMFile()
    {
        FileChooser psmFileChooser = new FileChooser();
        psmFileChooser.setTitle("Choose PSM File");
        psmFileChooser.getExtensionFilters().addAll(IdentFileUIUtils.getIdentFileFilters());

        File file = psmFileChooser.showOpenDialog(getScene().getWindow());
        if (file == null)
            return;
        IdentResultReadingTask task = new IdentResultReadingTask(file.toPath(), null);
        Task<IdentResult> proTask = NodeUtils.createTask(task);
        if (progressView != null) {
            progressView.getTasks().add(proTask);
        }
        proTask.setOnSucceeded(event -> {
            IdentResult identResult = proTask.getValue();
            Tolerance tol = identResult.getParameters().getFragmentTolerance();
            if (tol != null) {
                settings.setFragmentTolerance(tol);
                annotator.setTolerance(tol);
            }
            updatePSMTable(identResult);
        });

        Thread thread = new Thread(proTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void updatePSMTable(IdentResult identResult)
    {
        if (identResult == null || identResult.isEmpty())
            return;

        // choose a PSM to detect fields
        // it is better to be target PSM, as decoy protein may lack some fields.
        PeptideSpectrumMatch refPSM = null;
        for (PeptideSpectrumMatch psm : identResult) {
            if (psm.isTarget()) {
                refPSM = psm;
                break;
            }
        }
        if (refPSM == null)
            refPSM = identResult.get(0);

        NodeUtils.addIndexColumn(psmTableView);

        List<PSMColumn> psmColumnList = refPSM.getPSMColumnList();
        List<Score> psmScoreList = refPSM.getScoreList();
        List<MetaKey> psmPMList = refPSM.getMetaKeyList();

        for (PSMColumn column : psmColumnList) {
            TableColumn<PeptideSpectrumMatch, Object> col = new TableColumn<>(column.getTitle());
            switch (column) {
                case MS_FILE:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMsDataId().getId()));
                    break;
                case MS_SCAN:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getScanNumberList()));
                    break;
                case MS_INDEX:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getIdentifier().getIndex()));
                    break;
                case MS_TITLE:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTitle()));
                    break;
                case MS_RT:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getIdentifier().getRetentionTimeList()));
                    break;
                case PRECURSOR_MASS:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPrecursorMass()));
                    NodeUtils.formatDoubleColumn(col, MASS_FORMAT);
                    break;
                case PRECURSOR_CHARGE:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPrecursorCharge()));
                    break;
                case PRECURSOR_MZ:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPrecursorMz()));
                    NodeUtils.formatDoubleColumn(col, MASS_FORMAT);
                    break;
                case PRECURSOR_INTENSITY:
                    col.setCellValueFactory(param -> {
                        Optional<Double> precursorIntensity = param.getValue().getPrecursorIntensity();
                        return new ReadOnlyObjectWrapper<>(precursorIntensity.orElse(null));
                    });
                    NodeUtils.formatDoubleColumn(col, INTENSITY_FORMAT);
                    break;
                case PEPTIDE_MASS:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPeptideMass()));
                    NodeUtils.formatDoubleColumn(col, MASS_FORMAT);
                    break;
                case PSM_DELTA_MASS_DA:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMassDiffDa()));
                    NodeUtils.formatDoubleColumn(col, DELTA_FORMAT_DA);
                    break;
                case PSM_DELTA_MASS_PPM:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMassDiffPpm()));
                    NodeUtils.formatDoubleColumn(col, DELTA_FORMAT_PPM);
                    break;
                case PEPTIDE_MISS_CLEAVAGE:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPeptideMatch().getNumMissedCleavage().orElse(-1)));
                    break;
                case PSM_RANK:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getRank()));
                    break;
                case PEPTIDE_SEQUENCE:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getSequence()));
                    break;
                case PEPTIDE_LENGTH:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPeptideMatch().size()));
                    break;
                case PEPTIDE_Modifications:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPeptideMatch().getModifications()));
                    break;
                case PEPTIDE:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getPeptide()));
                    break;
                case HIT_TYPE:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getHitType()));
                    break;
                case PROTEIN_ACCESSIONS:
                    col.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(PeptideProteinMatch.toString(param.getValue().getPeptideMatch().getPeptideProteinMatches())));
            }

            psmTableView.getColumns().add(col);
        }

        for (Score score : psmScoreList) {
            TableColumn<PeptideSpectrumMatch, Object> column = new TableColumn<>(score.getName());
            column.setCellValueFactory(param -> {
                if (param.getValue().hasScore(score)) {
                    return new ReadOnlyObjectWrapper<>(param.getValue().getScore(score));
                }
                return new ReadOnlyObjectWrapper<>("");
            });

            ScoreFormatter formatter = score.getFormatter();
            if (formatter instanceof DoubleScoreFormatter) {
                NodeUtils.formatDoubleColumn(column, ((DoubleScoreFormatter) formatter).getNumberFormat());
            }

            psmTableView.getColumns().add(column);
        }

        for (MetaKey key : psmPMList) {
            TableColumn<PeptideSpectrumMatch, Object> column = new TableColumn<>(key.getId());
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMeta(key).orElse(null)));
            psmTableView.getColumns().add(column);
        }

        psmTableView.getItems().setAll(identResult.getPSMList());
    }

    private void selectMSFile()
    {
        FileChooser msFileChooser = new FileChooser();
        msFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All", "*.*"),
                new FileChooser.ExtensionFilter("Mascot Generic File", "*.mgf"),
                new FileChooser.ExtensionFilter("mzML", "*.mzML"),
                new FileChooser.ExtensionFilter("mzXML", "*.mzXML"));
        File file = msFileChooser.showOpenDialog(getScene().getWindow());
        if (file == null)
            return;

        MsDataAccessor accessor = new MsDataAccessor(file.toPath());
        Task<MsDataFile> task = NodeUtils.createTask(accessor);
        if (progressView != null) {
            progressView.getTasks().add(task);
        }
        task.exceptionProperty().addListener((observable, oldValue, newValue) -> {
            ExceptionAlert alert = new ExceptionAlert(newValue);
            alert.showAndWait();
        });
        task.setOnSucceeded(event -> {
            MsDataFile msDataFile = task.getValue();
            spectrumMap.clear();
            HashMap<String, MsnSpectrum> map = msDataFile.map(SpectrumKeyFunc.FILE_SCAN);
            spectrumMap.putAll(map);
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
