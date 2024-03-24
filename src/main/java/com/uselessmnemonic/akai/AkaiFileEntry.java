package com.uselessmnemonic.akai;

import java.nio.ByteBuffer;
import java.util.OptionalInt;

record AkaiFileEntry(
    AkaiPartition partition,
    String name,
    int fileType,
    int startBlock,
    OptionalInt size
) {
    ByteBuffer newByteBuffer() {
        return this.partition.newByteBuffer(this);
    }
}
