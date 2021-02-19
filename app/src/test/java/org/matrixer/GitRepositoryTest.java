package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Should target be the parent directory?

class GitRepositoryTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    final static Path TARGET_DIR = Path.of(TMP_DIR, File.separator, "matrixer-test");

    @BeforeEach
    void cleanUp() {
        removeDirectory(TARGET_DIR);
    }

    @Test
    void throwsExceptionIfTargetAlreadyExists() {
        createDirectory(TARGET_DIR);
        File target = temporaryTargetFile();
        assertThrows(Exception.class, () -> GitRepository.clone(TEST_REPO_URL, target));
    }

    @Test
    void downloadsRepositoryIfURLisValid() {
        File target = temporaryTargetFile();
        assertDoesNotThrow(() -> GitRepository.clone(TEST_REPO_URL, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfURLIsNotAGitRepository() {
        File target = temporaryTargetFile();
        assertThrows(GitAPIException.class, () -> GitRepository.clone("localhost", target));
    }

    @Test
    void canCloneLocalGitRepository() {
        File target = temporaryTargetFile();
        String projectDir = projectDirectory().toString();
        assertDoesNotThrow(() -> GitRepository.clone(projectDir, target));
        assertTrue(directoryContainsFile(target, ".git"));
    }

    @Test
    void throwsExceptionIfLocalPathIsNotAGitDirectory() {
        File target = temporaryTargetFile();
        assertThrows(GitAPIException.class, () -> GitRepository.clone(TMP_DIR, target));
    }

    File projectDirectory() {
        return new File(System.getProperty("user.dir")).getParentFile();
    }

    File temporaryTargetFile() {
        return TARGET_DIR.toFile();
    }

    private void createDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean directoryContainsFile(File dir, String fname) {
        return new File(dir, fname).exists();
    }

    private void removeDirectory(Path dir) {
        try {
            if (dir.toFile().exists()) {
                Files.walk(dir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            throw new RuntimeException("Clean up: Failed to remove dir: " + e.getMessage());
        }
    }
}
