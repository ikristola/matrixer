package org.matrixer.cli;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.matrixer.core.FileUtils;
import org.matrixer.core.Project;

class AppTest {

    static Path targetDirectory = FileUtils.getSystemTempDir().resolve("matrixer-test");
    static String testRepoURL = "https://github.com/ikristola/matrixer-test";

    static App app;

    @BeforeAll
    static void runCoverageOnRepo() {
        FileUtils.removeDirectory(targetDirectory);
        String[] args = {
                "--target", targetDirectory.toString(),
                "--pkg", "org.matrixertest",
                "--git", testRepoURL
        };
        app = new App(args);
        assertDoesNotThrow(() -> app.run());
    }

    @Test
    void outputFileContainsMethods() {
        try {
            Project project = app.getProject();
            Path resultsFile = project.resultsFile();
            assertTrue(resultsFile.toFile().exists(), resultsFile + " Does not exist");

            BufferedReader reader = new BufferedReader(new FileReader(resultsFile.toFile()));
            String[] methods = {
                    "org.matrixertest.calculator.Calculator.multiplication(int,int)",
                    "org.matrixertest.calculator.Calculator.addition(int,int)",
                    "org.matrixertest.calculator.Calculator.subtraction(int,int)",
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
        String reportFilename = App.DEFAULT_REPORT_FNAME;
        Path HTMLreport = project.outputDirectory().resolve(reportFilename);
        assertTrue(Files.exists(HTMLreport), HTMLreport + " does not exist");
    }
}
