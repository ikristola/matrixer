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

    final static String CWD = System.getProperty("user.dir");
    final static String AGENT_JAR_PATH = CWD + "/build/libs/agentJar.jar";
    final static String injectedString =
            String.format("\tjvmArgs \"-javaagent:%s=%s:%s",
                    AGENT_JAR_PATH, "arg1", "matrixertest");

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
        Properties prop = Properties.fromArgs("--target", TARGET_DIR.toString());
        ProjectPreparer projectPreparer = new ProjectPreparer(prop);
        assertDoesNotThrow(() -> projectPreparer.prepare());
    }

    @Test
    void returnsFalseOnNonProject() {
        Path tmpdir = FileUtils.createTempDirectory(Path.of(TMP_DIR));
        Properties prop = Properties.fromArgs("--target", tmpdir.toString());
        ProjectPreparer projectPreparer = new ProjectPreparer(prop);
        assertThrows(RuntimeException.class, () -> projectPreparer.prepare());
    }

    @Test
    void identifyGradleProject() {
        Properties prop = Properties.fromArgs("--target", TARGET_DIR.toString());
        ProjectPreparer projectPreparer = new ProjectPreparer(prop);
        projectPreparer.prepare();
        var gradleBuildFile = projectPreparer.getGradleBuildFile();
        assertTrue(gradleBuildFile.endsWith("build.gradle"));
    }

    @Test
    void gradleBuildFileIsCorrectlyModified() throws IOException {
        Properties prop = Properties.fromArgs("--target", TARGET_DIR.toString());
        ProjectPreparer projectPreparer = new ProjectPreparer(prop);
        projectPreparer.prepare();
        var results = FileUtils.searchInFile(injectedString,
                projectPreparer.getGradleBuildFile());
        assertTrue(results.isPresent());
    }
}
