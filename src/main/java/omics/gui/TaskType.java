package omics.gui;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

/**
 * This class is used to supply icon for different types of task.
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Dec 2019, 11:14 AM
 */
public enum TaskType
{
    OPEN_FILE,
    CHOOSE_FILE,
    OPEN_FOLDER,
    DELETE,
    READ_PSM,
    READ_MS,
    VIEW,
    RUN,
    STOP,
    REFRESH,
    SETTING;

    private final FontAwesome fontAwesome = new FontAwesome();

    /**
     * Return a icon for this task type.
     *
     * @param size icon size
     * @return a {@link Node} object.
     */
    public Glyph getIcon(double size, Color color)
    {
        Glyph glyph = null;
        switch (this) {
            case OPEN_FILE:
                glyph = fontAwesome.create(FontAwesome.Glyph.FILE_TEXT_ALT).size(size);
                break;
            case CHOOSE_FILE:
                glyph = fontAwesome.create(FontAwesome.Glyph.FILE).size(size);
                break;
            case OPEN_FOLDER:
                glyph = fontAwesome.create(FontAwesome.Glyph.FOLDER_OPEN_ALT).size(size);
                break;
            case DELETE:
                glyph = fontAwesome.create(FontAwesome.Glyph.TRASH_ALT).size(size);
                break;
            case READ_PSM:
                glyph = fontAwesome.create(FontAwesome.Glyph.SITEMAP).size(size);
                break;
            case READ_MS:
                glyph = fontAwesome.create(FontAwesome.Glyph.BAR_CHART).size(size);
                break;
            case VIEW:
                glyph = fontAwesome.create(FontAwesome.Glyph.TH_LIST).size(size);
                break;
            case RUN:
                glyph = fontAwesome.create(FontAwesome.Glyph.FORWARD).size(size);
                break;
            case STOP:
                glyph = fontAwesome.create(FontAwesome.Glyph.STOP).size(size);
                break;
            case SETTING:
                glyph = fontAwesome.create(FontAwesome.Glyph.COG).size(size);
                break;
            case REFRESH:
                glyph = fontAwesome.create(FontAwesome.Glyph.REFRESH).size(size);
        }
        if (glyph != null)
            glyph.color(color);
        return glyph;
    }

    /**
     * Return a icon for this task type with default size 14px;
     *
     * @return a {@link Node} object.
     */
    public Glyph getIcon(Color color)
    {
        return getIcon(14, color);
    }

    /**
     * Return a icon for this task type with default size 14px,
     * and default color {@link Color#BLACK}
     *
     * @return a {@link Node} object.
     */
    public Glyph getIcon()
    {
        return getIcon(14, Color.BLACK);
    }
}
