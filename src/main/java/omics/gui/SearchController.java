package omics.gui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import omics.gui.control.ExportResultPane;
import omics.gui.control.PSMViewer;
import omics.gui.control.ParameterPane;
import omics.gui.psm.util.NodeUtils;
import omics.pdk.ident.MainSearch;
import omics.pdk.ident.SearchParameters;
import omics.util.utils.SystemUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * @author JiaweiMao
 * @version 1.2.0
 * @since 11 Oct 2018, 12:34 PM
 */
public class SearchController
{
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    /**
     * select parameter file
     */
    @FXML
    private TextField parameterFileNode;
    @FXML
    private Button selectParameterFileButton;

    /**
     * select MS files
     */
    @FXML
    private Button addMSFileButton;
    @FXML
    private Button removeMSFileButton;

    /**
     * display MS files
     */
    @FXML
    private ListView<File> msFileListNode;

    /**
     * run
     */
    @FXML
    private Spinner<Integer> threadNode;
    @FXML
    private Spinner<Integer> numTaskNode;
    @FXML
    private Button startButton;
    @FXML
    private Button cancelButton;

    /**
     * progress viewer
     */
    @FXML
    private TaskProgressView<Task<?>> taskProgressNode;

    /**
     * main pane, used to add other tabs
     */
    @FXML
    private TabPane mainPane;
    @FXML
    private Tab reportTab;
    @FXML
    private Tab parameterTab;
    @FXML
    private Tab viewerTab;

    public SearchController() { }

    private Task<Void> searchTask;
    private Window mainWindow;

    public void setMainWindow(Window mainWindow)
    {
        this.mainWindow = mainWindow;
    }

    private final GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");

    @FXML
    private void initialize()
    {
        initSearch();

        int processors = Runtime.getRuntime().availableProcessors();
        int currentProcessor = processors / 2;
        if (currentProcessor < 1)
            currentProcessor = 1;
        threadNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, processors, currentProcessor));
        threadNode.setTooltip(new Tooltip("0 means automatic mode"));
        numTaskNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        numTaskNode.setTooltip(new Tooltip("0 means automatic mode"));

        reportTab.setContent(new ExportResultPane());
        parameterTab.setContent(new ParameterPane());
        initViewer();
//        URL imgUrl = getClass().getClassLoader().getResource("icon/search.png");
//        taskProgressNode.setGraphicFactory((Callback) param -> new ImageView(new Image(imgUrl.toExternalForm())));
    }

    private void initSearch()
    {
        selectParameterFileButton.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose parameter file");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Proteomics configuration file", "*.pcf"),
                    new FileChooser.ExtensionFilter("All", "*.*"));

            File file = chooser.showOpenDialog(mainWindow);
            if (file != null) {
                parameterFileNode.setText(file.getAbsolutePath());
            }
        });

        msFileListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addMSFileButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN_ALT));
        addMSFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose MS Files");

            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All", "*.*"),
                    new FileChooser.ExtensionFilter("MGF", "*.mgf"),
                    new FileChooser.ExtensionFilter("mzML", "*.mzML"),
                    new FileChooser.ExtensionFilter("mzXML", "*.mzXML"),
                    new FileChooser.ExtensionFilter("PKL", "*.pkl"));

            List<File> files = fileChooser.showOpenMultipleDialog(mainWindow);
            if (files != null) {
                msFileListNode.getItems().addAll(files);
            }
        });

        removeMSFileButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.TRASH_ALT));
        removeMSFileButton.setOnAction(event -> msFileListNode.getItems().removeAll(msFileListNode.getSelectionModel().getSelectedItems()));

        startButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FORWARD).color(Color.GREEN));

        cancelButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.POWER_OFF).color(Color.RED));

    }

    private void initViewer()
    {
        PSMViewer viewerPane = new PSMViewer();
        viewerPane.setProgressView(taskProgressNode);
        viewerTab.setContent(viewerPane);
    }

    /**
     * do database search
     */
    @FXML
    private void onSearch(ActionEvent actionEvent)
    {
//        SearchParameters parameter = SearchParameters.getParameter(parameterFileNode.getText());
//        Path databaseFile = parameter.getDatabase();
//        if (Files.notExists(databaseFile)) {
//            showAlert(Alert.AlertType.ERROR, "Database file: " + databaseFile + " not exist!");
//            return;
//        }
//
//        MainSearch search = new MainSearch(parameterFileNode.getText(), numTaskNode.getValue(), threadNode.getValue(), msFileListNode.getItems());
//        this.searchTask = NodeUtils.createTask(search);
//        taskProgressNode.getTasks().add(searchTask);
//        try {
//            Thread thread = new Thread(searchTask);
//            thread.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        searchTask.setOnSucceeded(event -> System.gc());
//        searchTask = null;
    }

    public void onCancel(ActionEvent actionEvent)
    {
        if (searchTask != null && searchTask.isRunning()) {
            searchTask.cancel(true);
            logger.info("The task is cancelled !!!");
        }
        System.gc();
    }

    private void showAlert(Alert.AlertType type, String msg)
    {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    /**
     * Exit the application.
     */
    @FXML
    private void exit(ActionEvent actionEvent)
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText("Confirm Exit");
        alert.setContentText("Area you sure you want to exit?");
        Optional<ButtonType> bt = alert.showAndWait();
        if (bt.isPresent()) {
            ButtonType buttonType1 = bt.get();
            if (buttonType1 == ButtonType.OK) {
                Platform.exit();
            }
        }
    }

    @FXML
    private void showHelpInfo(ActionEvent actionEvent)
    {
        VBox root = new VBox();
        root.setSpacing(10);
        root.setAlignment(Pos.BASELINE_LEFT);
        root.setPrefWidth(600);
        root.setPrefHeight(360);
        root.setPadding(new Insets(10));

        Label label = new Label(SearchWindow.NAME + " " + SearchWindow.VERSION);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(label);

        root.getChildren().add(hBox);

        Font font = Font.font("Arial", 16);
        Label timeLabel = new Label("Built on " + SearchWindow.BUILT_TIME);
        timeLabel.setFont(font);
        root.getChildren().add(timeLabel);

        // Empty line
        root.getChildren().add(new Label(""));
        Label runtimeLabel = new Label("Runtime version: " + SystemUtils.JAVA_RUNTIME_VERSION + " " + SystemUtils.OS_ARCH);
        runtimeLabel.setFont(font);
        root.getChildren().add(runtimeLabel);
        Label vmLabel = new Label("VM: " + SystemUtils.JAVA_VM_NAME + " by " + SystemUtils.JAVA_VM_VENDOR);
        vmLabel.setFont(font);
        root.getChildren().add(vmLabel);
        root.getChildren().add(new Label(""));

        Label mailLabel = new Label("Any questions about usage, please contact jiawei@dicp.ac.cn");
        mailLabel.setFont(font);
        root.getChildren().add(mailLabel);

        PopOver popOver = new PopOver(root);
        popOver.setTitle(SearchWindow.NAME);
        popOver.show(mainWindow);
    }
}
