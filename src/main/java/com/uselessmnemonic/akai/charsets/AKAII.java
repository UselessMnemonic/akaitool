package com.uselessmnemonic.akai.charsets;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class AKAII extends Charset {

    public static final AKAII INSTANCE = new AKAII();

    public AKAII() {
        super("AKAII", null);
    }

    @Override
    public boolean contains(Charset cs) {
        return cs instanceof AKAII;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new Decoder(this);
    }

    @Override
    public CharsetEncoder newEncoder() {
        return new Encoder(this);
    }

    public static class Decoder extends CharsetDecoder {

        private Decoder(Charset cs) {
            super(cs, 1f, 1f);
        }

        @Override
        protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
            int mark = in.position();
            try {
                while (in.hasRemaining()) {
                    final char c;
                    final var b = in.get();
                    if (b < 0 || b > 40) {
                        return CoderResult.malformedForLength(1);
                    }
                    else if (!out.hasRemaining()) {
                        return CoderResult.OVERFLOW;
                    }
                    if (b >= 0 && b <= 9) {
                        c = (char)('0' + b);
                    }
                    else if (b == 10) {
                        c = ' ';
                    }
                    else if (b >= 11 && b <= 36) {
                        c = (char)('A' + b - 11);
                    }
                    else {
                        c = switch (b) {
                            case 37 -> '#';
                            case 38 -> '+';
                            case 39 -> '-';
                            case 40 -> '.';
                            default -> throw new IllegalStateException();
                        };
                    }
                    out.put(c);
                    mark++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                in.position(mark);
            }
        }
    }

    public static class Encoder extends CharsetEncoder {

        private static final byte[] DEFAULT_REPLACEMENT = new byte[]{ '.' };

        private Encoder(Charset cs) {
            super(cs, 1f, 1f, DEFAULT_REPLACEMENT);
        }

        public boolean canEncode(char c) {
            return (c >= '0' && c <= '9') || c == ' ' || (c >= 'A' && c <= 'Z') || c == '#' || c == '+' || c == '-' || c == '.';
        }

        public boolean isLegalReplacement(byte[] repl) {
            return repl.length == 1 && repl[0] >= 0 && repl[0] <= 40;
        }

        @Override
        protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
            int mark = in.position();
            try {
                while (in.hasRemaining()) {
                    final byte b;
                    final var c = in.get();
                    if (!this.canEncode(c)) {
                        return CoderResult.unmappableForLength(1);
                    }
                    else if (!out.hasRemaining()) {
                        return CoderResult.OVERFLOW;
                    }
                    if (c >= '0' && c <= '9') {
                        b = (byte)(c - '0');
                    }
                    else if (c == ' ') {
                        b = 10;
                    }
                    else if (c >= 'A' && c <= 'Z') {
                        b = (byte)(c - 'A' + 11);
                    }
                    else {
                        b = switch (c) {
                            case '#' -> 37;
                            case '+' -> 38;
                            case '-' -> 39;
                            case '.' -> 40;
                            default -> throw new IllegalStateException();
                        };
                    }
                    out.put(b);
                    mark++;
                }
                return CoderResult.UNDERFLOW;
            } finally {
                in.position(mark);
            }
        }
    }
}
