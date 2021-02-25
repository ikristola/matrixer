package org.matrixer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.io.*;
import java.nio.file.Path;

import org.junit.jupiter.api.*;

class AppTest {

    static String TMP_DIR = System.getProperty("java.io.tmpdir");
    static Path target = Path.of(TMP_DIR, File.separator, "matrixer-test");
    static Path outputDir = target.resolve(Properties.DEFAULT_OUTDIR);
    static Path outputFile = outputDir.resolve("results.txt");

    @BeforeAll
    static void runCoverageOnRepo() {
        FileUtils.removeDirectory(target);
        String[] args = {
                "--target", target.toString(),
                "--git", "https://github.com/ikristola/matrixer-test"
        };
        var app = new App(args);
        assertDoesNotThrow(() -> app.run());
        assertTrue(outputFile.toFile().exists(), outputFile + " Does not exist");
    }

    @Test
    void testOutputFileContainsMethods() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(outputFile.toFile()));
            String[] methods = {
                "matrixertest.SimpleCalculations.multiplication(int,int)"
            };
            ArrayList<String> foundMethods = new ArrayList<>();
            var lines = reader.lines().iterator();
            for (;lines.hasNext(); ) {
                var line = lines.next();
                for (var m : methods) {
                    if (line.contains(m)){
                        foundMethods.add(m);
                    }
                }
            }
            assertEquals(methods.length, foundMethods.size());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + e);
        }
    }
}
