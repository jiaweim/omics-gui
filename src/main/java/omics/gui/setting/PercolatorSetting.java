package omics.gui.setting;

import omics.gui.Setting;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 5:07 PM
 */
public class PercolatorSetting implements Setting
{
    private String path;

    public PercolatorSetting() { }

    /**
     * @return runnable precolator path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * set the path of runnable percolator
     *
     * @param path percolator path
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    @Override
    public String getName()
    {
        return "Percolator";
    }
}
