package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.*;

class ProjectPreparerTest {

    final static String TEST_REPO_URL = "https://github.com/ikristola/matrixer-test";
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    final static Path TARGET_DIR = Path.of(TMP_DIR, File.separator, "matrixer-test");
    final static String targetRootPackage = "org.matrixertest";

    final static String CWD = System.getProperty("user.dir");
    final static Path AGENT_JAR_PATH =
            Path.of(CWD, "../agent/build/libs/agentJar.jar").normalize();
    final static Path GRADLE_OUTPUT_PATH =
            Properties.defaultOutputPath(TARGET_DIR, GradleProjectPreparer.BUILD_DIR_NAME);
    final static Path MAVEN_OUTPUT_PATH =
            Properties.defaultOutputPath(TARGET_DIR, MavenProjectPreparer.BUILD_DIR_NAME);

    @BeforeEach
    void setUpEach() throws GitAPIException {
        FileUtils.removeDirectory(TARGET_DIR);
        GitRepository.clone(TEST_REPO_URL, TARGET_DIR.toFile());
    }

    @AfterAll
    static void cleanUpAfter() {
        // FileUtils.removeDirectory(TARGET_DIR);
    }

    @Test
    void canAquireGradlePreparer() {
        var mvnFile = FileUtils.fileSearch(TARGET_DIR, "pom.xml")[0];
        assertDoesNotThrow(() -> Files.deleteIfExists(mvnFile));
        var handle = ProjectPreparer.scan(TARGET_DIR);
        assertEquals(BuildType.Gradle, handle.buildType());
    }

    @Test
    void canAquireMavenPreparer() {
        var gradleFile = FileUtils.fileSearch(TARGET_DIR, "build.gradle")[0];
        assertDoesNotThrow(() -> Files.deleteIfExists(gradleFile));
        var handle = ProjectPreparer.scan(TARGET_DIR);
        assertEquals(BuildType.Maven, handle.buildType());
    }

    @Test
    void canAquireBuildScript() {
        var mvnFile = FileUtils.fileSearch(TARGET_DIR, "pom.xml")[0];
        assertDoesNotThrow(() -> Files.deleteIfExists(mvnFile));
        var handle = ProjectPreparer.scan(TARGET_DIR);
        assertTrue(handle.getBuildScript().endsWith("build.gradle"));
        assertTrue(handle.getBuildScript().toFile().exists());
    }

    @Test
    void gradleBuildScriptContainsJvmAgentArgs() {
        var mvnFile = FileUtils.fileSearch(TARGET_DIR, "pom.xml")[0];
        assertDoesNotThrow(() -> Files.deleteIfExists(mvnFile));

        var handle = ProjectPreparer.scan(TARGET_DIR);
        handle.prepare();

        String agentArg = "jvmArgs \"" + gradleInjectedString() + "\"";
        assertFileContainsString(handle.getBuildScript(), agentArg);
    }

    @Test
    void mavenBuildScriptContainsJvmArgs() throws FileNotFoundException {
        removeGradleFiles();
        var handle = ProjectPreparer.scan(TARGET_DIR);
        handle.prepare();
        var buildScript = handle.getBuildScript();

        String agentArg = "<argLine>" + mavenInjectedString() + "</argLine>";
        String manifest = "<useManifestOnlyJar>false</useManifestOnlyJar>";
        assertFileContainsString(buildScript, agentArg);
        assertFileContainsString(buildScript, manifest);
    }

    @Test
    void throwsExceptionForUnsupportedProject() {
        Path tmpdir = FileUtils.createTempDirectory(Path.of(TMP_DIR));

        assertThrows(Exception.class, () -> ProjectPreparer.scan(tmpdir));
    }

    String gradleInjectedString() {
        return injectedString(AGENT_JAR_PATH, GRADLE_OUTPUT_PATH, targetRootPackage,
                targetRootPackage);
    }

    String mavenInjectedString() {
        return injectedString(AGENT_JAR_PATH, MAVEN_OUTPUT_PATH, targetRootPackage,
                targetRootPackage);
    }

    static String injectedString(Path agentJarPath, Path outputPath, String targetPkg,
            String testPkg) {
        return String.format("-javaagent:%s=%s:%s:%s",
                agentJarPath.toString(), outputPath.toString(),
                targetRootPackage, targetRootPackage);
    }

    void assertFileContainsString(Path file, String string) {
        try {
            var result = FileUtils.searchInFile(string, file);
            if (!result.isPresent()) {
                throw new AssertionError(file + " did not contain:\n" + string);
            }
            Integer index = result.get();
            assertTrue(index != -1, "Did not find\n'" + string + "'\nin\n" + file + "\n");
        } catch (IOException e) {
            throw new AssertionError("assertFileContainsString: " + e.getMessage());
        }
    }

    void removeGradleFiles() {
        var gradleFiles = new String[] {"gradle", "gradlew", "gradlew.bat", "build.gradle",
                "gradle.settings"};
        removeFiles(TARGET_DIR, gradleFiles);
    }

    void removeMavenFiles(Path projectDir) {
        var gradleFiles = new String[] {"pom.xml"};
        removeFiles(projectDir, gradleFiles);
    }

    void removeFiles(Path dir, String[] fnames) {
        try {
            for (var name : fnames) {
                var file = dir.resolve(name).toFile();
                if (file.isDirectory()) {
                    FileUtils.removeDirectory(file.toPath());
                }
                Files.deleteIfExists(file.toPath());
            }
        } catch (IOException e) {
            System.out.println("Could not remove file: " + e.getMessage());
        }
    }
}
