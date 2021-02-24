package org.matrixer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Wrapper class for file operations
 */
public class FileUtils {

    private static final String datePattern = "HHmmss";
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);

    /**
     * Check if a directory exists
     * 
     * @param path Path to the directory
     * @return True if directory exists
     */
    static boolean isExistingDirectory(Path path) {
        return path.toFile().isDirectory();
    }

    /**
     * Check if a file exists
     * 
     * @param path Path to the file
     * @return True if file exists
     */
    static boolean isExistingFile(Path path) {
        return path.toFile().isFile();
    }

    /**
     * Create a new directory
     * 
     * @param path Path to the new directory
     * @return True if operation succeeded
     */
    static boolean createDirectory(Path path) {
        return path.toFile().mkdirs();
    }

    /**
     * Create a temporary file in a given directory. Filename:
     * "tmpfileHHmmss"
     * 
     * @param path Path to the directory
     * @return The temporary file
     */
    static File createTempFile(Path path) {
        String timestamp = simpleDateFormat.format(new Date());
        try {
            var file = File.createTempFile(
                    "tmpfile" + timestamp, ".txt", path.toFile());
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file: " + e.getMessage());
        }
    }

    /**
     * Create a temporary directory in a given directory. Filename:
     * "tmpfileHHmmss"
     * 
     * @param path Path to the directory in which to create the directory
     * @return The path to the temporary directory
     */
    static Path createTempDirectory(Path path) {
        try {
            Path newDirPath = Files.createTempDirectory(path, "tmpdir");
            newDirPath.toFile().deleteOnExit();
            return newDirPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file: " + e.getMessage());
        }
    }

    /**
     * Search a directory and subdirectories for a file
     * 
     * @param dir
     * @param fileName
     * @return
     */
    static Path[] fileSearch(Path dir, String fileName) {
        try {
            Stream<Path> stream = Files.find(dir, 100, (path, basicFileAttributes) -> {
                File file = path.toFile();
                return !file.isDirectory() && file.getName().equals(fileName);
            });
            return stream.toArray(Path[]::new);
        } catch (IOException e) {
            throw new RuntimeException("Failed to search in file: " + e.getMessage());
        }
    }

    /**
     * Remove a directory and all its subfiles and subdirectories
     * 
     * @param dir Path to the directory to be removed
     */
    static void removeDirectory(Path dir) {
        try {
            if (dir.toFile().exists()) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to remove dir: " + e.getMessage());
        }
    }

    /**
     * Search for a string in a file
     * 
     * @param target The target string
     * @param file Path to the file
     * @return Optional of Integer with the index of the first occurrence of
     *         the target
     * @throws IOException
     */
    static Optional<Integer> searchInFile(String target, Path file) throws IOException {
        Stream<String> lines = Files.lines(file);
        return lines.map(line -> line.indexOf(target)).findFirst();
    }

    /**
     * Search and replace the first occurrence of a string in a file
     * 
     * @param file Path to the file
     * @param regex Regex string
     * @param replaceWith The replacing string
     */
    static void replaceFirstOccurrenceInFile(Path file, String regex, String replaceWith) {
        try {
            // Search for an instance of test task and replace it with the inject
            // string
            Stream<String> lines = Files.lines(file);
            List<String> replaced = lines.map(line -> line.replaceFirst(regex,
                    replaceWith)).collect(Collectors.toList());
            Files.write(file, replaced);
            lines.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not replace string: " + e.getMessage());
        }
    }

}
