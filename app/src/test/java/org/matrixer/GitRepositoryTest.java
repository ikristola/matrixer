package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class GitRepositoryTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static Path TARGET_DIR = Paths.get("/tmp/matrixer-test");

    @AfterAll
    static void cleanUp() {
        try {
            Files.deleteIfExists(TARGET_DIR);
        } catch (IOException e) {
            System.err.println("Cleanup: Error: Could not remove '" + TARGET_DIR + "'");
        }
    }

    @Test
    void firstTest() {
        File target = TARGET_DIR.toFile();
        GitRepository.clone(TEST_REPO_URL, target);
        // assertTrue(target.exists());
    }
}
