package org.matrixer.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;

import org.junit.jupiter.api.*;
import org.matrixer.core.util.FileUtils;
import org.matrixer.core.ExecutionData;
import org.matrixer.core.Project;

class AppTest {

    static Path targetDirectory = FileUtils.getSystemTempDirectory().resolve("matrixer-test");
    static String testRepoURL = "https://github.com/ikristola/matrixer-test";

    static App app;

    @Nested
    static class NoDepthLimit {

        @BeforeAll
        static void runCoverageOnRepo() {
            FileUtils.removeDirectory(targetDirectory);
            String[] args = {
                    "--target", targetDirectory.toString(),
                    "--pkg", "org.matrixertest",
                    "--git", testRepoURL
            };
            app = new App(args);
            assertDoesNotThrow(app::run);
        }

        @Test
        void outputFileContainsMethods() {
            try {
                Project project = app.getProject();
                Path resultsFile = project.resultsFile();
                assertTrue(resultsFile.toFile().exists(), resultsFile + " Does not exist");

                BufferedReader reader = new BufferedReader(new FileReader(resultsFile.toFile()));
                String[] methods = {
                        "org.matrixertest.calculator.Calculator.multiplication(II)I",
                        "org.matrixertest.calculator.Calculator.addition(II)I",
                        "org.matrixertest.calculator.Calculator.subtraction(II)I",
                };
                HashSet<String> foundMethods = new HashSet<>();
                var lines = reader.lines().iterator();
                for (; lines.hasNext();) {
                    var line = lines.next();
                    for (var m : methods) {
                        if (line.contains(m)) {
                            foundMethods.add(m);
                        }
                    }
                }
                assertEquals(methods.length, foundMethods.size(), "All methods not found");
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found: " + e);
            }
        }

        @Test
        void htmlReportIsGenerated() {
            Project project = app.getProject();
            String reportFilename = "matrixer-report.html";
            Path HTMLreport = project.outputDirectory().resolve(reportFilename);
            assertTrue(Files.exists(HTMLreport), HTMLreport + " does not exist");
        }

        @Test
        void canRunAnalyzerOnly() throws IOException {
            // Make project non-runnable and remove existing html report
            Project project = app.getProject();
            Path projectDir = project.directory();
            Path gradleScript = projectDir.resolve("build.gradle");
            Path mvnScript = projectDir.resolve("pom.xml");
            Path html = project.outputDirectory().resolve(App.HTML_REPORT_FILENAME);
            Path[] paths = {gradleScript, mvnScript, html};
            for (var p : paths) {
                backup(p);
            }
            // The preparer uses build script filename to detect the project type
            Files.createFile(project.buildScript());

            try {
                String[] args =
                        {"--analyze", project.directory().toString(), "--pkg", "org.matrixertest"};
                App analyzingApp = new App(args);
                assertDoesNotThrow(analyzingApp::run);
                assertTrue(Files.exists(html), "New html file not created");
            } finally {
                for (var p : paths) {
                    restore(p);
                }
            }
        }
    }

    @Nested
    static class WithDepthLimit {

        @Test
        void testRecordedMethodDepths() throws Exception {
            FileUtils.removeDirectory(targetDirectory);
            String[] args = {
                    "--target", targetDirectory.toString(),
                    "--pkg", "org.matrixertest",
                    "--git", testRepoURL,
                    "--depth", "1"
            };
            app = new App(args);
            app.run();

            ExecutionData data = app.getData();
            data.getAllTargetMethods()
                    .stream()
                    .map(method -> method.depthOfCalls().max())
                    .forEach(depth -> assertTrue(depth <= 1,
                            "Larger depth than 1 recorded " + depth));
        }
    }

    @AfterAll
    static void cleanUp() {
        // FileUtils.removeDirectory(targetDirectory);
    }

    static void backup(Path src) throws IOException {
        Path backup = src.resolveSibling(src.getFileName() + ".old");
        Files.move(src, backup);
    }

    static void restore(Path src) throws IOException {
        Path backup = src.resolveSibling(src.getFileName() + ".old");
        Files.move(backup, src, StandardCopyOption.REPLACE_EXISTING);
    }
}
