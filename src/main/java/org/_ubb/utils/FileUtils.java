package org._ubb.utils;

import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

@UtilityClass
public class FileUtils {

    public static int calculateTotalChunks(long fileSize, int chunkSize) {
        return (int) Math.ceil((double) fileSize / chunkSize);
    }

    public static byte[] readFileChunk(String filePath, int chunkSize, int chunkIndex) throws IOException {
        File file = new File(filePath);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long start = (long) chunkIndex * chunkSize;
            long length = Math.min(chunkSize, file.length() - start);
            byte[] buffer = new byte[(int) length];

            raf.seek(start);
            int bytesRead = raf.read(buffer, 0, buffer.length);
            if (bytesRead < length) {
                return Arrays.copyOf(buffer, bytesRead);
            }

            return buffer;
        }
    }
}
