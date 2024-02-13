package com.shade.decima.rtti.data;

import com.shade.util.NotNull;

import java.util.List;

public non-sealed interface RTTIDataEnum extends RTTI<RTTIDataEnum.Value> {
    interface Value {
        @NotNull
        String getName();

        int getValue();
    }

    @NotNull
    Value valueOf(@NotNull String name);

    @NotNull
    Value valueOf(int value);

    @NotNull
    List<Value> getValues();
}
