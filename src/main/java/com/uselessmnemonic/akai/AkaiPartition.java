package com.uselessmnemonic.akai;

import com.uselessmnemonic.akai.charsets.AKAII;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

public class AkaiPartition {

    public static final int AKAI_DIR_ENTRIES_OFFSET = 0xCA;
    public static final int AKAI_FAT_OFFSET = 0x70A;

    public static final int AKAI_DIR_ENTRY_SIZE = 16;
    public static final int AKAI_FILE_ENTRY_SIZE = 24;
    public static final int AKAI_MAX_DIR_ENTRIES = 0x64;

    public static final int AKAI_FILE_ENTRY_TYPE_S1000 = 0x1;
    public static final int AKAI_FILE_ENTRY_TYPE_S3000 = 0x3;

    private final AkaiDisk disk;
    private final ByteBuffer buffer;
    private final ArrayList<AkaiVolume> volumes = new ArrayList<>(AKAI_MAX_DIR_ENTRIES);

    AkaiPartition(AkaiDisk disk, ByteBuffer buffer) {
        this.disk = disk;
        this.buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public AkaiDisk getDisk() {
        return this.disk;
    }

    int getFATOffset(int block) {
        return 0xFFFF & this.buffer.getShort(AKAI_FAT_OFFSET + block * 2);
    }

    AkaiFileEntry getFileEntry(int block, int position) {
        final var nameBuffer = new byte[12];
        if (block == 0) {
            buffer.position(AKAI_DIR_ENTRIES_OFFSET + position * AKAI_DIR_ENTRY_SIZE);
            buffer.get(nameBuffer);
            final var name = new String(nameBuffer, AKAII.INSTANCE);
            final var fileType = 0xFFFF & buffer.getShort();
            final var startBlock = 0xFFFF & buffer.getShort();
            return new AkaiFileEntry(this, name, fileType, startBlock, OptionalInt.empty());
        }

        if (position < 341) {
            buffer.position(block * AkaiDisk.AKAI_BLOCK_SIZE + position * AKAI_FILE_ENTRY_SIZE);
        } else {
            final var fatOffset = this.getFATOffset(block);
            buffer.position(fatOffset * AkaiDisk.AKAI_BLOCK_SIZE + (position - 314) * AKAI_FILE_ENTRY_SIZE);
        }
        buffer.get(nameBuffer);
        final var name = new String(nameBuffer, AKAII.INSTANCE);
        buffer.position(buffer.position() + 4);
        final var fileType = 0xFF & buffer.get();
        final var size = 0x00FFFFFF & buffer.getInt();
        buffer.position(buffer.position() - 1);
        final var startBlock = 0xFFFF & buffer.getShort();
        return new AkaiFileEntry(this, name, fileType, startBlock, OptionalInt.of(size));
    }

    private List<AkaiVolume> volumesView;
    public List<AkaiVolume> getVolumes() {
        if (this.volumesView != null) {
            return this.volumesView;
        }
        for (int i = 0; i < AKAI_MAX_DIR_ENTRIES; i++) {
            final var entry = this.getFileEntry(0, i);
            if (entry.fileType() == 0 && entry.startBlock() == 0) {
                continue;
            }
            final var volume = switch (entry.fileType()) {
                case AKAI_FILE_ENTRY_TYPE_S1000 -> new AkaiVolume.S1000(entry);
                case AKAI_FILE_ENTRY_TYPE_S3000 -> new AkaiVolume.S3000(entry);
                default -> new AkaiVolume.Unknown(entry);
            };
            this.volumes.add(volume);
        }
        this.volumesView = Collections.unmodifiableList(this.volumes);
        return this.volumesView;
    }

    ByteBuffer newByteBuffer(AkaiFileEntry entry) {
        if (entry.size().isEmpty()) {
            throw new UnsupportedOperationException();
        }
        return this.buffer.slice(entry.startBlock() * AkaiDisk.AKAI_BLOCK_SIZE, entry.size().getAsInt());
    }
}
