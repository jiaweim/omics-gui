package omics.gui.parameter;

import javafx.stage.FileChooser;
import omics.gui.parameter.editor.FilesEditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 21 May 2020, 2:01 PM
 */
public class FilesParameter extends AbstractParameter<List<File>>
{
    private final List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();
    private File lastOpenFolder;

    /**
     * Constructor.
     *
     * @param name        parameter name
     * @param description parameter description
     * @param category    parameter category
     */
    public FilesParameter(String name, String description, String category)
    {
        super(name, description, category, FilesEditor.class);
    }


    /**
     * Add an {@link javafx.stage.FileChooser.ExtensionFilter}
     */
    public FilesParameter addExtensionFile(FileChooser.ExtensionFilter filter)
    {
        this.extensionFilters.add(filter);
        return this;
    }

    public File getLastOpenFolder()
    {
        return lastOpenFolder;
    }

    public void setLastOpenFolder(File lastOpenFolder)
    {
        this.lastOpenFolder = lastOpenFolder;
    }

    public List<FileChooser.ExtensionFilter> getExtensionFilters()
    {
        return extensionFilters;
    }
}
