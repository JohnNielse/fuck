package com.shade.decima.rtti.marker;

import com.shade.util.NotNull;

public interface DynamicallySized<T> {
    int getSize(@NotNull T t);
}
