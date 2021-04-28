package org.matrixer.core.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.matrixer.core.testsupport.ThrowingPath;

class FileUtilsTest {

    static final Path TMP_DIR = Path.of(System.getProperty("java.io.tmpdir"));
    static final Path TARGET_DIR = TMP_DIR.resolve("targetdir");

    @BeforeAll
    static void setUp() {
        FileUtils.removeDirectory(TARGET_DIR);
    }

    @Test
    void returnCorrectTempPath() {
        Path tmpDir = FileUtils.getSystemTempDirectory();
        assertFalse(tmpDir.toString().isEmpty());
        assertEquals(Path.of(System.getProperty("java.io.tmpdir")), tmpDir);
    }

    @Test
    void returnsCorrectCurrentWorkingDirectory() {
        Path cwd = FileUtils.getCurrentDir();
        assertFalse(cwd.toString().isEmpty());
        assertEquals(System.getProperty("user.dir"), cwd.toString());
    }

    @Test
    void isExistingDirectoryCheck() {
        assertTrue(FileUtils.isExistingDirectory(TMP_DIR));
    }

    @Test
    void catchesIsExistingDirectoryCheckWithNonExistingDir() {
        assertFalse(FileUtils.isExistingDirectory(TARGET_DIR));
    }

    @Test
    void catchesIsExistingDirectoryCheckWithExistingFile() {
        Path tmpfile = FileUtils.createTempFile(TMP_DIR);
        assertFalse(FileUtils.isExistingDirectory(tmpfile));
    }

    @Test
    void isExistingFileCheck() {
        Path tmpfile = FileUtils.createTempFile(TMP_DIR);
        assertTrue(FileUtils.isExistingFile(tmpfile));
    }

    @Test
    void createTemporaryFile() {
        Path tmpfile = FileUtils.createTempFile(TMP_DIR);
        assertTrue(Files.exists(tmpfile));
    }

    @Test
    void createTemporaryDirectory() {
        Path path = FileUtils.createTempDirectory(TMP_DIR);
        assertTrue(Files.exists(path));
        assertTrue(Files.isDirectory(path));
        assertTrue(path.startsWith(TMP_DIR));
    }

    @Test
    void createNonExistingPath() {
        Path path = FileUtils.getNonExistingPath();
        System.out.println("path: " + path);
        assertFalse(path.toFile().exists());
    }

    @Test
    void catchesCreateTemporaryDirectoryWithBadPath() {
        Path dir = FileUtils.createTempDirectory(TMP_DIR);
        dir.toFile().setWritable(false);
        Path subDir = dir.resolve("subdir");
        assertThrows(RuntimeException.class,
                () -> FileUtils.createTempDirectory(subDir));
    }

    @Test
    void appendStringToFile() throws IOException {
        String junk = "some junk\nstuff\n";
        String expected = "appended string";
        var file = FileUtils.createTempFile(FileUtils.getSystemTempDirectory());
        Files.writeString(file, junk);

        FileUtils.appendToFile(file, expected);

        try (Stream<String> lines = Files.lines(file)) {
            var lastLine = lines.reduce((first, second) -> second).orElse(null);
            assertEquals(expected, lastLine);
        }
    }

    @Test
    void removeDirectory() {
        FileUtils.createDirectory(TARGET_DIR);
        FileUtils.removeDirectory(TARGET_DIR);
        assertFalse(TARGET_DIR.toFile().exists());
    }

    @Test
    void catchesFileSearchInNonReadableDir() {
        Path dir = new ThrowingPath();
        String fname = "non-existant.txt";
        assertThrows(RuntimeException.class, () -> FileUtils.findFiles(dir, fname));
    }

    @Test
    void catchesRemoveDirWithNonReadableDir() {
        Path dir = new ThrowingPath();
        assertThrows(RuntimeException.class, () -> FileUtils.removeDirectory(dir));
    }

    @Test
    void catchesCreateTemporaryFileWithNonExistentPath() {
        assertThrows(RuntimeException.class, () -> FileUtils.createTempFile(TARGET_DIR));
    }

    @Test
    void catchesFileReplacementWithBadFile() {
        assertThrows(RuntimeException.class, () -> FileUtils
                .replaceFirstOccurrenceInFile(TARGET_DIR, "ABC", "CBA"));
    }

    @Test
    void canWriteToFile() throws IOException {
        String expected = "Expected\nfile\ncontents\n";
        Path file = FileUtils.createTempFile(TMP_DIR);

        FileUtils.writeToFile(expected, file.toString());

        StringBuilder stringBuilder = new StringBuilder();
        try (Stream<String> lines = Files.lines(file)) {
            lines.forEach(l -> stringBuilder.append(l).append("\n"));
        } catch (IOException e) {
            throw e;
        }
        assertEquals(expected, stringBuilder.toString());
    }

}
