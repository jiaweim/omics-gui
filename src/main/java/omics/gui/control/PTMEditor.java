package omics.gui.control;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import omics.gui.psm.util.NodeUtils;
import omics.util.chem.Composition;
import omics.util.protein.AminoAcid;
import omics.util.protein.mod.*;
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.StringUtils;
import omics.util.utils.SystemUtils;
import org.controlsfx.control.PopOver;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Pane to display PTM.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 03 Dec 2019, 1:53 PM
 */
public class PTMEditor extends SplitPane
{
    private final PTMFactory ptmFactory = PTMFactory.getInstance();

    @FXML
    private TableView<PTM> ptmTableNode;
    @FXML
    private TextField nameNode;
    @FXML
    private TextField descriptionNode;
    @FXML
    private TextField compositionNode;
    @FXML
    private Label massNode;
    @FXML
    private TableView<Specificity> specificityTable;
    @FXML
    private Button saveBtn;
    @FXML
    private ChoiceBox<AminoAcid> spAANode;
    @FXML
    private ChoiceBox<Pos> spPosNode;
    @FXML
    private TextField spNLNameNode;
    @FXML
    private TextField spNLShortNameNode;
    @FXML
    private TextField spNLCompositionNode;

    public PTMEditor()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/ptm_editor.fxml");
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
        initPTMTable();
        initSpecificityEdit();
        initSpecificityTable();

        TableView.TableViewSelectionModel<PTM> selectionModel = ptmTableNode.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                nameNode.setText(newValue.getTitle());
                descriptionNode.setText(newValue.getFullName());
                compositionNode.setText(newValue.getComposition().getFormula());
                massNode.setText(NumberFormatFactory.HIGH_MASS_PRECISION.format(newValue.getMolecularMass()));
                specificityTable.getItems().setAll(newValue.getSpecificityList());
            } else {
                clearInfo();
            }
        });

        compositionNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
                FormulaEditor formulaNode = new FormulaEditor();
                PopOver popOver = new PopOver(formulaNode);
                popOver.setTitle("Composition Editor");
                formulaNode.okButton().setOnAction(event -> {
                    TextField textField = formulaNode.compositionNode();
                    String text = textField.getText();
                    if (StringUtils.isNotEmpty(text)) {
                        compositionNode.setText(text);
                        massNode.setText(formulaNode.getMassNode().getText());
                    }
                    popOver.hide();
                });

                formulaNode.cancelButton().setOnAction(event -> popOver.hide());
                popOver.show(compositionNode);
            }
        });

        saveBtn.setTooltip(new Tooltip("WARN: Save modifications to the underlying configuration file is an irreversible operation"));
    }

    private void initPTMTable()
    {
        NodeUtils.addIndexColumn(ptmTableNode);

        TableColumn<PTM, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getTitle()));

        TableColumn<PTM, String> despCol = new TableColumn<>("Description");
        despCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getFullName()));

        TableColumn<PTM, String> compCol = new TableColumn<>("Composition");
        compCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getComposition().getFormula()));

        TableColumn<PTM, Double> massCol = new TableColumn<>("Mass");
        massCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMolecularMass()));
        massCol.setCellFactory(new Callback<TableColumn<PTM, Double>, TableCell<PTM, Double>>()
        {
            @Override
            public TableCell<PTM, Double> call(TableColumn<PTM, Double> param)
            {
                return new TableCell<PTM, Double>()
                {
                    @Override
                    protected void updateItem(Double item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            setText(NumberFormatFactory.HIGH_MASS_PRECISION.format(item));
                        }
                    }
                };
            }
        });

        TableColumn<PTM, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getUserName()));

        ptmTableNode.getColumns().addAll(nameCol, despCol, compCol, massCol, userCol);
        ptmTableNode.getItems().addAll(ptmFactory.getPTMList());
    }

    private void initSpecificityEdit()
    {
        List<AminoAcid> aas = new ArrayList<>();
        aas.add(AminoAcid.N_TERM);
        aas.addAll(Arrays.asList(AminoAcid.getStandardAminoAcids()));
        aas.add(AminoAcid.C_TERM);
        Map<String, AminoAcid> aaMap = new HashMap<>(aas.size());
        for (AminoAcid aa : aas) {
            aaMap.put(aa.getShortName(), aa);
        }
        spAANode.getItems().addAll(aas);
        spAANode.setConverter(new StringConverter<AminoAcid>()
        {
            @Override
            public String toString(AminoAcid object)
            {
                return object.getShortName();
            }

            @Override
            public AminoAcid fromString(String string)
            {
                return aaMap.get(string);
            }
        });

        spPosNode.getItems().addAll(Pos.values());

        spNLCompositionNode.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                FormulaEditor formulaNode = new FormulaEditor();
                PopOver popOver = new PopOver(formulaNode);
                popOver.setTitle("Composition Editor");
                formulaNode.okButton().setOnAction(event -> {
                    TextField textField = formulaNode.compositionNode();
                    String text = textField.getText();
                    if (StringUtils.isNotEmpty(text)) {
                        spNLCompositionNode.setText(text);
                    }
                    popOver.hide();
                });

                formulaNode.cancelButton().setOnAction(event -> popOver.hide());
                popOver.show(spNLCompositionNode);
            }
        });
    }

    private void initSpecificityTable()
    {
        TableColumn<Specificity, String> aaCol = new TableColumn<>("Amino Acid");
        aaCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAminoAcid().getShortName()));

        TableColumn<Specificity, Pos> posCol = new TableColumn<>("Position");
        posCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getPosition()));

        TableColumn<Specificity, String> nlCol = new TableColumn<>("Neutral Loss");
        nlCol.setCellValueFactory(param -> param.getValue().getNeutralLoss()
                .<ObservableValue<String>>map(loss -> new SimpleObjectProperty<>(loss.getName())).orElse(null));

        TableColumn<Specificity, String> nlShortNameCol = new TableColumn<>("Short Name");
        nlShortNameCol.setCellValueFactory(param -> param.getValue().getNeutralLoss()
                .<ObservableValue<String>>map(loss -> new SimpleObjectProperty<>(loss.getSymbol())).orElse(null));

        TableColumn<Specificity, Composition> nlCompCol = new TableColumn<>("Composition");
        nlCompCol.setCellValueFactory(param -> param.getValue().getNeutralLoss()
                .<ObservableValue<Composition>>map(loss -> new SimpleObjectProperty<>(loss.getComposition())).orElse(null));

        TableColumn<Specificity, Double> nlMassCol = new TableColumn<>("Mass");
        nlMassCol.setCellFactory(new Callback<TableColumn<Specificity, Double>, TableCell<Specificity, Double>>()
        {
            @Override
            public TableCell<Specificity, Double> call(TableColumn<Specificity, Double> param)
            {
                return new TableCell<Specificity, Double>()
                {
                    @Override
                    protected void updateItem(Double item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(NumberFormatFactory.HIGH_MASS_PRECISION.format(item));
                        }
                    }
                };
            }
        });
        nlMassCol.setCellValueFactory(param -> param.getValue().getNeutralLoss()
                .<ObservableValue<Double>>map(loss -> new SimpleObjectProperty<>(loss.getMolecularMass())).orElse(null));

        aaCol.setSortable(false);
        posCol.setSortable(false);
        nlCol.setSortable(false);
        nlShortNameCol.setSortable(false);
        nlCompCol.setSortable(false);
        nlMassCol.setSortable(false);
        specificityTable.getColumns().addAll(aaCol, posCol, nlCol, nlShortNameCol, nlCompCol, nlMassCol);
    }

    @FXML
    private void newPTM()
    {
        ptmTableNode.getSelectionModel().clearSelection();
        clearInfo();
        clearNLEditor();
    }

    @FXML
    private void delPTM()
    {
        TableView.TableViewSelectionModel<PTM> selectionModel = ptmTableNode.getSelectionModel();
        if (selectionModel.isEmpty())
            return;

        ptmFactory.remove(selectionModel.getSelectedItem());
        ptmTableNode.getItems().remove(selectionModel.getSelectedIndex());
        selectionModel.clearSelection();
    }

    private void clearInfo()
    {
        nameNode.clear();
        descriptionNode.clear();
        compositionNode.clear();
        massNode.setText("");
        specificityTable.getItems().clear();
    }

    private void clearNLEditor()
    {
        spAANode.getSelectionModel().clearSelection();
        spPosNode.getSelectionModel().clearSelection();
        spNLNameNode.clear();
        spNLShortNameNode.clear();
        spNLCompositionNode.clear();
    }


    @FXML
    private void addPTM()
    {
        String ptmName = nameNode.getText();
        if (StringUtils.isEmpty(ptmName)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Modification name is not defined!");
            alert.showAndWait();
            return;
        }
        if (ptmFactory.containPTM(ptmName)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Modification of name '"
                    + ptmName + "' is already exist!");
            alert.showAndWait();
            return;
        }

        String description = descriptionNode.getText();
        if (StringUtils.isEmpty(description)) {
            description = ptmName;
        }

        String formula = compositionNode.getText();
        if (StringUtils.isEmpty(formula)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "The composition is not defined!");
            alert.showAndWait();
            return;
        }

        Composition composition;
        try {
            composition = Composition.parseComposition(formula);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "The composition is not right, making sure to define it in the composition editor window!");
            alert.showAndWait();
            return;
        }

        ObservableList<Specificity> items = specificityTable.getItems();
        if (items.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Specificity is not defined!");
            alert.showAndWait();
            return;
        }

        PTM ptm = new PTM(ptmName, description, composition, new ArrayList<>(items), SystemUtils.USER_NAME);
        ptmFactory.add(ptm);
        ptmTableNode.getItems().add(ptm);
        ptmTableNode.getSelectionModel().select(ptm);
    }

    @FXML
    private void savePTM()
    {
        ptmFactory.write();
    }

    @FXML
    private void delSpecificity()
    {
        TableView.TableViewSelectionModel<Specificity> spModel = specificityTable.getSelectionModel();
        if (spModel.isEmpty())
            return;

        Specificity specificity = spModel.getSelectedItem();
        TableView.TableViewSelectionModel<PTM> aaModel = ptmTableNode.getSelectionModel();
        if (!aaModel.isEmpty()) {
            PTM ptm = aaModel.getSelectedItem();
            ptm.getSpecificityList().remove(specificity);
        }
        specificityTable.getItems().remove(spModel.getSelectedIndex());
    }

    @FXML
    private void addSpecificity()
    {
        SingleSelectionModel<AminoAcid> aaModel = spAANode.getSelectionModel();
        if (aaModel.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Select an amino acid first");
            alert.showAndWait();
            return;
        }
        SingleSelectionModel<Pos> posModel = spPosNode.getSelectionModel();
        if (posModel.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Select a pos first");
            alert.showAndWait();
            return;
        }

        NeutralLoss neutralLoss = null;
        String nlCompo = spNLCompositionNode.getText();
        if (StringUtils.isNotEmpty(nlCompo)) {
            Composition nlComposition = Composition.parseComposition(nlCompo);
            String nlName = spNLNameNode.getText();
            if (StringUtils.isEmpty(nlName)) {
                nlName = nlComposition.getFormula();
            }
            String nlShortName = spNLShortNameNode.getText();
            if (StringUtils.isEmpty(nlShortName)) {
                nlShortName = nlComposition.getFormula();
            }
            neutralLoss = new NeutralLoss(nlName, nlShortName, nlComposition);
        }
        Specificity specificity = new Specificity(aaModel.getSelectedItem(), posModel.getSelectedItem(), neutralLoss);
        specificityTable.getItems().add(specificity);
        TableView.TableViewSelectionModel<PTM> ptmModel = ptmTableNode.getSelectionModel();
        if (ptmModel.isEmpty())
            return;

        PTM selectedItem = ptmModel.getSelectedItem();
        selectedItem.addSpecificity(specificity);
    }

    @FXML
    private void savePTMToFile() throws IOException, XMLStreamException
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save modifications");
        fileChooser.setInitialDirectory(new File("C:\\"));
        fileChooser.setInitialFileName("mods.xml");
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            ptmFactory.write(file.toPath());
        }
    }
}
