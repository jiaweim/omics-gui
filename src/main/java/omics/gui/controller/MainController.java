package omics.gui.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import omics.gui.SearchWindow;
import omics.gui.Settings;
import omics.gui.ShowAlert;
import omics.gui.TaskType;
import omics.gui.control.*;
import omics.gui.psm.util.NodeUtils;
import omics.pdk.ident.IGD;
import omics.pdk.ident.SearchParameters;
import omics.util.utils.SystemUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.glyphfont.FontAwesome;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author JiaweiMao
 * @version 1.2.0
 * @since 11 Oct 2018, 12:34 PM
 */
public class MainController implements ShowAlert
{
    @FXML
    public MenuItem settingMenu;
    @FXML
    private TextField parameterFileNode;
    @FXML
    private Button selectParameterFileButton;
    @FXML
    private Button addMSFileButton;
    @FXML
    private Button removeMSFileButton;
    @FXML
    private ListView<File> msFileListNode;
    @FXML
    private Spinner<Integer> threadNode;
    @FXML
    private Button startButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TaskProgressView<Task<?>> taskProgressNode;
    @FXML
    private Tab reportTab;
    @FXML
    private Tab parameterTab;
    @FXML
    private Tab toolboxTab;

    public MainController() { }

    private Window mainWindow;

    public void setMainWindow(Window mainWindow)
    {
        this.mainWindow = mainWindow;
    }

    private final Settings settings = new Settings();

    @FXML
    private void initialize()
    {
        initMenu();
        initSearchPane();

        ExportResultPane exportResultPane = new ExportResultPane();
        exportResultPane.setTaskProgressView(taskProgressNode);
        exportResultPane.setSettings(settings);
        reportTab.setContent(exportResultPane);

        parameterTab.setContent(new ParameterEditorPane());

        ToolBox toolBox = new ToolBox();
        toolBox.setProgressView(taskProgressNode);
        toolboxTab.setContent(toolBox);
    }

    private void initMenu()
    {
        settingMenu.setGraphic(TaskType.getIcon(FontAwesome.Glyph.WRENCH, 14));
        settingMenu.setOnAction(event -> {

            SettingPane settingPane = new SettingPane();
            settingPane.updateView(settings);

            Scene scene = settingPane.getScene();
            if (scene == null) {
                scene = new Scene(settingPane);
            }

            Screen primary = Screen.getPrimary();
            Rectangle2D bounds = primary.getBounds();

            Stage stage = new Stage();
            stage.setMinWidth(bounds.getWidth() * 0.5);
            stage.setMinHeight(bounds.getHeight() * 0.66);
            stage.setScene(scene);
            stage.setTitle("Settings");
            stage.setOnCloseRequest(event1 -> settingPane.updateSettings(settings));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        });
    }

    private void initSearchPane()
    {
        selectParameterFileButton.setGraphic(TaskType.OPEN_FILE.getIcon());

        msFileListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        msFileListNode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ContextMenu contextMenu = NodeUtils.createMenu(newValue);
                msFileListNode.setContextMenu(contextMenu);
            }
        });

        addMSFileButton.setGraphic(TaskType.OPEN_FOLDER.getIcon());

        removeMSFileButton.setGraphic(TaskType.DELETE.getIcon());
        removeMSFileButton.setOnAction(event -> msFileListNode.getItems().removeAll(msFileListNode.getSelectionModel().getSelectedItems()));

        int processors = Runtime.getRuntime().availableProcessors();
        int currentProcessor = processors / 2;
        if (currentProcessor < 1)
            currentProcessor = 1;
        threadNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, processors, currentProcessor));

        startButton.setGraphic(TaskType.RUN.getIcon(Color.GREEN));
        startButton.setOnAction(event -> startSearch());

        cancelButton.setGraphic(TaskType.STOP.getIcon(Color.ORANGERED));
        cancelButton.setOnAction(event -> doCancel());
    }

    @FXML
    private void selectParameterFile()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose parameter file");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Proteomics configuration file", "*.pcf"),
                new FileChooser.ExtensionFilter("All", "*.*"));

        File file = chooser.showOpenDialog(mainWindow);
        if (file != null) {
            parameterFileNode.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void addMSFile()
    {
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
    }

    private Task<Void> searchTask = null;

    /**
     * do database search
     */
    private void startSearch()
    {
        ObservableList<File> items = msFileListNode.getItems();
        if (items.isEmpty())
            return;

        String parameterFile = parameterFileNode.getText();
        Integer threadCount = threadNode.getValue();

        SearchParameters parameter = SearchParameters.getParameter(parameterFile);
        Path database = parameter.getDatabase();
        if (Files.notExists(database)) {
            showAlert(Alert.AlertType.ERROR, "Database '" + database + "' not exist!");
            return;
        }

        int processorCount = SystemUtils.getProcessorCount();
        if (threadCount <= 0 || threadCount > processorCount)
            threadCount = processorCount;

        List<String> fileList = new ArrayList<>();
        for (File item : items) {
            fileList.add(item.getAbsolutePath());
        }

        IGD igd = new IGD(parameterFile, 666, threadCount, fileList);
        searchTask = NodeUtils.createTask(igd);
        taskProgressNode.getTasks().add(searchTask);

        searchTask.setOnSucceeded(event -> {
            startButton.setDisable(false);
            cancelButton.setDisable(true);
        });

        Thread thread = new Thread(searchTask);
        thread.setDaemon(true);
        thread.start();
        startButton.setDisable(true);
        cancelButton.setDisable(false);
    }

    private void doCancel()
    {
        if (searchTask != null && searchTask.isRunning()) {
            searchTask.cancel(true);
        }

        System.gc();
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
        AboutWindow aboutWindow = new AboutWindow(SearchWindow.NAME, SearchWindow.VERSION, SearchWindow.BUILT_TIME, "");

        PopOver popOver = new PopOver(aboutWindow);
        popOver.setTitle(SearchWindow.NAME);
        popOver.show(mainWindow);
    }
}
