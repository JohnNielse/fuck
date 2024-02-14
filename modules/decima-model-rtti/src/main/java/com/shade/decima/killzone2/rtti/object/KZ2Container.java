package com.shade.decima.killzone2.rtti.object;

import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataContainer;
import com.shade.decima.rtti.object.RTTIContainer;
import com.shade.util.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record KZ2Container<T>(@NotNull List<T> items, @NotNull RTTI<T> itemType, @NotNull RTTIDataContainer<T> containerType) implements RTTIContainer<T> {
    public KZ2Container(@NotNull RTTI<T> itemType, @NotNull RTTIDataContainer<T> containerType) {
        this(new ArrayList<>(), itemType, containerType);
    }

    @NotNull
    @Override
    public T get(int index) {
        return items.get(index);
    }

    @Override
    public void set(int index, @NotNull T value) {
        items.set(index, value);
    }

    @Override
    public void add(@NotNull T value) {
        items.add(value);
    }

    @Override
    public void insert(int index, @NotNull T value) {
        items.add(index, value);
    }

    @Override
    public void remove(int index) {
        items.remove(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @NotNull
    @Override
    public RTTI<T> getItemType() {
        return itemType;
    }

    @NotNull
    @Override
    public RTTIDataContainer<T> getContainerType() {
        return containerType;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        return containerType.getName() + items;
    }
}
