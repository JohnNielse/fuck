package com.shade.decima.killzone2.rtti;

import com.shade.decima.killzone2.rtti.data.KZ2DataAtom;
import com.shade.decima.rtti.RTTICoreFile;
import com.shade.decima.rtti.RTTIFactory;
import com.shade.decima.rtti.RTTIReader;
import com.shade.decima.rtti.data.RTTI;
import com.shade.decima.rtti.data.RTTIDataCompound;
import com.shade.decima.rtti.object.RTTICompound;
import com.shade.platform.model.util.BufferUtils;
import com.shade.util.NotNull;
import com.shade.util.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KZ2Reader implements RTTIReader {
    private final Map<String, byte[][]> atomTables = new HashMap<>();

    @NotNull
    @Override
    public RTTICoreFile read(@NotNull RTTIFactory factory, @NotNull byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        final var header = RTTIHeader.read(buffer);
        final var rttiInfo = RTTIInfo.read(buffer);
        final var objectTypes = RTTIObjectTypes.read(buffer, rttiInfo);
        final var objectHeaders = BufferUtils.getObjects(buffer, objectTypes.indices.length, RTTIObjectHeader[]::new, RTTIObjectHeader::read);
        final var atomTableAllocations = BufferUtils.getObjects(buffer, getVarInt(buffer), RTTIAllocation[]::new, RTTIAllocation::read);
        final var atomTables = BufferUtils.getObjects(buffer, getVarInt(buffer), RTTIAtomTable[]::new, buf -> RTTIAtomTable.read(buf, rttiInfo));
        final var indirectObjectIndex = getVarInt(buffer);
        final var objectConstructionAllocations = BufferUtils.getObjects(buffer, getVarInt(buffer), RTTIAllocation[]::new, RTTIAllocation::read);
        final var objectEntries = new RTTIObjectEntry[objectHeaders.length];

        for (RTTIAtomTable atomTable : atomTables) {
            this.atomTables.put(rttiInfo.names[atomTable.type], atomTable.data);
        }

        for (int i = 0; i < objectHeaders.length; i++) {
            final String objectType = rttiInfo.names[objectTypes.indices[i]];
            final RTTIObjectHeader objectHeader = objectHeaders[i];
            objectEntries[i] = RTTIObjectEntry.read(buffer, objectType, objectHeader, factory, this);
        }

        final List<RTTICompound> compounds = Arrays.stream(objectEntries)
            .map(RTTIObjectEntry::object)
            .toList();

        return new KZ2CoreFile(compounds);
    }

    @NotNull
    @Override
    public <T> T read(@NotNull RTTI<T> rtti, @NotNull RTTIFactory factory, @NotNull ByteBuffer buffer) {
        if (rtti instanceof KZ2DataAtom<T> atom && !atom.isSimple()) {
            final byte[][] atomTable = atomTables.get(atom.getName());
            if (atomTable == null) {
                throw new IllegalStateException("No atom table found for " + atom.getName());
            }
            final var index = getVarIndex(buffer, atomTable.length);
            final var wrapper = ByteBuffer.wrap(atomTable[index]).order(buffer.order());
            return rtti.read(this, factory, wrapper);
        } else {
            return rtti.read(this, factory, buffer);
        }
    }

    public static int getVarInt(@NotNull ByteBuffer buffer) {
        final int value = buffer.get() & 0xff;
        return switch (value) {
            case 0x80 -> buffer.getInt();
            case 0x81 -> buffer.getShort() & 0xffff;
            default -> value;
        };
    }

    private static int getVarIndex(@NotNull ByteBuffer buffer, int length) {
        if (length > 255) {
            return buffer.getShort() & 0xffff;
        } else {
            return buffer.get() & 0xff;
        }
    }

    private enum Version {
        RTTI_BIN_1_58("RTTIBin<1.58>\40\40"),
        RTTI_BIN_1_73("RTTIBin<1.73>\40\40"),
        RTTI_BIN_2_12("RTTIBin<2.12>\40\03"),
        RTTI_BIN_2_19("RTTIBin<2.19>\40\03");

        private final String magic;

        Version(@NotNull String magic) {
            this.magic = magic;
        }

        @NotNull
        public static Version ofMagic(@NotNull String magic) {
            for (Version version : values()) {
                if (version.magic.equals(magic)) {
                    return version;
                }
            }

            throw new EnumConstantNotPresentException(Version.class, magic);
        }

        public boolean atLeast(@NotNull Version version) {
            return ordinal() >= version.ordinal();
        }
    }

    private record RTTIHeader(@NotNull Version version, @NotNull ByteOrder endian, int pointerMapSize, int allocationCount, int vramAllocationCount, int requiredBinCount, int requiredVramBinCount) {
        @NotNull
        public static RTTIHeader read(@NotNull ByteBuffer buffer) {
            final var magic = Version.ofMagic(BufferUtils.getString(buffer, 15));
            final var endian = buffer.get() == 0 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

            buffer.order(endian);

            final var pointerMapSize = buffer.getInt();
            final var allocationCount = buffer.getInt();
            final var vramAllocationCount = buffer.getInt();
            final var requiredBinCount = buffer.getShort() & 0xffff;
            final var requiredVramBinCount = buffer.getShort() & 0xffff;

            return new RTTIHeader(magic, endian, pointerMapSize, allocationCount, vramAllocationCount, requiredBinCount, requiredVramBinCount);
        }
    }

    private record RTTIInfo(@NotNull String[] names) {
        @NotNull
        public static RTTIInfo read(@NotNull ByteBuffer buffer) {
            final var count = getVarInt(buffer);
            final var names = new String[count];

            for (int i = 0; i < count; i++) {
                names[i] = BufferUtils.getString(buffer, buffer.get() & 0xff);
            }

            return new RTTIInfo(names);
        }
    }

    private record RTTIObjectTypes(@NotNull int[] indices) {
        @NotNull
        public static RTTIObjectTypes read(@NotNull ByteBuffer buffer, @NotNull RTTIInfo info) {
            final var count = getVarInt(buffer);
            final var indices = new int[count];

            for (int i = 0; i < count; i++) {
                indices[i] = getVarIndex(buffer, info.names.length);
            }

            return new RTTIObjectTypes(indices);
        }
    }

    public record RTTIObjectHeader(@NotNull byte[] guid, int size, int unk0, int unk1, int unk2) {
        @NotNull
        public static RTTIObjectHeader read(@NotNull ByteBuffer buffer) {
            final var guid = BufferUtils.getBytes(buffer, 16);
            final var size = buffer.getInt();
            final var unk0 = buffer.getInt();
            final var unk1 = buffer.getInt();
            final var unk2 = buffer.getInt();

            return new RTTIObjectHeader(guid, size, unk0, unk1, unk2);
        }
    }

    public record RTTIAllocation(int size, int alignment, int unk1, int unk2, int unk3) {
        @NotNull
        public static RTTIAllocation read(@NotNull ByteBuffer buffer) {
            final var size = getVarInt(buffer);
            final var alignment = getVarInt(buffer);
            final var unk1 = getVarInt(buffer);
            final var unk2 = getVarInt(buffer);
            final var unk3 = getVarInt(buffer);

            return new RTTIAllocation(size, alignment, unk1, unk2, unk3);
        }
    }

    private record RTTIAtomTable(int type, @NotNull byte[][] data) {
        @NotNull
        public static RTTIAtomTable read(@NotNull ByteBuffer buffer, @NotNull RTTIInfo info) {
            final var type = getVarIndex(buffer, info.names.length);
            final var count = getVarInt(buffer);
            final var data = new byte[count][];

            for (int i = 0; i < count; i++) {
                data[i] = BufferUtils.getBytes(buffer, getVarInt(buffer));
            }

            return new RTTIAtomTable(type, data);
        }
    }

    private record RTTIObjectEntry(@NotNull RTTIAllocation[] objectAllocations, @NotNull RTTIAllocation[] vramAllocations, @Nullable RTTICompound object) {
        @NotNull
        public static RTTIObjectEntry read(@NotNull ByteBuffer buffer, @NotNull String typeName, @NotNull RTTIObjectHeader header, RTTIFactory factory, @NotNull RTTIReader reader) {
            final var objectAllocations = BufferUtils.getObjects(buffer, getVarInt(buffer), RTTIAllocation[]::new, RTTIAllocation::read);
            final var vramAllocations = BufferUtils.getObjects(buffer, getVarInt(buffer), RTTIAllocation[]::new, RTTIAllocation::read);
            final ByteBuffer source;

            if (header.size > 0) {
                source = buffer.slice(buffer.position(), header.size).order(buffer.order());
                buffer.position(buffer.position() + header.size);
            } else {
                source = buffer;
            }

            RTTIDataCompound type;
            RTTICompound instance;

            try {
                type = (RTTIDataCompound) factory.get(typeName);
                instance = reader.read(type, factory, source);
            } catch (Exception e) {
                if (header.size > 0) {
                    System.out.println("Couldn't read " + typeName + ": " + e.getMessage());
                    instance = null;
                } else {
                    throw e;
                }
            }

            return new RTTIObjectEntry(objectAllocations, vramAllocations, instance);
        }
    }
}
