package com.shade.decima.killzone2.rtti.dumper;

import com.shade.util.NotNull;
import com.shade.util.Nullable;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSection;
import net.fornwall.jelf.ElfSectionHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

public class KZ2TypeDumper {
    private final Map<Integer, PendingType> types = new HashMap<>();
    private final ElfFile elf;

    public KZ2TypeDumper(@NotNull ElfFile elf) {
        this.elf = elf;
    }

    @NotNull
    public static RTTIData[] dump(@NotNull Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path)) {
            final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            final KZ2TypeDumper dumper = new KZ2TypeDumper(ElfFile.from(buffer));

            dumper.registerTypes(0xEB080C); // gRTTIPCore
            dumper.registerTypes(0xEC0AC4); // gRTTIPGraphics3D
            dumper.registerTypes(0xEC4D60); // gRTTIPRTTI
            dumper.registerTypes(0xF03548); // gRTTIPGeometry
            dumper.registerTypes(0xF03B30); // gRTTIPGraphics
            dumper.registerTypes(0xF04E38); // gRTTIAnimation
            dumper.registerTypes(0xF06B2C); // gRTTICAssetManagement
            dumper.registerTypes(0xF0986C); // gRTTICGeometry

            return dumper.types.values().stream()
                .map(PendingType::get)
                .toArray(RTTIData[]::new);
        }
    }

    private void registerTypes(int offset) {
        final ByteBuffer data = getData(offset);

        while (true) {
            final int address = data.getInt();

            if (address == 0) {
                break;
            }

            registerType(address);
        }
    }

    @NotNull
    private PendingType registerType(int offset) {
        if (types.containsKey(offset)) {
            return types.get(offset);
        }

        System.out.println("Registering type at 0x" + Integer.toHexString(offset));

        final PendingType pending = new PendingType(offset);
        types.put(offset, pending);

        final ByteBuffer data = getData(offset);
        final var mType = data.get() & 0xff;

        pending.type = switch (mType) {
            case 1 -> RTTIDataAtom.read(this, data);
            case 2 -> RTTIDataPointer.read(this, data);
            case 3 -> RTTIDataContainer.read(this, data);
            case 4 -> RTTIDataEnum.read(this, data);
            case 5 -> RTTIDataCompound.read(this, data);
            default ->
                throw new IllegalArgumentException("Unsupported RTTI type at address 0x" + Integer.toHexString(offset) + ": " + mType);
        };

        return pending;
    }

    @NotNull
    private String getString(int offset) {
        final ByteBuffer data = getData(offset);
        final StringBuilder buffer = new StringBuilder();

        for (int ch = data.get(); ch != 0; ch = data.get()) {
            buffer.append((char) (ch & 0xff));
        }

        return buffer.toString();
    }

    @NotNull
    private <T> T[] getObjects(int offset, int count, @NotNull IntFunction<T[]> generator, @NotNull BiFunction<KZ2TypeDumper, ByteBuffer, T> reader) {
        final T[] result = generator.apply(count);

        if (count > 0) {
            final ByteBuffer data = getData(offset);

            for (int i = 0; i < count; i++) {
                result[i] = reader.apply(this, data);
            }
        }

        return result;
    }

    @NotNull
    private ByteBuffer getData(int offset) {
        final ElfSection section = getSectionByOffset(offset);

        return ByteBuffer
            .wrap(section.getData())
            .position(getRelativeAddress(section, offset));
    }

    private static int getRelativeAddress(@NotNull ElfSection section, int offset) {
        assert offset >= section.header.sh_addr;
        return (int) (offset - section.header.sh_addr);
    }

    @NotNull
    private ElfSection getSectionByOffset(long offset) {
        for (int i = 0; i < elf.e_shnum; i++) {
            final ElfSection section = elf.getSection(i);
            final ElfSectionHeader header = section.header;

            if (header.sh_addr <= offset && offset < header.sh_addr + header.sh_size) {
                return section;
            }
        }

        throw new IllegalArgumentException("Can't find section at offset 0x" + Long.toHexString(offset));
    }

    public static class PendingType {
        private final int offset;
        private RTTIData type;

        public PendingType(int offset) {
            this.offset = offset;
        }

        @NotNull
        public RTTIData get() {
            if (type == null) {
                throw new IllegalStateException("Type is not resolved: 0x" + Integer.toHexString(offset));
            }

            return type;
        }

        @Override
        public String toString() {
            return type != null ? type.toString() : "<pending>";
        }
    }

    public sealed interface RTTIData {
        @NotNull
        String getTypeName();
    }

    public record RTTIDataCompound(@NotNull String name, @NotNull RTTIBase[] bases, @NotNull RTTIAttr[] attrs, @NotNull RTTIMessageHandler[] messageHandlers, int version) implements RTTIData {
        @NotNull
        public static RTTIDataCompound read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            final var mNumBases = data.get() & 0xff;
            final var mNumAttrs = data.get() & 0xff;
            final var mNumFunctions = data.get() & 0xff;
            final var mNumMessageHandlers = data.get() & 0xff;
            final var mNumMessageOrderEntries = data.get() & 0xff;
            data.position(data.position() + 6); // unknown at 0x06
            final var mVersion = data.getShort();
            data.position(data.position() + 2); // padding
            final var mSize = data.getInt();
            final var mConstructor = data.getInt();
            final var mDestructor = data.getInt();
            final var mAttrChangedFunc = data.getLong();
            final var mFromString = data.getLong();
            final var mToString = data.getLong();
            final var mTypeName = data.getInt();
            data.position(data.position() + 4); // unknown at 0x38
            final var mBases = data.getInt();
            final var mAttrs = data.getInt();
            final var mFunctions = data.getInt();
            final var mMessageHandlers = data.getInt();
            final var mMessageOrderEntries = data.getInt();

            return new RTTIDataCompound(
                dumper.getString(mTypeName),
                dumper.getObjects(mBases, mNumBases, RTTIBase[]::new, RTTIBase::read),
                dumper.getObjects(mAttrs, mNumAttrs, RTTIAttr[]::new, RTTIAttr::read),
                dumper.getObjects(mMessageHandlers, mNumMessageHandlers, RTTIMessageHandler[]::new, RTTIMessageHandler::read),
                mVersion
            );
        }

        @NotNull
        @Override
        public String getTypeName() {
            return name;
        }

        @Override
        public String toString() {
            return getTypeName();
        }
    }

    public record RTTIDataAtom(@NotNull String name, @NotNull PendingType baseType, int size, boolean simple) implements RTTIData {
        @NotNull
        public static RTTIDataAtom read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            final var mSize = data.get() & 0xff;
            final var mIsSimple = data.get() & 0xff;
            data.position(data.position() + 1); // padding
            final var mTypeName = data.getInt();
            final var mBaseType = data.getInt();
            final var mFromString = data.getInt();
            final var mToString = data.getInt();
            final var mCopyFunc = data.getInt();
            final var mConstructor = data.getInt();
            final var mDestructor = data.getInt();
            final var mSerialize = data.getInt();
            final var mDeserialize = data.getInt();
            final var mGetSerializeSize = data.getInt();
            final var mGetMemorySize = data.getInt();

            return new RTTIDataAtom(
                dumper.getString(mTypeName),
                dumper.registerType(mBaseType),
                mSize,
                mIsSimple == 1
            );
        }

        @NotNull
        @Override
        public String getTypeName() {
            return name;
        }

        @Override
        public String toString() {
            return getTypeName();
        }
    }

    public record RTTIDataContainer(@NotNull PendingType itemType, @NotNull Data data) implements RTTIData {
        public record Data(@NotNull String name, int size) {
            @NotNull
            public static Data read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
                final var mTypeName = data.getInt();
                final var mSize = data.getInt();
                final var mConstructor = data.getInt();
                final var mDestructor = data.getInt();
                final var mResize = data.getInt();
                final var mRemove = data.getInt();
                final var mGetNumItems = data.getInt();
                final var mGetItem = data.getInt();
                final var mPredictAllocationForResize = data.getInt();
                final var mClear = data.getInt();

                return new Data(
                    dumper.getString(mTypeName),
                    mSize
                );
            }
        }

        @NotNull
        public static RTTIDataContainer read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            data.position(data.position() + 3); // padding
            final var mItemType = data.getInt();
            final var mContainerType = data.getInt();

            return new RTTIDataContainer(
                dumper.registerType(mItemType),
                Data.read(dumper, dumper.getData(mContainerType))
            );
        }

        @NotNull
        @Override
        public String getTypeName() {
            return "%s<%s>".formatted(data.name, itemType.get().getTypeName());
        }

        @Override
        public String toString() {
            return getTypeName();
        }
    }

    public record RTTIDataPointer(@NotNull PendingType itemType, @NotNull Data data) implements RTTIData {
        public record Data(@NotNull String name, int size) {
            @NotNull
            public static Data read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
                final var mName = data.getInt();
                final var mSize = data.getInt();
                final var mConstructor = data.getInt();
                final var mDestructor = data.getInt();
                final var mGetter = data.getInt();
                final var mSetter = data.getInt();
                final var mCopier = data.getInt();

                return new Data(
                    dumper.getString(mName),
                    mSize
                );
            }
        }

        @NotNull
        public static RTTIDataPointer read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            data.position(data.position() + 3); // padding
            final var mItemType = data.getInt();
            final var mContainerType = data.getInt();

            return new RTTIDataPointer(
                dumper.registerType(mItemType),
                Data.read(dumper, dumper.getData(mContainerType))
            );
        }

        @NotNull
        @Override
        public String getTypeName() {
            return "%s<%s>".formatted(data.name, itemType.get().getTypeName());
        }

        @Override
        public String toString() {
            return getTypeName();
        }
    }

    public record RTTIDataEnum(@NotNull String name, @NotNull RTTIValue[] values, int size) implements RTTIData {
        @NotNull
        public static RTTIDataEnum read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            final var mSize = data.get() & 0xff;
            final var mNumValues = data.get() & 0xff;
            data.position(data.position() + 1); // padding
            final var mTypeName = data.getInt();
            final var mValues = data.getInt();

            return new RTTIDataEnum(
                dumper.getString(mTypeName),
                dumper.getObjects(mValues, mNumValues, RTTIValue[]::new, RTTIValue::read),
                mSize
            );
        }

        @NotNull
        @Override
        public String getTypeName() {
            return name;
        }

        @Override
        public String toString() {
            return getTypeName();
        }
    }

    public record RTTIValue(@NotNull String name, int value) {
        @NotNull
        public static RTTIValue read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            final var mValue = data.getInt();
            final var mName = data.getInt();

            return new RTTIValue(
                dumper.getString(mName),
                mValue
            );
        }
    }

    public record RTTIBase(@NotNull PendingType type, int offset) {
        @NotNull
        public static RTTIBase read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            final var mType = data.getInt();
            final var mOffset = data.getInt();

            return new RTTIBase(
                dumper.registerType(mType),
                mOffset
            );
        }
    }

    public record RTTIAttr(@NotNull String name, @Nullable PendingType type, short flags, short offset) {
        @NotNull
        public static RTTIAttr read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            final var mType = data.getInt();
            final var mOffset = data.getShort();
            final var mFlags = data.getShort();
            final var mName = data.getInt();
            final var mGetterFunc = data.getLong();
            final var mSetterFunc = data.getLong();

            return new RTTIAttr(
                dumper.getString(mName),
                mType != 0 ? dumper.registerType(mType) : null,
                mFlags,
                mOffset
            );
        }
    }

    public record RTTIMessageHandler(@NotNull PendingType message, long handler) {
        @NotNull
        public static RTTIMessageHandler read(@NotNull KZ2TypeDumper dumper, @NotNull ByteBuffer data) {
            final var mMessage = data.getInt();
            final var mHandler = data.getLong();

            return new RTTIMessageHandler(
                dumper.registerType(mMessage),
                mHandler
            );
        }
    }
}
