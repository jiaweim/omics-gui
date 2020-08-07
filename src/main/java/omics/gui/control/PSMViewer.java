package omics.gui.control;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import omics.gui.TaskType;
import omics.gui.psm.PeptideSpectrumChart;
import omics.gui.psm.SpectrumViewStyle;
import omics.gui.psm.util.NodeUtils;
import omics.gui.util.DoubleStringConverter2;
import omics.gui.util.ExceptionAlert;
import omics.gui.util.IdentFileUIUtils;
import omics.gui.util.IntegerStringConverterV2;
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
import omics.pdk.ident.util.IonAnnotator;
import omics.pdk.io.IdentResultReadingTask;
import omics.pdk.psm.PSMKeyFunc;
import omics.pdk.ptm.glycosylation.GlycanComposition;
import omics.pdk.ptm.glycosylation.ident.OxoniumDB;
import omics.pdk.util.DoubleScoreFormatter;
import omics.pdk.util.ScoreFormatter;
import omics.util.MetaKey;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.PeakList;
import omics.util.ms.peaklist.Tolerance;
import omics.util.ms.peaklist.filter.NPeaksPerBinFilter;
import omics.util.protein.Peptide;
import omics.util.protein.ms.Ion;
import omics.util.protein.ms.PeptideFragmentAnnotator;
import omics.util.protein.ms.PeptideFragmenter;
import omics.util.protein.ms.PeptideIon;
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.StringUtils;
import org.controlsfx.control.TaskProgressView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
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
public class PSMViewer extends TabPane
{
    private static final NumberFormat DELTA_FORMAT_DA = NumberFormatFactory.MASS_PRECISION;
    private static final NumberFormat DELTA_FORMAT_PPM = NumberFormatFactory.DIGIT2;
    private static final NumberFormat MASS_FORMAT = NumberFormatFactory.MASS_PRECISION;
    private static final DecimalFormat INTENSITY_FORMAT = NumberFormatFactory.valueOf("0.####E0");

    @FXML
    private TableView<PeptideSpectrumMatch> psmTableView;
    @FXML
    private Button openPSMButton;
    @FXML
    private Button openMSButton;
    @FXML
    private CheckBox showButton;
    @FXML
    private Button updateSettingButton;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Tab viewTab;
    @FXML
    private Tab settingTab;

    //<editor-fold desc="parameters">
    @FXML
    private ComboBox<Double> leftTol;
    @FXML
    private ComboBox<Double> rightTol;
    @FXML
    private ChoiceBox<String> tolUnit;
    @FXML
    private CheckBox deisotope;
    @FXML
    private ComboBox<Double> deisoleftTol;
    @FXML
    private ComboBox<Double> deisorightTol;
    @FXML
    private ChoiceBox<String> deisotolUnit;
    @FXML
    private CheckBox filterPeak;
    @FXML
    private ComboBox<Integer> peakCount;
    @FXML
    private ComboBox<Double> binWidth;
    @FXML
    private CheckBox a;
    @FXML
    private CheckBox b;
    @FXML
    private CheckBox c;
    @FXML
    private CheckBox x;
    @FXML
    private CheckBox y;
    @FXML
    private CheckBox z;
    @FXML
    private CheckBox p;
    @FXML
    private CheckBox ph2o;
    @FXML
    private CheckBox pnh3;
    @FXML
    private CheckBox im;
    @FXML
    private CheckBox bg;
    @FXML
    private CheckBox yg;
    @FXML
    private CheckBox ncore;
    @FXML
    private CheckBox bn;
    @FXML
    private CheckBox yn;
    @FXML
    private ComboBox<Integer> a_minz;
    @FXML
    private ComboBox<Integer> b_minz;
    @FXML
    private ComboBox<Integer> c_minz;
    @FXML
    private ComboBox<Integer> x_minz;
    @FXML
    private ComboBox<Integer> y_minz;
    @FXML
    private ComboBox<Integer> z_minz;
    @FXML
    private ComboBox<Integer> p_minz;
    @FXML
    private ComboBox<Integer> ph2o_minz;
    @FXML
    private ComboBox<Integer> pnh3_minz;
    @FXML
    private ComboBox<Integer> im_minz;
    @FXML
    private ComboBox<Integer> bg_minz;
    @FXML
    private ComboBox<Integer> yg_minz;
    @FXML
    private ComboBox<Integer> ncore_minz;
    @FXML
    private ComboBox<Integer> bn_minz;
    @FXML
    private ComboBox<Integer> yn_minz;
    @FXML
    private ComboBox<Integer> a_maxz;
    @FXML
    private ComboBox<Integer> b_maxz;
    @FXML
    private ComboBox<Integer> c_maxz;
    @FXML
    private ComboBox<Integer> x_maxz;
    @FXML
    private ComboBox<Integer> y_maxz;
    @FXML
    private ComboBox<Integer> z_maxz;
    @FXML
    private ComboBox<Integer> p_maxz;
    @FXML
    private ComboBox<Integer> ph2o_maxz;
    @FXML
    private ComboBox<Integer> pnh3_maxz;
    @FXML
    private ComboBox<Integer> im_maxz;
    @FXML
    private ComboBox<Integer> bg_maxz;
    @FXML
    private ComboBox<Integer> yg_maxz;
    @FXML
    private ComboBox<Integer> ncore_maxz;
    @FXML
    private ComboBox<Integer> bn_maxz;
    @FXML
    private ComboBox<Integer> yn_maxz;

    @FXML
    private ColorPicker a_color;
    @FXML
    private ColorPicker b_color;
    @FXML
    private ColorPicker c_color;
    @FXML
    private ColorPicker x_color;
    @FXML
    private ColorPicker y_color;
    @FXML
    private ColorPicker z_color;
    @FXML
    private ColorPicker p_color;
    @FXML
    private ColorPicker ph2o_color;
    @FXML
    private ColorPicker pnh3_color;
    @FXML
    private ColorPicker im_color;
    @FXML
    private ColorPicker bg_color;
    @FXML
    private ColorPicker yg_color;
    @FXML
    private ColorPicker ncore_color;
    @FXML
    private ColorPicker bn_color;
    @FXML
    private ColorPicker yn_color;

    //</editor-fold>

    private TaskProgressView<Task<?>> progressView = null;

    private PeptideSpectrumChart chart;

    private final PeptideFragmentAnnotator annotator = new PeptideFragmentAnnotator(new PeptideFragmenter(),
            Tolerance.abs(0.05));
    private Tolerance fragmentTolerance = Tolerance.abs(0.05);
    private Tolerance deisotopeTolerance = Tolerance.abs(0.02);

    private final OxoniumDB oxoniumDB = OxoniumDB.getInstance();

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
    }

    @FXML
    private void initialize()
    {
        viewTab.setGraphic(TaskType.VIEW.getIcon());
        settingTab.setGraphic(TaskType.SETTING.getIcon());

        openPSMButton.setGraphic(TaskType.READ_PSM.getIcon());
        openPSMButton.setTooltip(new Tooltip("Open PSM file"));
        openPSMButton.setOnAction(event -> selectPSMFile());

        openMSButton.setGraphic(TaskType.READ_MS.getIcon());
        openMSButton.setTooltip(new Tooltip("Open MS file"));
        openMSButton.setOnAction(event -> selectMSFile());

        updateSettingButton.setGraphic(TaskType.REFRESH.getIcon(Color.GREEN));
        updateSettingButton.setOnAction(event -> updateSettings());

        initViewer();
        initSettings();

        psmTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (showButton.isSelected() && newValue != null) {
                repaintSpectrum(newValue);
            }
        });
    }

    private final SpectrumViewStyle viewStyle = new SpectrumViewStyle();

    private void setTolerance(Tolerance tolerance)
    {
        leftTol.setValue(tolerance.getMinusError());
        rightTol.setValue(tolerance.getPlusError());
        if (tolerance.isAbsolute()) {
            tolUnit.getSelectionModel().select(1);
        } else {
            tolUnit.getSelectionModel().select(0);
        }
    }

    private Tolerance getTolerance(ComboBox<Double> left, ComboBox<Double> right, ChoiceBox<String> unit)
    {
        Double leftValue = left.getValue();
        Double rightValue = right.getValue();
        String unitValue = unit.getValue();
        if (leftValue == null || rightValue == null) {
            return null;
        }
        if (unitValue.equals("ppm"))
            return Tolerance.ppm(leftValue, rightValue);
        else
            return Tolerance.abs(leftValue, rightValue);
    }

    private void setValues(Ion ion, ColorPicker colorPicker, ComboBox<Integer> minZ, ComboBox<Integer> maxZ,
                           List<Integer> values)
    {
        colorPicker.setValue(viewStyle.getColor(ion));
        minZ.getItems().addAll(values);
        minZ.setValue(1);
        maxZ.getItems().addAll(values);
        maxZ.setValue(2);
    }

    private void initSettings()
    {
        DoubleStringConverter2 converter2 = new DoubleStringConverter2();
        leftTol.setConverter(converter2);
        rightTol.setConverter(converter2);
        tolUnit.getItems().addAll("ppm", "Da");
        setTolerance(fragmentTolerance);

        deisoleftTol.setConverter(converter2);
        deisorightTol.setConverter(converter2);
        deisotolUnit.getItems().addAll("ppm", "Da");
        deisoleftTol.setValue(0.02);
        deisorightTol.setValue(0.02);
        deisotolUnit.getSelectionModel().select(1);
        deisotope.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                deisoleftTol.setDisable(false);
                deisorightTol.setDisable(false);
                deisotolUnit.setDisable(false);
            } else {
                deisoleftTol.setDisable(true);
                deisorightTol.setDisable(true);
                deisotolUnit.setDisable(true);
            }
        });
        deisotope.setSelected(false);
        filterPeak.setSelected(false);
        peakCount.getItems().addAll(1, 5, 10, 15, 20);
        peakCount.setValue(10);
        peakCount.setConverter(new IntegerStringConverterV2());
        binWidth.getItems().addAll(50., 100., 150., 200.);
        binWidth.setValue(100.);
        binWidth.setConverter(new DoubleStringConverter2());

        a.setSelected(false);
        b.setSelected(true);
        c.setSelected(false);
        x.setSelected(false);
        y.setSelected(true);
        z.setSelected(false);
        p.setSelected(true);
        ph2o.setSelected(true);
        pnh3.setSelected(true);
        im.setSelected(false);
        bg.setSelected(false);
        yg.setSelected(false);
        ncore.setSelected(false);
        bn.setSelected(false);
        yn.setSelected(false);

        List<Integer> charges = new ArrayList<>();
        charges.add(1);
        charges.add(2);
        charges.add(3);

        setValues(Ion.a, a_color, a_minz, a_maxz, charges);
        setValues(Ion.b, b_color, b_minz, b_maxz, charges);
        setValues(Ion.c, c_color, c_minz, c_maxz, charges);
        setValues(Ion.x, x_color, x_minz, x_maxz, charges);
        setValues(Ion.y, y_color, y_minz, y_maxz, charges);
        setValues(Ion.z, z_color, z_minz, z_maxz, charges);
        setValues(Ion.p, p_color, p_minz, p_maxz, charges);
        setValues(Ion.p_H2O, ph2o_color, ph2o_minz, ph2o_maxz, charges);
        setValues(Ion.p_NH3, pnh3_color, pnh3_minz, pnh3_maxz, charges);
        setValues(Ion.im, im_color, im_minz, im_maxz, charges);
        setValues(Ion.B, bg_color, bg_minz, bg_maxz, charges);
        setValues(Ion.Y, yg_color, yg_minz, yg_maxz, charges);
        setValues(Ion.b_HexNAc, bn_color, bn_minz, bn_maxz, charges);
        setValues(Ion.y_HexNAc, yn_color, yn_minz, yn_maxz, charges);

        im_maxz.setValue(1);
        bg_maxz.setValue(1);

        charges.add(Integer.MAX_VALUE);

        ncore_minz.getItems().addAll(charges);
        ncore_minz.setValue(1);
        ncore_color.setValue(viewStyle.getColor(Ion.Y));
        ncore_maxz.getItems().addAll(charges);

        IntegerStringConverterV2 intConv = new IntegerStringConverterV2("Auto", Integer.MAX_VALUE);
        p_maxz.setConverter(intConv);
        p_maxz.setValue(Integer.MAX_VALUE);
        ph2o_maxz.setConverter(intConv);
        ph2o_maxz.setValue(Integer.MAX_VALUE);
        pnh3_maxz.setConverter(intConv);
        pnh3_maxz.setValue(Integer.MAX_VALUE);
        yg_maxz.setConverter(intConv);
        yg_maxz.setValue(Integer.MAX_VALUE);
        ncore_maxz.setConverter(intConv);
        ncore_maxz.setValue(Integer.MAX_VALUE);

        ncore.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                yg.setSelected(false);
            }
        });
        yg.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                ncore.setSelected(false);
            }
        });

        updateSettings();
    }

    private void showAlert(Alert.AlertType alertType, String msg)
    {
        Alert alert = new Alert(alertType, msg);
        alert.showAndWait();
    }

    private final List<PeptideIon> peptideIons = new ArrayList<>();
    private NPeaksPerBinFilter filter = null;
    private ZscoreDeisotoper deisotoper = null;

    private void updateSettings()
    {
        Tolerance tol = getTolerance(leftTol, rightTol, tolUnit);
        if (tol == null) {
            showAlert(Alert.AlertType.ERROR, "Please set tolerance");
            return;
        }
        this.fragmentTolerance = tol;

        if (deisotope.isSelected()) {
            Tolerance tol2 = getTolerance(deisoleftTol, deisorightTol, deisotolUnit);
            if (tol2 == null) {
                showAlert(Alert.AlertType.ERROR, "Please set tolerance for deisotope");
                return;
            }
            this.deisotopeTolerance = tol2;
            deisotoper = new ZscoreDeisotoper(deisotopeTolerance);
            deisotoper.setConvert2One(false);
        } else {
            deisotoper = null;
        }

        if (filterPeak.isSelected()) {
            filter = new NPeaksPerBinFilter(peakCount.getValue(), binWidth.getValue());
        } else {
            filter = null;
        }

        peptideIons.clear();
        // update ion types
        updateIon(a, Ion.a, a_minz, a_maxz, a_color);
        updateIon(b, Ion.b, b_minz, b_maxz, b_color);
        updateIon(c, Ion.c, c_minz, c_maxz, c_color);
        updateIon(x, Ion.x, x_minz, x_maxz, x_color);
        updateIon(y, Ion.y, y_minz, y_maxz, y_color);
        updateIon(z, Ion.z, z_minz, z_maxz, z_color);

        updateIon(im, Ion.im, im_minz, im_maxz, im_color);
        updateIon(bn, Ion.b_HexNAc, bn_minz, bn_maxz, bn_color);
        updateIon(yn, Ion.y_HexNAc, yn_minz, yn_maxz, yn_color);

        annotator.setPeptideIonList(peptideIons);
        annotator.setTolerance(fragmentTolerance);

        // update colors
        viewStyle.setColor(Ion.p, p_color.getValue());
        viewStyle.setColor(Ion.p_H2O, ph2o_color.getValue());
        viewStyle.setColor(Ion.p_NH3, pnh3_color.getValue());
        viewStyle.setColor(Ion.B, bg_color.getValue());
        viewStyle.setColor(Ion.Y, yg_color.getValue());

        chart.setStyle(viewStyle);

        // update currently selected PSM
        PeptideSpectrumMatch psm = psmTableView.getSelectionModel().getSelectedItem();
        if (psm != null) {
            repaintSpectrum(psm);
        }
    }

    private final Table<Ion, Integer, PeptideIon> peptideIonTable = HashBasedTable.create();

    private void updateIon(CheckBox checkBox, Ion ion, ComboBox<Integer> minCharge,
                           ComboBox<Integer> maxCharge, ColorPicker colorPicker)
    {
        if (!checkBox.isSelected()) {
            return;
        }
        Integer minZ = minCharge.getValue();
        Integer maxZ = maxCharge.getValue();
        if (minZ > maxZ) {
            maxZ = minZ;
            minZ = maxCharge.getValue();
        }

        for (int i = minZ; i <= maxZ; i++) {
            PeptideIon peptideIon = peptideIonTable.get(ion, i);
            if (peptideIon == null) {
                peptideIon = new PeptideIon(ion, i);
                peptideIonTable.put(ion, i, peptideIon);
            }
            this.peptideIons.add(peptideIon);
        }
        viewStyle.setColor(ion, colorPicker.getValue());
    }

    private void initViewer()
    {
        this.chart = new PeptideSpectrumChart();
        showButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                splitPane.getItems().add(chart);
                splitPane.setDividerPositions(0.5);

                PeptideSpectrumMatch selectedItem = psmTableView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    repaintSpectrum(selectedItem);
                }
            } else {
                splitPane.getItems().remove(chart);
            }
        });
    }

    private void repaintSpectrum(PeptideSpectrumMatch psm)
    {
        String key = PSMKeyFunc.FILE_SCAN.getKey(psm);
        Peptide peptide = psm.getPeptide();
        if (!spectrumMap.containsKey(key)) {
            chart.clearSpectrum();
            chart.setPeptide(peptide);
            return;
        }
        MsnSpectrum spectrum = spectrumMap.get(key);
        spectrum.clearAnnotations();
        if (deisotoper != null) {
            spectrum = spectrum.copy(deisotoper);
        }
        if (filter != null) {
            spectrum = spectrum.copy(filter);
        }
        annotator.annotate((PeakList) spectrum, peptide);

        if (bg.isSelected()) {
            IonAnnotator.annotateOxonium(spectrum, fragmentTolerance, oxoniumDB.getMarkers(),
                    bg_minz.getValue(), bg_maxz.getValue());
        }

        if (yg.isSelected()) {
            String composition = psm.getMetaString(Delta.NAME);
            if (StringUtils.isNotEmpty(composition)) {
                GlycanComposition glycanComposition = GlycanComposition.fromName(composition);
                IonAnnotator.annotateGlycanY(spectrum, fragmentTolerance, peptide.getMolecularMass(),
                        yg_minz.getValue(), yg_maxz.getValue(), glycanComposition);
            }
        }

        if (ncore.isSelected()) {
            Integer maxz = ncore_maxz.getValue();
            if (maxz == Integer.MAX_VALUE)
                maxz = spectrum.getPrecursorCharge();
            IonAnnotator.annoNCore(spectrum, peptide.getMolecularMass(), fragmentTolerance, ncore_minz.getValue(), maxz);
        }

        if (p.isSelected()) {
            IonAnnotator.annotatePrecursor(spectrum, fragmentTolerance, p_minz.getValue(), p_maxz.getValue());
        }

        if (pnh3.isSelected()) {
            IonAnnotator.annotatePrecursorNH3(spectrum, fragmentTolerance,
                    pnh3_minz.getValue(), pnh3_maxz.getValue());
        }

        if (ph2o.isSelected()) {
            IonAnnotator.annotatePrecursorH2O(spectrum, fragmentTolerance,
                    ph2o_minz.getValue(), ph2o_maxz.getValue());
        }

        chart.setPeptideSpectrum(peptide, spectrum);
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
            this.identResult = proTask.getValue();
            Tolerance tol = identResult.getParameters().getFragmentTolerance();
            if (tol != null) {
                this.fragmentTolerance = tol;
                setTolerance(tol);
                this.annotator.setTolerance(tol);
            }
            updatePSMTable();
            openPSMButton.getTooltip().setText("Opened PSM file: " + file.getName());
        });

        Thread thread = new Thread(proTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void updatePSMTable()
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
            msDataFile = task.getValue();
            spectrumMap.clear();

            HashMap<String, MsnSpectrum> map = msDataFile.map(SpectrumKeyFunc.FILE_SCAN);
            spectrumMap.putAll(map);
            openMSButton.getTooltip().setText("Opened MS file: " + file.getName());
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
