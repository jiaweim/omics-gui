package omics.gui.control;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import omics.gui.TaskType;
import omics.gui.psm.PeptideSpectrumChart;
import omics.gui.psm.SpectrumChart;
import omics.gui.psm.SpectrumViewStyle;
import omics.gui.psm.util.NodeUtils;
import omics.gui.util.ExceptionAlert;
import omics.gui.util.IntegerStringConverterV2;
import omics.msdk.io.MsDataAccessor;
import omics.msdk.model.MsDataFile;
import omics.msdk.model.SpectrumKeyFunc;
import omics.pdk.IdentResult;
import omics.pdk.PSMColumn;
import omics.pdk.ident.model.Delta;
import omics.pdk.ident.model.PeptideProteinMatch;
import omics.pdk.ident.model.PeptideSpectrumMatch;
import omics.pdk.ident.model.Score;
import omics.pdk.ident.util.IonAnnotator;
import omics.pdk.io.IdentResultReadingTask;
import omics.pdk.io.ResultFileType;
import omics.pdk.psm.PSMKeyFunc;
import omics.pdk.ptm.glycosylation.GlycanComposition;
import omics.pdk.ptm.glycosylation.ident.OxoniumDB;
import omics.pdk.util.DoubleFormatter;
import omics.pdk.util.ScientificScoreFormatter;
import omics.pdk.util.ScoreFormatter;
import omics.util.MetaKey;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.Tolerance;
import omics.util.protein.Peptide;
import omics.util.protein.ms.FragmentIonType;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;
import omics.util.protein.ms.PeptideIon;
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.Pair;
import omics.util.utils.StringUtils;
import org.controlsfx.control.TaskProgressView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Pane for PSM.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 16 Dec 2019, 12:29 PM
 */
public class PSMViewer extends VBox
{
    private static final NumberFormat DELTA_FORMAT_DA = NumberFormatFactory.valueOf(4);
    private static final NumberFormat DELTA_FORMAT_PPM = NumberFormatFactory.valueOf(2);

    private static final NumberFormat MASS_FORMAT = NumberFormatFactory.MASS_PRECISION;
    private static final DecimalFormat INTENSITY_FORMAT = NumberFormatFactory.valueOf("0.####E0");

    @FXML
    private BorderPane psmViewPane;
    @FXML
    private TableView<PeptideSpectrumMatch> psmTableView;
    @FXML
    private Label statusLabel;
    @FXML
    private Button openPSMButton;
    @FXML
    private Button openMSButton;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TabPane psmViewTab;
    @FXML
    private StackPane upperPane;

    private TaskProgressView<Task<?>> progressView = null;

    private PeptideSpectrumChart chart;

    private final PeptideFragmentAnnotator annotator = new PeptideFragmentAnnotator(new PeptideFragmenter(),
            Tolerance.abs(0.05));

    private final OxoniumDB oxoniumDB = OxoniumDB.getInstance();
    /**
     * {@link Tolerance} used to match fragment peaks.
     */
    private final ObjectProperty<Tolerance> fragmentTolerance = new SimpleObjectProperty<>(Tolerance.abs(0.05));

    private IdentResult identResult;
    private MsDataFile msDataFile;

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
        getStylesheets().add("css/psm_viewer_pane.css");
    }

    @FXML
    private void initialize()
    {
        openPSMButton.setGraphic(TaskType.READ_PSM.getIcon());
        openPSMButton.setTooltip(new Tooltip("Open PSM file"));

        openMSButton.setGraphic(TaskType.READ_MS.getIcon());
        openMSButton.setTooltip(new Tooltip("Open MS file"));

        initChart();

        fragmentTolerance.addListener((observable, oldValue, newValue) -> {
            annotator.setTolerance(newValue);
            PeptideSpectrumMatch psm = psmTableView.getSelectionModel().getSelectedItem();
            if (psm != null) {
                repaintSpectrum(psm);
            }
        });

        psmTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            repaintSpectrum(newValue);
        });

        initPane();
        initIonTypes();
    }

    private final BooleanProperty isShowing = new SimpleBooleanProperty(false);
    private final DoubleProperty previousPosition = new SimpleDoubleProperty(0.6);

    private void initPane()
    {
        // keep the bottom pane fixed.
        SplitPane.setResizableWithParent(psmViewTab, false);
        psmViewTab.getTabs().get(0).setGraphic(TaskType.MS_VIEW.getIcon(18));
        psmViewTab.getTabs().get(1).setGraphic(TaskType.SETTING.getIcon(18));

        psmViewTab.setOnMouseClicked(event -> {
            EventTarget target = event.getTarget();
            if (target.getClass() == StackPane.class) {
                isShowing.set(!isShowing.get());
            }
        });

        isShowing.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                splitPane.setDividerPosition(0, previousPosition.get());
            } else
                splitPane.setDividerPosition(0, 1 - (psmViewTab.getTabMinHeight()) / splitPane.getHeight());
        });

        splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
            if (isShowing.get())
                previousPosition.set(newValue.doubleValue());
        });

        upperPane.maxHeightProperty().bind(splitPane.heightProperty().subtract(33));
        isShowing.set(false);
    }

    @FXML
    private CheckBox aCheck;
    @FXML
    private CheckBox bCheck;
    @FXML
    private CheckBox cCheck;
    @FXML
    private CheckBox xCheck;
    @FXML
    private CheckBox yCheck;
    @FXML
    private CheckBox zCheck;
    @FXML
    private CheckBox pCheck;
    @FXML
    private CheckBox pH2OCheck;
    @FXML
    private CheckBox pNH3Check;
    @FXML
    private CheckBox imCheck;
    @FXML
    private CheckBox gbCheck;
    @FXML
    private CheckBox gyCheck;

    @FXML
    private ChoiceBox<Integer> aMinCharge;
    @FXML
    private ChoiceBox<Integer> bMinCharge;
    @FXML
    private ChoiceBox<Integer> cMinCharge;
    @FXML
    private ChoiceBox<Integer> xMinCharge;
    @FXML
    private ChoiceBox<Integer> yMinCharge;
    @FXML
    private ChoiceBox<Integer> zMinCharge;
    @FXML
    private ChoiceBox<Integer> pMinCharge;
    @FXML
    private ChoiceBox<Integer> pNH3MinCharge;
    @FXML
    private ChoiceBox<Integer> pH2OMinCharge;
    @FXML
    private ChoiceBox<Integer> imMinCharge;
    @FXML
    private ChoiceBox<Integer> gbMinCharge;
    @FXML
    private ChoiceBox<Integer> gyMinCharge;

    @FXML
    private ChoiceBox<Integer> aMaxCharge;
    @FXML
    private ChoiceBox<Integer> bMaxCharge;
    @FXML
    private ChoiceBox<Integer> cMaxCharge;
    @FXML
    private ChoiceBox<Integer> xMaxCharge;
    @FXML
    private ChoiceBox<Integer> yMaxCharge;
    @FXML
    private ChoiceBox<Integer> zMaxCharge;
    @FXML
    private ChoiceBox<Integer> pMaxCharge;
    @FXML
    private ChoiceBox<Integer> pH2OMaxCharge;
    @FXML
    private ChoiceBox<Integer> pNH3MaxCharge;
    @FXML
    private ChoiceBox<Integer> imMaxCharge;
    @FXML
    private ChoiceBox<Integer> gbMaxCharge;
    @FXML
    private ChoiceBox<Integer> gyMaxCharge;
    @FXML
    private ColorPicker aColor;
    @FXML
    private ColorPicker bColor;
    @FXML
    private ColorPicker cColor;
    @FXML
    private ColorPicker xColor;
    @FXML
    private ColorPicker yColor;
    @FXML
    private ColorPicker zColor;
    @FXML
    private ColorPicker pColor;
    @FXML
    private ColorPicker imColor;
    @FXML
    private ColorPicker gbColor;
    @FXML
    private ColorPicker gyColor;

    private final SpectrumViewStyle viewStyle = new SpectrumViewStyle();
    static final int AUTO = Integer.MAX_VALUE;

    private void initIonTypes()
    {
        aColor.setValue(viewStyle.getColor(FragmentIonType.a));
        bColor.setValue(viewStyle.getColor(FragmentIonType.b));
        cColor.setValue(viewStyle.getColor(FragmentIonType.c));
        xColor.setValue(viewStyle.getColor(FragmentIonType.x));
        yColor.setValue(viewStyle.getColor(FragmentIonType.y));
        zColor.setValue(viewStyle.getColor(FragmentIonType.z));
        pColor.setValue(viewStyle.getColor(FragmentIonType.p));
        imColor.setValue(viewStyle.getColor(FragmentIonType.im));
        gbColor.setValue(viewStyle.getColor(FragmentIonType.B_G));
        gyColor.setValue(viewStyle.getColor(FragmentIonType.Y_G));

        bCheck.setSelected(true);
        yCheck.setSelected(true);
        pCheck.setSelected(true);

        List<Integer> charges = new ArrayList<>();
        charges.add(1);
        charges.add(2);
        charges.add(3);

        aMinCharge.getItems().setAll(charges);
        aMinCharge.setValue(1);
        bMinCharge.getItems().setAll(charges);
        bMinCharge.setValue(1);
        cMinCharge.getItems().setAll(charges);
        cMinCharge.setValue(1);
        xMinCharge.getItems().setAll(charges);
        xMinCharge.setValue(1);
        yMinCharge.getItems().setAll(charges);
        yMinCharge.setValue(1);
        zMinCharge.getItems().setAll(charges);
        zMinCharge.setValue(1);
        pMinCharge.getItems().setAll(charges);
        pMinCharge.setValue(1);
        pH2OMinCharge.getItems().setAll(charges);
        pH2OMinCharge.setValue(1);
        pNH3MinCharge.getItems().setAll(charges);
        pNH3MinCharge.setValue(1);
        imMinCharge.getItems().setAll(charges);
        imMinCharge.setValue(1);
        gbMinCharge.getItems().setAll(charges);
        gbMinCharge.setValue(1);
        gyMinCharge.getItems().setAll(charges);
        gyMinCharge.setValue(1);

        aMaxCharge.getItems().setAll(charges);
        aMaxCharge.setValue(2);
        bMaxCharge.getItems().setAll(charges);
        bMaxCharge.setValue(2);
        cMaxCharge.getItems().setAll(charges);
        cMaxCharge.setValue(2);
        xMaxCharge.getItems().setAll(charges);
        xMaxCharge.setValue(2);
        yMaxCharge.getItems().setAll(charges);
        yMaxCharge.setValue(2);
        zMaxCharge.getItems().setAll(charges);
        zMaxCharge.setValue(2);

        imMaxCharge.getItems().setAll(charges);
        imMaxCharge.setValue(1);
        gbMaxCharge.getItems().setAll(charges);
        gbMaxCharge.setValue(1);

        charges.add(AUTO);
        IntegerStringConverterV2 converter = new IntegerStringConverterV2(Collections.singletonList(Pair.create("auto", Integer.MAX_VALUE)));
        pMaxCharge.setConverter(converter);
        pMaxCharge.getItems().setAll(charges);
        pMaxCharge.setValue(AUTO);

        pNH3MaxCharge.setConverter(converter);
        pNH3MaxCharge.getItems().setAll(charges);
        pNH3MaxCharge.setValue(AUTO);

        pH2OMaxCharge.setConverter(converter);
        pH2OMaxCharge.getItems().setAll(charges);
        pH2OMaxCharge.setValue(AUTO);

        gyMaxCharge.setConverter(converter);
        gyMaxCharge.getItems().setAll(charges);
        gyMaxCharge.setValue(AUTO);

        addIon(FragmentIonType.b, bMinCharge, bMaxCharge);
        addIon(FragmentIonType.y, yMinCharge, yMaxCharge);
        annotator.setPeptideIonList(peptideIons);
    }

    private final Table<FragmentIonType, Integer, PeptideIon> peptideIonTable = HashBasedTable.create();
    private final List<PeptideIon> peptideIons = new ArrayList<>();

    @FXML
    private void updateIonTypes()
    {
        peptideIons.clear();
        if (aCheck.isSelected())
            addIon(FragmentIonType.a, aMinCharge, aMaxCharge);
        if (bCheck.isSelected())
            addIon(FragmentIonType.b, bMinCharge, bMaxCharge);
        if (cCheck.isSelected())
            addIon(FragmentIonType.c, cMinCharge, cMaxCharge);
        if (xCheck.isSelected())
            addIon(FragmentIonType.x, xMinCharge, xMaxCharge);
        if (yCheck.isSelected())
            addIon(FragmentIonType.y, yMinCharge, yMaxCharge);
        if (zCheck.isSelected())
            addIon(FragmentIonType.z, zMinCharge, zMaxCharge);
        if (imCheck.isSelected())
            addIon(FragmentIonType.im, imMinCharge, imMaxCharge);
        annotator.setPeptideIonList(peptideIons);
        viewStyle.setColor(FragmentIonType.a, aColor.getValue());
        viewStyle.setColor(FragmentIonType.b, bColor.getValue());
        viewStyle.setColor(FragmentIonType.c, cColor.getValue());
        viewStyle.setColor(FragmentIonType.x, xColor.getValue());
        viewStyle.setColor(FragmentIonType.y, yColor.getValue());
        viewStyle.setColor(FragmentIonType.z, zColor.getValue());
        viewStyle.setColor(FragmentIonType.p, pColor.getValue());
        viewStyle.setColor(FragmentIonType.im, imColor.getValue());
        viewStyle.setColor(FragmentIonType.B_G, gbColor.getValue());
        viewStyle.setColor(FragmentIonType.Y_G, gyColor.getValue());
        chart.setStyle(viewStyle);

        // update currently selected PSM
        PeptideSpectrumMatch psm = psmTableView.getSelectionModel().getSelectedItem();
        if (psm != null) {
            repaintSpectrum(psm);
        }
    }

    private void addIon(FragmentIonType fragmentIonType, ChoiceBox<Integer> minChoice, ChoiceBox<Integer> maxChoice)
    {
        Integer minCharge = minChoice.getValue();
        Integer maxCharge = maxChoice.getValue();
        if (minCharge > maxCharge) {
            maxCharge = minCharge;
            minCharge = maxChoice.getValue();
        }

        for (int i = minCharge; i <= maxCharge; i++) {
            PeptideIon peptideIon = peptideIonTable.get(fragmentIonType, i);
            if (peptideIon == null) {
                peptideIon = new PeptideIon(fragmentIonType, i);
                peptideIonTable.put(fragmentIonType, i, peptideIon);
            }
            this.peptideIons.add(peptideIon);
        }
    }

    private void initChart()
    {
        this.chart = new PeptideSpectrumChart();
        psmViewPane.setCenter(chart);
        SpectrumChart spectrumChart = chart.getSpectrumChart();
        spectrumChart.mzProperty().addListener((observable, oldValue, newValue) ->
                statusLabel.setText(String.format("Mass: %.4f, Intensity: %.4e", newValue.doubleValue(), spectrumChart.intensityProperty().doubleValue())));
        spectrumChart.intensityProperty().addListener((observable, oldValue, newValue) ->
                statusLabel.setText(String.format("Mass: %.4f, Intensity: %.4e", spectrumChart.mzProperty().doubleValue(), newValue.doubleValue())));

        ContextMenu menu = new ContextMenu();
        MenuItem item = new Menu("Save to PNG");
        item.setOnAction(event1 -> {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("spectrum.png");
            File file = chooser.showSaveDialog(getScene().getWindow());
            if (file != null) {
                NodeUtils.saveNodeAsPng(chart, 2, file.getAbsolutePath());
            }
        });
        menu.getItems().add(item);
        menu.setAutoHide(true);

        chart.setOnContextMenuRequested(event -> menu.show(chart.getScene().getWindow(), event.getScreenX(), event.getScreenY()));
    }

    private void repaintSpectrum(PeptideSpectrumMatch psm)
    {
        String key = PSMKeyFunc.FILE_SCAN.getKey(psm);
        Peptide peptide = psm.getPeptide();
        if (spectrumMap.containsKey(key)) {
            MsnSpectrum spectrum = spectrumMap.get(key);
            spectrum.clearAnnotations();
            annotator.annotate((PeakList) spectrum, peptide);

            Tolerance tolerance = fragmentTolerance.get();
            if (gbCheck.isSelected()) {
                IonAnnotator.annotateOxonium(spectrum, tolerance, oxoniumDB.getMarkers(),
                        gbMinCharge.getValue(), gbMaxCharge.getValue());
            }

            if (gyCheck.isSelected()) {
                String composition = psm.getMetaString(Delta.NAME);
                if (StringUtils.isNotEmpty(composition)) {
                    GlycanComposition glycanComposition = GlycanComposition.parseComposition(composition);
                    IonAnnotator.annotateGlycanY(spectrum, tolerance, peptide.getMolecularMass(),
                            gyMinCharge.getValue(), gyMaxCharge.getValue(), glycanComposition);
                }
//                IonAnnotator.annotateOGlycanY(spectrum, tolerance,
//                        gyMinCharge.getValue(), gyMaxCharge.getValue());
            }

            if (pCheck.isSelected()) {
                IonAnnotator.annotatePrecursor(spectrum, tolerance,
                        pMinCharge.getValue(), pMaxCharge.getValue());
            }

            if (pNH3Check.isSelected()) {
                IonAnnotator.annotatePrecursorNH3(spectrum, tolerance,
                        pNH3MinCharge.getValue(), pNH3MaxCharge.getValue());
            }

            if (pH2OCheck.isSelected()) {
                IonAnnotator.annotatePrecursorH2O(spectrum, fragmentTolerance.get(),
                        pH2OMinCharge.getValue(), pH2OMaxCharge.getValue());
            }

            chart.setPeptideSpectrum(peptide, spectrum);
        } else {
            chart.clearSpectrum();
            chart.setPeptide(peptide);
        }
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

    @FXML
    private void onSelectPSMFile()
    {
        FileChooser psmFileChooser = new FileChooser();
        psmFileChooser.setTitle("Choose PSM File");
        psmFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All", "*.*"),
                new FileChooser.ExtensionFilter(ResultFileType.MzIdentML.getName(), ResultFileType.MzIdentML.getExtension()),
                new FileChooser.ExtensionFilter(ResultFileType.MASCOT_CSV.getName(), ResultFileType.MASCOT_CSV.getExtension()),
                new FileChooser.ExtensionFilter(ResultFileType.MASCOT_DAT.getName(), ResultFileType.MASCOT_DAT.getExtension()),
                new FileChooser.ExtensionFilter(ResultFileType.OMICS_EXCEL.getName(), ResultFileType.OMICS_EXCEL.getExtension()),
                new FileChooser.ExtensionFilter(ResultFileType.PEP_XML.getName(), ResultFileType.PEP_XML.getExtension()));

        File file = psmFileChooser.showOpenDialog(getScene().getWindow());
        if (file == null)
            return;
        IdentResultReadingTask task = new IdentResultReadingTask(file.toPath(), null);
        Task<IdentResult> proTask = NodeUtils.createTask(task);
        if (progressView != null) {
            progressView.getTasks().add(proTask);
            progressView.setGraphicFactory(new Callback<Task<?>, Node>()
            {
                @Override
                public Node call(Task<?> param)
                {
                    return null;
                }
            });
        }
        proTask.setOnSucceeded(event -> {
            identResult = proTask.getValue();
            fragmentTolerance.set(identResult.getParameters().getFragmentTolerance());
            updatePSMTable();
            openPSMButton.getTooltip().setText("Opened PSM file: " + file.getName());
        });

        Thread thread = new Thread(proTask);
        thread.start();
    }

    private void updatePSMTable()
    {
        // choose a PSM to detect fields
        // it is better to be target PSM, as decoy protein may lack some fields.
        if (identResult == null || identResult.isEmpty())
            return;

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
            if (formatter instanceof DoubleFormatter) {
                NodeUtils.formatDoubleColumn(column, ((DoubleFormatter) formatter).getFormat());
            } else if (formatter instanceof ScientificScoreFormatter) {
                NodeUtils.formatDoubleColumn(column, ((ScientificScoreFormatter) formatter).getFormat());
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

    @FXML
    private void onSelectMSFile()
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
            msDataFile = task.getValue();
            spectrumMap.clear();

            HashMap<String, MsnSpectrum> map = msDataFile.map(SpectrumKeyFunc.FILE_SCAN);
            spectrumMap.putAll(map);
            openMSButton.getTooltip().setText("Opened MS file: " + file.getName());
        });
        Thread thread = new Thread(task);
        thread.start();
    }
}
