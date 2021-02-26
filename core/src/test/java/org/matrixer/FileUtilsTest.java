package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FileUtilsTest {

    static final String SEP = File.separator;
    static final Path TMP_DIR = Path.of(System.getProperty("java.io.tmpdir"));

    static final Path TARGET_DIR = Path.of(
            TMP_DIR + SEP + "targetdir");

    @BeforeAll
    static void setUp() {
        FileUtils.removeDirectory(TARGET_DIR);
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
        File tmpfile = FileUtils.createTempFile(TMP_DIR);
        assertFalse(FileUtils.isExistingDirectory(tmpfile.toPath()));
    }

    @Test
    void isExistingFileCheck() {
        File tmpfile = FileUtils.createTempFile(TMP_DIR);
        assertTrue(FileUtils.isExistingFile(tmpfile.toPath()));
    }

    @Test
    void createTemporaryFile() {
        File tmpfile = FileUtils.createTempFile(TMP_DIR);
        assertTrue(tmpfile.exists());
    }

    @Test
    void createTemporaryDirectory() {
        Path path = FileUtils.createTempDirectory(TMP_DIR);
        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isDirectory());
        assertTrue(path.startsWith(TMP_DIR));
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
        Path dir = Path.of(TMP_DIR + SEP + "nonreadable");
        FileUtils.createDirectory(dir);
        dir.toFile().deleteOnExit();
        dir.toFile().setReadable(false);
        var file = FileUtils.createTempFile(dir);
        assertThrows(RuntimeException.class, () -> FileUtils.fileSearch(dir, file.toString()));
    }

    @Test
    void catchesRemoveDirWithNonReadableDir() {
        Path dir = Path.of(TMP_DIR + SEP + "nonreadable");
        FileUtils.createDirectory(dir);
        dir.toFile().deleteOnExit();
        dir.toFile().setReadable(false);
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

}
