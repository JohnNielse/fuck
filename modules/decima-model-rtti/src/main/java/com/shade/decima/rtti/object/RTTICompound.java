package com.shade.decima.rtti.object;

import com.shade.decima.rtti.data.RTTIDataCompound;
import com.shade.util.NotNull;

public interface RTTICompound {
    @NotNull
    <T> T get(@NotNull RTTIDataCompound.Attr<? extends T> attr);

    <T> void set(@NotNull RTTIDataCompound.Attr<? super T> attr, @NotNull T value);

    @NotNull
    RTTIDataCompound getType();
}
