package omics.gui.psm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import omics.util.protein.Peptide;
import omics.util.protein.ms.FragmentIonType;
import omics.util.protein.ms.PeptideFragAnnotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Display peptides sequence.
 *
 * @author JiaweiMao
 * @version 1.0.1
 * @since 17 Apr 2018, 9:42 PM
 */
public class PeptideChart extends Pane
{
    private static final Insets PAD = new Insets(6, 6, 0, 6);
    /**
     * left and right padding of amino acid
     */
    private static final int PAD_AA = 6;
    private static final int PAD_AA_VLine = 4;
    private static final int PAD_VLine_Label = 4;

    private static final int LINE_WIDTH = 2;
    private Peptide peptide;
    private List<PeptideFragAnnotation> annotations = new ArrayList<>();
    private SpectrumViewStyle config = new SpectrumViewStyle();

    public PeptideChart()
    {
        heightProperty().addListener((observable, oldValue, newValue) -> draw());
        widthProperty().addListener((observable, oldValue, newValue) -> draw());
    }

    public SpectrumViewStyle getConfig()
    {
        return config;
    }

    /**
     * Set the Peptide and its annotation.
     *
     * @param peptide     {@link Peptide}
     * @param annotations list of {@link PeptideFragAnnotation}
     */
    public void setPeptide(Peptide peptide, List<PeptideFragAnnotation> annotations)
    {
        this.peptide = peptide;
        this.annotations.clear();
        if (annotations != null)
            this.annotations.addAll(annotations);

        draw();
    }

    /**
     * set the {@link SpectrumViewStyle} for display.
     */
    public void setStyle(SpectrumViewStyle style)
    {
        this.config = style;
    }

    private void draw()
    {
        getChildren().clear();

        if (peptide == null)
            return;

        double width = getWidth();
        double height = getHeight();

        int length = peptide.size();
        double padding = PAD.getLeft() + PAD.getRight() + PAD_AA * 2 * length;
        if (width < padding)
            return;

        //region calculate suitable width
        Font aaFont = config.getAminoAcidFont();
        Text text = new Text("W"); // 'W' is the amino acid with maximum width
        text.setFont(config.getAminoAcidFont());
        double aaHeight = text.getLayoutBounds().getHeight();
        double aaWidth = text.getLayoutBounds().getWidth();
        if (aaWidth * length > (width - padding)) {
            int newSize = (int) (config.getAminoAcidFont().getSize() * (width - padding) / aaWidth * length);
            if (newSize < 6)
                return;
            aaFont = Font.font(config.getAminoAcidFont().getFamily(), FontWeight.BOLD, newSize);
            text.setFont(aaFont);
            aaHeight = text.getBoundsInLocal().getHeight();
            aaWidth = text.getBoundsInLocal().getWidth();
        }
        //endregion

        double y_aa = (height - aaHeight) / 2;
        double x_start = (width - padding - aaWidth * length) / 2 + PAD.getLeft();
        for (int i = 0; i < peptide.size(); i++) {
            String symbol = peptide.getSymbol(i).getSymbol();
            Text aaText = new Text(symbol);
            aaText.setFont(aaFont);
            aaText.setTextOrigin(VPos.TOP);
            aaText.relocate(x_start + (aaWidth + PAD_AA * 2) * i + PAD_AA, y_aa);
            getChildren().add(aaText);
        }

        if (annotations.isEmpty())
            return;

        ListMultimap<FragmentIonType, PeptideFragAnnotation> annotationMap = ArrayListMultimap.create();
        for (PeptideFragAnnotation annotation : annotations) {
            annotationMap.put(annotation.getFragmentIonType(), annotation);
        }

        Set<Integer> processedSet = new HashSet<>();
        //region PeptideIonType.b
        if (annotationMap.containsKey(FragmentIonType.b)) {
            List<PeptideFragAnnotation> annotations = annotationMap.get(FragmentIonType.b);

            for (PeptideFragAnnotation annotation : annotations) {

                int size = annotation.getFragment().size();
                if (processedSet.contains(size))
                    continue;

                double x1 = x_start + (aaWidth + PAD_AA * 2) * (size - 1) + PAD_AA;
                double x2 = x1 + aaWidth + PAD_AA;
                double y1 = y_aa + aaHeight + PAD_AA_VLine;
                double y3 = y_aa + aaHeight / 2;

                Polyline polyline = new Polyline((int) x1, (int) y1, (int) x2, (int) y1, (int) x2, (int) y3);
                polyline.setStroke(config.getColor(FragmentIonType.b));
                polyline.setStrokeWidth(LINE_WIDTH);
                getChildren().add(polyline);

                String label = "b" + size;

                Text labelTxt = new Text(label);
                labelTxt.setFont(config.getAminoAcidLabelFont());
                labelTxt.setTextOrigin(VPos.TOP);
                labelTxt.setFill(config.getColor(FragmentIonType.b));
                labelTxt.relocate(x1, y1 + PAD_VLine_Label);
                getChildren().add(labelTxt);

                processedSet.add(size);
            }
        }
        //endregion

        processedSet.clear();
        if (annotationMap.containsKey(FragmentIonType.y)) {
            List<PeptideFragAnnotation> annotations = annotationMap.get(FragmentIonType.y);

            for (PeptideFragAnnotation annotation : annotations) {

                int size = annotation.getFragment().size();
                if (processedSet.contains(size))
                    continue;

                double x1 = x_start + (aaWidth + PAD_AA * 2) * (length - size);
                double x3 = x1 + aaWidth + PAD_AA;
                double y1 = y_aa + aaHeight / 2;
                double y2 = y_aa - PAD_AA_VLine;

                Polyline polyline = new Polyline((int) x1, (int) y1, (int) x1, (int) y2, (int) x3, (int) y2);
                polyline.setStroke(config.getColor(FragmentIonType.y));
                polyline.setStrokeWidth(LINE_WIDTH);
                getChildren().add(polyline);

                String label = "y" + size;

                Text labelTxt = new Text(label);
                labelTxt.setFont(config.getAminoAcidLabelFont());
                labelTxt.setTextOrigin(VPos.TOP);
                labelTxt.setFill(config.getColor(FragmentIonType.y));
                labelTxt.relocate(x1, y2 - PAD_VLine_Label - labelTxt.getBoundsInLocal().getHeight());

                getChildren().add(labelTxt);

                processedSet.add(size);
            }
        }
        processedSet.clear();
    }
}
