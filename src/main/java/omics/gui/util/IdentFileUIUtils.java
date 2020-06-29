package omics.gui.util;

import javafx.stage.FileChooser;
import omics.pdk.io.ResultFileType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 29 Jun 2020, 2:05 PM
 */
public class IdentFileUIUtils
{
    public static List<FileChooser.ExtensionFilter> getIdentFileFilters()
    {
        List<FileChooser.ExtensionFilter> filterList = new ArrayList<>();
        filterList.add(new FileChooser.ExtensionFilter("All", "*.*"));
        filterList.add(new FileChooser.ExtensionFilter(ResultFileType.MzIdentML.getName(), ResultFileType.MzIdentML.getExtension()));
        filterList.add(new FileChooser.ExtensionFilter(ResultFileType.MASCOT_CSV.getName(), ResultFileType.MASCOT_CSV.getExtension()));
        filterList.add(new FileChooser.ExtensionFilter(ResultFileType.MASCOT_DAT.getName(), ResultFileType.MASCOT_DAT.getExtension()));
        filterList.add(new FileChooser.ExtensionFilter(ResultFileType.OMICS_EXCEL.getName(), ResultFileType.OMICS_EXCEL.getExtension()));
        filterList.add(new FileChooser.ExtensionFilter(ResultFileType.PEP_XML.getName(), ResultFileType.PEP_XML.getExtension()));

        return filterList;
    }
}
