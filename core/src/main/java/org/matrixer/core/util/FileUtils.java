package org.matrixer.core.util;

import java.io.*;
import java.nio.file.*;
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
     * @param path
     *            Path to the directory
     * @return True if directory exists
     */
    public static boolean isExistingDirectory(Path path) {
        return path.toFile().isDirectory();
    }

    /**
     * Check if a file exists
     *
     * @param path
     *            Path to the file
     * @return True if file exists
     */
    public static boolean isExistingFile(Path path) {
        return path.toFile().isFile();
    }

    /**
     * Create a new directory
     *
     * @param path
     *            Path to the new directory
     * @return True if operation succeeded
     */
    public static boolean createDirectory(Path path) {
        return path.toFile().mkdirs();
    }

    /**
     * Create a temporary file in a given directory. Filename:
     * "tmpfileHHmmss"
     *
     * @param dir
     *            Path to the directory
     * @return The temporary file
     */
    public static Path createTempFile(Path dir) {
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
     * Creates a temporary file in an application specific temporary
     * directory.
     *
     * @return The temporary file
     */
    public static Path createTempFile() {
        try {
            Path systemp = getSystemTempDirectory();
            Path dir = systemp.resolve("matrixer");
            if (!Files.isDirectory(dir)) {
                Files.createDirectory(dir);
                dir.toFile().deleteOnExit();
            }
            return createTempFile(dir);
        } catch (IOException e) {
            throw new RuntimeException("creating application temp file: " + e.getMessage());
        }
    }

    /**
     * Returns the path to the current working directory
     */
    public static Path getCurrentDir() {
        return Path.of(System.getProperty("user.dir"));
    }

    /**
     * Creates a new temporary directory in the system temp directory
     */
    public static Path createTempDirectory() {
        return createTempDirectory(getSystemTempDirectory());
    }

    /**
     * Create a temporary directory in a given directory. Filename:
     * "tmpfileHHmmss"
     *
     * @param parent
     *            Path to the directory in which to create the directory
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
     * @param dir
     *            The directory to search
     * @param fileName
     *            The file to find
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
     * @param dir
     *            Path to the directory to be removed
     */
    public static void removeDirectory(Path dir) {
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
     * Removes a list of files from dir
     */
    public static void removeFiles(Path dir, String[] fnames) {
        try {
            for (var name : fnames) {
                var file = dir.resolve(name).toFile();
                if (file.isDirectory()) {
                    removeDirectory(file.toPath());
                } else {
                    Files.deleteIfExists(file.toPath());
                }
            }
        } catch (IOException e) {
            System.out.println("Could not remove file: " + e.getMessage());
        }
    }

    /**
     * Search for a string in a file
     *
     * @param target
     *            The target string
     * @param file
     *            Path to the file
     * @return Optional of Integer with the index of the first occurrence of
     *         the target
     * @throws IOException
     */
    public static Optional<Integer> searchInFile(String target, Path file) throws IOException {
        try (Stream<String> lines = Files.lines(file)) {
            return lines.map(line -> line.indexOf(target))
                    .filter((idx) -> idx != -1)
                    .findFirst()
                    .or(() -> Optional.empty());

        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Search and replace the first occurrence of a string in a file
     *
     * @param file
     *            Path to the file
     * @param regex
     *            Regex string
     * @param replaceWith
     *            The replacing string
     */
    public static void replaceFirstOccurrenceInFile(Path file, String regex, String replaceWith) {
        try (Stream<String> lines = Files.lines(file)) {
            // Search for an instance of test task and replace it with the inject
            // string

            List<String> replaced = lines.map(line -> line.replaceFirst(regex, replaceWith))
                    .collect(Collectors.toList());
            Files.write(file, replaced);
        } catch (IOException e) {
            throw new RuntimeException("Could not replace string: " + e.getMessage());
        }
    }

    /**
     * Append a string to file
     *
     * @param file
     *            Path to file
     * @param append
     *            String to append
     */
    public static void appendToFile(Path file, String append) {
        try {
            Files.write(file, append.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Could not append to file: " + e.getMessage());
        }
    }

    /**
     * Get the path to the platform-specific temporary folder
     *
     * @return Path to the temporary folder
     */
    public static Path getSystemTempDirectory() {
        return Path.of(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Get a path that does not correspond to any file
     *
     * @return Path to the non existing file
     */
    public static Path getNonExistingPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String date = simpleDateFormat.format(new Date());
        return Path.of(System.getProperty("java.io.tmpdir"), date);
    }

    /**
     * Write a string to a file. If the file does not exist it is created.
     * If the file already exists it will be overwritten with the new
     * string.
     *
     * @param string
     *            The string to be written
     * @param filePath
     *            String representation of the file path
     */
    public static void writeToFile(String string, String filepath) {
        try {
            Files.writeString(Path.of(filepath), string, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("Could not write to file: " + e.getMessage());
        }
    }

}
