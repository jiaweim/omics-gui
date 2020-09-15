package omics.gui.task;

import omics.pdk.ident.SearchParameters;
import omics.util.protein.database.FastaSequence;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 8:43 PM
 */
public class InitialState
{
    private SearchParameters searchParameters;
    private FastaSequence targetSequence;
    private FastaSequence decoySequence;
    private int[] targetLength2Count;
    private int[] decoyLength2Count;

    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

    public void setSearchParameters(SearchParameters searchParameters)
    {
        this.searchParameters = searchParameters;
    }

    public FastaSequence getTargetSequence()
    {
        return targetSequence;
    }

    public void setTargetSequence(FastaSequence targetSequence)
    {
        this.targetSequence = targetSequence;
    }

    public FastaSequence getDecoySequence()
    {
        return decoySequence;
    }

    public void setDecoySequence(FastaSequence decoySequence)
    {
        this.decoySequence = decoySequence;
    }

    public int[] getTargetLength2Count()
    {
        return targetLength2Count;
    }

    public void setTargetLength2Count(int[] targetLength2Count)
    {
        this.targetLength2Count = targetLength2Count;
    }

    public int[] getDecoyLength2Count()
    {
        return decoyLength2Count;
    }

    public void setDecoyLength2Count(int[] decoyLength2Count)
    {
        this.decoyLength2Count = decoyLength2Count;
    }
}
