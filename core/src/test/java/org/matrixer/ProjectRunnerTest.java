package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;


import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;

public class ProjectRunnerTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    final static Path TARGET_DIR = Path.of(TMP_DIR, "matrixer-test");
    final static String LOGFILE_NAME = "testlog.txt";

    @BeforeAll
    static void setUp() throws GitAPIException {
        System.out.println("Clean up");
        FileUtils.removeDirectory(TARGET_DIR);
        GitRepository.clone(TEST_REPO_URL, TARGET_DIR.toFile());
        if (!Files.exists(TARGET_DIR)) {
            throw new RuntimeException("Git repo did not exist after cloning");
        }
    }

    @AfterAll
    static void cleanUp() {
        FileUtils.removeDirectory(TARGET_DIR);
    }

    @Test
    void builderSetProjectPathFromPath() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR)
                .build();
        assertEquals(projectRunner.getProjectDir(), TARGET_DIR);
    }

    @Test
    void builderSetProjectPathFromString() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .build();
        assertEquals(projectRunner.getProjectDir(), TARGET_DIR);
    }

    @Test
    void builderSetLogFilePathFromString() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFileDir(TARGET_DIR.toString())
                .build();
        assertEquals(projectRunner.getLogFileDir(), TARGET_DIR);
    }

    @Test
    void builderSetLogFilePathFromPath() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR)
                .logFileDir(TARGET_DIR)
                .build();
        assertEquals(projectRunner.getLogFileDir(), TARGET_DIR);
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
    void builderSetBuildSystemGradle() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .buildSystem("gradle")
                .build();
        assertEquals(projectRunner.getBuildSystem(), "gradle");
    }

    @Test
    void builderSetBuildSystemMaven() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .buildSystem("maven")
                .build();
        assertEquals(projectRunner.getBuildSystem(), "maven");
    }

    @Test
    void canCreateProcess() {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFileDir(TARGET_DIR.toString())
                .logFileName(LOGFILE_NAME)
                .buildSystem("gradle")
                .build();
        assertDoesNotThrow(projectRunner::run);
    }

    @Test
    void catchesIfBadBuildSystemIsSet() {
        ProjectRunner projectRunner = new ProjectRunner.Builder().projectPath(TARGET_DIR.toString())
                .logFileDir(TARGET_DIR.toString()).logFileName(LOGFILE_NAME)
                .buildSystem("Imaginary Build System")
                .build();
        assertThrows(RuntimeException.class, projectRunner::run);
    }

    @Test
    void canRunGradleProject() throws IOException {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR.toString())
                .logFileDir(TARGET_DIR.toString()).logFileName(LOGFILE_NAME)
                .task("help")
                .buildSystem("gradle")
                .build();
        projectRunner.run();
        String target = "Welcome to Gradle";
        Path logFile = projectRunner.getLogFile();
        // Search in the project runner log file for the Gradle default message
        assertTrue(FileUtils.searchInFile(target, logFile).isPresent());
    }

    @Test
    void canRunMavenProject() throws IOException {
        ProjectRunner projectRunner = new ProjectRunner.Builder()
                .projectPath(TARGET_DIR)
                .logFileDir(TARGET_DIR)
                .logFileName(LOGFILE_NAME)
                .buildSystem("maven")
                .task("validate")
                .build();
        int status = projectRunner.run();
        assertEquals(0, status, "Maven process exited with error");

        String target = "BUILD SUCCESS";
        Path logFile = projectRunner.getLogFile();
        assertTrue(Files.exists(logFile), "File does not exist: " + logFile);

        boolean logContainsTarget = FileUtils.searchInFile(target, logFile).isPresent();
        assertTrue(logContainsTarget, "Maven process did not run normally");
    }
}
