package omics.gui.psm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.util.Callback;
import omics.util.protein.Peptide;
import omics.util.protein.PeptideFragment;
import omics.util.protein.ms.FragmentIonType;
import omics.util.protein.ms.FragmentType;
import omics.util.protein.ms.PeptideFragAnnotation;
import omics.util.protein.ms.PeptideSpectrum;
import omics.util.utils.NumberFormatFactory;

import java.text.NumberFormat;
import java.util.*;

import static omics.util.utils.ObjectUtils.checkNotNull;

/**
 * Table view of the peptide theoretical peaks.
 *
 * @author JiaweiMao
 * @version 1.0.1
 * @since 08 Jan 2018, 4:43 PM
 */
public class PeptideTableView extends TableView
{
    private static final String DeltaSymbol = new String(Character.toChars(0x0394));

    private static NumberFormat massFormat = NumberFormatFactory.valueOf(4);
    // PeptideIonType, charge, size, delta mass
    private PeptideSpectrum iSpectrum;
    private Map<PeptideFragAnnotation, Double> iMatchPeaks = new HashMap<>();
    private String deltaUnit;

    public PeptideTableView(PeptideSpectrum aSpectrum)
    {
        this(aSpectrum, null, null);
    }

    public PeptideTableView(PeptideSpectrum spectrum, Map<PeptideFragAnnotation, Double> matchPeaks, String deltaUnit)
    {
        checkNotNull(spectrum);

        this.iSpectrum = spectrum;
        this.deltaUnit = " (" + deltaUnit + ")";

        if (matchPeaks != null)
            iMatchPeaks.putAll(matchPeaks);

        fill();
        beautify();
    }

    private void beautify()
    {
        setSortPolicy(param -> false);

        // disable column resizing
        setColumnResizePolicy(resizeFeatures -> true);

        int size = getColumns().size();
        double width = 25 + (size - 3) * 160;
        setPrefWidth(width);

        getStylesheets().add("css/peptide_table.css");

        // clear selection
        TableViewSelectionModel<Map> selectionModel = getSelectionModel();
        selectionModel.clearSelection();

        // define the height
        setFixedCellSize(24);
        prefHeightProperty().bind(Bindings.size(getItems()).multiply(getFixedCellSize()).add(55));
    }


    /**
     * set the mass format to display.
     *
     * @param format a {@link NumberFormat} instance.
     */
    public static void setMassFormat(NumberFormat format)
    {
        massFormat = format;
    }

    protected void fill()
    {
        // the first row is the title, the columns count is not known now.
        ObservableList<Map<String, String>> rows = FXCollections.observableArrayList();

        Peptide peptide = iSpectrum.getPeptide();
        // for each row add amino acid and index
        String seqId = "Seq.";
        String reverseId = "reverseId";
        String forwardId = "forwardId";
        for (int i = 1; i <= peptide.size(); i++) {
            Map<String, String> map = new HashMap<>();

            map.put(forwardId, String.valueOf(i));
            map.put(reverseId, String.valueOf(iSpectrum.getPeptide().size() + 1 - i));
            map.put(seqId, peptide.getSymbol(i - 1).getSymbol());

            rows.add(map);
        }

        // table for FragmentIonType, index, column title
        Table<FragmentIonType, Integer, String> idTable = HashBasedTable.create();

        Set<String> labels = new HashSet<>(); // all labels include delta mass
        //<editor-fold desc="populate value to map">
        for (int i = 0; i < iSpectrum.size(); i++) {
            for (PeptideFragAnnotation annotation : iSpectrum.getAnnotations(i)) {
                FragmentIonType peptideIonType = annotation.getFragmentIonType();

                // neutral Loss is not supported
                if (annotation.hasNeutralLoss()) {
                    continue;
                }

                if (peptideIonType == FragmentIonType.p)
                    continue;
                PeptideFragment fragment = annotation.getFragment();
                int size = fragment.size();

                int index = size - 1;
                if (fragment.getFragmentType() == FragmentType.REVERSE)
                    index = peptide.size() - size;

                String label = idTable.get(peptideIonType, annotation.getCharge());
                if (label == null) {
                    StringBuilder labelBuilder = new StringBuilder(peptideIonType.getName());
                    if (annotation.getCharge() > 1) {
                        int charge = annotation.getCharge();
                        while (charge > 0) {
                            labelBuilder.append("+");
                            charge--;
                        }
                    }
                    label = labelBuilder.toString();
                    idTable.put(peptideIonType, annotation.getCharge(), label);
                    labels.add(label);
                }

                System.out.println(annotation.getTheoreticalMz());

                rows.get(index).put(label, massFormat.format(annotation.getTheoreticalMz()));

                Double delta = iMatchPeaks.get(annotation);
                if (delta != null) {
                    String deltaLabel = label + "delta";
                    labels.add(deltaLabel);
                    String deltaValue = massFormat.format(delta);
                    if (delta > 0)
                        deltaValue = "+" + deltaValue;
                    rows.get(index).put(deltaLabel, deltaValue);
                }
            }
        }
        //</editor-fold>

        getItems().addAll(rows);

        TableColumn<Map, String> forwardIdCol = new TableColumn<>("");

        TableColumn<Map, String> fidCol = new TableColumn<>("#");
        fidCol.setCellValueFactory(new MapValueFactory<>(forwardId));
        fidCol.getStyleClass().add("aa");
        forwardIdCol.getColumns().add(fidCol);

        getColumns().add(forwardIdCol);

        List<FragmentIonType> types = new ArrayList<>(idTable.rowKeySet());
        types.sort(Comparator.comparing(FragmentIonType::getName));

        TableColumn<Map, String> seqCol = new TableColumn<>("");
        TableColumn<Map, String> seqValueCol = new TableColumn<>("Seq.");
        seqValueCol.setCellValueFactory(new MapValueFactory<>(seqId));
        seqValueCol.getStyleClass().add("aa");

        seqCol.getColumns().add(seqValueCol);

        boolean firstReverse = true;
        for (FragmentIonType ionType : types) {
            if (ionType.getFragmentType() == FragmentType.REVERSE && firstReverse) {
                getColumns().add(seqCol);
                firstReverse = false;
            }

            Map<Integer, String> chargeLabelMap = idTable.row(ionType);
            List<Integer> chargeList = new ArrayList<>(chargeLabelMap.keySet());
            chargeList.sort(Comparator.naturalOrder());

            for (int charge : chargeList) {
                String label = idTable.get(ionType, charge);
                TableColumn<Map, String> ionCol = new TableColumn<>(label);
                ionCol.getStyleClass().add("col2");

                TableColumn<Map, String> mzCol = new TableColumn<>("m/z");
                mzCol.setCellValueFactory(new MapValueFactory<>(label));
                mzCol.getStyleClass().add("mz");

                ionCol.getColumns().add(mzCol);

                String deltaLabel = label + "delta";
                if (labels.contains(deltaLabel)) {

                    TableColumn<Map, String> deltaCol = new TableColumn<>(DeltaSymbol + deltaUnit);
                    deltaCol.setCellValueFactory(new MapValueFactory<>(deltaLabel));
                    deltaCol.getStyleClass().add("mz");

                    ionCol.getColumns().add(deltaCol);
                }

                mzCol.setCellFactory(new Callback<TableColumn<Map, String>, TableCell<Map, String>>()
                {
                    @Override
                    public TableCell<Map, String> call(TableColumn<Map, String> param)
                    {
                        return new TableCell<Map, String>()
                        {
                            @Override
                            protected void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                if (!empty) {
                                    setText(item);
                                    if (getTableRow() != null) {
                                        int index = getTableRow().getIndex();
                                        if (rows.get(index).containsKey(deltaLabel)) {
                                            getStyleClass().addAll("match-mz", ionType.toString());
                                        }
                                    }
                                }
                            }
                        };
                    }
                });

                getColumns().add(ionCol);
            }
        }

        TableColumn<Map, String> reverseIdCol = new TableColumn<>("");
        TableColumn<Map, String> rIdCol = new TableColumn<>("#");
        rIdCol.setCellValueFactory(new MapValueFactory<>(reverseId));
        rIdCol.getStyleClass().add("aa");

        reverseIdCol.getColumns().add(rIdCol);
        getColumns().add(reverseIdCol);
    }
}
