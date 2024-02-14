package com.shade.decima.rtti;

import com.shade.decima.rtti.data.RTTI;
import com.shade.util.NotNull;

import java.nio.ByteBuffer;

/**
 * A class responsible for reading and writing RTTI objects from and to a file.
 */
public interface RTTIReader {
    @NotNull
    RTTICoreFile read(@NotNull RTTIFactory factory, @NotNull byte[] data);

    @NotNull
    <T> T read(@NotNull RTTI<T> rtti, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer);
}
