package omics.gui.control;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import omics.gui.TaskType;
import omics.gui.task.ExportResultTask;
import omics.gui.util.DoubleStringConverter2;
import omics.gui.util.IdentFileUIUtils;
import omics.gui.util.IntegerStringConverterV2;
import omics.util.protein.database.util.DoShuffleDB;
import omics.util.utils.StringUtils;
import org.controlsfx.control.TaskProgressView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    private CheckBox percolatorCheckNode;
    @FXML
    private TextField percolatorPathNode;
    @FXML
    private Button choosePercolatorNode;
    @FXML
    private TextField decoyTagNode;
    @FXML
    private ComboBox<Double> fdrNode;
    @FXML
    private ComboBox<Integer> topNNode;
    @FXML
    private ComboBox<Integer> rankScoreNode;
    @FXML
    private ComboBox<Double> evalueNode;
    @FXML
    private CheckBox deltaNode;
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
    private CheckBox samesetNode;
    @FXML
    private TextField fastaNode;
    @FXML
    private Button chooseFastaNode;

    private TaskProgressView<Task<?>> taskProgressView;

    public void setTaskProgressView(TaskProgressView<Task<?>> taskProgressView)
    {
        this.taskProgressView = taskProgressView;
    }

    @FXML
    private void initialize()
    {
        addButton.setGraphic(TaskType.OPEN_FOLDER.getIcon());
        addButton.setOnAction(event -> selectFile());

        removeButton.setGraphic(TaskType.DELETE.getIcon());
        removeButton.setOnAction(event -> resultFileListNode.getItems().removeAll(resultFileListNode.getSelectionModel().getSelectedItems()));

        resultFileListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resultFileListNode.setEditable(false);

        chooseOutpathButton.setGraphic(TaskType.CHOOSE_FILE.getIcon());
        chooseOutpathButton.setOnAction(event -> selectOutPath());

        runButton.setGraphic(TaskType.RUN.getIcon(Color.GREEN));
        runButton.setOnAction(event -> export());

        initParameters();
    }

    private void initParameters()
    {
        percolatorCheckNode.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                percolatorPathNode.setEditable(true);
                percolatorPathNode.setDisable(false);
                choosePercolatorNode.setDisable(false);
            } else {
                percolatorPathNode.setDisable(true);
                choosePercolatorNode.setDisable(true);
            }
        });
        percolatorCheckNode.setSelected(false);
        percolatorPathNode.setDisable(true);
        choosePercolatorNode.setDisable(true);

        choosePercolatorNode.setGraphic(TaskType.CHOOSE_FILE.getIcon());
        choosePercolatorNode.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Runnable Percolator");
            File file = chooser.showOpenDialog(getScene().getWindow());
            if (file != null)
                percolatorPathNode.setText(file.getAbsolutePath());
        });

        decoyTagNode.setText(DoShuffleDB.DECOY_PROTEIN_PREFIX);

        fdrNode.setConverter(new DoubleStringConverter2());
        fdrNode.setValue(0.01);

        topNNode.setConverter(new IntegerStringConverterV2());
        topNNode.setValue(1);

        rankScoreNode.setConverter(new IntegerStringConverterV2("No Limit", Integer.MIN_VALUE));
        rankScoreNode.getItems().addAll(Integer.MIN_VALUE, 0, 10, 15, 20);
        rankScoreNode.setValue(20);

        evalueNode.setConverter(new DoubleStringConverter2("No Limit", Double.MIN_VALUE));
        evalueNode.getItems().addAll(Double.MIN_VALUE, 0.01, 0.05);
        evalueNode.setValue(0.01);
        evalueNode.setTooltip(new Tooltip("The maximum e-value for PSM"));

        deltaNode.setTooltip(new Tooltip("Keep only PSM with delta"));

        samesetNode.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                fastaNode.setDisable(false);
                chooseFastaNode.setDisable(false);
            } else {
                fastaNode.setDisable(true);
                chooseFastaNode.setDisable(true);
            }
        });
        samesetNode.setSelected(false);
        fastaNode.setDisable(true);
        chooseFastaNode.setDisable(true);

        chooseFastaNode.setGraphic(TaskType.CHOOSE_FILE.getIcon());
        chooseFastaNode.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Fasta file");
            File file = chooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                fastaNode.setText(file.getAbsolutePath());
            }
        });
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

        boolean usePercolator = percolatorCheckNode.isSelected();
        if (usePercolator) {
            String path = percolatorPathNode.getText();
            File perPath = new File(path);
            if (!perPath.exists()) {
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
        Integer rankScore = rankScoreNode.getValue();
        Double evalue = evalueNode.getValue();
        if (evalue == null || evalue < 0 || evalue > 1) {
            showAlert(Alert.AlertType.ERROR, "E-Value should in range [0, 1]");
            return;
        }
        boolean onlyDelta = deltaNode.isSelected();
        String decoyTag = decoyTagNode.getText();

        if (usePercolator) {

        } else {
            ExportResultTask task = new ExportResultTask(fdr, evalue, rankScore, topN, onlyDelta, fileList, targetFilePath, decoyTag);
            if (samesetNode.isSelected()) {
                task.setRemoveSameset(true);
                task.setFasta(fastaNode.getText());
            }
            taskProgressView.getTasks().add(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private File lastOpenFolder = null;

    /**
     * select identification files to process
     */
    private void selectFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select identification file");
        chooser.getExtensionFilters().addAll(IdentFileUIUtils.getIdentFileFilters());
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
