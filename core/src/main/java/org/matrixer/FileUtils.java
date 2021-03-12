package org.matrixer;

import java.io.*;
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
     * @param dir Path to the directory
     * @return The temporary file
     */
    static Path createTempFile(Path dir) {
        try {
            String timestamp = simpleDateFormat.format(new Date());
            String basename = "tmpfile" + timestamp;
            var path = Files.createTempFile(dir, basename, ".txt");
            path.toFile().deleteOnExit();
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file: " + e.getMessage());
        }
    }

    /**
     * Returns the path to the system temporary directory
     *
     * On *Nix this is probably /tmp
     */
    public static Path getSystemTempDir() {
        return Path.of(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Returns the path to the current working directory
     */
    public static Path getCurrentDir() {
        return Path.of(System.getProperty("user.dir"));
    }

    /**
     * Create a temporary directory in a given directory. Filename:
     * "tmpfileHHmmss"
     * 
     * @param parent Path to the directory in which to create the directory
     * @return The path to the temporary directory
     */
    public static Path createTempDirectory(Path parent) {
        try {
            Path newDirPath = Files.createTempDirectory(parent, "tmpdir");
            newDirPath.toFile().deleteOnExit();
            return newDirPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file: " + e.getMessage());
        }
    }

    /**
     * Search a directory and subdirectories for a file
     * 
     * @param dir      The directory to search
     * @param fileName The file to find
     * @return A path to the file
     *
     * @Throws RuntimeException if the file is not found
     */
    public static Path[] fileSearch(Path dir, String fileName) {
        try {
            var paths = Files.find(dir, 100, (path, basicFileAttributes) -> {
                return !Files.isDirectory(path)
                        && path.endsWith(fileName);
            })
                    .toArray(Path[]::new);
            return paths;
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
        if (Files.isDirectory(dir)) {
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new RuntimeException("Failed to remove dir: " + e.getMessage());
            }
        }
    }

    /**
     * Search for a string in a file
     *
     * @param target The target string
     * @param file   Path to the file
     * @return Optional of Integer with the index of the first occurrence of
     *         the target
     * @throws IOException
     */
    static Optional<Integer> searchInFile(String target, Path file) throws IOException {
        return Files.lines(file)
                .map(line -> line.indexOf(target))
                .filter((idx) -> idx != -1)
                .findFirst()
                .or(() -> Optional.empty());
    }

    /**
     * Search and replace the first occurrence of a string in a file
     *
     * @param file        Path to the file
     * @param regex       Regex string
     * @param replaceWith The replacing string
     */
    static void replaceFirstOccurrenceInFile(Path file, String regex, String replaceWith) {
        try {
            // Search for an instance of test task and replace it with the inject
            // string
            List<String> replaced = Files.lines(file)
                    .map(line -> line.replaceFirst(regex, replaceWith))
                    .collect(Collectors.toList());
            Files.write(file, replaced);
        } catch (IOException e) {
            throw new RuntimeException("Could not replace string: " + e.getMessage());
        }
    }

    /**
     * Get the path to the platform-specific temporary folder
     *
     * @return Path to the temporary folder
     */
    static Path getTempDirPath() {
        return Path.of(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Write a string to a file
     *
     * @param string The string to be written
     * @param filePath String representation of the file path
     */
    static void writeToFile(String string, String filePath) {
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(string);
        } catch (IOException e) {
            throw new RuntimeException("Could not write to file: " + e.getMessage());
        }
    }

}
