package omics.gui.psm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import omics.gui.psm.util.NodeUtils;
import omics.util.protein.Peptide;
import omics.util.protein.ms.FragmentIonType;
import omics.util.protein.ms.PeptideFragAnnotation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A canvas for Peptide sequence annotation.
 *
 * @author JiaweiMao
 * @version 1.0.1
 * @since 26 Dec 2017, 4:52 PM
 */
public class PeptideCanvas extends Canvas
{
    private static final double PAD_AA = 8;
    private static final double PAD_AA_LINE = 3;
    private static final double PAD_LABEL_LINE = 4;

    private static final double LINE_WIDTH = 2;
    private static final Font FONT_AMINOACID = Font.font("Calibri", FontWeight.BOLD, 20);
    private static final Font labelFont = Font.font("Calibri", FontWeight.BOLD, 14);

    private Peptide peptide;
    private final List<PeptideFragAnnotation> annotations = new ArrayList<>();
    private final SpectrumViewStyle config = new SpectrumViewStyle();

    public PeptideCanvas()
    {
        this(960, 80);
    }

    public PeptideCanvas(double width, double height)
    {
        super(width, height);

        widthProperty().addListener(event -> draw());
        heightProperty().addListener(event -> draw());
    }

    public SpectrumViewStyle getConfig()
    {
        return config;
    }

    public void setPeptide(Peptide peptide, List<PeptideFragAnnotation> annotations)
    {
        this.peptide = peptide;

        this.annotations.clear();
        this.annotations.addAll(annotations);

        draw();
    }

    private void draw()
    {
        if (peptide == null)
            return;

        double width = getWidth();
        double height = getHeight();

        GraphicsContext gc = getGraphicsContext2D();
        gc.save();
        gc.clearRect(0, 0, width, height);

        int size = peptide.size();
        String seq = peptide.toSymbolString();

        Bounds bounds = NodeUtils.textSize(FONT_AMINOACID, VPos.TOP, seq);
        double aaUnit = bounds.getWidth() / size;

        gc.setFont(FONT_AMINOACID);
        gc.setStroke(Color.BLACK);
        gc.setTextBaseline(VPos.CENTER);

        double startX = (width - bounds.getWidth()) / 2 - PAD_AA * size;
        double startY = height / 2;
        for (int i = 0; i < peptide.size(); i++) {
            double x = startX + aaUnit * i + PAD_AA * 2 * i + PAD_AA;
            String symbol = peptide.getSymbol(i).getSymbol();
            gc.fillText(symbol, x, startY);
        }

        gc.setLineWidth(LINE_WIDTH);
        gc.setFont(labelFont);

        double x1, x2, x3;
        double y1, y2, y3;
        double labelXLoc, labelYLoc;
        String label;

        ListMultimap<FragmentIonType, PeptideFragAnnotation> annoMap = ArrayListMultimap.create();
        for (PeptideFragAnnotation annotation : annotations) {
            annoMap.put(annotation.getFragmentIonType(), annotation);
        }

        Set<Integer> processedSet = new HashSet<>();
        if (annoMap.containsKey(FragmentIonType.b)) {
            List<PeptideFragAnnotation> annotations = annoMap.get(FragmentIonType.b);

            gc.setTextBaseline(VPos.TOP);
            gc.setStroke(config.getColor(FragmentIonType.b));
            gc.setFill(config.getColor(FragmentIonType.b));

            for (PeptideFragAnnotation annotation : annotations) {
                int len = annotation.getFragment().size();
                if (processedSet.contains(len))
                    continue;

                x1 = startX + (aaUnit + PAD_AA * 2) * (len - 1) + PAD_AA;
                x2 = x1 + aaUnit + PAD_AA;
                x3 = x2;
                y1 = startY + bounds.getHeight() / 2 + PAD_AA_LINE;
                y2 = y1;
                y3 = startY;
                labelXLoc = x1;
                labelYLoc = y1 + PAD_LABEL_LINE;
                label = "b" + len;

                // as the line width is even
                gc.strokePolyline(new double[]{(int) x1, (int) x2, (int) x3}, new double[]{(int) y1, (int) y2, (int) y3}, 3);
                gc.fillText(label, labelXLoc, labelYLoc);

                processedSet.add(len);
            }
        }

        processedSet.clear();
        if (annoMap.containsKey(FragmentIonType.y)) {
            List<PeptideFragAnnotation> annotations = annoMap.get(FragmentIonType.y);

            gc.setTextBaseline(VPos.BOTTOM);
            gc.setStroke(config.getColor(FragmentIonType.y));
            gc.setFill(config.getColor(FragmentIonType.y));

            for (PeptideFragAnnotation annotation : annotations) {
                int len = annotation.getFragment().size();
                if (processedSet.contains(len))
                    continue;

                x1 = startX + (aaUnit + PAD_AA * 2) * (size - len);
                x2 = x1;
                x3 = x2 + aaUnit + PAD_AA;
                y1 = startY;
                y2 = startY - bounds.getHeight() / 2 - PAD_AA_LINE;
                y3 = y2;

                labelXLoc = x2;
                labelYLoc = y2 - PAD_LABEL_LINE;

                label = "y" + len;

                gc.strokePolyline(new double[]{(int) x1, (int) x2, (int) x3}, new double[]{(int) y1, (int) y2, (int) y3}, 3);
                gc.fillText(label, labelXLoc, labelYLoc);
                processedSet.add(len);
            }
        }

        processedSet.clear();
        gc.restore();
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public double prefWidth(double height)
    {
        return getWidth();
    }

    @Override
    public double prefHeight(double width)
    {
        return getHeight();
    }
}
