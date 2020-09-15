package omics.gui.task;

import javafx.concurrent.Task;
import omics.pdk.IdentResult;
import omics.pdk.ident.MsAccessor;
import omics.pdk.ident.SearchIOPath;
import omics.pdk.ident.SearchParameters;
import omics.pdk.ident.SpectrumMatch;
import omics.pdk.ident.util.SpectrumMatch2IdentResult;
import omics.pdk.io.ResultFileType;
import omics.pdk.task.SearchFastaTask;
import omics.pdk.util.ThreadPoolExecutorWithProgress;
import omics.util.io.FilenameUtils;
import omics.util.protein.AminoAcidSet;
import omics.util.protein.database.FastaSequence;
import omics.util.protein.database.SuffixArraySequence;
import omics.util.protein.database.SuffixIterator;
import omics.util.protein.database.util.DoShuffleDB;
import omics.util.protein.digest.Protease;
import omics.util.utils.NumberFormatFactory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static omics.pdk.ident.SeparateMainSearch.DECOY_FASTA;

/**
 * Database search entry class, separate target and decoy search.
 *
 * @author JiaweiMao
 * @version 1.0.1
 * @since 05 Oct 2019, 5:15 PM
 */
public class OneSearch extends Task<Void>
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OneSearch.class);

    private final SearchParameters parameters;
    private final int taskUnit;
    private final int nrThread;
    private final File msFile;

    public OneSearch(SearchParameters searchParameters, int taskUnit, int nrThread, File msFile)
    {
        this.parameters = searchParameters;
        this.taskUnit = taskUnit;
        this.nrThread = nrThread;
        this.msFile = msFile;
    }

    @Override
    protected Void call() throws IOException
    {
        long time = System.currentTimeMillis();
        SearchIOPath searchIOPath = new SearchIOPath(msFile.toPath());

        initDatabase();

        executor = ThreadPoolExecutorWithProgress.newFixedThreadPool(nrThread);

        ThreadPoolExecutorWithProgress.ProgressReporter progressReporter = executor.progressReporter();
        progressReporter.progressProperty().addListener(evt -> updateProgress((Double) evt.getNewValue(), 1.0));
        progressReporter.messageProperty().addListener(evt -> logger.info((String) evt.getNewValue()));

        Path msFile = searchIOPath.getMSFile();
        logger.info(msFile.toString());
        updateTitle(msFile.getFileName().toString());

        Path targetPath = searchIOPath.getTargetPath();
        // Check the outputFile is valid for writing
        Path parent = targetPath.getParent();
        if (Files.notExists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                logger.error("The output directory {} is not exist, and create it failed.", parent);
                return null;
            }
        }

        MsAccessor accessor = readSpectrum(searchIOPath);
        if (accessor == null)
            return null;

        updateTitle("Search Target");
        logger.info("Searching target");
        search(accessor, targetSequence, targetLenCount, targetPath);

        if (parameters.isUseTDA()) {
            logger.info("Searching decoy");
            search(accessor, decoySequence, decoyLenCount, searchIOPath.getDecoyPath());
        }

        double deltaTime = (System.currentTimeMillis() - time) / (double) 1000;

        executor.shutdown();
        try {
            executor.awaitTerminationWithExceptions(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Throwable throwable) {
            if (executor.hasThrownData()) {
                logger.error(throwable.getMessage());
                throwable.printStackTrace();
            }
            throwable.printStackTrace();
        }

        logger.info(String.format("All searches complete (total elapsed time: %s)", omics.util.ms.TimeUnit.formatTime(deltaTime, NumberFormatFactory.DIGIT2)));
        targetSequence.clear();
        if (decoySequence != null)
            decoySequence.clear();
        return null;
    }

    //region generated fields
    private FastaSequence targetSequence = null;
    private FastaSequence decoySequence = null;
    private int[] targetLenCount = null;
    private int[] decoyLenCount = null;
    private ThreadPoolExecutorWithProgress executor;
    //endregion

    private void search(MsAccessor accessor, FastaSequence fastaSequence, int[] len2Count, Path outPath)
    {
        long startTime = System.currentTimeMillis();

        int numberOfScan = accessor.size();

        int numberOfTask;
        if (numberOfScan <= taskUnit) {
            numberOfTask = 1;
        } else if ((numberOfScan % taskUnit) > (taskUnit / 2))
            numberOfTask = numberOfScan / taskUnit + 1;
        else
            numberOfTask = numberOfScan / taskUnit;

        executor.setTaskCount(numberOfTask);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfTask);

        List<SpectrumMatch> matchList = new ArrayList<>();
        try {
            List<Future<List<SpectrumMatch>>> futures = new ArrayList<>(numberOfTask);
            for (int i = 0; i < numberOfTask; i++) {
                int startIndex = i * taskUnit;
                int endIndex;
                if (i == numberOfTask - 1)
                    endIndex = numberOfScan;
                else
                    endIndex = (i + 1) * taskUnit;
                SearchFastaTask task = new SearchFastaTask(accessor, startIndex, endIndex, parameters,
                        fastaSequence, countDownLatch);
                futures.add(executor.submit(task));
            }
            countDownLatch.await();

            for (Future<List<SpectrumMatch>> future : futures) {
                List<SpectrumMatch> matches = future.get();
                matchList.addAll(matches);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            executor.shutdownNow();
            System.exit(1);
        } catch (ExecutionException e) {
            e.printStackTrace();
            executor.shutdownNow();
            System.exit(1);
        } catch (OutOfMemoryError ex) {
            logger.error(ex.getMessage());
            logger.error("Task terminated; results incomplete. Please run again with a greater amount of memory, using \"-Xmx12G\", for example.");
            ex.printStackTrace();
            executor.shutdownNow();
            updateMessage("Out Of Memory");
            System.exit(1);
        } catch (Throwable ex) {
            logger.error(ex.getMessage());
            logger.error("Task terminated; results incomplete. Please run again.");
            ex.printStackTrace();
            executor.shutdownNow();
            System.exit(1);
        }

        logger.info("Writing result...");
        updateMessage("Writing result");

        SpectrumMatch2IdentResult spectrumMatch2IdentResult = new SpectrumMatch2IdentResult(parameters, fastaSequence,
                len2Count, accessor, matchList);
        spectrumMatch2IdentResult.progressProperty().addListener(evt -> updateProgress((Double) evt.getNewValue(), 1.0));
        spectrumMatch2IdentResult.go();
        IdentResult result = spectrumMatch2IdentResult.getValue();

        result.write(outPath.toAbsolutePath().toString(), ResultFileType.MzIdentML);

        String info = "Finished in " + omics.util.ms.TimeUnit.formatTime(
                (float) (System.currentTimeMillis() - startTime) / 1000, NumberFormatFactory.DIGIT2);
        updateMessage(info);
        logger.info(info);
    }

    private void initDatabase() throws IOException
    {
        Path database = parameters.getDatabase();
        Path targetDatabase = FilenameUtils.newExtension(database, DoShuffleDB.TARGET_DB_EXT);
        if (Files.notExists(targetDatabase)) {
            Files.copy(database, targetDatabase);
        }
        targetSequence = new FastaSequence(targetDatabase.toString());
        AminoAcidSet aaSet = parameters.getAminoAcidSet();
        aaSet.setAminoAcidProbabilities(targetSequence);
        targetLenCount = getLength2CountArray(targetSequence);

        Protease protease = parameters.getProtease();
        Optional<Protease> secondProtease = parameters.getSecondProtease();
        if (secondProtease.isPresent()) {
            aaSet.registerEnzyme(protease.getEnzyme(), secondProtease.get().getEnzyme());
        } else {
            aaSet.registerEnzyme(protease.getEnzyme());
        }

        if (parameters.isUseTDA()) {
            Path decoyDatabase = FilenameUtils.newExtension(database, DECOY_FASTA);
            if (Files.notExists(decoyDatabase)) {
                DoShuffleDB shuffleDB = new DoShuffleDB(database, decoyDatabase, DoShuffleDB.DecoyType.REVERSE,
                        false, DoShuffleDB.DECOY_PROTEIN_PREFIX, DoShuffleDB.TagPos.BEFORE_ACC);
                shuffleDB.go();
            }
            decoySequence = new FastaSequence(decoyDatabase.toString());
            this.decoyLenCount = getLength2CountArray(decoySequence);
        }
    }

    private int[] getLength2CountArray(FastaSequence fastaSequence) throws IOException
    {
        SuffixIterator si = new SuffixIterator(new SuffixArraySequence(fastaSequence));
        int maxPeptideLength = si.getMaxPeptideLength();
        int[] count = new int[maxPeptideLength + 1];
        if (parameters.getProtease().isUnspecific()) {
            for (int i = 1; i <= maxPeptideLength; i++) {
                count[i] = si.getPeptideCount(i);
            }
        } else {
            for (int i = 1; i < maxPeptideLength; i++) {
                count[i] = si.getPeptideCount(i + 1);
            }
        }
        si.close();

        return count;
    }

    private MsAccessor readSpectrum(SearchIOPath searchIOPath) throws IOException
    {
        String msg = "Reading spectra";
        updateMessage(msg);
        logger.info(msg);
        long startTime = System.currentTimeMillis();

        Path msFile = searchIOPath.getMSFile();
        MsAccessor specAcc = new MsAccessor(msFile, searchIOPath.getMSFileType(), parameters);
        specAcc.progressProperty().addListener(evt -> updateProgress((Double) evt.getNewValue(), 1.0));
        specAcc.go();

        int specSize = specAcc.size();
        if (specSize == 0) {
            msg = msFile.getFileName() + " does not have any valid spectrum, skip";
            updateMessage(msg);
            logger.error(msg);
            updateMessage(msFile + " does not have any valid spectrum, skip");
            return null;
        }

        logger.info("Reading spectra finished " + String.format("(elapsed time: %.2f sec)",
                (float) (System.currentTimeMillis() - startTime) / 1000));
        return specAcc;
    }
}
