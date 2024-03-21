package com.shade.decima.ui.data.managers;

import com.shade.decima.model.rtti.objects.RTTIObject;
import com.shade.decima.ui.data.MutableValueController;
import com.shade.decima.ui.data.MutableValueController.EditType;
import com.shade.decima.ui.data.ValueEditor;
import com.shade.decima.ui.data.ValueManager;
import com.shade.decima.ui.data.editors.GGUUIDValueEditor;
import com.shade.decima.ui.data.registry.ValueHandlerRegistration.Selector;
import com.shade.decima.ui.data.registry.ValueHandlerRegistration.Type;
import com.shade.decima.ui.data.registry.ValueManagerRegistration;
import com.shade.util.NotNull;

@ValueManagerRegistration({
    @Selector(type = @Type(name = "GGUUID"))
})
public class GGUUIDValueManager implements ValueManager<RTTIObject> {
    @NotNull
    @Override
    public ValueEditor<RTTIObject> createEditor(@NotNull MutableValueController<RTTIObject> controller) {
        if (controller.getEditType() == EditType.INLINE) {
            return new GGUUIDValueEditor(controller);
        } else {
            throw new IllegalArgumentException("Unsupported edit type: " + controller.getEditType());
        }
    }

    @Override
    public boolean canEdit(@NotNull EditType type) {
        return type == EditType.INLINE;
    }
}
