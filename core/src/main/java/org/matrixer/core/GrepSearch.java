package org.matrixer.core;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * A class that performs a grep-like search in files/directories
 */
public class GrepSearch {

    /**
     * Search in file/directory for the specified pattern
     *
     * @param file      The search target
     * @param pattern   The pattern for matching
     * @param maxDepth  Depth of search
     * @param log       Granularity of log messages
     * @return          List of results
     * @throws IOException
     */
    static List<SearchResult> find(Path file, Pattern pattern, int maxDepth, LogLevel log)
            throws IOException {
        if (log == LogLevel.LOW || log == LogLevel.HIGH) {
            System.out.format("Searching in %s with pattern %s\n", file, pattern.toString());
        }

        SearchInFileVisitor visitor = new SearchInFileVisitor(pattern, log);
        Files.walkFileTree(file, EnumSet.noneOf(FileVisitOption.class), maxDepth, visitor);
        List<SearchResult> results = visitor.getResults();

        if (log == LogLevel.LOW || log == LogLevel.HIGH) {
            System.out.format("\nSearch complete. %d results found. %d files and %d directories" +
                    " were visited. Read failed on %d files.",
                    results.size(),
                    visitor.getFileCount().intValue(),
                    visitor.getDirectoryCount().intValue(),
                    visitor.getExceptionCount().intValue());
        }

        return results;
    }

    /**
     * Search in file/directory for the specified pattern.
     * Searches recursively if target is a directory.
     *
     * @param file      The search target
     * @param pattern   The pattern for matching
     * @param log       Granularity of log messages
     * @return
     * @throws IOException
     */
    static List<SearchResult> find(Path file, Pattern pattern, LogLevel log) throws IOException {
        return GrepSearch.find(file, pattern, Integer.MAX_VALUE, log);
    }

    /**
     * Search in file/directory for the specified pattern.
     * Searches recursively if target is a directory.
     * Prints no log messages.
     *
     * @param file      The search target
     * @param pattern   The pattern for matching
     * @return          List of results
     * @throws IOException
     */
    static List<SearchResult> find(Path file, Pattern pattern) throws IOException {
        return GrepSearch.find(file, pattern, Integer.MAX_VALUE, LogLevel.NONE);
    }

    private static class SearchInFileVisitor extends SimpleFileVisitor<Path> {

        private final Pattern pattern;
        private final List<SearchResult> results;
        private final LogLevel log;
        private final AtomicInteger fileCount;
        private final AtomicInteger directoryCount;
        private final AtomicInteger exceptionCount;

        SearchInFileVisitor(Pattern pattern, LogLevel log) {
            this.pattern = pattern;
            this.log = log;
            this.fileCount = new AtomicInteger(0);
            this.directoryCount = new AtomicInteger(0);
            this.exceptionCount = new AtomicInteger(0);
            results = new ArrayList<>();
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) { // for every file
            if (attr.isRegularFile() && Files.isReadable(file)) {
                if (log == LogLevel.HIGH) {
                    System.out.println("Searching in file: " + file);
                }

                AtomicInteger lineCount = new AtomicInteger(1);
                try {
                    Files.lines(file).forEach(line -> {
                        Matcher m = pattern.matcher(line);  // match line against pattern
                        if (m.find()) {                     // on match
                            var result = new SearchResult(file, m.group(), lineCount.get(),
                                    m.start(), m.end());    // add to results
                            results.add(result);
                            if (log == LogLevel.HIGH || log == LogLevel.LOW) {
                                if (log == LogLevel.LOW) {
                                    System.out.println("Match in file: " + file);
                                }
                                System.out.format("\tLine %d, Position: %d - %d\n\tString: %s\n",
                                        lineCount.get(), result.start, result.end, result.string);
                            }
                        }
                        lineCount.incrementAndGet();
                    });
                    fileCount.incrementAndGet();
                }

                catch (Exception e) {   // if read fails
                    exceptionCount.incrementAndGet();
                    if (log == LogLevel.HIGH) {
                        System.out.println("Could not read: " + file + ": " + e.getMessage());
                    }
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {  // after every dir
            directoryCount.incrementAndGet();
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) {  // if visit failed
            exceptionCount.incrementAndGet();
            if (log == LogLevel.HIGH) {
                System.out.println("Could not visit file: " + file + ": " + e.getMessage());
            }
            return CONTINUE;
        }

        public List<SearchResult> getResults() {
            return results;
        }

        public AtomicInteger getFileCount() {
            return fileCount;
        }

        public AtomicInteger getDirectoryCount() {
            return directoryCount;
        }

        public AtomicInteger getExceptionCount() {
            return exceptionCount;
        }
    }

    /**
     * Stores search result data
     */
    public static class SearchResult {
        Path file;
        String string;
        int line;
        int start;
        int end;

        SearchResult(Path file, String string, int line, int start, int end) {
            this.file = file;
            this.string = string;
            this.line = line;
            this.start = start;
            this.end = end;
        }
    }

    public enum LogLevel {
        NONE, LOW, HIGH;
    }
}
