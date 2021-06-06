/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
