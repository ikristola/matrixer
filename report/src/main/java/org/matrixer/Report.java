package org.matrixer;

import java.io.*;
import java.nio.file.Path;

public class Report {

    private String dataFilePath;
    private String outputPath;

    public static class Builder {
        private String dataFilePath;
        private String outputPath;

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

        Report build() {
            Report reportGenerator = new Report();
            reportGenerator.dataFilePath = this.dataFilePath;
            reportGenerator.outputPath = this.outputPath;
            return reportGenerator;
        }
    }

    private Report() {}

    boolean generate() {
        String rawData = readDataFile();
        String html = DataParser.htmlFromData(rawData);
        try {
            writeToFile(html);
        } catch (IOException e) {
            System.err.println("Failed to write html to file: " + e.getMessage());
        }
        return true;
    }

    private String readDataFile() {
        return "";
    }

    private void writeToFile(String html) throws IOException {
        File outputFile = new File(outputPath + System.lineSeparator() + "report.html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write(html);
        writer.close();
    }

}
