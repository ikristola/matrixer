package org.matrixer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wrapper class for file operations
 */
public class FileUtils {

    private static final String datePattern = "HHmmss";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);

    /**
     * Check if a directory exists
     * @param path  Path to the directory
     * @return      True if directory exists
     */
    static boolean isExistingDirectory(Path path) {
        return path.toFile().isDirectory();
    }

    /**
     * Check if a file exists
     * @param path  Path to the file
     * @return      True if file exists
     */
    static boolean isExistingFile(Path path) {
        return path.toFile().isFile();
    }

    /**
     * Create a new directory
     * @param path  Path to the new directory
     * @return      True if operation succeeded
     */
    static boolean createDirectory(Path path) {
        return path.toFile().mkdirs();
    }

    /**
     * Create a temporary file in a given directory.
     * Filename: "tmpfileHHmmss"
     * @param path  Path to the directory
     * @return      The temporary file
     */
    static File createTempFile(Path path) {
        String timestamp = simpleDateFormat.format(new Date());
        try {
            return File.createTempFile("tmpfile" + timestamp, ".txt", path.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file: " + e.getMessage());
        }
    }
}
