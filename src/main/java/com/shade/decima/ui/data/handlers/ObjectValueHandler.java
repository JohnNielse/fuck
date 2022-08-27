package com.shade.decima.ui.data.handlers;

import com.shade.decima.model.rtti.RTTIType;
import com.shade.decima.model.rtti.objects.RTTIObject;
import com.shade.decima.model.rtti.types.RTTITypeClass;
import com.shade.decima.ui.data.ValueHandlerCollection;
import com.shade.platform.ui.controls.ColoredComponent;
import com.shade.util.NotNull;

import java.util.Collection;

public class ObjectValueHandler implements ValueHandlerCollection<RTTIObject, RTTITypeClass.Member> {
    public static final ObjectValueHandler INSTANCE = new ObjectValueHandler();

    private ObjectValueHandler() {
    }

    @Override
    public void appendInlineValue(@NotNull RTTIType<?> type, @NotNull Object value, @NotNull ColoredComponent component) {
        // no inline value
    }

    @Override
    public boolean hasInlineValue() {
        return false;
    }

    @NotNull
    @Override
    public Collection<RTTITypeClass.Member> getChildren(@NotNull RTTIType<?> type, @NotNull RTTIObject object) {
        return object.getMembers().keySet();
    }

    @NotNull
    @Override
    public String getChildName(@NotNull RTTIType<?> type, @NotNull RTTIObject object, @NotNull RTTITypeClass.Member member) {
        return member.name();
    }

    @NotNull
    @Override
    public Object getChildValue(@NotNull RTTIType<?> type, @NotNull RTTIObject object, @NotNull RTTITypeClass.Member member) {
        return object.get(member);
    }

    @NotNull
    @Override
    public RTTIType<?> getChildType(@NotNull RTTIType<?> type, @NotNull RTTIObject object, @NotNull RTTITypeClass.Member member) {
        return member.type();
    }
}