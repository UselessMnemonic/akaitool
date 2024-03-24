package com.uselessmnemonic.akai;

import java.nio.ByteBuffer;

public class AkaiFileHandle {

    private final AkaiFileEntry entry;

    AkaiFileHandle(AkaiFileEntry entry) {
        this.entry = entry;
    }

    public String getName() {
        return this.entry.name();
    }

    public ByteBuffer newByteBuffer() {
        return this.entry.newByteBuffer();
    }
}
