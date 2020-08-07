package omics.gui.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import omics.gui.util.EnzymeStringConverter;
import omics.gui.util.IntegerStringConverterV2;
import omics.pdk.ident.SearchParameters;
import omics.pdk.ident.model.*;
import omics.util.chem.PeriodicTable;
import omics.util.ms.Dissociation;
import omics.util.ms.peaklist.Tolerance;
import omics.util.protein.PeptideMod;
import omics.util.protein.digest.Enzyme;
import omics.util.protein.digest.EnzymeFactory;
import omics.util.protein.digest.Protease;
import omics.util.protein.mod.PTM;
import omics.util.protein.mod.PTMFactory;
import omics.util.utils.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Pane to edit parameter file.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 09 Dec 2019, 9:36 PM
 */
public class ParameterEditorPane extends VBox
{
    private final PTMFactory ptmFactory = PTMFactory.getInstance();
    private final DeltaFactory deltaFactory = DeltaFactory.getInstance();

    @FXML
    private TextField editParameterNode;
    @FXML
    private Button saveButton;
    @FXML
    private Button openButton;
    @FXML
    private TextField fastaField;
    @FXML
    private Button chooseFastaButton;
    @FXML
    private CheckBox searchDecoyCheck;
    @FXML
    private ChoiceBox<Enzyme> enzymeNode;
    @FXML
    private ChoiceBox<Integer> missedCleavageNode;
    @FXML
    private ChoiceBox<Protease.Cleavage> cleavageModeNode;
    @FXML
    private ChoiceBox<Enzyme> enzymeNode2;
    @FXML
    private ChoiceBox<Integer> missedCleavageNode2;
    @FXML
    private ChoiceBox<Protease.Cleavage> cleavageModeNode2;
    /**
     * Consider protein N-term Met cleavage
     */
    @FXML
    private CheckBox nMChoose;
    @FXML
    private ComboBox<Integer> isoformNode;
    @FXML
    private ComboBox<Integer> maxModNode;
    @FXML
    private Spinner<Integer> minLenNode;
    @FXML
    private Spinner<Integer> maxLenNode;
    @FXML
    private ComboBox<Double> leftTolNode;
    @FXML
    private ComboBox<Double> rightTolNode;
    @FXML
    private ChoiceBox<String> parentUnitBox;
    @FXML
    private ComboBox<Double> leftFragTolNode;
    @FXML
    private ComboBox<Double> rightFragNode;
    @FXML
    private ChoiceBox<String> fragUnitNode;
    @FXML
    private Spinner<Integer> minIso;
    @FXML
    private Spinner<Integer> maxIso;
    @FXML
    private Spinner<Integer> minChargeNode;
    @FXML
    private Spinner<Integer> maxChargeNode;
    @FXML
    private ComboBox<Integer> topN;
    @FXML
    private ComboBox<Integer> minDenovoNode;
    @FXML
    private ChoiceBox<Instrument> instruNode;
    @FXML
    private ChoiceBox<Dissociation> activationNode;
    @FXML
    private ChoiceBox<Protocol> protocolNode;
    @FXML
    private ChoiceBox<Double> carrerMassNode;
    @FXML
    private CheckBox moreFeaturesNode;
    @FXML
    private CheckBox clearBButton;
    @FXML
    private CheckBox clearYButton;
    @FXML
    private ComboBox<Integer> minPeakNode;
    @FXML
    private ListView<PeptideMod> variableModificationNode;
    @FXML
    private ListView<PeptideMod> fixedModificationNode;
    @FXML
    private ListView<PeptideMod> ptmListNode;
    @FXML
    private TextField filterPTMNode;
    @FXML
    private Button refreshPTMButton;
    @FXML
    private CheckBox preferDeltaNode;
    @FXML
    private ListView<Delta> deltaList4Search;
    @FXML
    private TextField filterDeltaNode;
    @FXML
    private ListView<Delta> deltaListNode;
    @FXML
    private Button refreshDeltaListButton;

    public ParameterEditorPane()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/parameter_editor.fxml");
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
        GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
        openButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN));
        openButton.setOnAction(event -> readParam());

        saveButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.SAVE));
        saveButton.setOnAction(event -> saveParams());

        refreshPTMButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.REFRESH).color(Color.GREEN));
        refreshDeltaListButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.REFRESH).color(Color.GREEN));
        ((CustomTextField) filterPTMNode).setLeft(fontAwesome.create(FontAwesome.Glyph.FILTER).color(Color.GRAY));
        ((CustomTextField) filterDeltaNode).setLeft(fontAwesome.create(FontAwesome.Glyph.FILTER).color(Color.GRAY));

        chooseFastaButton.setGraphic(fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN_ALT));
        chooseFastaButton.setOnAction(event -> chooseFasta());

        EnzymeStringConverter enzymeConvert = new EnzymeStringConverter();
        enzymeNode.setConverter(enzymeConvert);
        enzymeNode2.setConverter(enzymeConvert);

        List<Enzyme> enzymeList = EnzymeFactory.getInstance().getEnzymeList();
        enzymeNode.getItems().addAll(enzymeList);
        enzymeNode2.getItems().addAll(enzymeList);

        Integer[] misses = new Integer[]{0, 1, 2, 3, 4, 5};
        missedCleavageNode.getItems().addAll(misses);
        missedCleavageNode2.getItems().addAll(misses);

        Protease.Cleavage[] cleavages = Protease.Cleavage.values();
        cleavageModeNode.getItems().addAll(cleavages);
        cleavageModeNode2.getItems().addAll(cleavages);

        minLenNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 40, 7));
        maxLenNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 100, 40));

        isoformNode.setConverter(new IntegerStringConverter());
        isoformNode.getItems().add(128);
        maxModNode.setConverter(new IntegerStringConverterV2());
        maxModNode.getItems().addAll(1, 2, 3, 4);

        initPeakMatching();
        initModification();
        initDeltaList();

        updateUI(new SearchParameters());
    }

    private void initPeakMatching()
    {
        parentUnitBox.getItems().addAll("ppm", "Da");
        fragUnitNode.getItems().addAll("ppm", "Da");

        DoubleStringConverter doubleStringConverter = new DoubleStringConverter();
        leftTolNode.setConverter(doubleStringConverter);
        rightTolNode.setConverter(doubleStringConverter);
        leftFragTolNode.setConverter(doubleStringConverter);
        rightFragNode.setConverter(doubleStringConverter);

        leftTolNode.getItems().addAll(5., 10., 15., 20.);
        rightTolNode.getItems().addAll(5., 10., 15., 20.);

        leftFragTolNode.getItems().addAll(0.01, 0.05, 0.1, 0.5);
        rightFragNode.getItems().addAll(0.01, 0.05, 0.1, 0.5);

        minIso.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2, 0));
        maxIso.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 4, 1));

        minChargeNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 2));
        maxChargeNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 4));

        topN.setConverter(new IntegerStringConverter());
        topN.getItems().addAll(1, 5, 10);

        minDenovoNode.setConverter(new IntegerStringConverterV2());
        minDenovoNode.getItems().addAll(Integer.MIN_VALUE, 0, 5, 10, 15, 20);

        instruNode.setConverter(new StringConverter<Instrument>()
        {
            @Override
            public String toString(Instrument object)
            {
                return object.getName();
            }

            @Override
            public Instrument fromString(String string)
            {
                return Instrument.nameOf(string);
            }
        });
        instruNode.getItems().addAll(Instrument.getAllRegisteredInstrumentTypes());

        activationNode.setConverter(new StringConverter<Dissociation>()
        {
            @Override
            public String toString(Dissociation object)
            {
                return object.getName();
            }

            @Override
            public Dissociation fromString(String string)
            {
                return ScoreParam.getDissociation(string);
            }
        });
        activationNode.getItems().addAll(ScoreParam.getAllRegisteredDissociations());

        protocolNode.setConverter(new StringConverter<Protocol>()
        {
            @Override
            public String toString(Protocol object)
            {
                return object.getName();
            }

            @Override
            public Protocol fromString(String string)
            {
                return Protocol.ofName(string);
            }
        });
        protocolNode.getItems().addAll(Protocol.getAllProtocols());

        carrerMassNode.setConverter(new StringConverter<Double>()
        {
            @Override
            public String toString(Double object)
            {
                if (object == PeriodicTable.PROTON_MASS)
                    return "H";
                if (object == 22.98922189)
                    return "Na";
                if (object == 38.96315989)
                    return "K";
                return object.toString();
            }

            @Override
            public Double fromString(String string)
            {
                return Double.parseDouble(string);
            }
        });
        carrerMassNode.getItems().addAll(PeriodicTable.PROTON_MASS, 22.98922189, 38.96315989);

        minPeakNode.getItems().addAll(10, 15, 20);
    }

    private final ObservableList<PeptideMod> peptideModList = FXCollections.observableArrayList();

    private void initModification()
    {
        this.ptmListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.variableModificationNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.fixedModificationNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.ptmListNode.setItems(peptideModList);
        refreshPTM();
        this.refreshPTMButton.setOnAction(event -> refreshPTM());

        this.filterPTMNode.setMaxWidth(160);
        filterPTMNode.textProperty().addListener((observable, oldValue, newValue) -> {
            if ((oldValue != null && (newValue.length() < oldValue.length())) || StringUtils.isEmpty(newValue)) {
                ptmListNode.setItems(peptideModList);
            } else {
                ObservableList<PeptideMod> subEntries = FXCollections.observableArrayList();
                String s = newValue.trim().toUpperCase();
                for (PeptideMod item : ptmListNode.getItems()) {
                    if (item.toString().toUpperCase().contains(s)) {
                        subEntries.add(item);
                    }
                }
                ptmListNode.setItems(subEntries);
            }
        });
    }

    private void refreshPTM()
    {
        for (PTM ptm : ptmFactory.getPTMList()) {
            peptideModList.addAll(ptm.getPeptideModList());
        }
        peptideModList.sort(Comparator.comparing(PeptideMod::toString));
    }

    private final ObservableList<Delta> deltaList = FXCollections.observableArrayList();

    private void initDeltaList()
    {
        this.deltaListNode.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.deltaListNode.setItems(deltaList);

        this.deltaList4Search.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.deltaList.addAll(deltaFactory.getDeltaList());
        this.deltaList.sort(Comparator.comparing(Delta::getName));
        refreshDeltaListButton.setOnAction(event -> {
            deltaList.clear();
            deltaList.addAll(deltaFactory.getDeltaList());
            this.deltaList.sort(Comparator.comparing(Delta::getName));
        });

        this.filterDeltaNode.setMaxWidth(160);
        filterDeltaNode.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                if (StringUtils.isEmpty(newValue)) {
                    deltaListNode.setItems(deltaList);
                } else {
                    ObservableList<Delta> subEntries = FXCollections.observableArrayList();
                    String s = newValue.trim().toUpperCase();
                    for (Delta item : deltaListNode.getItems()) {
                        if (item.getName().toUpperCase().contains(s)) {
                            subEntries.add(item);
                        }
                    }
                    deltaListNode.setItems(subEntries);
                }
            }
        });
    }

    /**
     * update the Parameter editor page
     */
    private void updateUI(SearchParameters parameters)
    {
        Path databaseFile = parameters.getDatabase();
        if (databaseFile != null) {
            fastaField.setText(databaseFile.toString());
        }
        searchDecoyCheck.setSelected(parameters.isUseTDA());

        Protease protease = parameters.getProtease();
        enzymeNode.setValue(protease.getEnzyme());
        missedCleavageNode.setValue(protease.getMaxMissedCleavage());
        cleavageModeNode.getSelectionModel().select(protease.getCleavage());

        Optional<Protease> secondProtease = parameters.getSecondProtease();
        if (secondProtease.isPresent()) {
            Protease p2 = secondProtease.get();
            enzymeNode2.setValue(p2.getEnzyme());
            missedCleavageNode2.setValue(p2.getMaxMissedCleavage());
            cleavageModeNode2.getSelectionModel().select(p2.getCleavage());
        }

        nMChoose.setSelected(parameters.isRemoveProteinNTermM());
        isoformNode.setValue(parameters.getMaxNrVariantsPerPeptide());
        maxModNode.setValue(parameters.getMaxModsPerPeptide());

        minLenNode.getValueFactory().setValue(parameters.getMinPeptideLength());
        maxLenNode.getValueFactory().setValue(parameters.getMaxPeptideLength());

        Tolerance parentTolerance = parameters.getPrecursorTolerance();
        Tolerance fragTolerance = parameters.getFragmentTolerance();
        if (!parentTolerance.isAbsolute()) {
            parentUnitBox.getSelectionModel().select(0);
        } else {
            parentUnitBox.getSelectionModel().select(1);
        }
        if (!fragTolerance.isAbsolute()) {
            fragUnitNode.getSelectionModel().select(0);
        } else {
            fragUnitNode.getSelectionModel().select(1);
        }

        leftTolNode.setValue(parentTolerance.getMinusError());
        rightTolNode.setValue(parentTolerance.getPlusError());
        leftFragTolNode.setValue(fragTolerance.getMinusError());
        rightFragNode.setValue(fragTolerance.getPlusError());

        minIso.getValueFactory().setValue(parameters.getMinIsotopeError());
        maxIso.getValueFactory().setValue(parameters.getMaxIsotopeError());

        minChargeNode.getValueFactory().setValue(parameters.getMinCharge());
        maxChargeNode.getValueFactory().setValue(parameters.getMaxCharge());

        topN.setValue(parameters.getNumberMatchesPerSpectrum());
        minDenovoNode.setValue(parameters.getMinDeNovoScore());
        minPeakNode.setValue(parameters.getMinNumberPeaksPerSpectrum());
        clearBButton.setSelected(parameters.isRemoveOxonium());
        clearYButton.setSelected(parameters.isRemoveGlycoPeptideIon());

        instruNode.setValue(parameters.getInstrument());
        activationNode.setValue(parameters.getDissociation());
        protocolNode.setValue(parameters.getProtocol());
        carrerMassNode.setValue(parameters.getChargeCarrierMass());

        moreFeaturesNode.setSelected(parameters.addMoreFeatures());
        preferDeltaNode.setSelected(parameters.isPreferDelta());

        Set<Delta> deltaEntries = parameters.getDeltaSet();
        deltaList4Search.getItems().addAll(deltaEntries);

        List<PeptideMod> modidifcationList = parameters.getModidifcationList();
        for (PeptideMod peptideMod : modidifcationList) {
            if (peptideMod.isFixed()) {
                this.fixedModificationNode.getItems().add(peptideMod);
            } else {
                this.variableModificationNode.getItems().add(peptideMod);
            }
        }
    }

    /**
     * choose fasta file.
     */
    private void chooseFasta()
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Fasta File");

        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            this.fastaField.setText(file.getAbsolutePath());
        }
    }

    private void showAlert(Alert.AlertType type, String msg)
    {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    /**
     * Save parameters to a file
     */
    private void saveParams()
    {
        if (minLenNode.getValue() > maxLenNode.getValue()) {
            showAlert(Alert.AlertType.ERROR, "Max peptide length must be larger than min peptide length");
            minLenNode.requestFocus();
            return;
        }

        if (minChargeNode.getValue() > maxChargeNode.getValue()) {
            showAlert(Alert.AlertType.ERROR, "Min charge must not be larger than max charge");
            minChargeNode.requestFocus();
            return;
        }

        Enzyme firstEnzyme = enzymeNode.getSelectionModel().getSelectedItem();
        if (firstEnzyme == null) {
            showAlert(Alert.AlertType.WARNING, "Please select an Enzyme");
            enzymeNode.requestFocus();
            return;
        }

        SearchParameters parameters = new SearchParameters();

        Path fastaPath = Paths.get(fastaField.getText());
        parameters.setDatabase(fastaPath);
        parameters.setUseTDA(searchDecoyCheck.isSelected());

        parameters.setProtease(new Protease(firstEnzyme, missedCleavageNode.getSelectionModel().getSelectedItem(),
                cleavageModeNode.getSelectionModel().getSelectedItem()));
        if (!enzymeNode2.getSelectionModel().isEmpty()) {
            parameters.setSecondProtease(new Protease(enzymeNode2.getSelectionModel().getSelectedItem(),
                    missedCleavageNode2.getSelectionModel().getSelectedItem(), cleavageModeNode2.getSelectionModel().getSelectedItem()));
        }

        parameters.setRemoveProteinNTermM(nMChoose.isSelected());
        parameters.setMaxNrVariantsPerPeptide(isoformNode.getValue());
        parameters.setMaxModsPerPeptide(maxModNode.getValue());
        parameters.setMinPeptideLength(minLenNode.getValue());
        parameters.setMaxPeptideLength(maxLenNode.getValue());

        double leftTol = leftTolNode.getValue();
        double rightTol = rightTolNode.getValue();
        Tolerance parentTol;
        int parentUnit = parentUnitBox.getSelectionModel().getSelectedIndex();
        if (parentUnit == 0)
            parentTol = Tolerance.ppm(leftTol, rightTol);
        else
            parentTol = Tolerance.abs(leftTol, rightTol);
        parameters.setPrecursorTolerance(parentTol);

        double fragLeftTol = leftFragTolNode.getValue();
        double fragRightTol = rightFragNode.getValue();
        Tolerance fragTol;
        if (fragUnitNode.getSelectionModel().getSelectedIndex() == 0)
            fragTol = Tolerance.ppm(fragLeftTol, fragRightTol);
        else
            fragTol = Tolerance.abs(fragLeftTol, fragRightTol);
        parameters.setFragmentTolerance(fragTol);

        parameters.setMinIsotopeError(minIso.getValue());
        parameters.setMaxIsotopeError(maxIso.getValue());

        parameters.setMinCharge(minChargeNode.getValue());
        parameters.setMaxCharge(maxChargeNode.getValue());

        parameters.setNumberMatchesPerSpectrum(topN.getSelectionModel().getSelectedItem());
        parameters.setMinDeNovoScore(minDenovoNode.getValue());

        parameters.setInstrument(instruNode.getSelectionModel().getSelectedItem());
        parameters.setDissociation(activationNode.getSelectionModel().getSelectedItem());
        parameters.setProtocol(protocolNode.getSelectionModel().getSelectedItem());
        parameters.setChargeCarrierMass(carrerMassNode.getSelectionModel().getSelectedItem());
        parameters.setAddMoreFeatures(moreFeaturesNode.isSelected());

        parameters.setMinNumberPeaksPerSpectrum(minPeakNode.getSelectionModel().getSelectedItem());

        for (PeptideMod item : variableModificationNode.getItems())
            parameters.addPeptideMod(item);
        for (PeptideMod item : fixedModificationNode.getItems())
            parameters.addPeptideMod(PeptideMod.of(item.getModification(), item.getSpecificity(), true));

        parameters.setPreferDelta(preferDeltaNode.isSelected());
        parameters.setRemoveOxonium(clearBButton.isSelected());
        parameters.setRemoveGlycopep(clearYButton.isSelected());
        for (Delta delta : deltaList4Search.getItems()) {
            parameters.addDelta(delta);
        }

        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("standard.pcf");

        String absPath = editParameterNode.getText();
        if (StringUtils.isNotEmpty(absPath)) {
            Path path = Paths.get(absPath).getParent();
            if (Files.exists(path)) {
                chooser.setInitialDirectory(path.toFile());
            }
        }

        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            String outPath = file.getAbsolutePath();
            editParameterNode.setText(outPath);
            parameters.write(outPath);
        }
    }

    /**
     * read parameters file show in the parameter editor
     */
    @FXML
    private void readParam()
    {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Proteomics configuration file", "*.pcf"),
                new FileChooser.ExtensionFilter("All", "*.*"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            String path = file.getAbsolutePath();
            editParameterNode.setText(path);

            try {
                SearchParameters parameter = SearchParameters.getParameter(path);
                updateUI(parameter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void removeVariableMod()
    {
        this.variableModificationNode.getItems().removeAll(variableModificationNode.getSelectionModel().getSelectedItems());
    }

    @FXML
    private void addVariableMod()
    {
        ObservableList<PeptideMod> selectedItems = this.ptmListNode.getSelectionModel().getSelectedItems();
        for (PeptideMod selectedItem : selectedItems) {
            if (!variableModificationNode.getItems().contains(selectedItem)) {
                this.variableModificationNode.getItems().add(selectedItem);
            }
        }
    }

    @FXML
    private void removeFixedMod()
    {
        fixedModificationNode.getItems().removeAll(fixedModificationNode.getSelectionModel().getSelectedItems());
    }

    @FXML
    private void addFixedMod()
    {
        ObservableList<PeptideMod> selectedItems = ptmListNode.getSelectionModel().getSelectedItems();
        for (PeptideMod selectedItem : selectedItems) {
            if (!fixedModificationNode.getItems().contains(selectedItem)) {
                this.fixedModificationNode.getItems().add(selectedItem);
            }
        }
    }

    @FXML
    private void removeDelta()
    {
        this.deltaList4Search.getItems().removeAll(deltaList4Search.getSelectionModel().getSelectedItems());
    }

    @FXML
    private void selectDelta()
    {
        for (Delta selectedItem : deltaListNode.getSelectionModel().getSelectedItems()) {
            if (!deltaList4Search.getItems().contains(selectedItem)) {
                deltaList4Search.getItems().add(selectedItem);
            }
        }
    }
}
