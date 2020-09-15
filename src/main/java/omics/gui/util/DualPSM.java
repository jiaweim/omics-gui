package omics.gui.util;

import omics.util.protein.Peptide;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 03 Sep 2020, 12:16 PM
 */
public class DualPSM
{
    private Peptide topPeptide;
    private int topScan;
    private String topFile;
    private String topTitle;
    private String topSubTitle;

    private Peptide bottomPeptide;
    private int bottomScan;
    private String bottomFile;
    private String bottomTitle;
    private String bottomSubTitle;

    public String getTopKey()
    {
        return topFile + "_" + topScan;
    }

    public String getBottomKey()
    {
        return bottomFile + "_" + bottomScan;
    }

    public Peptide getTopPeptide()
    {
        return topPeptide;
    }

    public void setTopPeptide(Peptide topPeptide)
    {
        this.topPeptide = topPeptide;
    }

    public int getTopScan()
    {
        return topScan;
    }

    public void setTopScan(int topScan)
    {
        this.topScan = topScan;
    }

    public String getTopFile()
    {
        return topFile;
    }

    public void setTopFile(String topFile)
    {
        this.topFile = topFile;
    }

    public String getTopTitle()
    {
        return topTitle;
    }

    public void setTopTitle(String topTitle)
    {
        this.topTitle = topTitle;
    }

    public String getTopSubTitle()
    {
        return topSubTitle;
    }

    public void setTopSubTitle(String topSubTitle)
    {
        this.topSubTitle = topSubTitle;
    }

    public Peptide getBottomPeptide()
    {
        return bottomPeptide;
    }

    public void setBottomPeptide(Peptide bottomPeptide)
    {
        this.bottomPeptide = bottomPeptide;
    }

    public int getBottomScan()
    {
        return bottomScan;
    }

    public void setBottomScan(int bottomScan)
    {
        this.bottomScan = bottomScan;
    }

    public String getBottomFile()
    {
        return bottomFile;
    }

    public void setBottomFile(String bottomFile)
    {
        this.bottomFile = bottomFile;
    }

    public String getBottomTitle()
    {
        return bottomTitle;
    }

    public void setBottomTitle(String bottomTitle)
    {
        this.bottomTitle = bottomTitle;
    }

    public String getBottomSubTitle()
    {
        return bottomSubTitle;
    }

    public void setBottomSubTitle(String bottomSubTitle)
    {
        this.bottomSubTitle = bottomSubTitle;
    }
}
