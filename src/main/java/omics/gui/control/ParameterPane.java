package omics.gui.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

/**
 * Parameter pane.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 17 Dec 2019, 7:32 PM
 */
public class ParameterPane extends StackPane
{
    @FXML
    private Tab parameterEditTab;
    @FXML
    private Tab modificationTab;
    @FXML
    private Tab deltaTab;

    public ParameterPane()
    {
        URL fxmlURL = getClass().getClassLoader().getResource("fxml/parameters.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(fxmlURL);
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
        ParameterEditorPane editorNode = new ParameterEditorPane();
        parameterEditTab.setContent(editorNode);

        PTMEditor ptmTableNode = new PTMEditor();
        modificationTab.setContent(ptmTableNode);

        DeltaEditor deltaEditor = new DeltaEditor();
        deltaTab.setContent(deltaEditor);
    }
}
