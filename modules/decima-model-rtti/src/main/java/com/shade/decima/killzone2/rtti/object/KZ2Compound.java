package com.shade.decima.killzone2.rtti.object;

import com.shade.decima.rtti.data.RTTIDataCompound;
import com.shade.decima.rtti.object.RTTICompound;
import com.shade.util.NotNull;

import java.util.HashMap;
import java.util.Map;

public record KZ2Compound(@NotNull RTTIDataCompound type, @NotNull Map<RTTIDataCompound.Attr<?>, Object> data) implements RTTICompound {
    public KZ2Compound(@NotNull RTTIDataCompound type) {
        this(type, new HashMap<>());
    }

    @NotNull
    @Override
    public <T> T get(@NotNull RTTIDataCompound.Attr<? extends T> attr) {
        return attr.getType().getInstanceType().cast(data.get(attr));
    }

    @Override
    public <T> void set(@NotNull RTTIDataCompound.Attr<? super T> attr, @NotNull T value) {
        data.put(attr, attr.getType().getInstanceType().cast(value));
    }

    @NotNull
    @Override
    public RTTIDataCompound getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString() + "{...}";
    }
}
