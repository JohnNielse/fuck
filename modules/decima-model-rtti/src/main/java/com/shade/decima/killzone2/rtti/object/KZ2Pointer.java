package com.shade.decima.killzone2.rtti.object;

import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataPointer;
import com.shade.decima.rtti.object.RTTIPointer;
import com.shade.util.NotImplementedException;
import com.shade.util.NotNull;

public record KZ2Pointer<T>(@NotNull Kind kind, int index, @NotNull RTTI<T> itemType, @NotNull RTTIDataPointer<T> pointerType) implements RTTIPointer<T> {
    public enum Kind {
        POINTER,
        DEPENDENT_LINK
    }

    @NotNull
    @Override
    public T get() {
        throw new NotImplementedException();
    }

    @Override
    public void set(@NotNull T value) {
        throw new NotImplementedException();
    }

    @NotNull
    @Override
    public RTTI<T> getItemType() {
        return itemType;
    }

    @NotNull
    @Override
    public RTTIDataPointer<T> getPointerType() {
        return pointerType;
    }

    @Override
    public String toString() {
        return pointerType + "[" + index + "]";
    }
}
