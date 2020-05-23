package omics.gui.control;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import omics.gui.parameter.*;
import omics.util.protein.database.util.DoShuffleDB;
import omics.util.utils.StringUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Pane to processing identifications.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 20 Dec 2019, 10:54 AM
 */
public class ExportResultPane extends SplitPane
{
    public ExportResultPane()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/report.fxml");
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
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private ListView<File> resultFileListNode;
    @FXML
    private TextField targetFileNode;
    @FXML
    private Button chooseOutpathButton;
    @FXML
    private Button runButton;
    @FXML
    private StackPane rightPane;

    @FXML
    private void initialize()
    {
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        addButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN_ALT));
        addButton.setOnAction(event -> selectFile());

        removeButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.TRASH_ALT));
        removeButton.setOnAction(event -> resultFileListNode.getItems().removeAll(resultFileListNode.getSelectionModel().getSelectedItems()));

        resultFileListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resultFileListNode.setEditable(false);

        chooseOutpathButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN));
        chooseOutpathButton.setOnAction(event -> selectOutPath());
        runButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FORWARD).color(Color.GREEN));
        runButton.setOnAction(event -> export());

        initSheet();
    }

    private final BooleanParameter usePercolatorNode = new BooleanParameter("Use percolator", "Use percolator to calculate FDR", "Percolator", true);
    private final FileParameter percolatorPathNode = new FileParameter("Percolator", "Runnable percolator path", "Percolator", FileParameter.ChooseType.OPEN);

    private final StringParameter decoyIdentifier = new StringParameter("Decoy Tag", "Decoy protein identifier", "Settings", DoShuffleDB.DECOY_PROTEIN_PREFIX);
    private final DoubleParameter fdrNode = new DoubleParameter("FDR", "PSM Level FDR threshold", "Score", 0.01);
    private final IntegerParameter topNNode = new IntegerParameter("TopN", "Maximum number of PSM for each spectrum", "Settings", 1);
    private final IntegerParameter rawScoreNode = (IntegerParameter) new IntegerParameter("Raw Score", "The minimum raw score", "Score", 0)
            .candidateValues(Arrays.asList(Integer.MIN_VALUE, 0, 10, 15, 20)).addPair("No Limit", Integer.MIN_VALUE);
    private final DoubleParameter eValueNode = (DoubleParameter) new DoubleParameter("E-Value", "The maximum e-value for PSM", "Score", 0.01)
            .candidateValues(Arrays.asList(Double.MIN_VALUE, 0.01, 0.05)).addPair("No Limit", Double.MIN_VALUE);
    private final BooleanParameter onlyDeltaNode = new BooleanParameter("Only delta", "Keep only PSM with delta", "Settings", false);

    private void initSheet()
    {
        ParameterSheet sheet = new ParameterSheet();
        rightPane.getChildren().add(sheet);
        sheet.getItems().addAll(usePercolatorNode, percolatorPathNode, decoyIdentifier, fdrNode, topNNode, rawScoreNode, eValueNode, onlyDeltaNode);
    }

    private void showAlert(Alert.AlertType type, String msg)
    {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    /**
     * process and export search result.
     */
    private void export()
    {
        ObservableList<File> fileList = resultFileListNode.getItems();
        if (fileList == null || fileList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "The file list is empty");
            return;
        }

        String targetFilePath = targetFileNode.getText();
        if (StringUtils.isEmpty(targetFilePath.trim())) {
            showAlert(Alert.AlertType.ERROR, "The target file path is not valid");
            return;
        }

        Boolean userPercolator = usePercolatorNode.getValue();
        if (userPercolator) {
            File percolatorPath = percolatorPathNode.getValue();
            if (!percolatorPath.exists()) {
                showAlert(Alert.AlertType.ERROR, "Please specify the percolator path");
                return;
            }
        }

        Double fdr = fdrNode.getValue();
        if (fdr == null || fdr > 1 || fdr < 0) {
            showAlert(Alert.AlertType.ERROR, "fdr should in range [0,1]");
            return;
        }

        Integer topN = topNNode.getValue();
        Integer rawScore = rawScoreNode.getValue();
        Double eValue = eValueNode.getValue();
        if (eValue == null || eValue < 0 || eValue > 1) {
            showAlert(Alert.AlertType.ERROR, "E-Value should in range [0, 1]");
            return;
        }
        Boolean onlyDelta = onlyDeltaNode.getValue();
        String decoyTag = decoyIdentifier.getValue();

        if (userPercolator) {
            File percolatorPath = percolatorPathNode.getValue();
            if (!percolatorPath.exists()) {
                showAlert(Alert.AlertType.ERROR, "Please specify the percolator path");
            }
        }


//
//        mainPane.getSelectionModel().select(1);
//
//        ExportResultTask task = new ExportResultTask(fdrValue, maxEValue, minRaw, topN,
//                keepNonGlycanNode.isSelected(), fileList, targetFilePath, DoShuffleDB.DECOY_PROTEIN_PREFIX);
//        Task<Void> runTask = new Task<Void>()
//        {
//            @Override
//            protected Void call() throws Exception
//            {
//
//                return null;
//            }
//        };
//        Thread thread = new Thread(runTask);
//        thread.setDaemon(true);
//        thread.start();
    }

    private File lastOpenFolder = null;

    /**
     * select identification files to process
     */
    private void selectFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select identification file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MzIdentML File", "*.mzid"),
                new FileChooser.ExtensionFilter("All", "*.*"));
        chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));

        if (lastOpenFolder != null)
            chooser.setInitialDirectory(lastOpenFolder);

        List<File> files = chooser.showOpenMultipleDialog(getScene().getWindow());
        if (files != null) {
            resultFileListNode.getItems().addAll(files);
            lastOpenFolder = files.get(0).getParentFile();
        }
    }

    /**
     * select the target file to store result.
     */
    private void selectOutPath()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select output file path");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel", "*.xlsx"),
                new FileChooser.ExtensionFilter("All", "*.*"));
        chooser.setSelectedExtensionFilter(chooser.getExtensionFilters().get(0));
        if (lastOpenFolder != null)
            chooser.setInitialDirectory(lastOpenFolder);

        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            targetFileNode.setText(file.getAbsolutePath());
            lastOpenFolder = file.getParentFile();
        }
    }
}
