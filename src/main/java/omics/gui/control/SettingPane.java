package omics.gui.control;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import omics.gui.*;
import omics.gui.setting.GapEditor;
import omics.gui.setting.PTMEditor;
import omics.gui.setting.PercolatorView;

import java.util.HashMap;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 4:56 PM
 */
public class SettingPane extends SplitPane implements ShowAlert, FXMLPane
{
    @FXML
    private TreeView<String> itemTree;
    @FXML
    private StackPane optionPane;

    public SettingPane()
    {
        load(getClass().getClassLoader(), "fxml/settings.fxml");
    }

    private final HashMap<String, Node> controlMap = new HashMap<>();
    private TreeItem<String> root;

    @FXML
    private void initialize()
    {
        root = new TreeItem<>("Settings");
        itemTree.setRoot(root);
        itemTree.setShowRoot(false);

        itemTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String value = newValue.getValue();
            Node control = controlMap.get(value);
            optionPane.getChildren().clear();
            optionPane.getChildren().add(control);
            AnchorPane.setLeftAnchor(control, 6.);
            AnchorPane.setTopAnchor(control, 6.);
        });

        addSetting("Modifications", new PTMEditor());
        addSetting("Gaps", new GapEditor());
        addSetting("Percolator", new PercolatorView());

        itemTree.getSelectionModel().select(0);
    }

    private void addSetting(String name, Node control)
    {
        TreeItem<String> item = new TreeItem<>(name);
        root.getChildren().add(item);

        controlMap.put(name, control);
    }

    /**
     * Get value from the view
     *
     * @param settings {@link Settings} to update
     */
    public void updateSettings(Settings settings)
    {
        for (Setting setting : settings.getSettingList()) {
            String name = setting.getName();
            ((SettingView) controlMap.get(name)).updateSetting(setting);
        }
    }

    /**
     * update the setting view
     *
     * @param settings {@link Settings}
     */
    public void updateView(Settings settings)
    {
        List<Setting> settingList = settings.getSettingList();
        for (Setting setting : settingList) {
            String name = setting.getName();
            Node node = controlMap.get(name);
            ((SettingView) node).updateView(setting);
        }
    }
}
