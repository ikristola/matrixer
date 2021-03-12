package org.matrixer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Builds a human-readable html report from a raw data matrix file
 */
public class ReportBuilder {

    private final String dataFilePath;
    private final String styleSheetPath;
    SortedSet<String> testMethods;  // set of all test methods in data file
    List<AppMethod> appMethods;     // all application methods in data file

    ReportBuilder(String dataFilePath, String styleSheetPath) {
        this.dataFilePath = dataFilePath;
        this.styleSheetPath = styleSheetPath;
        appMethods = new ArrayList<>();
        testMethods = new TreeSet<>();
    }

    ReportBuilder(String dataFilePath) {
        this(dataFilePath, "");
    }

    /**
     * Generates a html report from the supplied raw data file.
     * If a stylesheet has been provided it is applied to the report.
     *
     * @return String containing the html report
     */
    public String generateReport() {

        String table = "";
        String style = "";
        try {
            parseDataFile(); // create app and test method objects from raw data file
            if (!styleSheetPath.equals("")) {
                style = readStyleSheet();
            }
            table = generateHtmlTable();  // create html table from app and test method objects
        } catch (IOException e) {
            System.err.println("Failed to generate report: " + e.getMessage());
            e.printStackTrace();
        }

        // generate full html document
        return ElementFactory.doctype() +
                ElementFactory.html(
                        ElementFactory.head(),
                        ElementFactory.style(style),
                        ElementFactory.body(table)
                );
    }

    private String generateHtmlTable() {

        StringBuilder rows = new StringBuilder();

        // create header row with a cell for every test method
        StringBuilder headerRow = new StringBuilder(ElementFactory.tableHeader());
        for (String method : testMethods) {
            headerRow.append(ElementFactory.rotatedTableHeader(method));
        }
        rows.append(ElementFactory.tableRow(headerRow.toString()));

        // create a row for every app method
        for (AppMethod appMethod : appMethods) {
            StringBuilder row = new StringBuilder();
            row.append(ElementFactory.tableCell(appMethod.name));

            // loop through all test methods and check if current app method
            // contains the test methods in its collection of tester methods
            var testers = appMethod.testers;
            for (String testMethod : testMethods) {
                if (testers.contains(testMethod)) {
                    row.append(ElementFactory.tableCell("x"));
                }
                else {
                    row.append(ElementFactory.tableCell());
                }
            }
            rows.append(ElementFactory.tableRow(row.toString()));
        }

        return ElementFactory.table(
                rows.toString()
        );
    }

    private String readStyleSheet() throws IOException {
        StringBuilder styleString = new StringBuilder();

        var lines = Files.lines(Path.of(styleSheetPath));
        lines.forEach(styleString::append);

        return styleString.toString();
    }

    private void parseDataFile() throws IOException {
        // read lines from file
        var lines = Files.lines(Path.of(dataFilePath));
        lines.map(l -> l.split("\\|"))
                .filter(s -> s.length > 0)
                .forEach(this::parseLineSubstrings);
    }


    private void parseLineSubstrings(String[] strings) {
        // first string in array is the app method, the rest (if they exist) are test methods
        var appMethod = new AppMethod(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            if (strings[i] != null) {
                appMethod.addTestMethod(strings[i]);
                testMethods.add(strings[i]);
            }
        }
        appMethods.add(appMethod);
    }

    /**
     * Represents an app method. Keeps a list of the test methods that has called it.
     */
    private static class AppMethod {
        String name;
        List<String> testers;

        public AppMethod(String name) {
            this.name = name;
            testers = new ArrayList<>();
        }

        void addTestMethod(String method) {
            testers.add(method);
        }
    }
}

