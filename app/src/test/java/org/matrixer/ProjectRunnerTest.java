package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;

public class ProjectRunnerTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    final static Path TARGET_DIR = Path.of(TMP_DIR, File.separator, "matrixer-test");
    final static String LOGFILE_NAME = "testlog.txt";

    @BeforeAll
    static void setUp() throws GitAPIException {
        FileUtils.removeDirectory(TARGET_DIR);
        GitRepository.clone(TEST_REPO_URL, TARGET_DIR.toFile());
    }

    @AfterAll
    static void cleanUp() {
        FileUtils.removeDirectory(TARGET_DIR);
    }

    @Test
    void builderSetProjectPath() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .build();
        assertEquals(projectRunner.getProjectPath(), TARGET_DIR.toString());
    }

    @Test
    void builderSetLogFilePath() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFilePath(TARGET_DIR.toString())
                .build();
        assertEquals(projectRunner.getLogFilePath(), TARGET_DIR.toString());
    }

    @Test
    void builderSetLogFileName() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFileName(LOGFILE_NAME)
                .build();
        assertEquals(projectRunner.getLogFileName(), LOGFILE_NAME);
    }

    @Test
    void builderSetTask() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .task("test")
                .build();
        assertEquals(projectRunner.getTask(), "test");
    }

    @Test
    void builderSetBuildSystem() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .buildSystem("gradle")
                .build();
        assertEquals(projectRunner.getBuildSystem(), "gradle");
    }

    @Test
    void canCreateProcess() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFilePath(TARGET_DIR.toString())
                .logFileName(LOGFILE_NAME)
                .buildSystem("gradle")
                .build();
        assertDoesNotThrow(projectRunner::run);
    }

    @Test
    void catchesIfBadBuildSystemIsSet() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFilePath(TARGET_DIR.toString())
                .logFileName(LOGFILE_NAME)
                .buildSystem("Imaginary Build System")
                .build();
        assertThrows(RuntimeException.class, projectRunner::run);
    }

    @Test
    void canRunGradleProject() throws IOException {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFilePath(TARGET_DIR.toString())
                .logFileName(LOGFILE_NAME)
                .task("help")
                .buildSystem("gradle")
                .build();
        projectRunner.run();
        String target = "Welcome to Gradle";
        Path logFile = Path.of(projectRunner.getLogFilePath() + File.separator
                + LOGFILE_NAME);
        // Search in the project runner log file for the Gradle default message
        assertTrue(FileUtils.searchInFile(target, logFile).isPresent());
    }
}
