package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AppTest {

    static String TMP_DIR = System.getProperty("java.io.tmpdir");
    static Path target = Path.of(TMP_DIR, File.separator, "matrixer-test");
    static Path outputDir = target.resolve(Properties.DEFAULT_OUTDIR);

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
            Path outputFile = outputDir.resolve("org.matrixertest.calculator.Calculator.txt");
            assertTrue(outputFile.toFile().exists(), outputFile + " Does not exist");

            BufferedReader reader = new BufferedReader(new FileReader(outputFile.toFile()));
            String[] methods = {
                "org.matrixertest.calculator.Calculator.multiplication(int,int)",
                "org.matrixertest.calculator.Calculator.addition(int,int)",
                "org.matrixertest.calculator.Calculator.subtraction(int,int)",
            };
            HashSet<String> foundMethods = new HashSet<>();
            var lines = reader.lines().iterator();
            for (;lines.hasNext(); ) {
                var line = lines.next();
                for (var m : methods) {
                    if (line.contains(m)){
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
