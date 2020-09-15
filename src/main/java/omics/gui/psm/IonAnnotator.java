package omics.gui.psm;

import com.google.common.collect.Lists;
import omics.pdk.ptm.glyco.*;
import omics.util.chem.Composition;
import omics.util.chem.PeriodicTable;
import omics.util.ms.MsnSpectrum;
import omics.util.ms.peaklist.Peak;
import omics.util.ms.peaklist.Tolerance;
import omics.util.ms.peaklist.impl.MzSymbolAnnotation;
import omics.util.protein.mod.NeutralLoss;
import omics.util.protein.ms.Ion;
import omics.util.utils.NumberFormatFactory;
import omics.util.utils.StringUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 16 Dec 2019, 11:59 PM
 */
public class IonAnnotator
{
    /**
     * Annotate oxonium ions in the spectrum.
     *
     * @param spectrum   spectrum to be annotated
     * @param fragTol    {@link Tolerance} to match peaks
     * @param markerList {@link OxoniumMarker} to find
     */
    public static void annotateOxonium(MsnSpectrum spectrum, Tolerance fragTol, List<OxoniumMarker> markerList, int minCharge, int maxCharge)
    {
        NumberFormat d0 = NumberFormatFactory.INT_PRECISION;
        for (OxoniumMarker marker : markerList) {
            double mass = marker.getMolecularMass();
            for (int charge = minCharge; charge <= maxCharge; charge++) {
                double mz = Ion.calcMz(mass, charge, PeriodicTable.PROTON_MASS);
                int index = spectrum.getMostIntenseIndex(mz, fragTol);
                if (index >= 0) {
                    spectrum.addAnnotation(index, new MzSymbolAnnotation(mz, charge, d0.format(mz), Ion.B));
                }
            }
        }
    }

    public static final List<DeltaYMarker> DELTA_Y_MARKERS;
    public static final List<DeltaYMarker> DELTA_Y_MARKERS_H;
    public static final List<DeltaYMarker> DELTA_Y_MARKERS_ALL;

    static {
        DELTA_Y_MARKERS = Lists.newArrayList(
                new DeltaYMarker("-[HNS2]", Composition.parseComposition("C36H57N3O26")),
                new DeltaYMarker("-[HS2]", Composition.parseComposition("C28H44N2O21")),
                new DeltaYMarker("-[S2]", Composition.parseComposition("C22H34N2O16")),
                new DeltaYMarker("-[S]", Composition.parseComposition("C11H17NO8")),

                new DeltaYMarker("-[H2N2]", Composition.parseComposition("C28H46N2O20")),
                new DeltaYMarker("-[H2N]", Composition.parseComposition("C20H33NO15")),

                new DeltaYMarker("-[HNS]", Composition.parseComposition("C25H40N2O18")),
                new DeltaYMarker("-[HNS°]", Composition.parseComposition("C25H42N2O19")),

                new DeltaYMarker("-[HN]", Composition.parseComposition("C14H23NO10")),
                new DeltaYMarker("-[HN°]", Composition.parseComposition("C14H25NO11")),
                new DeltaYMarker("-[HN*]", Composition.parseComposition("C14H26N2O10")),
                new DeltaYMarker("-[H]", Composition.parseComposition("C6H10O5")),

                new DeltaYMarker("-[NS]", Composition.parseComposition("C19H30N2O13")),

                new DeltaYMarker("-[HS]", Composition.parseComposition("C17H27NO13")),
                new DeltaYMarker("-[H4N5]", Composition.parseComposition("C64H105N5O45"))
        );

        DELTA_Y_MARKERS_H = Lists.newArrayList(
                new DeltaYMarker("-[HN]-H", Composition.parseComposition("C14H24NO10")),
                new DeltaYMarker("-[HN°]-H", Composition.parseComposition("C14H26NO11"))

        );
        DELTA_Y_MARKERS_ALL = new ArrayList<>();
        DELTA_Y_MARKERS_ALL.addAll(DELTA_Y_MARKERS);
        DELTA_Y_MARKERS_ALL.addAll(DELTA_Y_MARKERS_H);
    }


    /**
     * Annotate delta Y in the spectrum
     *
     * @param spectrum    {@link MsnSpectrum} to be annotated.
     * @param fragmentTol {@link Tolerance} to match peaks.
     */
    public static void annotateGlycanY(MsnSpectrum spectrum, Tolerance fragmentTol, double pepMass,
                                       int minCharge, int maxCharge, GlycanComposition composition)
    {
        final FragmentFactory fragmentFactory = FragmentFactory.getInstance();
        int charge = spectrum.getPrecursor().getCharge();
        if (maxCharge != Integer.MAX_VALUE)
            charge = maxCharge;
        Ion yG = Ion.Y;
        for (int i = minCharge; i <= charge; i++) {
            double mz = Ion.calcMz(pepMass, i);
            int index = spectrum.getMostIntenseIndex(mz, fragmentTol);
            if (index < 0)
                continue;
            String symbol = "Y0";
            if (i > 1)
                symbol += StringUtils.superscript(i + "+");
            spectrum.addAnnotation(index, new MzSymbolAnnotation(mz, index, symbol, yG));
        }

        for (YMarker fragment : fragmentFactory.getFragments(composition)) {
            double molecularMass = fragment.getMolecularMass() + pepMass;
            for (int i = minCharge; i <= charge; i++) {
                double mz = Ion.calcMz(molecularMass, i);
                int index = spectrum.getMostIntenseIndex(mz, fragmentTol);
                if (index < 0)
                    continue;

                String symbol = fragment.getSymbol();
                if (i > 1)
                    symbol += StringUtils.superscript(i + "+");
                spectrum.addAnnotation(index, new MzSymbolAnnotation(mz, index, symbol, yG));
            }
        }
    }


    /**
     * Annotate delta Y in the spectrum
     *
     * @param spectrum    {@link MsnSpectrum} to be annotated.
     * @param fragmentTol {@link Tolerance} to match peaks.
     */
    public static void annotateOGlycanY(MsnSpectrum spectrum, Tolerance fragmentTol, int minCharge, int maxCharge)
    {
        Peak precursor = spectrum.getPrecursor();
        double mass = precursor.getMass();
        int charge = precursor.getCharge();
        if (maxCharge != Integer.MAX_VALUE)
            charge = maxCharge;
        Ion yG = Ion.Y;
        for (DeltaYMarker marker : DELTA_Y_MARKERS_ALL) {
            double markerMass = marker.getMass();
            double remainMass = mass - markerMass;
            for (int i = minCharge; i <= charge; i++) {
                double mz = Ion.calcMz(remainMass, i);
                int index = spectrum.getMostIntenseIndex(mz, fragmentTol);
                if (index < 0)
                    continue;

                String symbol = marker.getSymbol();
                if (i > 1)
                    symbol += StringUtils.superscript(i + "+");
                MzSymbolAnnotation annotation = new MzSymbolAnnotation(mz, index, symbol, yG);
                spectrum.addAnnotation(index, annotation);
            }
        }
    }

    public static void annoNCore(MsnSpectrum spectrum, double pepMass, Tolerance fragTol, int minCharge, int maxCharge)
    {
        for (int i = minCharge; i <= maxCharge; i++) {
            double mzy0 = Ion.calcMz(pepMass, i);
            int y0idx = spectrum.getMostIntenseIndex(mzy0, fragTol);
            if (y0idx >= 0) {
                spectrum.addAnnotation(y0idx, new YPeakAnnotation(0, i));
            }

            double mzy0n = Ion.calcMz(pepMass - PeriodicTable.NH3_MASS, i);
            int y0nidx = spectrum.getMostIntenseIndex(mzy0n, fragTol);
            if (y0nidx >= 0) {
                spectrum.addAnnotation(y0nidx, new YPeakAnnotation(0, i, NeutralLoss.NH3_LOSS));
            }
        }
        double mass = pepMass;
        for (int i = 0; i < YFunc.N_CORE.length; i++) {
            Glycosyl glycosyl = YFunc.N_CORE[i];
            mass += glycosyl.getMolecularMass();
            for (int c = minCharge; c <= maxCharge; c++) {
                double mz = Ion.calcMz(mass, c);
                int idx = spectrum.getMostIntenseIndex(mz, fragTol);
                if (idx >= 0) {
                    spectrum.addAnnotation(idx, new YPeakAnnotation(i + 1, c));
                }
                double h2oMz = Ion.calcMz(mass - PeriodicTable.H2O_MASS, c);
                idx = spectrum.getMostIntenseIndex(h2oMz, fragTol);
                if (idx >= 0) {
                    spectrum.addAnnotation(idx, new YPeakAnnotation(i + 1, c, NeutralLoss.H2O_LOSS));
                }
            }
        }
    }

    /**
     * Add annotation of precursor of all possible charges.
     *
     * @param spectrum {@link MsnSpectrum}
     * @param fragTol  {@link Tolerance} to match peaks.
     */
    public static void annotatePrecursor(MsnSpectrum spectrum, Tolerance fragTol,
                                         int minCharge, int maxCharge)
    {
        Peak precursor = spectrum.getPrecursor();
        double mass = precursor.getMass();
        int charge = precursor.getCharge();
        if (maxCharge != Integer.MAX_VALUE)
            charge = maxCharge;
        Ion p = Ion.p;
        for (int i = minCharge; i <= charge; i++) {
            double mz = Ion.calcMz(mass, i);

            int index = spectrum.getMostIntenseIndex(mz, fragTol);
            if (index >= 0) {
                String symbol = p.getName();
                if (i > 1) {
                    symbol += StringUtils.superscript(i + "+");
                }
                MzSymbolAnnotation annotation = new MzSymbolAnnotation(mz, i, symbol, p);
                spectrum.addAnnotation(index, annotation);
            }
        }
    }

    /**
     * Add annotation of precursor of all possible charges.
     *
     * @param spectrum {@link MsnSpectrum}
     * @param fragTol  {@link Tolerance} to match peaks.
     */
    public static void annotatePrecursorH3PO4(MsnSpectrum spectrum, Tolerance fragTol,
                                              int minCharge, int maxCharge)
    {
        Peak precursor = spectrum.getPrecursor();
        int charge = precursor.getCharge();
        if (maxCharge != Integer.MAX_VALUE)
            charge = maxCharge;
        Ion p = Ion.p;
        double h3po4Mass = precursor.getMass() - Composition.H3PO4.getMolecularMass();
        for (int i = minCharge; i <= charge; i++) {
            double mz = Ion.calcMz(h3po4Mass, i);
            int index = spectrum.getMostIntenseIndex(mz, fragTol);
            if (index < 0)
                continue;

            String symbol = "[Mp]";
            if (i > 1)
                symbol += StringUtils.superscript(i + "+");
            MzSymbolAnnotation annotation = new MzSymbolAnnotation(mz, i, symbol, p);
            spectrum.addAnnotation(index, annotation);
        }
    }

    /**
     * Add annotation of precursor of all possible charges.
     *
     * @param spectrum {@link MsnSpectrum}
     * @param fragTol  {@link Tolerance} to match peaks.
     */
    public static void annotatePrecursorNH3(MsnSpectrum spectrum, Tolerance fragTol, int minCharge, int maxCharge)
    {
        Peak precursor = spectrum.getPrecursor();
        int charge = precursor.getCharge();
        if (maxCharge != Integer.MAX_VALUE)
            charge = maxCharge;
        Ion p = Ion.p_NH3;
        double nh3Mass = precursor.getMass() - Composition.NH3.getMolecularMass();
        for (int i = minCharge; i <= charge; i++) {
            double mz = Ion.calcMz(nh3Mass, i);
            int index = spectrum.getMostIntenseIndex(mz, fragTol);
            if (index < 0)
                continue;

            String symbol = "[M*]";
            if (i > 1)
                symbol += StringUtils.superscript(i + "+");
            MzSymbolAnnotation annotation = new MzSymbolAnnotation(mz, i, symbol, p);
            spectrum.addAnnotation(index, annotation);
        }
    }

    /**
     * Add annotation of precursor of all possible charges.
     *
     * @param spectrum {@link MsnSpectrum}
     * @param fragTol  {@link Tolerance} to match peaks.
     */
    public static void annotatePrecursorH2O(MsnSpectrum spectrum, Tolerance fragTol, int minCharge, int maxCharge)
    {
        Peak precursor = spectrum.getPrecursor();
        int charge = precursor.getCharge();
        if (maxCharge != Integer.MAX_VALUE)
            charge = maxCharge;
        Ion p = Ion.p_H2O;
        double h2oMass = precursor.getMass() - Composition.H2O.getMolecularMass();
        for (int i = minCharge; i <= charge; i++) {
            double mz = Ion.calcMz(h2oMass, i);
            int index = spectrum.getMostIntenseIndex(mz, fragTol);
            if (index < 0)
                continue;

            String symbol = "[M°]";
            if (i > 1)
                symbol += StringUtils.superscript(i + "+");
            MzSymbolAnnotation annotation = new MzSymbolAnnotation(mz, i, symbol, p);
            spectrum.addAnnotation(index, annotation);
        }
    }
}
