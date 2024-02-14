package com.shade.decima.killzone2.rtti;

import com.shade.decima.rtti.RTTICoreFile;
import com.shade.decima.rtti.object.RTTICompound;
import com.shade.util.NotNull;

import java.util.Iterator;
import java.util.List;

public record KZ2CoreFile(@NotNull List<? extends RTTICompound> objects) implements RTTICoreFile {
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<RTTICompound> iterator() {
        return (Iterator<RTTICompound>) objects.iterator();
    }
}
