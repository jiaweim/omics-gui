package omics.gui.task;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import javafx.concurrent.Task;
import omics.pdk.IdentResult;
import omics.pdk.ident.model.*;
import omics.pdk.parameters.IdentParameters;
import omics.pdk.pia.ProteinInference;
import omics.pdk.psm.PSMKeyFunc;
import omics.pdk.psm.consumer.CalcLogEValue;
import omics.pdk.psm.filter.PSMScoreFilter;
import omics.pdk.psm.filter.RemoveDecoyProtein;
import omics.pdk.psm.score.CalcPSMQValue;
import omics.util.MetaKey;
import omics.util.OmicsException;
import omics.util.io.FilenameUtils;
import omics.util.ms.MsDataId;
import omics.util.protein.database.Protein;
import omics.util.protein.database.ProteinDB;
import omics.util.utils.DelegateFilter;
import omics.util.utils.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Combine multiple mzid files to omics-excel.
 *
 * @author JiaweiMao
 * @version 2.0.0
 * @since 24 Oct 2018, 10:27 AM
 */
public class ExportResultTask extends Task<Void>
{
    private final double fdr;
    private final double maxEValue;
    private final int minRankScore;
    private final int topN;
    private final boolean onlyKeepDelta;
    private final List<File> fileList;
    private final String targetFile;

    private final String decoyTag;
    private boolean removeSameset;
    private String fasta;

    private final List<String> keepFields = Arrays.asList("Delta Mass", "Delta Name", "Isotope");

    /**
     * Constructor.
     *
     * @param fdr           false discovery rate
     * @param maxEValue     E-value upper limit
     * @param minRankScore  score lower limit
     * @param topN          top-N PSMs to keep for each spectrum
     * @param onlyKeepDelta true if only keep PSM with identified {@link omics.pdk.ident.model.Delta}
     * @param fileList      identification {@link File}s to process
     * @param targetFile    output file
     * @param decoyTag      tag to identify decoy proteins
     */
    public ExportResultTask(double fdr, double maxEValue, int minRankScore, int topN, boolean onlyKeepDelta,
            List<File> fileList, String targetFile, String decoyTag)
    {
        this.fdr = fdr;
        this.maxEValue = maxEValue;
        this.minRankScore = minRankScore;
        this.topN = topN;
        this.onlyKeepDelta = onlyKeepDelta;
        this.fileList = fileList;
        this.targetFile = targetFile;
        this.decoyTag = decoyTag;
    }

    public void setRemoveSameset(boolean removeSameset)
    {
        this.removeSameset = removeSameset;
    }

    public void setFasta(String fasta)
    {
        this.fasta = fasta;
    }

    @Override
    protected Void call() throws OmicsException
    {
        updateTitle("Generate result");
        if (removeSameset) {
            if (fasta == null || Files.notExists(Paths.get(fasta))) {
                updateMessage("Fasta file do not exists.");
                return null;
            }
        }

        DelegateFilter<PeptideSpectrumMatch> filterList = new DelegateFilter<>();
        filterList.add(PSMScoreFilter.atLeast(Score.RANK_SCORE, minRankScore));
        filterList.add(PSMScoreFilter.atMost(Score.PSM_E_VALUE, maxEValue));
        if (onlyKeepDelta) {
            filterList.add(filterable -> StringUtils.isNotEmpty(filterable.getMetaString("Delta Name").trim()));
        }

        IdentParameters parameters = null;
        Comparator<PeptideSpectrumMatch> scoreComp = Score.RANK_SCORE::compare;
        ArrayListMultimap<String, PeptideSpectrumMatch> psmMap = ArrayListMultimap.create();
        Set<MsDataId> idSet = new HashSet<>();
        for (File file : fileList) {
            updateMessage("Reading " + file.getName());
            IdentResult result = IdentResult.read(file.toPath(), decoyTag);

            // the original id is not unique across multiple files.
            for (MsDataId msDataID : result.getMsDataIds()) {
                msDataID.setId(FilenameUtils.removeExtension(msDataID.getName()));
                idSet.add(msDataID);
            }
            if (parameters == null)
                parameters = result.getParameters();

            result.apply(filterList);
            ArrayListMultimap<String, PeptideSpectrumMatch> tmpMap = ArrayListMultimap.create();
            for (PeptideSpectrumMatch psm : result) {
                String key = psm.getId(PSMKeyFunc.FILE_SCAN);
                tmpMap.put(key, psm);
            }

            for (String key : tmpMap.keySet()) {
                List<PeptideSpectrumMatch> psmList = tmpMap.get(key);
                psmList.sort(scoreComp);
                List<PeptideSpectrumMatch> rankList = rank(psmList);
                psmMap.putAll(key, rankList);
            }
        }

        assert parameters != null;
        parameters.setMsDataIds(idSet);
        parameters.addPSMScore(Score.RANK_SCORE, minRankScore);
        parameters.addPSMScore(Score.PSM_E_VALUE, maxEValue);
        parameters.addPSMScore(Score.PSM_Q_VALUE, fdr);

        IdentResult identResult = new IdentResult("Identification Result").setParameters(parameters);
        for (String key : psmMap.keySet()) {
            List<PeptideSpectrumMatch> psmList = psmMap.get(key);
            psmList.sort(scoreComp);

            List<PeptideSpectrumMatch> rankList = rank(psmList);
            identResult.addAll(rankList);
        }

        identResult.apply(CalcLogEValue.with(Score.SPEC_EVALUE));
        identResult.apply(CalcPSMQValue.with(Score.LOG_E_VALUE));
        identResult.apply(PSMScoreFilter.atMost(Score.PSM_Q_VALUE, fdr));
        identResult.apply(new RemoveDecoyProtein());

        if (identResult.size() > 0) {
            identResult.apply(ProteinInference.occamRazor(Score.LOG_E_VALUE));
        }

        if (removeSameset) {
            removeSameset(identResult);
        }
        identResult.buildReference();
        // remove some fields not to output
        Set<String> fields = new HashSet<>(keepFields);
        for (PeptideSpectrumMatch psm : identResult) {
            Set<MetaKey> metaKeys = new HashSet<>(psm.getMetaKeys());
            for (MetaKey metaKey : metaKeys) {
                if (!fields.contains(metaKey.getId())) {
                    psm.removeMeta(metaKey);
                }
            }
            psm.removeScore(Score.LOG_E_VALUE);
        }
        identResult.write(targetFile);
        return null;
    }


    private List<PeptideSpectrumMatch> rank(List<PeptideSpectrumMatch> psmList)
    {
        List<PeptideSpectrumMatch> resultList = new ArrayList<>(topN);
        int preScore = Integer.MIN_VALUE;
        int rank = 0;
        for (int i = psmList.size() - 1; i >= 0; i--) {
            PeptideSpectrumMatch psm = psmList.get(i);
            Integer rawScore = psm.getScoreInt(Score.RANK_SCORE);
            if (rawScore != preScore) {
                rank++;
                preScore = rawScore;
            }
            if (rank > topN)
                break;
            psm.setRank(rank);
            resultList.add(psm);
        }
        return resultList;
    }


    private void removeSameset(IdentResult identResult) throws OmicsException
    {
        identResult.buildReference();
        ProteinDB proteinDB = ProteinDB.read(fasta);
        HashMap<String, Boolean> reviewMap = new HashMap<>(proteinDB.size());
        for (Protein protein : proteinDB) {
            reviewMap.put(protein.getAccession(), protein.isReviewed());
        }
        ListMultimap<String, ProteinMatch> proteinMap = ArrayListMultimap.create();
        for (ProteinMatch match : identResult.getProteinSet()) {
            Optional<String> groupId = match.getGroupId();
            assert groupId.isPresent();
            proteinMap.put(groupId.get(), match);
        }

        Comparator<ProteinMatch> comparator = (o1, o2) -> ComparisonChain.start()
                .compareTrueFirst(reviewMap.get(o1.getAccession()), reviewMap.get(o2.getAccession()))
                .compare(o1.getAccession(), o2.getAccession(), Ordering.natural()).result();

        Set<ProteinMatch> keptProteins = new HashSet<>();
        for (String s : proteinMap.keySet()) {
            List<ProteinMatch> proteinMatches = proteinMap.get(s);
            Set<ProteinMatch> retainedSet = new HashSet<>();
            retainedSet.add(proteinMatches.get(0));

            for (int i = 1; i < proteinMatches.size(); i++) {
                ProteinMatch match2 = proteinMatches.get(i);
                boolean foundEqual = false;
                for (ProteinMatch match1 : retainedSet) {
                    if (equal(match1, match2)) {
                        foundEqual = true;
                        int compare = comparator.compare(match1, match2);
                        if (compare > 0) {
                            retainedSet.add(match2);
                            retainedSet.remove(match1);
                        }
                    }
                }
                if (!foundEqual) {
                    retainedSet.add(match2);
                }
            }
            keptProteins.addAll(retainedSet);
        }

        for (PeptideMatch peptideMatch : identResult.getPeptideSet()) {
            Set<PeptideProteinMatch> newSet = new HashSet<>();
            for (PeptideProteinMatch peptideProteinMatch : peptideMatch.getPeptideProteinMatches()) {
                ProteinMatch proteinMatch = peptideProteinMatch.getProteinMatch();
                if (keptProteins.contains(proteinMatch)) {
                    newSet.add(peptideProteinMatch);
                }
            }
            peptideMatch.setPeptideProteinMatches(newSet);
        }
    }

    private static boolean equal(ProteinMatch match1, ProteinMatch match2)
    {
        HashSet<PeptideMatch> set1 = new HashSet<>(match1.getPeptideMatchList());
        HashSet<PeptideMatch> set2 = new HashSet<>(match2.getPeptideMatchList());
        return set1.equals(set2);
    }
}
