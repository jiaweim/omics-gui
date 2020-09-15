package omics.gui.setting;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;
import omics.gui.Setting;
import omics.gui.SettingView;
import omics.gui.ShowAlert;
import omics.gui.control.FormulaEditor;
import omics.pdk.ident.model.Delta;
import omics.pdk.ptm.glyco.GlycanComposition;
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
public class GapEditor extends GridPane implements ShowAlert, SettingView
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
    private CheckBox isGlycanCheck;
    @FXML
    private Button saveButton;
    @FXML
    private Button cleanButton;
    @FXML
    private Button buildButton;
    @FXML
    private Button addCombinationButton;

    private boolean changed = false;

    public GapEditor()
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

    private GapSetting gapSetting;

    @FXML
    public void initialize()
    {
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        saveButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.SAVE).color(Color.BLACK));
        saveButton.setOnAction(event -> saveDatabaseChange());

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

        selectedUnitList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        buildList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        buildButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.PLAY).color(Color.GREEN));
        Tooltip.install(buildButton, new Tooltip("Run gap combination"));
        buildButton.setOnAction(event -> build());

        maxUnitCount.setConverter(new IntegerStringConverter());
        maxUnitCount.getItems().addAll(1, 2, 3, 4, 5);
        maxUnitCount.getSelectionModel().select(2);


        cleanButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILTER));
        Tooltip.install(cleanButton, new Tooltip("Remove items that have already in the database."));
        cleanButton.setOnAction(event -> removeDuplicates());

        addCombinationButton.setOnAction(event -> addDeltaEntry2Database());
    }

    private void removeDuplicates()
    {
        ObservableList<Delta> items = buildList.getItems();
        List<Delta> duplicateItems = new ArrayList<>();
        for (Delta item : items) {
            if (gapSetting.contains(item)) {
                duplicateItems.add(item);
            }
        }
        buildList.getItems().removeAll(duplicateItems);
    }

    /**
     * calculate the combinations of delta units.
     */
    private void build()
    {
        if (selectedUnitList.getItems().isEmpty())
            return;

        Integer countValue = maxUnitCount.getValue();
        List<Delta> glycanCombinations = Delta.getCombinations(selectedUnitList.getItems(), countValue);
        buildList.getItems().setAll(glycanCombinations);
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
            showAlert(Alert.AlertType.ERROR, "The name is empty");
            return;
        }
        String massTxt = massField.getText().trim();
        if (massTxt.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "The mass is empty");
            return;
        }

        Composition composition = Composition.parseComposition(massTxt);
        Delta delta;
        if (isGlycanCheck.isSelected()) {
            GlycanComposition gc = GlycanComposition.fromName(name);
            if (!gc.getComposition().equals(composition)) {
                showAlert(Alert.AlertType.ERROR, "The mass parsed from name is not equal to the formula," +
                        "make sure the glycan name is in right format");
                return;
            }
            delta = gc;
        } else {
            delta = new Delta(name, composition);
        }

        boolean add = gapSetting.add(delta);
        if (!add) {
            showAlert(Alert.AlertType.ERROR, "Gap with the same name already exists");
            return;
        }

        databaseItemList.getItems().add(delta);
        nameField.clear();
        massField.clear();
        isGlycanCheck.setSelected(false);
        changed = true;
    }

    private void addDeltaEntry2Database()
    {
        ObservableList<Delta> selectedItems = buildList.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty())
            return;

        for (Delta item : selectedItems) {
            boolean add = gapSetting.add(item);
            if (add) {
                buildList.getItems().remove(item);
                databaseItemList.getItems().add(item);
                changed = true;
            } else {
                buildList.getSelectionModel().select(item);
                showAlert(Alert.AlertType.ERROR, " Gap with same '" + item.getName() + "' already exists");
                break;
            }
        }
    }

    private void saveDatabaseChange()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sure to save?");
        alert.setHeaderText("Save gaps to serialization folder");
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isEmpty() || !buttonType.get().equals(ButtonType.OK))
            return;

        if (changed) {
            gapSetting.save();
            changed = false;
        }
    }

    @Override
    public void updateSetting(Setting setting)
    {
        if (changed) {
            gapSetting.save();
            changed = false;
        }
    }

    @Override
    public void updateView(Setting setting)
    {
        this.gapSetting = (GapSetting) setting;
        databaseItemList.getItems().setAll(gapSetting.getDeltaList());
    }
}
