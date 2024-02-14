package com.shade.decima.killzone2.rtti.data;

import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.RTTIReader;
import com.shade.decima.rtti.data.RTTIDataEnum;
import com.shade.util.NotNull;

import java.nio.ByteBuffer;
import java.util.List;

public class KZ2DataEnum implements RTTIDataEnum {
    public record Value(@NotNull String name, int value) implements RTTIDataEnum.Value {
        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name + " (" + value + ")";
        }
    }

    private final String name;
    private final List<Value> values;
    private final int size;

    public KZ2DataEnum(@NotNull String name, @NotNull List<Value> values, int size) {
        this.name = name;
        this.values = values;
        this.size = size;
    }

    @NotNull
    @Override
    public RTTIDataEnum.Value read(@NotNull RTTIReader reader, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer) {
        final var value = switch (size) {
            case 1 -> buffer.get() & 0xff;
            case 2 -> buffer.getShort() & 0xffff;
            case 4 -> buffer.getInt();
            default -> throw new IllegalArgumentException("Unexpected enum size: " + size);
        };

        return valueOf(value);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public RTTIDataEnum.Value valueOf(@NotNull String name) {
        return values.stream()
            .filter(v -> v.name.equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No such value found in enum."));
    }

    @NotNull
    @Override
    public RTTIDataEnum.Value valueOf(int value) {
        return values.stream()
            .filter(v -> v.value == value)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No such value found in enum."));
    }

    @NotNull
    @Override
    public List<? extends RTTIDataEnum.Value> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
