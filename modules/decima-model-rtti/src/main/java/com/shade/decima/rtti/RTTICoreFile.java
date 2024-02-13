package com.shade.decima.rtti;

import com.shade.decima.rtti.object.RTTICompound;
import com.shade.util.NotNull;

import java.util.List;

public interface RTTICoreFile extends Iterable<RTTICompound> {
    @NotNull
    List<? extends RTTICompound> objects();
}
