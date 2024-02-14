package com.shade.decima.rtti.data;

import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.RTTIReader;
import com.shade.util.NotNull;

import java.nio.ByteBuffer;

public sealed interface RTTI<T> permits RTTIDataAtom, RTTIDataCompound, RTTIDataContainer, RTTIDataEnum, RTTIDataPointer {
    @NotNull
    T read(@NotNull RTTIReader reader, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer);

    @NotNull
    String getName();

    @NotNull
    default String getFullName() {
        return getName();
    }

    @NotNull
    Class<T> getInstanceType();
}
