package omics.gui.control;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import omics.gui.ShowAlert;
import omics.gui.psm.PSMViewSettings;
import omics.gui.util.DoubleStringConverter2;
import omics.gui.util.IntegerStringConverterV2;
import omics.util.ms.peaklist.Tolerance;
import omics.util.protein.mod.NeutralLoss;
import omics.util.protein.ms.Ion;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 01 Sep 2020, 2:57 PM
 */
public class IonTypePane extends GridPane implements ShowAlert
{
    @FXML
    private ComboBox<Double> leftTolBox;
    @FXML
    private ComboBox<Double> rightTolBox;
    @FXML
    private ChoiceBox<String> tolUnitBox;
    @FXML
    private ToggleGroup match;
    @FXML
    private RadioButton mostIntenseCheck;
    @FXML
    private RadioButton nearestCheck;
    @FXML
    private CheckBox deisotopeCheck;
    @FXML
    private ComboBox<Double> deisoLeftTolBox;
    @FXML
    private ComboBox<Double> deisoRightTolBox;
    @FXML
    private ChoiceBox<String> deisoTolUnitBox;
    @FXML
    private CheckBox filterPeakCheck;
    @FXML
    private ComboBox<Integer> peakCountBox;
    @FXML
    private ComboBox<Double> binWidthBox;

    @FXML
    private CheckBox aCheck;
    @FXML
    private CheckBox bCheck;
    @FXML
    private CheckBox cCheck;
    @FXML
    private CheckBox xCheck;
    @FXML
    private CheckBox yCheck;
    @FXML
    private CheckBox zCheck;
    @FXML
    private CheckBox pCheck;
    @FXML
    private CheckBox yaCheck;
    @FXML
    private CheckBox ybCheck;
    @FXML
    private CheckBox imCheck;
    @FXML
    private CheckBox bgCheck;
    @FXML
    private CheckBox ygCheck;
    @FXML
    private CheckBox ncoreCheck;
    @FXML
    private CheckBox bnCheck;
    @FXML
    private CheckBox ynCheck;

    @FXML
    private ComboBox<Integer> aMinZ;
    @FXML
    private ComboBox<Integer> bMinZ;
    @FXML
    private ComboBox<Integer> cMinZ;
    @FXML
    private ComboBox<Integer> xMinZ;
    @FXML
    private ComboBox<Integer> yMinZ;
    @FXML
    private ComboBox<Integer> zMinZ;
    @FXML
    private ComboBox<Integer> pMinZ;
    @FXML
    private ComboBox<Integer> yaMinZ;
    @FXML
    private ComboBox<Integer> ybMinZ;
    @FXML
    private ComboBox<Integer> imMinZ;
    @FXML
    private ComboBox<Integer> bgMinZ;
    @FXML
    private ComboBox<Integer> ygMinZ;
    @FXML
    private ComboBox<Integer> ncoreMinZ;
    @FXML
    private ComboBox<Integer> bnMinZ;
    @FXML
    private ComboBox<Integer> ynMinZ;

    @FXML
    private ComboBox<Integer> aMaxZ;
    @FXML
    private ComboBox<Integer> bMaxZ;
    @FXML
    private ComboBox<Integer> cMaxZ;
    @FXML
    private ComboBox<Integer> xMaxZ;
    @FXML
    private ComboBox<Integer> yMaxZ;
    @FXML
    private ComboBox<Integer> zMaxZ;
    @FXML
    private ComboBox<Integer> pMaxZ;
    @FXML
    private ComboBox<Integer> yaMaxZ;
    @FXML
    private ComboBox<Integer> ybMaxZ;
    @FXML
    private ComboBox<Integer> imMaxZ;
    @FXML
    private ComboBox<Integer> bgMaxZ;
    @FXML
    private ComboBox<Integer> ygMaxZ;
    @FXML
    private ComboBox<Integer> ncoreMaxZ;
    @FXML
    private ComboBox<Integer> bnMaxZ;
    @FXML
    private ComboBox<Integer> ynMaxZ;

    @FXML
    private ColorPicker aColor;
    @FXML
    private ColorPicker bColor;
    @FXML
    private ColorPicker cColor;
    @FXML
    private ColorPicker xColor;
    @FXML
    private ColorPicker yColor;
    @FXML
    private ColorPicker zColor;
    @FXML
    private ColorPicker pColor;
    @FXML
    private ColorPicker yaColor;
    @FXML
    private ColorPicker ybColor;
    @FXML
    private ColorPicker imColor;
    @FXML
    private ColorPicker bgColor;
    @FXML
    private ColorPicker ygColor;
    @FXML
    private ColorPicker ncoreColor;
    @FXML
    private ColorPicker bnColor;
    @FXML
    private ColorPicker ynColor;

    @FXML
    private CheckBox aH2OCheck;
    @FXML
    private CheckBox bH2OCheck;
    @FXML
    private CheckBox cH2OCheck;
    @FXML
    private CheckBox xH2OCheck;
    @FXML
    private CheckBox yH2OCheck;
    @FXML
    private CheckBox zH2OCheck;
    @FXML
    private CheckBox pH2OCheck;
    @FXML
    private CheckBox yaH2OCheck;
    @FXML
    private CheckBox ybH2OCheck;

    @FXML
    private CheckBox aNH3Check;
    @FXML
    private CheckBox bNH3Check;
    @FXML
    private CheckBox cNH3Check;
    @FXML
    private CheckBox xNH3Check;
    @FXML
    private CheckBox yNH3Check;
    @FXML
    private CheckBox zNH3Check;
    @FXML
    private CheckBox pNH3Check;
    @FXML
    private CheckBox yaNH3Check;
    @FXML
    private CheckBox ybNH3Check;

    @FXML
    private CheckBox aH3PCheck;
    @FXML
    private CheckBox bH3PCheck;
    @FXML
    private CheckBox cH3PCheck;
    @FXML
    private CheckBox xH3PCheck;
    @FXML
    private CheckBox yH3PCheck;
    @FXML
    private CheckBox zH3PCheck;
    @FXML
    private CheckBox pH3PCheck;
    @FXML
    private CheckBox yaH3PCheck;
    @FXML
    private CheckBox ybH3PCheck;

    public IonTypePane()
    {
        URL fxmlUrl = getClass().getClassLoader().getResource("fxml/ion_pane.fxml");
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
        DoubleStringConverter2 doubleConverter = new DoubleStringConverter2();
        IntegerStringConverterV2 intConverter = new IntegerStringConverterV2("Auto", Integer.MAX_VALUE);

        leftTolBox.setConverter(doubleConverter);
        rightTolBox.setConverter(doubleConverter);
        tolUnitBox.getItems().addAll("ppm", "Da");

        deisoLeftTolBox.setConverter(doubleConverter);
        deisoRightTolBox.setConverter(doubleConverter);
        deisoTolUnitBox.getItems().addAll("ppm", "Da");

        peakCountBox.getItems().addAll(1, 5, 10, 15, 20);
        peakCountBox.setConverter(intConverter);
        binWidthBox.getItems().addAll(50., 100., 150., 200.);
        binWidthBox.setConverter(doubleConverter);

        List<Integer> charges = new ArrayList<>();
        charges.add(1);
        charges.add(2);
        charges.add(3);

        aMinZ.setConverter(intConverter);
        bMinZ.setConverter(intConverter);
        cMinZ.setConverter(intConverter);
        xMinZ.setConverter(intConverter);
        yMinZ.setConverter(intConverter);
        zMinZ.setConverter(intConverter);
        pMinZ.setConverter(intConverter);
        yaMinZ.setConverter(intConverter);
        ybMinZ.setConverter(intConverter);
        imMinZ.setConverter(intConverter);
        bgMinZ.setConverter(intConverter);
        ygMinZ.setConverter(intConverter);
        ncoreMinZ.setConverter(intConverter);
        bnMinZ.setConverter(intConverter);
        ynMinZ.setConverter(intConverter);

        aMinZ.getItems().addAll(charges);
        bMinZ.getItems().addAll(charges);
        cMinZ.getItems().addAll(charges);
        xMinZ.getItems().addAll(charges);
        yMinZ.getItems().addAll(charges);
        zMinZ.getItems().addAll(charges);
        pMinZ.getItems().addAll(charges);
        yaMinZ.getItems().addAll(charges);
        ybMinZ.getItems().addAll(charges);
        imMinZ.getItems().addAll(charges);
        bgMinZ.getItems().addAll(charges);
        ygMinZ.getItems().addAll(charges);
        ncoreMinZ.getItems().addAll(charges);
        bnMinZ.getItems().addAll(charges);
        ynMinZ.getItems().addAll(charges);

        aMaxZ.setConverter(intConverter);
        bMaxZ.setConverter(intConverter);
        cMaxZ.setConverter(intConverter);
        xMaxZ.setConverter(intConverter);
        yMaxZ.setConverter(intConverter);
        zMaxZ.setConverter(intConverter);
        pMaxZ.setConverter(intConverter);
        yaMaxZ.setConverter(intConverter);
        ybMaxZ.setConverter(intConverter);
        imMaxZ.setConverter(intConverter);
        bgMaxZ.setConverter(intConverter);
        ygMaxZ.setConverter(intConverter);
        ncoreMaxZ.setConverter(intConverter);
        bnMaxZ.setConverter(intConverter);
        ynMaxZ.setConverter(intConverter);

        aMaxZ.getItems().addAll(charges);
        bMaxZ.getItems().addAll(charges);
        cMaxZ.getItems().addAll(charges);
        xMaxZ.getItems().addAll(charges);
        yMaxZ.getItems().addAll(charges);
        zMaxZ.getItems().addAll(charges);
        pMaxZ.getItems().addAll(charges);
        yaMaxZ.getItems().addAll(charges);
        ybMaxZ.getItems().addAll(charges);
        imMaxZ.getItems().addAll(charges);
        bgMaxZ.getItems().addAll(charges);
        ygMaxZ.getItems().addAll(charges);
        ncoreMaxZ.getItems().addAll(charges);
        bnMaxZ.getItems().addAll(charges);
        ynMaxZ.getItems().addAll(charges);

        pMaxZ.getItems().add(Integer.MAX_VALUE);
        ygMaxZ.getItems().add(Integer.MAX_VALUE);
        ncoreMaxZ.getItems().add(Integer.MAX_VALUE);

        ncoreCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) ygCheck.setSelected(false);
        });
        ygCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) ncoreCheck.setSelected(false);
        });
    }

    public void updateView(PSMViewSettings settings)
    {
        setTolerance(settings.getFragmentTolerance(), leftTolBox, rightTolBox, tolUnitBox);

        if (settings.isMatchMostIntense()) {
            mostIntenseCheck.setSelected(true);
        } else {
            nearestCheck.setSelected(true);
        }

        deisotopeCheck.setSelected(settings.isDeisotope());
        setTolerance(settings.getDeisotopeTolerance(), deisoLeftTolBox, deisoRightTolBox, deisoTolUnitBox);

        filterPeakCheck.setSelected(settings.isFilterPeaks());
        peakCountBox.setValue(settings.getPeakCount());
        binWidthBox.setValue(settings.getBinWidth());

        aCheck.setSelected(settings.isShowIon(Ion.a));
        bCheck.setSelected(settings.isShowIon(Ion.b));
        cCheck.setSelected(settings.isShowIon(Ion.c));
        xCheck.setSelected(settings.isShowIon(Ion.x));
        yCheck.setSelected(settings.isShowIon(Ion.y));
        zCheck.setSelected(settings.isShowIon(Ion.z));
        pCheck.setSelected(settings.isShowIon(Ion.p));
        yaCheck.setSelected(settings.isShowIon(Ion.ya));
        ybCheck.setSelected(settings.isShowIon(Ion.yb));
        imCheck.setSelected(settings.isShowIon(Ion.im));
        bgCheck.setSelected(settings.isShowIon(Ion.B));
        ygCheck.setSelected(settings.isShowIon(Ion.Y));
        ncoreCheck.setSelected(settings.isShowNCore());
        bnCheck.setSelected(settings.isShowIon(Ion.b_HexNAc));
        ynCheck.setSelected(settings.isShowIon(Ion.y_HexNAc));

        aMinZ.setValue(settings.getMinCharge(Ion.a));
        bMinZ.setValue(settings.getMinCharge(Ion.b));
        cMinZ.setValue(settings.getMinCharge(Ion.c));
        xMinZ.setValue(settings.getMinCharge(Ion.x));
        yMinZ.setValue(settings.getMinCharge(Ion.y));
        zMinZ.setValue(settings.getMinCharge(Ion.z));
        pMinZ.setValue(settings.getMinCharge(Ion.p));
        yaMinZ.setValue(settings.getMinCharge(Ion.ya));
        ybMinZ.setValue(settings.getMinCharge(Ion.ya));
        imMinZ.setValue(settings.getMinCharge(Ion.im));
        bgMinZ.setValue(settings.getMinCharge(Ion.B));
        ygMinZ.setValue(settings.getMinCharge(Ion.Y));
        ncoreMinZ.setValue(settings.getnCoreMinCharge());
        bnMinZ.setValue(settings.getMinCharge(Ion.b_HexNAc));
        ynMinZ.setValue(settings.getMinCharge(Ion.y_HexNAc));

        aMaxZ.setValue(settings.getMaxCharge(Ion.a));
        bMaxZ.setValue(settings.getMaxCharge(Ion.b));
        cMaxZ.setValue(settings.getMaxCharge(Ion.c));
        xMaxZ.setValue(settings.getMaxCharge(Ion.x));
        yMaxZ.setValue(settings.getMaxCharge(Ion.y));
        zMaxZ.setValue(settings.getMaxCharge(Ion.z));
        pMaxZ.setValue(settings.getMaxCharge(Ion.p));
        yaMaxZ.setValue(settings.getMaxCharge(Ion.ya));
        ybMaxZ.setValue(settings.getMaxCharge(Ion.ya));
        imMaxZ.setValue(settings.getMaxCharge(Ion.im));
        bgMaxZ.setValue(settings.getMaxCharge(Ion.B));
        ygMaxZ.setValue(settings.getMaxCharge(Ion.Y));
        ncoreMaxZ.setValue(settings.getnCoreMaxCharge());
        bnMaxZ.setValue(settings.getMaxCharge(Ion.b_HexNAc));
        ynMaxZ.setValue(settings.getMaxCharge(Ion.y_HexNAc));

        aColor.setValue(settings.getColor(Ion.a));
        bColor.setValue(settings.getColor(Ion.b));
        cColor.setValue(settings.getColor(Ion.c));
        xColor.setValue(settings.getColor(Ion.x));
        yColor.setValue(settings.getColor(Ion.y));
        zColor.setValue(settings.getColor(Ion.z));
        pColor.setValue(settings.getColor(Ion.p));
        yaColor.setValue(settings.getColor(Ion.ya));
        ybColor.setValue(settings.getColor(Ion.ya));
        imColor.setValue(settings.getColor(Ion.im));
        bgColor.setValue(settings.getColor(Ion.B));
        ygColor.setValue(settings.getColor(Ion.Y));
        ncoreColor.setValue(settings.getNcoreColor());
        bnColor.setValue(settings.getColor(Ion.b_HexNAc));
        ynColor.setValue(settings.getColor(Ion.y_HexNAc));

        aH2OCheck.setSelected(settings.isShowIon(Ion.a, NeutralLoss.H2O_LOSS));
        bH2OCheck.setSelected(settings.isShowIon(Ion.b, NeutralLoss.H2O_LOSS));
        cH2OCheck.setSelected(settings.isShowIon(Ion.c, NeutralLoss.H2O_LOSS));
        xH2OCheck.setSelected(settings.isShowIon(Ion.x, NeutralLoss.H2O_LOSS));
        yH2OCheck.setSelected(settings.isShowIon(Ion.y, NeutralLoss.H2O_LOSS));
        zH2OCheck.setSelected(settings.isShowIon(Ion.z, NeutralLoss.H2O_LOSS));
        pH2OCheck.setSelected(settings.isShowIon(Ion.p, NeutralLoss.H2O_LOSS));
        yaH2OCheck.setSelected(settings.isShowIon(Ion.ya, NeutralLoss.H2O_LOSS));
        ybH2OCheck.setSelected(settings.isShowIon(Ion.yb, NeutralLoss.H2O_LOSS));

        aNH3Check.setSelected(settings.isShowIon(Ion.a, NeutralLoss.NH3_LOSS));
        bNH3Check.setSelected(settings.isShowIon(Ion.b, NeutralLoss.NH3_LOSS));
        cNH3Check.setSelected(settings.isShowIon(Ion.c, NeutralLoss.NH3_LOSS));
        xNH3Check.setSelected(settings.isShowIon(Ion.x, NeutralLoss.NH3_LOSS));
        yNH3Check.setSelected(settings.isShowIon(Ion.y, NeutralLoss.NH3_LOSS));
        zNH3Check.setSelected(settings.isShowIon(Ion.z, NeutralLoss.NH3_LOSS));
        pNH3Check.setSelected(settings.isShowIon(Ion.p, NeutralLoss.NH3_LOSS));
        yaNH3Check.setSelected(settings.isShowIon(Ion.ya, NeutralLoss.NH3_LOSS));
        ybNH3Check.setSelected(settings.isShowIon(Ion.yb, NeutralLoss.NH3_LOSS));

        aH3PCheck.setSelected(settings.isShowIon(Ion.a, NeutralLoss.H3PO4_LOSS));
        bH3PCheck.setSelected(settings.isShowIon(Ion.b, NeutralLoss.H3PO4_LOSS));
        cH3PCheck.setSelected(settings.isShowIon(Ion.c, NeutralLoss.H3PO4_LOSS));
        xH3PCheck.setSelected(settings.isShowIon(Ion.x, NeutralLoss.H3PO4_LOSS));
        yH3PCheck.setSelected(settings.isShowIon(Ion.y, NeutralLoss.H3PO4_LOSS));
        zH3PCheck.setSelected(settings.isShowIon(Ion.z, NeutralLoss.H3PO4_LOSS));
        pH3PCheck.setSelected(settings.isShowIon(Ion.p, NeutralLoss.H3PO4_LOSS));
        yaH3PCheck.setSelected(settings.isShowIon(Ion.ya, NeutralLoss.H3PO4_LOSS));
        ybH3PCheck.setSelected(settings.isShowIon(Ion.yb, NeutralLoss.H3PO4_LOSS));
    }

    public void updateSettings(PSMViewSettings settings)
    {
        Tolerance tol = getTolerance(leftTolBox, rightTolBox, tolUnitBox);
        if (tol == null) {
            showAlert(Alert.AlertType.ERROR, "Please set tolerance");
            return;
        }
        settings.setFragmentTolerance(tol);
        settings.setMatchMostIntense(mostIntenseCheck.isSelected());
        settings.setDeisotope(deisotopeCheck.isSelected());
        if (deisotopeCheck.isSelected()) {
            Tolerance tol2 = getTolerance(deisoLeftTolBox, deisoRightTolBox, deisoTolUnitBox);
            if (tol2 == null) {
                showAlert(Alert.AlertType.ERROR, "Please set tolerance for deisotope");
                return;
            }
            settings.setDeisotopeTolerance(tol2);
        }

        settings.setFilterPeaks(filterPeakCheck.isSelected());
        settings.setPeakCount(peakCountBox.getValue());
        settings.setBinWidth(binWidthBox.getValue());

        settings.setShowIon(Ion.a, aCheck.isSelected());
        settings.setShowIon(Ion.b, bCheck.isSelected());
        settings.setShowIon(Ion.c, cCheck.isSelected());
        settings.setShowIon(Ion.x, xCheck.isSelected());
        settings.setShowIon(Ion.y, yCheck.isSelected());
        settings.setShowIon(Ion.z, zCheck.isSelected());
        settings.setShowIon(Ion.p, pCheck.isSelected());
        settings.setShowIon(Ion.ya, yaCheck.isSelected());
        settings.setShowIon(Ion.yb, ybCheck.isSelected());
        settings.setShowIon(Ion.im, imCheck.isSelected());
        settings.setShowIon(Ion.B, bgCheck.isSelected());
        settings.setShowIon(Ion.Y, ygCheck.isSelected());
        settings.setShowNCore(ncoreCheck.isSelected());
        settings.setShowIon(Ion.b_HexNAc, bnCheck.isSelected());
        settings.setShowIon(Ion.y_HexNAc, ynCheck.isSelected());

        settings.setMinCharge(Ion.a, aMinZ.getValue());
        settings.setMinCharge(Ion.b, bMinZ.getValue());
        settings.setMinCharge(Ion.c, cMinZ.getValue());
        settings.setMinCharge(Ion.x, xMinZ.getValue());
        settings.setMinCharge(Ion.y, yMinZ.getValue());
        settings.setMinCharge(Ion.z, zMinZ.getValue());
        settings.setMinCharge(Ion.p, pMinZ.getValue());
        settings.setMinCharge(Ion.ya, yaMinZ.getValue());
        settings.setMinCharge(Ion.yb, ybMinZ.getValue());
        settings.setMinCharge(Ion.im, imMinZ.getValue());
        settings.setMinCharge(Ion.B, bgMinZ.getValue());
        settings.setMinCharge(Ion.Y, ygMinZ.getValue());
        settings.setnCoreMinCharge(ncoreMinZ.getValue());
        settings.setMinCharge(Ion.b_HexNAc, bnMinZ.getValue());
        settings.setMinCharge(Ion.y_HexNAc, ynMinZ.getValue());

        settings.setMaxCharge(Ion.a, aMaxZ.getValue());
        settings.setMaxCharge(Ion.b, bMaxZ.getValue());
        settings.setMaxCharge(Ion.c, cMaxZ.getValue());
        settings.setMaxCharge(Ion.x, xMaxZ.getValue());
        settings.setMaxCharge(Ion.y, yMaxZ.getValue());
        settings.setMaxCharge(Ion.z, zMaxZ.getValue());
        settings.setMaxCharge(Ion.p, pMaxZ.getValue());
        settings.setMaxCharge(Ion.ya, yaMaxZ.getValue());
        settings.setMaxCharge(Ion.yb, ybMaxZ.getValue());
        settings.setMaxCharge(Ion.im, imMaxZ.getValue());
        settings.setMaxCharge(Ion.B, bgMaxZ.getValue());
        settings.setMaxCharge(Ion.Y, ygMaxZ.getValue());
        settings.setnCoreMaxCharge(ncoreMaxZ.getValue());
        settings.setMaxCharge(Ion.b_HexNAc, bnMaxZ.getValue());
        settings.setMaxCharge(Ion.y_HexNAc, ynMaxZ.getValue());

        settings.setColor(Ion.a, aColor.getValue());
        settings.setColor(Ion.b, bColor.getValue());
        settings.setColor(Ion.c, cColor.getValue());
        settings.setColor(Ion.x, xColor.getValue());
        settings.setColor(Ion.y, yColor.getValue());
        settings.setColor(Ion.z, zColor.getValue());
        settings.setColor(Ion.p, pColor.getValue());
        settings.setColor(Ion.ya, yaColor.getValue());
        settings.setColor(Ion.yb, ybColor.getValue());
        settings.setColor(Ion.im, imColor.getValue());
        settings.setColor(Ion.B, bgColor.getValue());
        settings.setColor(Ion.Y, ygColor.getValue());
        settings.setNcoreColor(ncoreColor.getValue());
        settings.setColor(Ion.b_HexNAc, bnColor.getValue());
        settings.setColor(Ion.y_HexNAc, ynColor.getValue());

        settings.setShowIon(Ion.a, NeutralLoss.H2O_LOSS, aH2OCheck.isSelected());
        settings.setShowIon(Ion.b, NeutralLoss.H2O_LOSS, bH2OCheck.isSelected());
        settings.setShowIon(Ion.c, NeutralLoss.H2O_LOSS, cH2OCheck.isSelected());
        settings.setShowIon(Ion.x, NeutralLoss.H2O_LOSS, xH2OCheck.isSelected());
        settings.setShowIon(Ion.y, NeutralLoss.H2O_LOSS, yH2OCheck.isSelected());
        settings.setShowIon(Ion.z, NeutralLoss.H2O_LOSS, zH2OCheck.isSelected());
        settings.setShowIon(Ion.p, NeutralLoss.H2O_LOSS, pH2OCheck.isSelected());
        settings.setShowIon(Ion.ya, NeutralLoss.H2O_LOSS, yaH2OCheck.isSelected());
        settings.setShowIon(Ion.yb, NeutralLoss.H2O_LOSS, ybH2OCheck.isSelected());

        settings.setShowIon(Ion.a, NeutralLoss.NH3_LOSS, aNH3Check.isSelected());
        settings.setShowIon(Ion.b, NeutralLoss.NH3_LOSS, bNH3Check.isSelected());
        settings.setShowIon(Ion.c, NeutralLoss.NH3_LOSS, cNH3Check.isSelected());
        settings.setShowIon(Ion.x, NeutralLoss.NH3_LOSS, xNH3Check.isSelected());
        settings.setShowIon(Ion.y, NeutralLoss.NH3_LOSS, yNH3Check.isSelected());
        settings.setShowIon(Ion.z, NeutralLoss.NH3_LOSS, zNH3Check.isSelected());
        settings.setShowIon(Ion.p, NeutralLoss.NH3_LOSS, pNH3Check.isSelected());
        settings.setShowIon(Ion.ya, NeutralLoss.NH3_LOSS, yaNH3Check.isSelected());
        settings.setShowIon(Ion.yb, NeutralLoss.NH3_LOSS, ybNH3Check.isSelected());

        settings.setShowIon(Ion.a, NeutralLoss.H3PO4_LOSS, aH3PCheck.isSelected());
        settings.setShowIon(Ion.b, NeutralLoss.H3PO4_LOSS, bH3PCheck.isSelected());
        settings.setShowIon(Ion.c, NeutralLoss.H3PO4_LOSS, cH3PCheck.isSelected());
        settings.setShowIon(Ion.x, NeutralLoss.H3PO4_LOSS, xH3PCheck.isSelected());
        settings.setShowIon(Ion.y, NeutralLoss.H3PO4_LOSS, yH3PCheck.isSelected());
        settings.setShowIon(Ion.z, NeutralLoss.H3PO4_LOSS, zH3PCheck.isSelected());
        settings.setShowIon(Ion.p, NeutralLoss.H3PO4_LOSS, pH3PCheck.isSelected());
        settings.setShowIon(Ion.ya, NeutralLoss.H3PO4_LOSS, yaH3PCheck.isSelected());
        settings.setShowIon(Ion.yb, NeutralLoss.H3PO4_LOSS, ybH3PCheck.isSelected());
    }

    private Tolerance getTolerance(ComboBox<Double> left, ComboBox<Double> right, ChoiceBox<String> unit)
    {
        Double leftValue = left.getValue();
        Double rightValue = right.getValue();
        String unitValue = unit.getValue();
        if (leftValue == null || rightValue == null) {
            return null;
        }
        if (unitValue.equals("ppm"))
            return Tolerance.ppm(leftValue, rightValue);
        else
            return Tolerance.abs(leftValue, rightValue);
    }

    private void setTolerance(Tolerance tolerance, ComboBox<Double> leftTolBox, ComboBox<Double> rightTolBox,
                              ChoiceBox<String> unitBox)
    {
        leftTolBox.setValue(tolerance.getMinusError());
        rightTolBox.setValue(tolerance.getPlusError());
        if (tolerance.isAbsolute())
            unitBox.getSelectionModel().select(1);
        else
            unitBox.getSelectionModel().select(0);
    }
}
