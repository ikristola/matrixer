package org.matrixer;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReportTest {

    static final String SEP = File.separator;
    final static String TMP_DIR = System.getProperty("java.io.tmpdir");

    @Test
    void returnFalseIfDataFileDoesNotExist() {
        Path tmpDir = FileUtils.createTempDirectory(Path.of(TMP_DIR));
        Report reportGenerator = new Report.Builder()
                .dataFilePath(tmpDir + SEP + "nonexisting.txt")
                .outputPath(TMP_DIR)
                .build();
        assertFalse(reportGenerator.generate());
    }

    @Test
    void canGenerateReport() {
        // todo use with valid raw data file from matrixer-test

//        ReportGenerator reportGenerator = new ReportGenerator.Builder()
//                .dataFilePath()
//                .outputPath(TMP_DIR)
//                .build();
    }


}