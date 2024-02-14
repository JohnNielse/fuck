package com.shade.decima.killzone2.rtti.data;

import com.shade.decima.killzone2.rtti.object.KZ2Container;
import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.RTTIReader;
import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataContainer;
import com.shade.decima.rtti.object.RTTIContainer;
import com.shade.util.NotNull;

import java.nio.ByteBuffer;

public class KZ2DataContainer<T> implements RTTIDataContainer<T> {
    private final String name;
    private final RTTI<T> type;

    public KZ2DataContainer(@NotNull String name, @NotNull RTTI<T> type) {
        this.name = name;
        this.type = type;
    }

    @NotNull
    @Override
    public RTTIContainer<T> read(@NotNull RTTIReader reader, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer) {
        final var count = buffer.get() & 0xff; // TODO: Might be VarInt!
        final var container = new KZ2Container<>(type, this);

        for (int i = 0; i < count; i++) {
            container.add(reader.read(type, factory, buffer));
        }

        return container;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getFullName() {
        return name + '<' + type.getFullName() + '>';
    }

    @NotNull
    @Override
    public RTTI<T> getItemType() {
        return type;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
