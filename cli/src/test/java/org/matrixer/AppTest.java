package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AppTest {

    static Path TMP_DIR = FileUtils.getSystemTempDir();
    static Path target = TMP_DIR.resolve("matrixer-test");
    static String buildDirName = GradleProjectPreparer.BUILD_DIR_NAME;
    static Path outputDir = target.resolve(Properties.defaultOutputPath(target, buildDirName));

    @BeforeAll
    static void runCoverageOnRepo() {
        FileUtils.removeDirectory(target);
        String[] args = {
                "--target", target.toString(),
                "--git", "https://github.com/ikristola/matrixer-test"
        };
        var app = new App(args);
        assertDoesNotThrow(() -> app.run());
    }

    @Test
    void testOutputFileContainsMethods() {
        try {
            // Path outputFile =
            // outputDir.resolve("org.matrixertest.calculator.Calculator.txt");
            Path outputFile = outputDir.resolve("matrixer-results.txt");
            assertTrue(outputFile.toFile().exists(), outputFile + " Does not exist");

            BufferedReader reader = new BufferedReader(new FileReader(outputFile.toFile()));
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
}
