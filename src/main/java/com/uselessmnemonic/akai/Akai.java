package com.uselessmnemonic.akai;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Akai {

    private static final Path EXPORT_ROOT = Paths.get("exports");

    public static void main(String[] args) throws IOException {
        final var path = Paths.get(args[0]).toAbsolutePath();
        final var diskName = path.getFileName().toString();
        final var disk = new AkaiDisk(path);
        final var diskExportRoot = EXPORT_ROOT.resolve(diskName);
        final var partitions = disk.getPartitions();

        for (int i = 0; i < partitions.size(); i++) {
            System.out.println("Partition " + i);
            final var partition = partitions.get(i);
            final var partitionExportRoot = diskExportRoot.resolve("Partition " + i);
            for (var volume: partition.getVolumes()) {
                if (volume instanceof AkaiVolume.Unknown) {
                    continue;
                }
                System.out.println("\tVolume " + volume.getName());
                final var volumeExportRoot = partitionExportRoot.resolve(volume.getName().strip());
                for (var file: volume.getFiles()) {
                    if (!(file instanceof AkaiSampleHandle sampleHandle)) {
                        continue;
                    }
                    final var fileExportPath = volumeExportRoot.resolve(file.getName().strip() + ".wav");
                    System.out.println("\t\tFile " + file.getName());
                    if (Files.exists(fileExportPath)) {
                        continue;
                    }
                    Files.createDirectories(volumeExportRoot);
                    try(
                        final var strm = sampleHandle.newAudioStream();
                    ) {
                        final var fmt = new AudioFileFormat(AudioFileFormat.Type.WAVE, sampleHandle.getAudioFormat(), AudioSystem.NOT_SPECIFIED);
                        final var out = fileExportPath.toFile();
                        AudioSystem.write(strm, fmt.getType(), out);
                    }
                }
            }
        }
    }
}
