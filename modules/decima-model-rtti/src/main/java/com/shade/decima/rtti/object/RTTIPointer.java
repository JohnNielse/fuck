package com.shade.decima.rtti.object;

import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataPointer;
import com.shade.util.NotNull;

public interface RTTIPointer<T> {
    @NotNull
    T get();

    void set(@NotNull T value);

    @NotNull
    RTTI<T> getItemType();

    @NotNull
    RTTIDataPointer<T> getPointerType();
}
