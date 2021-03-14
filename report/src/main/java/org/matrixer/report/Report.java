package org.matrixer.report;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.matrixer.core.FileUtils;

/**
 * Builds a human-readable html report from a raw data matrix file
 */
public class Report {

    private Path dataFile;
    private Path outputDirectory;
    private String reportFilename;

    public static class Builder {
        private Path dataFile;
        private Path outputDirectory = FileUtils.getSystemTempDir();
        private String reportFilename = "matrixer-report.html";

        public Builder() {
        }

        public Builder dataFile(String dataFilePath) {
            return dataFile(Path.of(dataFilePath));
        }

        public Builder dataFile(Path dataFile) {
            this.dataFile = dataFile;
            return this;
        }


        public Builder outputPath(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }

        public Builder outputPath(String outputPath) {
            return outputPath(Path.of(outputPath));
        }

        public Builder reportName(String filename) {
            this.reportFilename = filename;
            return this;
        }

        public Report build() {
            Report reportGenerator = new Report();
            reportGenerator.dataFile = this.dataFile;
            reportGenerator.outputDirectory = this.outputDirectory;
            reportGenerator.reportFilename = this.reportFilename;
            return reportGenerator;
        }
    }

    private Report() {
    }

    /**
     * Generate a html report from the set raw data file. The the report is
     * saved to the set directory path with the set file name.
     *
     * @throws URISyntaxException
     * @throws IOException
     */
    public void generate() throws URISyntaxException, IOException {
        if (!Files.exists(dataFile)) {
            throw new RuntimeException("Failed to generate report: Data file does not exist");
        }

        String stylesheetName = "report-style.css";
        InputStream cssStream = getResourceStream(stylesheetName);
        HtmlGenerator htmlGenerator = new HtmlGenerator(dataFile, cssStream);
        String html = htmlGenerator.generateReport();

        try {
            writeToFile(html);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate report. " +
                    "Write to html file failed: " + e.getMessage());
        }
    }

    private InputStream getResourceStream(String fname) throws URISyntaxException, IOException {
        InputStream resource = getClass().getResourceAsStream(fname);
        if (resource == null) {
            throw new RuntimeException("Could not locate " + fname);
        }
        return resource;
    }

    private void writeToFile(String html) throws IOException {
        OutputStream out = Files.newOutputStream(outputDirectory.resolve(reportFilename));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write(html);
        writer.close();
    }

}
