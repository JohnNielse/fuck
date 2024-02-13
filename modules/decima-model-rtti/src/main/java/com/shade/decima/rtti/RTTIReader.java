package com.shade.decima.rtti;

import com.shade.util.NotNull;

/**
 * A class responsible for reading and writing RTTI objects from and to a file.
 */
public interface RTTIReader {
    @NotNull
    RTTICoreFile read(@NotNull RTTIFactory factory, @NotNull byte[] data);
}
