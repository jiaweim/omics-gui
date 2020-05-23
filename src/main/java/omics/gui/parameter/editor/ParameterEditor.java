package omics.gui.parameter.editor;

import javafx.scene.Node;
import org.controlsfx.property.editor.PropertyEditor;

/**
 * Editor for parameter
 *
 * @author JiaweiMao
 * @version 1.0.0
 * @since 20 May 2020, 11:06 PM
 */
public interface ParameterEditor<T> extends PropertyEditor<T>
{
    @Override
    Node getEditor();

    @Override
    T getValue();

    @Override
    void setValue(T value);
}
