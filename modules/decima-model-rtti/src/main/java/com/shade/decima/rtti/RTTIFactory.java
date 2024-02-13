package com.shade.decima.rtti;

import com.shade.decima.rtti.data.RTTI;
import com.shade.util.NotNull;

public interface RTTIFactory {
    @NotNull
    RTTI<?> get(@NotNull String name);
}
