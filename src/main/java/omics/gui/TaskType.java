package omics.gui;

import javafx.scene.Node;
import javafx.scene.paint.Color;
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
    VIEW,
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
            case READ_PSM:
                glyph = fontAwesome.create(FontAwesome.Glyph.SITEMAP).size(size);
                break;
            case READ_MS:
                glyph = fontAwesome.create(FontAwesome.Glyph.BAR_CHART).size(size);
                break;
            case VIEW:
                glyph = fontAwesome.create(FontAwesome.Glyph.TH_LIST).size(size);
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
     * Return a icon for this task type with default size 16px;
     *
     * @return a {@link Node} object.
     */
    public Glyph getIcon(Color color)
    {
        return getIcon(16, color);
    }

    /**
     * Return a icon for this task type with default size 16px,
     * and default color {@link Color#BLACK}
     *
     * @return a {@link Node} object.
     */
    public Glyph getIcon()
    {
        return getIcon(16, Color.BLACK);
    }
}
