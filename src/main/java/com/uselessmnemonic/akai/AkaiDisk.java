package com.uselessmnemonic.akai;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AkaiDisk {

    public static final int AKAI_BLOCK_SIZE = 0x2000;
    public static final int AKAI_PARTITION_END_MARK = 0x8000;

    private final ByteBuffer buffer;
    private final ArrayList<AkaiPartition> partitions = new ArrayList<>(10);

    AkaiDisk(Path path) throws IOException {
        final String absPath = path.toAbsolutePath().toString();
        try (RandomAccessFile file = new RandomAccessFile(absPath, "r")) {
            long fileSize = Files.size(path);
            this.buffer = file.getChannel()
                .map(FileChannel.MapMode.READ_ONLY, 0, fileSize)
                .order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    private List<AkaiPartition> partitionView;
    public List<AkaiPartition> getPartitions() {
        if (this.partitionView != null) {
            return this.partitionView;
        }
        this.partitionView = Collections.unmodifiableList(this.partitions);
        for (
            int partitionSize, offset = 0, numBlocks = 0xFFFF & this.buffer.getShort(offset);
            numBlocks < 0x7800 && numBlocks != AKAI_PARTITION_END_MARK && numBlocks != 0x0FFF && numBlocks != 0xFFFF;
            offset += partitionSize, numBlocks = 0xFFFF & this.buffer.getShort(offset)
        ) {
            partitionSize = numBlocks * AKAI_BLOCK_SIZE;
            if (this.partitions.size() >= 9) {
                System.err.printf("extraneous partition? offset = %X, numBlocks = %d, size = %x\n", offset, numBlocks, partitionSize);
            }
            final var partitionBuffer = this.buffer.slice(offset, partitionSize);
            final var partition = new AkaiPartition(this, partitionBuffer);
            this.partitions.add(partition);
        }
        return this.partitionView;
    }
}
