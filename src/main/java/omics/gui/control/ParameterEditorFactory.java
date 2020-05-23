package omics.gui.control;

import omics.gui.parameter.Parameter;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.HashMap;
import java.util.Map;

class ParameterEditorFactory extends DefaultPropertyEditorFactory
{
    private final Map<PropertySheet.Item, PropertyEditor<?>> editorsMap;

    public ParameterEditorFactory()
    {
        this.editorsMap = new HashMap<>();
    }

    @Override
    public PropertyEditor<?> call(PropertySheet.Item item)
    {
        if (!(item instanceof Parameter))
            throw new IllegalArgumentException("This ParameterEditorFactory can be only used for Parameter instances");

        PropertyEditor<?> editor = super.call(item);

        // Save the reference for the editor
        editorsMap.put(item, editor);

        return editor;
    }

    @SuppressWarnings("unchecked")
    <ValueType> PropertyEditor<ValueType> getEditorForItem(PropertySheet.Item item)
    {
        return (PropertyEditor<ValueType>) editorsMap.get(item);
    }
}
