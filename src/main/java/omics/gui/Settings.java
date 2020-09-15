package omics.gui;

import omics.gui.setting.GapSetting;
import omics.gui.setting.PTMSetting;
import omics.gui.setting.PercolatorSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 11 Sep 2020, 6:17 PM
 */
public class Settings
{
    private final PercolatorSetting percolatorSetting = new PercolatorSetting();
    private final PTMSetting ptmSetting = new PTMSetting();
    private final GapSetting gapSetting = new GapSetting();

    private final List<Setting> settingList = new ArrayList<>();

    public Settings()
    {
        settingList.add(ptmSetting);
        settingList.add(gapSetting);
        settingList.add(percolatorSetting);
    }

    /**
     * @return percolator path
     */
    public String getPercolatorPath()
    {
        return percolatorSetting.getPath();
    }

    /**
     * @return all {@link Setting}
     */
    public List<Setting> getSettingList()
    {
        return settingList;
    }
}
