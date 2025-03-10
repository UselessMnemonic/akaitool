package com.uselessmnemonic.akai.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

    private final ByteBuffer buffer;

    public ByteBufferInputStream(ByteBuffer buf) {
        this.buffer = buf;
    }

    public int read() throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        return buffer.get() & 0xFF;
    }

    public int read(byte[] bytes, int off, int len) {
        if (!buffer.hasRemaining()) {
            return -1;
        }
        len = Math.min(len, buffer.remaining());
        buffer.get(bytes, off, len);
        return len;
    }
}
