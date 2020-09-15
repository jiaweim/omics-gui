package omics.gui.task;

import javafx.concurrent.Task;
import omics.pdk.ident.SearchParameters;
import omics.util.io.FilenameUtils;
import omics.util.protein.AminoAcidSet;
import omics.util.protein.database.FastaSequence;
import omics.util.protein.database.SuffixArraySequence;
import omics.util.protein.database.SuffixIterator;
import omics.util.protein.database.util.DoShuffleDB;
import omics.util.protein.digest.Protease;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Initialization task for database search.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 12 Jan 2020, 11:33 AM
 */
public class InitialTask extends Task<InitialState>
{
    private SearchParameters parameters;

    public InitialTask(SearchParameters searchParameters)
    {
        this.parameters = searchParameters;
    }

    @Override
    protected InitialState call()
    {
        updateTitle("Initialize database");
        InitialState initialState = new InitialState();

        updateProgress(20, 100);

        if (isCancelled()) {
            return initialState;
        }

        updateMessage("Initialize target database");
        Path database = parameters.getDatabase();
        Path targetDatabase = FilenameUtils.newExtension(database, DoShuffleDB.TARGET_DB_EXT);
        if (Files.notExists(targetDatabase)) {
            try {
                Files.copy(database, targetDatabase);
            } catch (IOException e) {
                setException(e);
                e.printStackTrace();
            }
        }

        if (isCancelled()) {
            return initialState;
        }

        FastaSequence targetSequence = new FastaSequence(targetDatabase.toString());
        AminoAcidSet aaSet = parameters.getAminoAcidSet();
        aaSet.setAminoAcidProbabilities(targetSequence);

        updateProgress(60, 100);

        if (isCancelled()) {
            return initialState;
        }

        Protease protease = parameters.getProtease();
        Optional<Protease> secondProtease = parameters.getSecondProtease();
        if (secondProtease.isPresent()) {
            aaSet.registerEnzyme(protease.getEnzyme(), secondProtease.get().getEnzyme());
        } else {
            aaSet.registerEnzyme(protease.getEnzyme());
        }

        initialState.setTargetSequence(targetSequence);
        int[] targetCount = getLength2CountArray(targetSequence, protease.isUnspecific());
        initialState.setTargetLength2Count(targetCount);

        if (isCancelled()) {
            return initialState;
        }

        updateProgress(70, 100);
        if (parameters.isUseTDA()) {
            updateMessage("Initialize decoy database");
            Path decoyDatabase = FilenameUtils.newExtension(database, DoShuffleDB.DECOY_DB_EXT);
            if (Files.notExists(decoyDatabase)) {
                DoShuffleDB shuffleDB = new DoShuffleDB(database, decoyDatabase, DoShuffleDB.DecoyType.REVERSE,
                        false, DoShuffleDB.DECOY_PROTEIN_PREFIX, DoShuffleDB.TagPos.BEFORE_ACC);
                try {
                    shuffleDB.go();
                } catch (Exception e) {
                    setException(e);
                    e.printStackTrace();
                }
            }

            if (isCancelled()) {
                return initialState;
            }

            FastaSequence decoySequence = new FastaSequence(decoyDatabase.toString());
            initialState.setDecoySequence(decoySequence);
            initialState.setDecoyLength2Count(getLength2CountArray(decoySequence, protease.isUnspecific()));
        }
        updateProgress(100, 100);
        updateValue(initialState);
        return initialState;
    }

    private int[] getLength2CountArray(FastaSequence fastaSequence, boolean isUnspecific)
    {
        SuffixIterator si = new SuffixIterator(new SuffixArraySequence(fastaSequence));
        int maxPeptideLength = si.getMaxPeptideLength();
        int[] count = new int[maxPeptideLength + 1];
        if (isUnspecific) {
            for (int i = 1; i <= maxPeptideLength; i++) {
                count[i] = si.getPeptideCount(i);
            }
        } else {
            for (int i = 1; i < maxPeptideLength; i++) {
                count[i] = si.getPeptideCount(i + 1);
            }
        }
        try {
            si.close();
        } catch (IOException e) {
            setException(e);
            e.printStackTrace();
        }

        return count;
    }
}
