package omics.gui.util;

import javafx.util.StringConverter;
import omics.util.protein.digest.Enzyme;

/**
 * @author JiaweiMao
 * @version 1.0.0
 * @since 09 Dec 2019, 10:09 PM
 */
public class EnzymeStringConverter extends StringConverter<Enzyme>
{
    @Override
    public String toString(Enzyme object)
    {
        return object.getName();
    }

    @Override
    public Enzyme fromString(String string)
    {
        return Enzyme.ofName(string);
    }
}
