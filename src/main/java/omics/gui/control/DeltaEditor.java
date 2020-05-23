package omics.gui.control;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;
import omics.pdk.ident.model.Delta;
import omics.pdk.ident.model.DeltaFactory;
import omics.pdk.ptm.glycosylation.GlycanComposition;
import omics.util.chem.Composition;
import omics.util.utils.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Editor of Delta.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Jan 2019, 8:47 AM
 */
public class DeltaEditor extends GridPane
{
    @FXML
    private ListView<Delta> databaseItemList;
    @FXML
    private ListView<Delta> selectedUnitList;
    @FXML
    private ListView<Delta> buildList;
    @FXML
    private ChoiceBox<Integer> maxUnitCount;
    @FXML
    private TextField nameField;
    @FXML
    private TextField massField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cleanButton;
    @FXML
    private Button buildButton;
    @FXML
    private Button addCombinationButton;

    private DeltaFactory deltaFactory;

    private boolean changed = false;

    public DeltaEditor()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/delta_editor.fxml");
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
    public void initialize()
    {
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        saveButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.SAVE).color(Color.BLACK));
        saveButton.setOnAction(event -> saveDatabaseChange());

        deltaFactory = DeltaFactory.getInstance();

        // editor of new delta composition
        massField.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                FormulaEditor formulaNode = new FormulaEditor();
                PopOver popOver = new PopOver(formulaNode);
                popOver.setTitle("Composition Editor");
                formulaNode.okButton().setOnAction(event -> {
                    TextField textField = formulaNode.compositionNode();
                    String text = textField.getText();
                    if (StringUtils.isNotEmpty(text)) {
                        massField.setText(text);
                    }
                    popOver.hide();
                });

                formulaNode.cancelButton().setOnAction(event -> popOver.hide());
                popOver.show(massField);
            }
        });

        databaseItemList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        databaseItemList.getItems().addAll(deltaFactory.getDeltaList());

        selectedUnitList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        buildList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        buildButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.PLAY).color(Color.GREEN));
        Tooltip.install(buildButton, new Tooltip("Run delta combination"));
        buildButton.setOnAction(event -> build());

        maxUnitCount.setConverter(new IntegerStringConverter());
        maxUnitCount.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9);
        maxUnitCount.getSelectionModel().select(2);


        cleanButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILTER));
        Tooltip.install(cleanButton, new Tooltip("Remove items that have already in the database."));
        cleanButton.setOnAction(event -> {
            ObservableList<Delta> items = buildList.getItems();
            List<Delta> duplicateItems = new ArrayList<>();
            for (Delta item : items) {
                if (deltaFactory.contains(item)) {
                    duplicateItems.add(item);
                }
            }
            buildList.getItems().removeAll(duplicateItems);
        });

        addCombinationButton.setOnAction(event -> addDeltaEntry2Database());
    }

    /**
     * calculate the combinations of delta units.
     */
    public void build()
    {
        if (selectedUnitList.getItems().isEmpty())
            return;

        Integer countValue = maxUnitCount.getValue();
        boolean isglycan = true;
        for (Delta item : selectedUnitList.getItems()) {
            if (!(item instanceof GlycanComposition)) {
                isglycan = false;
                break;
            }
        }

        buildList.getItems().clear();
        if (isglycan) {
            List<GlycanComposition> compositionList = new ArrayList<>();
            for (Delta item : selectedUnitList.getItems()) {
                compositionList.add((GlycanComposition) item);
            }

            List<GlycanComposition> glycanCombinations = GlycanComposition.getGlycanCombinations(compositionList, countValue);
            buildList.getItems().addAll(glycanCombinations);
        } else {
            List<Delta> entryList = Delta.getCombinations(selectedUnitList.getItems(), countValue);
            buildList.getItems().addAll(entryList);
        }
    }

    /**
     * add a delta unit from database to selected units
     */
    @FXML
    public void addDeltaUnit(ActionEvent actionEvent)
    {
        ObservableList<Delta> selectedItems = databaseItemList.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty())
            return;

        selectedUnitList.getItems().addAll(selectedItems);
    }

    /**
     * remove selected delta units
     */
    @FXML
    public void removeDeltaUnit(ActionEvent actionEvent)
    {
        ObservableList<Delta> selectedItems = selectedUnitList.getSelectionModel().getSelectedItems();
        selectedUnitList.getItems().removeAll(selectedItems);
    }

    /**
     * add a custom delta entry to the final list.
     */
    @FXML
    public void addDeltaEntry(ActionEvent actionEvent)
    {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("The delta name should not be empty");
            alert.showAndWait();
            return;
        }
        String massTxt = massField.getText().trim();
        if (massTxt.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("The delta mass should not be empty");
            alert.showAndWait();
            return;
        }

        Composition composition = Composition.parseComposition(massTxt);
        Delta delta = new Delta(name, composition);
        boolean add = deltaFactory.add(delta);
        if (!add) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Delta item with the same name already exists");
            alert.showAndWait();
            return;
        }

        databaseItemList.getItems().add(delta);
        nameField.clear();
        massField.clear();
    }

    private void addDeltaEntry2Database()
    {
        ObservableList<Delta> selectedItems = buildList.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty())
            return;

        for (Delta item : selectedItems) {
            boolean add = deltaFactory.add(item);
            if (add) {
                buildList.getItems().remove(item);
            } else {
                buildList.getSelectionModel().select(item);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Delta item with name '" + item.getName() + "' already exists");
                alert.showAndWait();
                break;
            }
        }
        databaseItemList.getItems().setAll(deltaFactory.getDeltaList());
    }

    private void saveDatabaseChange()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sure to save?");
        alert.setHeaderText("Save delta to serialization folder");
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (!buttonType.isPresent() || !buttonType.get().equals(ButtonType.OK))
            return;
        deltaFactory.write();
        changed = true;
    }

    /**
     * @return true if the delta databse has been modified.
     */
    public boolean isChanged()
    {
        return changed;
    }
}
