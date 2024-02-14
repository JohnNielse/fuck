package com.shade.decima.killzone2.rtti;

import com.shade.decima.killzone2.rtti.data.*;
import com.shade.decima.killzone2.rtti.dumper.KZ2TypeDumper;
import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.data.RTTI;
import com.shade.platform.model.Lazy;
import com.shade.util.NotImplementedException;
import com.shade.util.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KZ2Factory implements RTTIFactory {
    private final Map<String, RTTI<?>> types;

    public KZ2Factory(@NotNull Path elf) throws IOException {
        this.types = new HashMap<>();

        for (var type : KZ2TypeDumper.dump(elf)) {
            final RTTI<?> converted = convert(type);

            if (types.putIfAbsent(converted.getName(), converted) != null) {
                System.out.println("Duplicate RTTI type: " + converted.getName());
            }
        }
    }

    @NotNull
    @Override
    public RTTI<?> get(@NotNull String name) {
        final RTTI<?> rtti = types.get(name);
        if (rtti == null) {
            throw new IllegalArgumentException("Unknown RTTI type: " + name);
        }
        return rtti;
    }

    @NotNull
    private RTTI<?> getOrConvert(@NotNull KZ2TypeDumper.RTTIData data) {
        final RTTI<?> rtti = types.get(data.getTypeName());
        if (rtti != null) {
            return rtti;
        }
        final RTTI<?> converted = convert(data);
        types.put(data.getTypeName(), converted);
        return converted;
    }

    @NotNull
    private RTTI<?> convert(@NotNull KZ2TypeDumper.RTTIData data) {
        if (data instanceof KZ2TypeDumper.RTTIDataCompound compound) {
            final var bases = Arrays.stream(compound.bases())
                .map(base -> new KZ2DataCompound.Base((KZ2DataCompound) getOrConvert(base.type().get()), base.offset()))
                .toList();
            final var attrs = Arrays.stream(compound.attrs())
                .filter(attr -> attr.type() != null) // TODO: handle categories
                .map(attr -> new KZ2DataCompound.Attr<>(getOrConvert(attr.type().get()), attr.name(), attr.offset()))
                .toList();
            return new KZ2DataCompound(compound.name(), bases, attrs);
        } else if (data instanceof KZ2TypeDumper.RTTIDataEnum enumeration) {
            final var values = Arrays.stream(enumeration.values())
                .map(value -> new KZ2DataEnum.Value(value.name(), value.value()))
                .toList();
            return new KZ2DataEnum(enumeration.name(), values, enumeration.size());
        } else if (data instanceof KZ2TypeDumper.RTTIDataContainer container) {
            return new KZ2DataContainer<>(container.data().name(), getOrConvert(container.itemType().get()));
        } else if (data instanceof KZ2TypeDumper.RTTIDataPointer pointer) {
            return new KZ2DataPointer<>(pointer.data().name(), Lazy.of(() -> getOrConvert(pointer.itemType().get())));
        } else if (data instanceof KZ2TypeDumper.RTTIDataAtom atom) {
            if (atom == atom.baseType().get()) {
                return new KZ2DataAtom<>(atom.name(), Object.class, atom.simple());
            } else {
                return new KZ2DataAtom<>(atom.name(), Object.class, (KZ2DataAtom<?>) getOrConvert(atom.baseType().get()), atom.simple());
            }
        } else {
            throw new NotImplementedException();
        }
    }
}
