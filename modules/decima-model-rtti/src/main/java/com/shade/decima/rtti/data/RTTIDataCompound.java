package com.shade.decima.rtti.data;

import com.shade.decima.rtti.object.RTTICompound;
import com.shade.util.NotNull;

import java.util.List;

public non-sealed interface RTTIDataCompound extends RTTI<RTTICompound> {
    interface Base {
        @NotNull
        RTTIDataCompound getType();

        int getOffset();
    }

    interface Attr<T> {
        @NotNull
        RTTI<T> getType();

        @NotNull
        String getName();

        int getOffset();
    }

    @NotNull
    List<? extends Base> getBases();

    @NotNull
    List<? extends Attr<?>> getAttrs();

    @NotNull
    @Override
    default Class<RTTICompound> getInstanceType() {
        return RTTICompound.class;
    }
}
