package com.uselessmnemonic.akai;

import com.uselessmnemonic.akai.utils.ByteBufferInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AkaiSampleHandle extends AkaiFileHandle {

    public static final int AKAI_SAMPLE_ID = 3;

    public record Header(
        int midiRootNote,
        int numActiveLoops,
        int firstActiveLoop,
        int loopMode,
        int tuneCents,
        int tuneSemitones,
        long numSamples,
        long startMarker,
        long endMarker,
        Loop[] loops,
        int samplingFrequency,
        int loopTuneOffset
    ) {
        private static Header fromBuffer(ByteBuffer buffer) {
            buffer.position(buffer.position() + 2); // sample id = 3 and freq. id
            final int midiRootNote = 0xFF & buffer.get(); // MIDI root note (C3 = 60)
            buffer.position(buffer.position() + 12); // filename
            buffer.get(); // = 128
            final int numActiveLoops = 0xFF & buffer.get();
            final int firstActiveLoop = 0xFF & buffer.get();
            buffer.get(); // = 0
            final int loopMode = 0xFF & buffer.get();
            final int tuneCents = 0xFF & buffer.get();
            final int tuneSemitones = 0xFF & buffer.get();
            buffer.position(buffer.position() + 4);
            final long numSamples = 0xFFFFFFFFL & buffer.getInt();
            final long startMarker = 0xFFFFFFFFL & buffer.getInt();
            final long endMarker = 0xFFFFFFFFL & buffer.getInt();
            final var loops = new Loop[8];
            for (int n = 1; n <= loops.length; n++) {
                final long marker = 0xFFFFFFFFL & buffer.getInt();
                final int fineLength = 0xFFFF & buffer.getShort();
                final long coarseLength = 0xFFFFFFFFL & buffer.getInt();
                final int loopTime = 0xFFFF & buffer.getShort();
                loops[0] = new Loop(marker, fineLength, coarseLength, loopTime);
            }
            buffer.position(buffer.position() + 4);
            final int samplingFrequency = 0xFFFF & buffer.getShort();
            final int loopTuneOffset = 0xFF & buffer.get();
            return new Header(midiRootNote, numActiveLoops, firstActiveLoop, loopMode, tuneCents, tuneSemitones, numSamples, startMarker, endMarker, loops, samplingFrequency, loopTuneOffset);
        }
        public static final int LOOP_MODE_IN_RELEASE = 0;
        public static final int LOOP_MODE_UNTIL_RELEASE = 1;
        public static final int LOOP_MODE_NONE = 2;
        public static final int LOOP_MODE_PLAY_TO_END = 3;
    }

    public record Loop(
        long marker,
        int fineLength, // in 65536ths
        long coarseLength, // in words
        int loopTime // in msec, where 9999 == infinity
    ) {}

    AkaiSampleHandle(AkaiFileEntry entry) {
        super(entry);
        final var firstByte = entry.newByteBuffer().get();
        if (firstByte != AKAI_SAMPLE_ID) {
            throw new IllegalArgumentException();
        }
    }

    private Header header;
    public Header getHeader() {
        if (this.header != null) {
            return this.header;
        }
        final var headerSlice = this.newByteBuffer().slice(0, 150).order(ByteOrder.LITTLE_ENDIAN);
        this.header = Header.fromBuffer(headerSlice);
        return this.header;
    }

    private AudioFormat audioFormat;
    public AudioFormat getAudioFormat() {
        if (this.audioFormat == null) {
            this.audioFormat = new AudioFormat(this.getHeader().samplingFrequency(), 16, 1, false, false);
        }
        return this.audioFormat;
    }

    public ByteBuffer newAudioBuffer() {
        return this.newByteBuffer().slice(150, (int) (this.getHeader().numSamples() * 2));
    }

    public AudioInputStream newAudioStream() {
        final var audioSlice = this.newAudioBuffer();
        final var sliceStream = new ByteBufferInputStream(audioSlice);
        return new AudioInputStream(sliceStream, this.getAudioFormat(), this.getHeader().numSamples());
    }
}
