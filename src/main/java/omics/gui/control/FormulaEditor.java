package omics.gui.control;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import omics.util.chem.Atom;
import omics.util.chem.AtomicSymbol;
import omics.util.chem.Composition;
import omics.util.chem.PeriodicTable;
import omics.util.utils.NumberFormatFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Node to edit chemical formula.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 03 Dec 2019, 8:48 AM
 */
public class FormulaEditor extends GridPane
{
    private final PeriodicTable periodicTable = PeriodicTable.getInstance();

    @FXML
    private ComboBox<Atom> atomNode;

    @FXML
    private Spinner<Integer> atomCountNode;
    @FXML
    private Label massNode;
    @FXML
    private TextField compositionNode;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button okBtn;

    private final Object2IntMap<Atom> atomMap = new Object2IntOpenHashMap<>();

    public FormulaEditor()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/formula_editor.fxml");
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
        List<Atom> atomList = new ArrayList<>();
        for (AtomicSymbol value : AtomicSymbol.values()) {
            Atom atom = periodicTable.getAtom(value);
            if (atom != null)
                atomList.add(atom);
        }

        atomList.add(PeriodicTable.H2);
        atomList.add(periodicTable.getAtom(AtomicSymbol.H, 3));
        atomList.add(PeriodicTable.C13);
        atomList.add(periodicTable.getAtom(AtomicSymbol.N, 15));
        atomList.add(periodicTable.getAtom(AtomicSymbol.S, 33));
        atomList.add(periodicTable.getAtom(AtomicSymbol.S, 34));

        atomList.sort(Comparator.comparingInt(Atom::getNucleonCount).thenComparingInt(Atom::getNeutronCount));
        atomNode.getItems().addAll(atomList);
        atomNode.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> atomCountNode.getValueFactory().setValue(0));

        atomCountNode.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-25, 250, 0));
        compositionNode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                massNode.setText("");
                atomMap.clear();
            }
        });
    }

    public Button cancelButton()
    {
        return cancelBtn;
    }

    public Button okButton()
    {
        return okBtn;
    }

    public TextField compositionNode()
    {
        return compositionNode;
    }

    public Label getMassNode()
    {
        return massNode;
    }

    @FXML
    private void addAtom()
    {
        Atom atom = atomNode.getSelectionModel().getSelectedItem();
        int value = atomCountNode.getValue();
        if (value != 0) {
            atomMap.put(atom, value);
            Composition comp = new Composition(atomMap, 0);
            compositionNode.setText(comp.toString());
            massNode.setText(NumberFormatFactory.HIGH_MASS_PRECISION.format(comp.getMolecularMass()));
        }
    }
}
