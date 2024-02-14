package com.shade.decima.killzone2.rtti.data;

import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.RTTIReader;
import com.shade.decima.rtti.data.RTTIDataAtom;
import com.shade.platform.model.util.BufferUtils;
import com.shade.util.NotImplementedException;
import com.shade.util.NotNull;

import java.nio.ByteBuffer;

public class KZ2DataAtom<T> implements RTTIDataAtom<T> {
    private final String name;
    private final Class<T> type;
    private final KZ2DataAtom<?> baseType;
    private final boolean simple;

    public KZ2DataAtom(@NotNull String name, @NotNull Class<T> type, @NotNull KZ2DataAtom<?> baseType, boolean simple) {
        this.name = name;
        this.type = type;
        this.baseType = baseType;
        this.simple = simple;
    }

    public KZ2DataAtom(@NotNull String name, @NotNull Class<T> type, boolean simple) {
        this.name = name;
        this.type = type;
        this.simple = simple;
        this.baseType = this;
    }

    @NotNull
    @Override
    public T read(@NotNull RTTIReader reader, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer) {
        return switch (name) {
            case "String" -> type.cast(BufferUtils.getString(buffer, buffer.get() & 0xff /* TODO: Might be VarInt */));
            case "bool" -> type.cast(buffer.get() != 0);
            case "uint8" -> type.cast(buffer.get());
            case "uint16" -> type.cast(buffer.getShort());
            case "int", "uint32" -> type.cast(buffer.getInt());
            case "float" -> type.cast(buffer.getFloat());
            case "HalfFloat" -> type.cast(BufferUtils.getHalfFloat(buffer));
            default -> throw new NotImplementedException();
        };
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public Class<T> getInstanceType() {
        return type;
    }

    @NotNull
    @Override
    public RTTIDataAtom<?> getBaseType() {
        return baseType;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public boolean isSimple() {
        return simple;
    }
}
