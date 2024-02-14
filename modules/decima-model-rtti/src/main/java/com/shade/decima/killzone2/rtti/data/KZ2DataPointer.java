package com.shade.decima.killzone2.rtti.data;

import com.shade.decima.killzone2.rtti.KZ2Reader;
import com.shade.decima.killzone2.rtti.object.KZ2Pointer;
import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.RTTIReader;
import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataPointer;
import com.shade.decima.rtti.object.RTTIPointer;
import com.shade.platform.model.Lazy;
import com.shade.util.NotNull;

import java.nio.ByteBuffer;

public class KZ2DataPointer<T> implements RTTIDataPointer<T> {
    private final String name;
    private final Lazy<RTTI<T>> type;

    public KZ2DataPointer(@NotNull String name, @NotNull Lazy<RTTI<T>> type) {
        this.name = name;
        this.type = type;
    }

    @NotNull
    @Override
    public RTTIPointer<T> read(@NotNull RTTIReader reader, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer) {
        final byte kind = buffer.get();
        return switch (kind) {
            case 0 -> new KZ2Pointer<>(KZ2Pointer.Kind.POINTER, KZ2Reader.getVarInt(buffer), type.get(), this);
            case 2 -> new KZ2Pointer<>(KZ2Pointer.Kind.DEPENDENT_LINK, KZ2Reader.getVarInt(buffer), type.get(), this);
            default -> throw new IllegalArgumentException("Unknown pointer kind: " + kind);
        };
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public RTTI<T> getItemType() {
        return type.get();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
