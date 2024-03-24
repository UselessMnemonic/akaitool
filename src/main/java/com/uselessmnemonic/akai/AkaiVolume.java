package com.uselessmnemonic.akai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AkaiVolume {

    public static class Unknown extends AkaiVolume {

        Unknown(AkaiFileEntry entry) {
            super(entry);
        }

        public List<AkaiFileHandle> getFiles() {
            return Collections.emptyList();
        }
    }

    static abstract class BasicAkaiVolume extends AkaiVolume {

        private final ArrayList<AkaiFileHandle> files = new ArrayList<>(this.getMaxEntries() / 2);

        BasicAkaiVolume(AkaiFileEntry entry) {
            super(entry);
        }

        abstract int getMaxEntries();

        private List<AkaiFileHandle> filesView;
        public List<AkaiFileHandle> getFiles() {
            if (this.filesView != null) {
                return this.filesView;
            }
            this.filesView = Collections.unmodifiableList(this.files);
            for (int i = 0; i < this.getMaxEntries(); i++) {
                final var entry = this.getPartition().getFileEntry(this.getStartBlock(), i);

                if (entry.fileType() == 's') {
                    final var sample = new AkaiSampleHandle(entry);
                    this.files.add(sample);
                }
            }
            return this.filesView;
        }
    }

    public static class S1000 extends BasicAkaiVolume {

        public static final int AKAI_MAX_FILE_ENTRIES_S1000 = 125;

        S1000(AkaiFileEntry entry) {
            super(entry);
        }

        @Override
        int getMaxEntries() {
            return AKAI_MAX_FILE_ENTRIES_S1000;
        }
    }

    public static class S3000 extends BasicAkaiVolume {

        public static final int AKAI_MAX_FILE_ENTRIES_S3000 = 509;

        S3000(AkaiFileEntry entry) {
            super(entry);
        }

        @Override
        int getMaxEntries() {
            return AKAI_MAX_FILE_ENTRIES_S3000;
        }
    }

    private final AkaiFileEntry entry;

    AkaiVolume(AkaiFileEntry entry) {
        this.entry = entry;
    }

    public AkaiPartition getPartition() {
        return this.entry.partition();
    }

    public String getName() {
        return this.entry.name();
    }

    public int getStartBlock() {
        return this.entry.startBlock();
    }

    public abstract List<AkaiFileHandle> getFiles();
}
