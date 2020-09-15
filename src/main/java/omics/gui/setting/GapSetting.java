package omics.gui.setting;

import omics.gui.Setting;
import omics.pdk.ident.model.Delta;
import omics.pdk.ident.model.DeltaFactory;

import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 14 Sep 2020, 2:26 PM
 */
public class GapSetting implements Setting
{
    private final DeltaFactory deltaFactory = DeltaFactory.getInstance();

    public GapSetting() { }

    public DeltaFactory getDeltaFactory()
    {
        return deltaFactory;
    }

    public List<Delta> getDeltaList()
    {
        return deltaFactory.getItemList();
    }

    public boolean contains(Delta delta)
    {
        return deltaFactory.contains(delta);
    }

    /**
     * add a new {@link Delta}
     *
     * @param delta a {@link Delta} instance
     * @return true if add successfully
     */
    public boolean add(Delta delta)
    {
        return deltaFactory.add(delta);
    }

    public void save()
    {
        deltaFactory.write();
    }

    @Override
    public String getName()
    {
        return "Gaps";
    }
}
