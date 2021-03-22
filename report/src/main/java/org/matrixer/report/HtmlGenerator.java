package org.matrixer.report;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Generates html from a raw data file
 */
public class HtmlGenerator {

    private final Path dataFile;
    private final InputStream stylesheetStream;
    SortedSet<String> testMethods; // set of all test methods in data file
    SortedSet<TargetMethod> targetMethods; // all target (SUT) methods in data file

    HtmlGenerator(String dataFilePath, String stylesheetPath) throws IOException {
        this(Path.of(dataFilePath), Files.newInputStream(Path.of(stylesheetPath)));
    }

    HtmlGenerator(Path dataFile, Path stylesheet) throws IOException {
        this(dataFile, Files.newInputStream(stylesheet));
    }

    HtmlGenerator(Path dataFile, InputStream stylesheetStream) {
        this.stylesheetStream = stylesheetStream;
        this.dataFile = dataFile;
        targetMethods = new TreeSet<>();
        testMethods = new TreeSet<>();
    }

    HtmlGenerator(String dataFilePath) {
        this(Path.of(dataFilePath), (InputStream) null);
    }

    HtmlGenerator(Path dataFile) {
        this(dataFile, (InputStream) null);
    }

    /**
     * Generates a html report from the supplied raw data file. If a
     * stylesheet has been provided it is applied to the report.
     *
     * @return String containing the html report
     */
    public String generateReport() {

        String table = "";
        String style = "";
        try {
            parseDataFile(); // create target and test method objects from raw data file
            if (stylesheetStream != null) {
                style = readStyleSheet(stylesheetStream);
            }
            table = generateHtmlTable(); // create html table from target and test method objects
        } catch (IOException e) {
            System.err.println("Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }

        // generate full html document
        return ElementFactory.doctype() +
                ElementFactory.html(
                        ElementFactory.head(),
                        ElementFactory.style(style),
                        ElementFactory.body(table));
    }

    private String generateHtmlTable() {

        StringBuilder rows = new StringBuilder();

        // create header row with a cell for every test method
        StringBuilder headerRow = new StringBuilder(ElementFactory.tableHeader());
        for (String method : testMethods) {
            headerRow.append(ElementFactory.rotatedTableHeader(method));
        }
        rows.append(ElementFactory.tableRow(headerRow.toString()));

        // create a row for every target method
        for (TargetMethod targetMethod : targetMethods) {
            StringBuilder row = new StringBuilder();
            row.append(ElementFactory.tableCell(targetMethod.name));

            // loop through all test methods and check if current target method
            // contains the test methods in its collection of tester methods
            var testers = targetMethod.testers;
            for (String testMethod : testMethods) {
                if (testers.contains(testMethod)) {
                    row.append(ElementFactory.tableCell("x"));
                } else {
                    row.append(ElementFactory.tableCell());
                }
            }
            rows.append(ElementFactory.tableRow(row.toString()));
        }

        return ElementFactory.table(
                rows.toString());
    }

    private String readStyleSheet(InputStream stream) throws IOException {
        StringBuilder styleString = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        reader.lines()
                .forEach(styleString::append);

        return styleString.toString();
    }

    private void parseDataFile() throws IOException {
        // read lines from file
        try (Stream<String> lines = Files.lines(dataFile)) {
            lines.map(l -> l.split("\\|"))
                    .filter(s -> s.length > 0)
                    .forEach(this::parseLineSubstrings);
        } catch (IOException e) {
            throw new IOException("Parsing " + dataFile + "failed: " + e.getMessage());
        }
    }


    private void parseLineSubstrings(String[] strings) {
        // first string in array is the target method, the rest (if they exist)
        // are
        // test methods
        var targetMethod = new TargetMethod(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            if (strings[i] != null) {
                targetMethod.addTestMethod(strings[i]);
                testMethods.add(strings[i]);
            }
        }
        targetMethods.add(targetMethod);
    }

    /**
     * Represents a method in the target project (SUT). Keeps a list of the
     * test methods that has called it.
     */
    private static class TargetMethod implements Comparable<TargetMethod> {
        String name;
        List<String> testers;

        public TargetMethod(String name) {
            this.name = name;
            testers = new ArrayList<>();
        }

        void addTestMethod(String method) {
            testers.add(method);
        }

        @Override
        public int compareTo(TargetMethod other) {
            return name.compareTo(other.name);
        }
    }
}

