package org.matrixer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ReportTest {

    static final String SEP = File.separator;
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    static final String INVALID_PATH = "\0";
    static final String INVALID_NAME = "\0";


    @Test
    void throwsExceptionIfDataFileDoesNotExist() {
        Path tmpDir = FileUtils.createTempDirectory(Path.of(TMP_DIR));
        Report report = new Report.Builder()
                .dataFilePath(tmpDir + SEP + "nonexisting.txt")
                .build();
        assertThrows(RuntimeException.class, report::generate);
    }

    @Test
    void canGenerateReport() {
        File emptyDataFile = FileUtils.createTempFile(Path.of(TMP_DIR));

        Report report = new Report.Builder()
                .dataFilePath(emptyDataFile.toPath())
                .outputPath(TMP_DIR)
                .reportName("test-report.html")
                .build();
        report.generate();

        assertTrue(Files.exists(Path.of(TMP_DIR + SEP + "test-report.html")));
    }

    @Test
    void catchesInvalidOutputPath() {
        File emptyDataFile = FileUtils.createTempFile(Path.of(TMP_DIR));

        Report report = new Report.Builder()
                .dataFilePath(emptyDataFile.toPath())
                .outputPath(INVALID_PATH)
                .build();

        assertThrows(RuntimeException.class, report::generate);
    }

    @Test
    void catchesInvalidNamePath() {
        File emptyDataFile = FileUtils.createTempFile(Path.of(TMP_DIR));

        Report report = new Report.Builder()
                .dataFilePath(emptyDataFile.toPath())
                .reportName(INVALID_NAME)
                .build();

        assertThrows(RuntimeException.class, report::generate);
    }

}
