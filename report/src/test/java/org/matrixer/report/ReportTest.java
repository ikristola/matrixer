package org.matrixer.report;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.matrixer.core.FileUtils;

class ReportTest {

    static final String SEP = File.separator;
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    static final String INVALID_PATH = "\0";
    static final String INVALID_NAME = "\0";


    @Test
    void throwsExceptionIfDataFileDoesNotExist() {
        Path tmpDir = FileUtils.createTempDirectory(Path.of(TMP_DIR));
        Report report = new Report.Builder()
                .dataFile(tmpDir + SEP + "nonexisting.txt")
                .build();
        assertThrows(RuntimeException.class, report::generate);
    }

    @Test
    void canGenerateReport() throws URISyntaxException, IOException {
        Path emptyDataFile = FileUtils.createTempFile(Path.of(TMP_DIR));

        Report report = new Report.Builder()
                .dataFile(emptyDataFile)
                .outputPath(TMP_DIR)
                .reportName("test-report.html")
                .build();
        report.generate();

        assertTrue(Files.exists(Path.of(TMP_DIR + SEP + "test-report.html")));
    }

    @Test
    void catchesInvalidOutputPath() {
        Path emptyDataFile = FileUtils.createTempFile(Path.of(TMP_DIR));

        assertThrows(Exception.class, () -> {
            Report report = new Report.Builder()
                    .dataFile(emptyDataFile)
                    .outputPath(INVALID_PATH)
                    .build();
            report.generate();
        });
    }

    @Test
    void catchesInvalidNamePath() {
        Path emptyDataFile = FileUtils.createTempFile(Path.of(TMP_DIR));

        Report report = new Report.Builder()
                .dataFile(emptyDataFile)
                .reportName(INVALID_NAME)
                .build();

        assertThrows(RuntimeException.class, report::generate);
    }

}
