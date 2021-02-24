package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;

class ProjectPreparerTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    final static Path TARGET_DIR = Path.of(TMP_DIR, File.separator, "matrixer-test");

    final static String injectedString = "\tjvmArgs \"-javaagent:" +
            System.getProperty("user.dir") +
            "/build/libs/agentJar.jar=arg1:matrixertest\"";

    @BeforeEach
    void setUpEach() throws GitAPIException {
        FileUtils.removeDirectory(TARGET_DIR);
        GitRepository.clone(TEST_REPO_URL, TARGET_DIR.toFile());
    }

    @AfterEach
    void cleanUpAfter() {
        FileUtils.removeDirectory(TARGET_DIR);
    }

    @Test
    void canPrepare() {
        ProjectPreparer projectPreparer = new ProjectPreparer();
        assertDoesNotThrow(() -> projectPreparer.prepare(TARGET_DIR));
    }

    @Test
    void returnsFalseOnNonProject() {
        ProjectPreparer projectPreparer = new ProjectPreparer();
        Path tmpdir = FileUtils.createTempDirectory(Path.of(TMP_DIR));
        assertThrows(RuntimeException.class, () -> projectPreparer.prepare(tmpdir));
    }

    @Test
    void identifyGradleProject() {
        ProjectPreparer projectPreparer = new ProjectPreparer();
        projectPreparer.prepare(TARGET_DIR);
        var gradleBuildFile = projectPreparer.getGradleBuildFile();
        assertTrue(gradleBuildFile.endsWith("build.gradle"));
    }

    @Test
    void gradleBuildFileIsCorrectlyModified() throws IOException {
        ProjectPreparer projectPreparer = new ProjectPreparer();
        projectPreparer.prepare(TARGET_DIR);
        var results = FileUtils.searchInFile(injectedString,
                projectPreparer.getGradleBuildFile());
        assertTrue(results.isPresent());
    }
}
