package com.shade.decima.rtti.data;

import com.shade.decima.rtti.object.RTTIPointer;
import com.shade.util.NotNull;

public non-sealed interface RTTIDataPointer<T> extends RTTI<RTTIPointer<T>> {
    @NotNull
    RTTI<T> getItemType();
}
