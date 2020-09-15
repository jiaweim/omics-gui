package omics.gui.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import omics.msdk.filter.*;
import omics.util.ms.peaklist.filter.*;
import omics.util.ms.peaklist.transform.MzShiftTransformer;
import omics.util.ms.peaklist.transform.NHighestPeaksNormalizer;
import omics.util.ms.peaklist.transform.NthPeakNormalizer;
import omics.util.ms.peaklist.transform.SqrtTransformer;
import omics.util.utils.IntervalList;
import omics.util.utils.Pair;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 31 Oct 2018, 9:47 AM
 */
public class SpectrumProcessorController extends ProcessorController implements Initializable
{
    /**
     * peak count and window size in N-peaks/bin filter
     */
    public ComboBox<Integer> count_1Node;
    public ComboBox<Double> window_1Node;
    public CheckBox isIncludeNode2;
    public ComboBox<Integer> topNNode;
    public TextField rangeStartNode;
    public TextField rangeEndNode;
    public ListView<Pair<Double, Double>> rangeListNode;
    public TextField centroidMassNode;
    public ChoiceBox<IntensityMode> centroidModeNode;

    public ComboBox<Integer> nswCountNode;
    public TextField nswWindowNode;
    public TextField nswDistanceNode;
    public TextField thresholdValueNode;
    public ChoiceBox<ThresholdFilter.Inequality> thresholdTypeNode;
    public TextField shiftMzNode;
    public ComboBox<Integer> nthNormNode;
    public ComboBox<Integer> sumNNormNode;
    public TextField sumNIntensityNode;
    public ComboBox<Integer> peakCountMinNode;
    public ComboBox<Integer> peakCountMaxNode;
    public ComboBox<Integer> levelMinNode;
    public ComboBox<Integer> levelMaxNode;
    public ComboBox<Integer> chargeMinNode;
    public ComboBox<Integer> chargeMaxNode;
    public TextField ticMinNode;
    public TextField precursorMinMzNode;
    public TextField precursorMaxMzNode;
    public ListView<Pair<Double, Double>> precursorListNode;
    public CheckBox precursorIncludeNode;
    public Label spectrumSizeNode;

    private ListView<Object> peakProcessorListView;

    public void setMainProcessorController(MainProcessorController mainProcessorController)
    {
        this.peakProcessorListView = mainProcessorController.getPeakProcessorListNode();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        IntegerStringConverter intCt = new IntegerStringConverter();
        DoubleStringConverter doubleCt = new DoubleStringConverter();

        count_1Node.setConverter(intCt);
        count_1Node.getItems().addAll(5, 10, 15);
        window_1Node.setConverter(doubleCt);
        window_1Node.getItems().addAll(100., 110.);

        centroidModeNode.getItems().addAll(IntensityMode.values());

        topNNode.setConverter(intCt);

        nswCountNode.setConverter(intCt);
        topNNode.getItems().addAll(50, 100);

        thresholdTypeNode.getItems().addAll(ThresholdFilter.Inequality.values());
        thresholdTypeNode.getSelectionModel().select(ThresholdFilter.Inequality.GREATER_EQUALS);

        nswDistanceNode.setText("0.0");
        nthNormNode.setConverter(intCt);
        nthNormNode.getItems().addAll(1, 2, 3);

        sumNNormNode.setConverter(intCt);
        sumNNormNode.getItems().addAll(1, 2, 3);

        peakCountMinNode.setConverter(intCt);
        peakCountMaxNode.setConverter(intCt);
        peakCountMaxNode.setValue(Integer.MAX_VALUE);

        levelMinNode.setConverter(intCt);
        levelMaxNode.setConverter(intCt);

        chargeMaxNode.setConverter(intCt);
        chargeMinNode.setConverter(intCt);
    }

    /**
     * add N peaks/bin filter
     */
    @FXML
    public void onAddNPeaksBin(ActionEvent actionEvent)
    {
        Integer count = count_1Node.getValue();
        if (count <= 0) {
            showAlter(Alert.AlertType.ERROR, "Peak count should > 0");
            return;
        }
        Double window = window_1Node.getValue();
        if (window <= 0) {
            showAlter(Alert.AlertType.ERROR, "Window size should > 0");
            return;
        }
        NPeaksPerBinFilter filter = new NPeaksPerBinFilter(count, window);
        peakProcessorListView.getItems().add(filter);
    }


    /**
     * add the mz range filter
     */
    @FXML
    public void onAddMzRangeFilter(ActionEvent actionEvent)
    {
        boolean include = isIncludeNode2.isSelected();
        ObservableList<Pair<Double, Double>> rangeList = rangeListNode.getItems();
        if (rangeList.isEmpty()) {
            showAlter(Alert.AlertType.WARNING, "The range list is empty.");
            return;
        }

        IntervalList intervalList = new IntervalList();
        for (Pair<Double, Double> range : rangeList) {
            intervalList.addInterval(range.getFirst(), range.getSecond());
        }

        MzRangeFilter filter = new MzRangeFilter(intervalList, include);
        peakProcessorListView.getItems().add(filter);
    }

    /**
     * add centroidFilter
     */
    @FXML
    public void onAddCentroid(ActionEvent actionEvent)
    {
        Double massDiff = getDoubleValue("Mz diff", centroidMassNode.getText());
        if (massDiff == null)
            return;

        IntensityMode mode = centroidModeNode.getValue();
        CentroidFilter centroidFilter = new CentroidFilter(massDiff, mode);
        peakProcessorListView.getItems().add(centroidFilter);
    }

    /**
     * add NPeakFilter
     */
    @FXML
    public void onAddTopN(ActionEvent actionEvent)
    {
        Integer count = topNNode.getValue();
        NPeaksFilter filter = new NPeaksFilter(count);
        peakProcessorListView.getItems().add(filter);
    }

    /**
     * add a new range in the MzRangeFilter
     */
    public void onAddRange(ActionEvent actionEvent)
    {
        Double startMz = getDoubleValue("Start Mz", rangeStartNode.getText());
        if (startMz == null)
            return;
        Double endMz = getDoubleValue("End Mz", rangeEndNode.getText());
        if (endMz == null)
            return;

        Pair<Double, Double> pair = Pair.create(startMz, endMz);
        rangeListNode.getItems().add(pair);
    }

    /**
     * add N peaks/sliding window filter
     */
    public void onAddNSW(ActionEvent actionEvent)
    {
        Integer count = nswCountNode.getValue();
        Double window = getDoubleValue("Window value", "window is not a valid double value", nswWindowNode.getText());
        if (window == null)
            return;
        Double dist = getDoubleValue("Distance value", "Distance is not a valid double value", nswDistanceNode.getText());
        if (dist == null)
            return;

        NPeaksPerSlidingWindowFilter nsw = new NPeaksPerSlidingWindowFilter(count, window, dist);
        peakProcessorListView.getItems().add(nsw);
    }

    private Double getDoubleValue(String title, String value)
    {
        return getDoubleValue(title, "Not a valid double value", value);
    }

    private Double getDoubleValue(String title, String content, String value)
    {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            showAlter(Alert.AlertType.ERROR, content, title);
            return null;
        }
    }

    public void onAddThreshold(ActionEvent actionEvent)
    {
        Double intensity = getDoubleValue("Intensity value", thresholdValueNode.getText());
        if (intensity == null)
            return;

        ThresholdFilter.Inequality inequality = thresholdTypeNode.getValue();
        ThresholdFilter filter = new ThresholdFilter(intensity, inequality);
        peakProcessorListView.getItems().add(filter);
    }

    public void onAddShiftMz(ActionEvent actionEvent)
    {
        Double shift_mz = getDoubleValue("Shift mz", shiftMzNode.getText());
        if (shift_mz == null)
            return;
        MzShiftTransformer transformer = new MzShiftTransformer(shift_mz);
        peakProcessorListView.getItems().add(transformer);
    }

    @FXML
    public void onAddNthNorm(ActionEvent actionEvent)
    {
        Integer nth = nthNormNode.getValue();
        NthPeakNormalizer normalizer = new NthPeakNormalizer(nth);
        peakProcessorListView.getItems().add(normalizer);
    }

    public void onAddSumNNorm(ActionEvent actionEvent)
    {
        Integer topN = sumNNormNode.getValue();
        Double target_intensity = getDoubleValue("Target Intensity", sumNIntensityNode.getText());
        if (target_intensity == null)
            return;

        NHighestPeaksNormalizer nHighestPeaksNormalizer = new NHighestPeaksNormalizer(topN, target_intensity);
        peakProcessorListView.getItems().add(nHighestPeaksNormalizer);
    }

    public void onAddSrqt(ActionEvent actionEvent)
    {
        SqrtTransformer transformer = new SqrtTransformer();
        peakProcessorListView.getItems().add(transformer);
    }

    public void onAddPeakCountFilter(ActionEvent actionEvent)
    {
        Integer max = peakCountMaxNode.getValue();
        Integer min = peakCountMinNode.getValue();
        SpectrumSizeFilter filter = new SpectrumSizeFilter(min, max);
        peakProcessorListView.getItems().add(filter);
    }

    public void onAddLevelFilter(ActionEvent actionEvent)
    {
        Integer minLevel = levelMinNode.getValue();
        Integer maxLevel = levelMaxNode.getValue();
        MsLevelFilter filter = new MsLevelFilter(minLevel, maxLevel);
        peakProcessorListView.getItems().add(filter);
    }

    public void onAddPrecursorChargeFilter(ActionEvent actionEvent)
    {
        Integer minCharge = chargeMinNode.getValue();
        Integer maxCharge = chargeMaxNode.getValue();
        PrecursorChargeFilter filter = new PrecursorChargeFilter(minCharge, maxCharge);
        peakProcessorListView.getItems().add(filter);
    }

    public void onAddSpectrumTICFilter(ActionEvent actionEvent)
    {
        Double tic = getDoubleValue("TIC Value", ticMinNode.getText());
        if (tic == null)
            return;
        TICFilter filter = new TICFilter(tic);
        peakProcessorListView.getItems().add(filter);
    }

    /**
     * add a new precursor to the precursor mz filter
     */
    public void onAddPrecursorMz(ActionEvent actionEvent)
    {
        Double startMz = getDoubleValue("Start Mz", precursorMinMzNode.getText());
        if (startMz == null)
            return;
        Double endMz = getDoubleValue("End Mz", precursorMaxMzNode.getText());
        if (endMz == null)
            return;

        precursorListNode.getItems().add(Pair.create(startMz, endMz));
    }

    public void onAddPrecursorMzFilter(ActionEvent actionEvent)
    {
        ObservableList<Pair<Double, Double>> items = precursorListNode.getItems();
        if (items.isEmpty()) {
            showAlter(Alert.AlertType.WARNING, "Mz List is empty", "Mz List");
            return;
        }

        IntervalList intervalList = new IntervalList();
        for (Pair<Double, Double> pair : items) {
            intervalList.addInterval(pair.getFirst(), pair.getSecond());
        }

        PrecursorMzFilter filter = new PrecursorMzFilter(intervalList, precursorIncludeNode.isSelected());
        peakProcessorListView.getItems().add(filter);
    }
}
