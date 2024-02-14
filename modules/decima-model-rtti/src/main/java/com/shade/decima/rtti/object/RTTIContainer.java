package com.shade.decima.rtti.object;

import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataContainer;
import com.shade.util.NotNull;

public interface RTTIContainer<T> extends Iterable<T> {
    @NotNull
    T get(int index);

    void set(int index, @NotNull T value);

    void add(@NotNull T value);

    void insert(int index, @NotNull T value);

    void remove(int index);

    int size();

    @NotNull
    RTTI<T> getItemType();

    @NotNull
    RTTIDataContainer<T> getContainerType();
}
