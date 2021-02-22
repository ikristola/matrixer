package org.matrixer;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    static final String SEP = File.separator;
    static final Path TMP_DIR = Path.of(System.getProperty("java.io.tmpdir"));
    static final Path NON_EXISTENT_PATH = Path.of(
            TMP_DIR + SEP + "some" + SEP + "nonexistent" + SEP + "path");

    @Test
    void isExistingDirectoryCheck() {
        assertTrue(FileUtils.isExistingDirectory(TMP_DIR));
    }

    @Test
    void catchesIsExistingDirectoryCheckWithNonExistingDir() {
        assertFalse(FileUtils.isExistingDirectory(NON_EXISTENT_PATH));
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
    void catchesCreateTemporaryFileWithNonExistentPath() {
        assertThrows(RuntimeException.class, () -> FileUtils.createTempFile(NON_EXISTENT_PATH));
    }
}
