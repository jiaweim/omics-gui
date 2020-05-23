package omics.gui.parameter;

import javafx.stage.FileChooser;
import omics.gui.parameter.editor.FileEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * File name parameter
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 1:14 PM
 */
public class FileParameter extends AbstractParameter<File>
{
    public enum ChooseType
    {
        OPEN, SAVE
    }

    private List<FileChooser.ExtensionFilter> extensionFilters = null;
    private final ChooseType type;
    private File lastOpenFolder;

    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public FileParameter(String name, String description, String category, ChooseType type)
    {
        super(name, description, category, FileEditor.class);
        this.type = type;
    }

    /**
     * Add an {@link javafx.stage.FileChooser.ExtensionFilter}
     */
    public FileParameter addExtensionFile(FileChooser.ExtensionFilter filter)
    {
        if (extensionFilters == null)
            extensionFilters = new ArrayList<>();
        this.extensionFilters.add(filter);
        return this;
    }

    public ChooseType getChooseType()
    {
        return type;
    }

    public List<FileChooser.ExtensionFilter> getExtensionFilters()
    {
        return extensionFilters;
    }

    public File getLastOpenFolder()
    {
        return lastOpenFolder;
    }

    public void setLastOpenFolder(File lastOpenFolder)
    {
        this.lastOpenFolder = lastOpenFolder;
    }
}
