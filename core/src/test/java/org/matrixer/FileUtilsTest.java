package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FileUtilsTest {

    static final String SEP = File.separator;
    static final Path TMP_DIR = Path.of(System.getProperty("java.io.tmpdir"));

    static final Path TARGET_DIR = Path.of(TMP_DIR + SEP + "targetdir");

    @BeforeAll
    static void setUp() {
        FileUtils.removeDirectory(TARGET_DIR);
    }

    @Test
    void returnCorrectTempPath() {
        Path tmpDir = FileUtils.getSystemTempDir();
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
        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isDirectory());
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
        Path subDir = Path.of(dir + SEP + "subdir");
        assertThrows(RuntimeException.class,
                () -> FileUtils.createTempDirectory(subDir));
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
        assertThrows(RuntimeException.class, () -> FileUtils.fileSearch(dir, fname));
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
        Stream<String> lines = Files.lines(file);
        lines.forEach(l -> stringBuilder.append(l).append("\n"));
        assertEquals(expected, stringBuilder.toString());
    }

}
