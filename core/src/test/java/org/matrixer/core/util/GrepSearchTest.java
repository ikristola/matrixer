package org.matrixer.core.util;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class GrepSearchTest {

    private final Path TMP_DIR = FileUtils.getSystemTempDirectory();
    private Path targetFile;

    @BeforeEach
    void setup() {
        targetFile = FileUtils.createTempFile(TMP_DIR);
        String fileContents = "some random text on first line\nsome target random text on line two";
        FileUtils.writeToFile(fileContents, targetFile.toString());
    }

    @Test
    void correctSearchInFile() throws IOException {
        int expLine = 2;
        int expStart = 5;
        int expEnd = 11;
        String expString = "target";

        Pattern regex = Pattern.compile("target");

        var results = GrepSearch.find(targetFile, regex, GrepSearch.LogLevel.HIGH);
        assertEquals(1, results.size());
        assertSearchResult(results.get(0), targetFile, expString, expLine, expStart, expEnd);
    }

    @Test
    void searchInDirectoryWithMultipleFiles() throws IOException {
        Path dir = FileUtils.createTempDirectory(TMP_DIR);
        Path targetFileCopy = dir.resolve("copy.txt");
        Path targetFileCopy1 = dir.resolve("copy1.txt");
        Files.copy(targetFile, targetFileCopy);
        Files.copy(targetFile, targetFileCopy1);

        Pattern regex = Pattern.compile("target");

        var results = GrepSearch.find(dir, regex, GrepSearch.LogLevel.HIGH);
        assertEquals(2, results.size());
    }

    void assertSearchResult(GrepSearch.SearchResult result, Path expPath,
            String expString, int expLine, int expStart, int expEnd) {
        assertAll(
                () -> assertEquals(expPath, result.file),
                () -> assertEquals(expString, result.string),
                () -> assertEquals(expLine, result.line),
                () -> assertEquals(expStart, result.start),
                () -> assertEquals(expEnd, result.end));
    }
}
