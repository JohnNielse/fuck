package com.shade.decima.rtti.data;

import com.shade.util.NotNull;

public non-sealed interface RTTIDataAtom<T> extends RTTI<T> {
    @NotNull
    RTTIDataAtom<?> getBaseType();
}
