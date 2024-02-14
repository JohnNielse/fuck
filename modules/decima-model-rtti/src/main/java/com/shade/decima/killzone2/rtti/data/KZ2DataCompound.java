package com.shade.decima.killzone2.rtti.data;

import com.shade.decima.killzone2.rtti.object.KZ2Compound;
import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.RTTIReader;
import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataCompound;
import com.shade.decima.rtti.object.RTTICompound;
import com.shade.util.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class KZ2DataCompound implements RTTIDataCompound {
    public record Base(@NotNull KZ2DataCompound type, int offset) implements RTTIDataCompound.Base {
        @NotNull
        @Override
        public RTTIDataCompound getType() {
            return type;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return type + " @ " + offset;
        }
    }

    public record Attr<T>(@NotNull RTTI<T> type, @NotNull String name, int offset) implements RTTIDataCompound.Attr<T> {
        @NotNull
        @Override
        public RTTI<T> getType() {
            return type;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return name + " (" + type + ")";
        }
    }

    private final String name;
    private final List<Base> bases;
    private final List<? extends Attr<?>> attrs;

    public KZ2DataCompound(@NotNull String name, @NotNull List<Base> bases, @NotNull List<? extends Attr<?>> attrs) {
        this.name = name;
        this.bases = bases;
        this.attrs = attrs;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public RTTICompound read(@NotNull RTTIReader reader, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer) {
        final List<AttrInfo> attrs = new ArrayList<>();
        collectAttrs(attrs, 0);

        final KZ2Compound compound = new KZ2Compound(this);
        for (AttrInfo attr : attrs) {
            compound.set((RTTIDataCompound.Attr<? super Object>) attr.attr, reader.read(attr.attr.getType(), factory, buffer));
        }

        return compound;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public List<? extends Base> getBases() {
        return bases;
    }

    @NotNull
    @Override
    public List<? extends Attr<?>> getAttrs() {
        return attrs;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    private void collectAttrs(@NotNull List<AttrInfo> output, int offset) {
        for (Base base : bases) {
            base.type.collectAttrs(output, offset + base.offset);
        }
        for (Attr<?> attr : attrs) {
            output.add(new AttrInfo(attr, offset + attr.offset));
        }
    }

    private record AttrInfo(@NotNull Attr<?> attr, int offset) {}
}
