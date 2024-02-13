package com.shade.decima.rtti.data;

import com.shade.decima.rtti.object.RTTIContainer;
import com.shade.util.NotNull;

public non-sealed interface RTTIDataContainer<T> extends RTTI<RTTIContainer<T>> {
    @NotNull
    RTTI<T> getItemType();
}
