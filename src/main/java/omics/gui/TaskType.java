package omics.gui;

import javafx.scene.Node;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 28 Dec 2019, 11:14 AM
 */
public enum TaskType
{
    READ_PSM,
    READ_MS,
    MS_VIEW,
    SETTING;

    /**
     * Return a icon for this task type.
     *
     * @param size icon size
     * @return a {@link Node} object.
     */
    public Node getIcon(double size)
    {
        FontAwesome fontAwesome = new FontAwesome();
        Glyph glyph = null;
        switch (this) {
            case READ_PSM:
                glyph = fontAwesome.create(FontAwesome.Glyph.CHILD).size(size);
                break;
            case READ_MS:
                glyph = fontAwesome.create(FontAwesome.Glyph.SITEMAP).size(size);
                break;
            case MS_VIEW:
                glyph = fontAwesome.create(FontAwesome.Glyph.ADJUST).size(size);
                break;
            case SETTING:
                glyph = fontAwesome.create(FontAwesome.Glyph.COGS).size(size);
                break;
        }
        return glyph;
    }

    /**
     * Return a icon for this task type with default size 24px;
     *
     * @return a {@link Node} object.
     */
    public Node getIcon()
    {
        return getIcon(24);
    }
}
