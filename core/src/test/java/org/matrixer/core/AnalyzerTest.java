package org.matrixer.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AnalyzerTest {

    private static final Path TMP_DIR = FileUtils.getTempDirPath();

    @Test
    void producesCorrectlyFormattedRawData() throws IOException {
        String sourceData =
                "package.Class.AppMethod()<=package.TestClass:TestMethod\n" +
                        "package.Class.AppMethod()<=package.TestClass:TestMethod\n" +
                        "package.Class.AppMethod()<=package.AnotherTestClass:TestMethod\n" +
                        "package.AnotherClass.AppMethod()<=package.TestClass:TestMethod";

        String expected =
                "package.AnotherClass.AppMethod()|package.TestClass:TestMethod\n" +
                        "package.Class.AppMethod()|package.AnotherTestClass:TestMethod|" +
                        "package.TestClass:TestMethod\n";

        Path targetFile = FileUtils.createTempFile(TMP_DIR);
        Path sourceFile = FileUtils.createTempFile(TMP_DIR);
        FileUtils.writeToFile(sourceData, sourceFile.toString());

        Analyzer analyzer = new Analyzer(sourceFile, targetFile);
        analyzer.analyze();

        StringBuilder stringBuilder = new StringBuilder();
        Files.lines(targetFile)
            .forEach(l -> stringBuilder.append(l).append("\n"));
        assertEquals(expected, stringBuilder.toString());
    }

    @Test
    void catchesIfInvalidSourcePath() {
        Path invalidPath = new ThrowingPath();
        assertThrows(NullPointerException.class, () -> new Analyzer(invalidPath));
    }

    @Test
    void catchesIfSourceFileDoesNotExist() {
        Path nonExistingPath = FileUtils.getNonExistingPath();
        Analyzer analyzer = new Analyzer(nonExistingPath);
        assertThrows(IOException.class, analyzer::analyze);
    }

}
