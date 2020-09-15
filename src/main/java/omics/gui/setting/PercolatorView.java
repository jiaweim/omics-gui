package omics.gui.setting;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import omics.gui.Setting;
import omics.gui.SettingView;
import omics.gui.TaskType;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 5:08 PM
 */
public class PercolatorView extends AnchorPane implements SettingView
{
    @FXML
    private TextField percolatorPathField;
    @FXML
    private Button chooseFileButton;

    public PercolatorView()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/view_percolator.fxml");
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
        chooseFileButton.setGraphic(TaskType.OPEN_FOLDER.getIcon());
    }

    private boolean isModified = false;

    @FXML
    private void choose()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Percolator Executable");
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            percolatorPathField.setText(file.getAbsolutePath());
            isModified = true;
        }
    }

    @Override
    public void updateSetting(Setting setting)
    {
        if (isModified) {
            PercolatorSetting percolatorSetting = (PercolatorSetting) setting;
            percolatorSetting.setPath(percolatorPathField.getText());
            isModified = false;
        }
    }

    @Override
    public void updateView(Setting setting)
    {
        PercolatorSetting percolatorSetting = (PercolatorSetting) setting;
        percolatorPathField.setText(percolatorSetting.getPath());
    }
}
