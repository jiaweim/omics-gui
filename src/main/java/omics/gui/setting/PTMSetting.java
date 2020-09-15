package omics.gui.setting;

import omics.gui.Setting;
import omics.util.protein.mod.PTM;
import omics.util.protein.mod.PTMFactory;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 14 Sep 2020, 9:37 AM
 */
public class PTMSetting implements Setting
{
    private final PTMFactory ptmFactory = PTMFactory.getInstance();

    public PTMSetting() { }

    public PTMFactory getPTMFactory()
    {
        return ptmFactory;
    }

    /**
     * save ptm to default file location.
     */
    public void save()
    {
        ptmFactory.write();
    }

    /**
     * save ptm to given file
     *
     * @param file file path
     */
    public void saveTo(String file)
    {
        try {
            ptmFactory.write(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    /**
     * add a new PTM
     *
     * @param ptm {@link PTM} to add
     */
    public void add(PTM ptm)
    {
        this.ptmFactory.add(ptm);
    }

    /**
     * Return true if contain given ptm
     *
     * @param ptmName ptm name
     * @return true if contain the ptm
     */
    public boolean contains(String ptmName)
    {
        return ptmFactory.containPTM(ptmName);
    }


    /**
     * remove PTM
     *
     * @param ptm {@link PTM} to remove
     */
    public void remove(PTM ptm)
    {
        this.ptmFactory.remove(ptm);
    }

    /**
     * @return current available PTM list.
     */
    public List<PTM> getPTMList()
    {
        return ptmFactory.getPTMList();
    }

    @Override
    public String getName()
    {
        return "Modifications";
    }
}
