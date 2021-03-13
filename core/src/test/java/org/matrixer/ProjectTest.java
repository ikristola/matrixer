package org.matrixer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;

class ProjectTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    static Path TARGET_DIR = FileUtils.getSystemTempDir().resolve("matrixer-test");

    static GitRepository git;

    @BeforeEach
    void setUp() throws GitAPIException, IOException {
        if (Files.exists(TARGET_DIR)) {
            var repo = GitRepository.open(TARGET_DIR);
            repo.restore();
            return;
        }
        GitRepository.clone(TEST_REPO_URL, TARGET_DIR.toFile());
    }

    @Nested
    class GradleProject {
        @Test
        void testFindsGradleBuildScript() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            assertEquals(project.buildScript(), TARGET_DIR.resolve("build.gradle"));
        }

        @Test
        void testSetsCorrectOutputPath() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path outputDir = Path.of("build", "matrixer-cov");
            assertEquals(TARGET_DIR.resolve(outputDir), project.outputDirectory());
        }

        @Test
        void canInitializeFromProperties() {
            Properties prop = new Properties();
            URI remoteURL = TestUtils.asURI(TEST_REPO_URL);
            prop.setTargetDir(TARGET_DIR);
            prop.setRemoteURL(remoteURL);

            Project project = ProjectFactory.from(prop);

            Path buildScript = TARGET_DIR.resolve("build.gradle");
            Path outputDir = TARGET_DIR.resolve("build/matrixer-cov");

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir, project.outputDirectory());
            assertEquals(buildScript, project.buildScript());
            assertEquals(remoteURL, project.remoteURL());
        }

        @Test
        void testReturnsSpecifiedOutputPathIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir, project.outputDirectory());
        }

        @Test
        void logFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path logFile = Path.of("matrixer-log.txt");
            assertEquals(project.outputDirectory().resolve(logFile), project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path resultsFile = Path.of("matrixer-results.txt");

            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }

        @Test
        void logFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path logFile = Path.of("matrixer-log.txt");
            Path expected = project.outputDirectory().resolve(logFile);
            assertEquals(expected, project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path resultsFile = Path.of("matrixer-results.txt");
            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }
    } // End Gradle Project

    @Nested
    class MavenProject {

        @BeforeEach
        void setupMaven() {
            TestUtils.removeGradleFiles(TARGET_DIR);
        }

        @Test
        void testFindsGradleBuildScript() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            assertEquals(project.buildScript(), TARGET_DIR.resolve("pom.xml"));
        }

        @Test
        void testSetsCorrectOutputPath() throws IOException {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path outputDir = Path.of("target", "matrixer-cov");
            assertEquals(TARGET_DIR.resolve(outputDir), project.outputDirectory());
        }

        @Test
        void canInitializeFromProperties() {
            Properties prop = new Properties();
            URI remoteURL = TestUtils.asURI(TEST_REPO_URL);
            prop.setTargetDir(TARGET_DIR);
            prop.setRemoteURL(remoteURL);

            Project project = ProjectFactory.from(prop);

            Path buildScript = TARGET_DIR.resolve("pom.xml");
            Path outputDir = TARGET_DIR.resolve("target/matrixer-cov");

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir, project.outputDirectory());
            assertEquals(buildScript, project.buildScript());
            assertEquals(remoteURL, project.remoteURL());
        }

        @Test
        void testReturnsSpecifiedOutputPathIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            assertEquals(TARGET_DIR, project.directory());
            assertEquals(outputDir, project.outputDirectory());
        }

        @Test
        void logFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path logFile = Path.of("matrixer-log.txt");

            Path expected = project.outputDirectory().resolve(logFile);
            assertEquals(expected, project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryWithDefaults() {
            Project project = ProjectFactory.scan(TARGET_DIR);
            Path resultsFile = Path.of("matrixer-results.txt");

            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }

        @Test
        void logFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path logFile = Path.of("matrixer-log.txt");
            Path expected = project.outputDirectory().resolve(logFile);
            assertEquals(expected, project.logFile());
        }

        @Test
        void resultsFileIsInOutputDirectoryIfGiven() {
            Properties prop = new Properties();
            Path outputDir = Path.of("/tmp/matrixer-out");
            prop.setOutputDir(outputDir);
            prop.setTargetDir(TARGET_DIR);

            Project project = ProjectFactory.from(prop);

            Path resultsFile = Path.of("matrixer-results.txt");
            Path expected = project.outputDirectory().resolve(resultsFile);
            assertEquals(expected, project.resultsFile());
        }
    } // End Maven Project

}
