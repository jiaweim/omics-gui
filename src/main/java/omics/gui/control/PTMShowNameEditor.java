package omics.gui.control;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import omics.gui.ShowAlert;
import omics.util.utils.Pair;
import omics.util.utils.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 10:52 AM
 */
public class PTMShowNameEditor extends GridPane implements ShowAlert
{
    @FXML
    private TextField ptmNameField;
    @FXML
    private TextField showNameField;
    @FXML
    private Button addBtn;
    @FXML
    private Button delBtn;
    @FXML
    private TableView<Pair<String, String>> table;

    public PTMShowNameEditor()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/showptm_table.fxml");
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

    public void updateView(Map<String, String> map)
    {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            table.getItems().add(Pair.create(entry.getKey(), entry.getValue()));
        }
    }

    public Map<String, String> getMap()
    {
        HashMap<String, String> map = new HashMap<>(table.getItems().size());
        for (Pair<String, String> item : table.getItems()) {
            map.put(item.getKey(), item.getValue());
        }
        return map;
    }

    @FXML
    private void initialize()
    {
        TableColumn<Pair<String, String>, String> ptmNameCol = new TableColumn<>("PTM Name");
        ptmNameCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getKey()));
        ptmNameCol.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<Pair<String, String>, String> showNameCol = new TableColumn<>("Show Name");
        showNameCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));
        showNameCol.setCellFactory(TextFieldTableCell.forTableColumn());

        ptmNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.6));
        showNameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));

        table.getColumns().add(ptmNameCol);
        table.getColumns().add(showNameCol);

        addBtn.setOnAction(event -> {
            String ptmName = ptmNameField.getText();
            String showName = showNameField.getText();

            if (StringUtils.isEmpty(ptmName)) {
                showAlert(Alert.AlertType.ERROR, "PTM name is empty");
                return;
            }
            if (StringUtils.isEmpty(showName)) {
                showAlert(Alert.AlertType.ERROR, "Show name is empty");
            }

            table.getItems().add(Pair.create(ptmName, showName));
        });

        delBtn.setOnAction(event -> {
            Pair<String, String> selectedItem = table.getSelectionModel().getSelectedItem();
            if (selectedItem != null)
                table.getItems().remove(selectedItem);
        });
    }
}
