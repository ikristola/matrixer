package org.matrixer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Report {

    private String dataFilePath;
    private String outputPath;
    private String reportName;

    public static class Builder {
        private String dataFilePath;
        private String outputPath = System.getProperty("java.io.tmpdir");
        private String reportName = "matrixer-report.html";

        Builder() {}

        Builder dataFilePath(Path dataFilePath) {
            return dataFilePath(dataFilePath.toString());
        }

        Builder dataFilePath(String dataFilePath) {
            this.dataFilePath = dataFilePath;
            return this;
        }

        Builder outputPath(Path outputPath) {
            return outputPath(outputPath.toString());
        }

        Builder outputPath(String outputPath) {
            this.outputPath = outputPath;
            return this;
        }

        Builder reportName(String reportName) {
            this.reportName = reportName;
            return this;
        }

        Report build() {
            Report reportGenerator = new Report();
            reportGenerator.dataFilePath = this.dataFilePath;
            reportGenerator.outputPath = this.outputPath;
            reportGenerator.reportName = this.reportName;
            return reportGenerator;
        }
    }

    private Report() {}

    /**
     * Generate a html report from the set raw data file.
     * The the report is saved to the set directory path with the set file name.
     */
    void generate() {
        if (!Files.exists(Path.of(dataFilePath))) {
            throw new RuntimeException("Failed to generate report: Data file does not exist");
        }

        String styleSheetPath = System.getProperty("user.dir") +
                "/src/main/resources/report-style.css";

        ReportBuilder reportBuilder = new ReportBuilder(dataFilePath, styleSheetPath);
        String html = reportBuilder.generateReport();

        try {
            writeToFile(html);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate report. " +
                    "Write to html file failed: " + e.getMessage());
        }
    }

    private void writeToFile(String html) throws IOException {
        File outputFile = new File(outputPath + File.separator + reportName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(html);
        writer.close();
    }

}
