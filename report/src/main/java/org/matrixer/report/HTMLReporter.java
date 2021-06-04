package org.matrixer.report;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.matrixer.core.*;
import org.matrixer.core.util.Range;

/**
 * Builds a human-readable html report from collected execution data
 */
public class HTMLReporter {

    private ExecutionData data;

    /**
     * Creates a reporter
     *
     * @param data
     *            collected execution data to use
     */
    public HTMLReporter(ExecutionData data) {
        this.data = data;
    }

    public void reportTo(OutputStream out) throws IOException {
        Collection<String> testCaseNames = getSortedTestCaseNames();
        Collection<ExecutedMethod> targetMethods = getSortedTargetMethods();

        String style = getDefaultStyle();
        String executionMatrix = createExecutionTable(testCaseNames, targetMethods);
        String methodCallDepths = createMethodCallDepthsTable(targetMethods);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write(ElementFactory.doctype());
        writer.write(ElementFactory.html(
                ElementFactory.head(),
                ElementFactory.style(style),
                ElementFactory.body(
                        executionMatrix,
                        methodCallDepths)));

        writer.flush();
    }

    private String getDefaultStyle() throws IOException {
        String stylesheetName = "report-style.css";
        try {
            InputStream cssStream = getResourceStream(stylesheetName);
            return readStyleSheet(cssStream);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error reading " + stylesheetName + ": " + e.getMessage());
        }
    }

    private InputStream getResourceStream(String fname) throws URISyntaxException, IOException {
        InputStream resource = getClass().getResourceAsStream(fname);
        if (resource == null) {
            throw new RuntimeException("Could not locate " + fname);
        }
        return resource;
    }

    private String readStyleSheet(InputStream stream) throws IOException {
        StringBuilder styleString = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        reader.lines().forEach(styleString::append);
        return styleString.toString();
    }

    String createExecutionTable(Collection<String> testCaseNames,
            Collection<ExecutedMethod> targetMethods) {
        StringBuilder rows = new StringBuilder();

        // Table header of test cases
        String headerRow = createExecutionTableHeaderRow(testCaseNames);
        rows.append(ElementFactory.tableRow(headerRow));

        // Rows of target methods
        for (var method : targetMethods) {
            String row = createMethodRow(method, testCaseNames);
            rows.append(ElementFactory.tableRow(row.toString()));
        }
        return ElementFactory.table(rows.toString());
    }

    String createMethodCallDepthsTable(Collection<ExecutedMethod> targetMethods) {
        StringBuilder rows = new StringBuilder();

        String headerRow = ElementFactory.tableRow(
                ElementFactory.tableHeader("Name"),
                ElementFactory.tableHeader("Min"),
                ElementFactory.tableHeader("Max"));
        rows.append(headerRow);

        for (var method : targetMethods) {
            Range depthRange = method.depthOfCalls();
            rows.append(ElementFactory.tableRow(
                    ElementFactory.tableCell(method.name()),
                    ElementFactory.tableCell(Integer.toString(depthRange.min())),
                    ElementFactory.tableCell(Integer.toString(depthRange.max()))));
        }
        return ElementFactory.table(rows.toString());
    }

    Collection<String> getSortedTestCaseNames() {
        return data.getAllTestCases()
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    Collection<ExecutedMethod> getSortedTargetMethods() {
        return data.getAllTargetMethods()
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    String createExecutionTableHeaderRow(Collection<String> testCaseNames) {
        StringBuilder headerRow = new StringBuilder(ElementFactory.tableHeader());
        for (String testCaseName : testCaseNames) {
            headerRow.append(ElementFactory.rotatedTableHeader(testCaseName));
        }
        return headerRow.toString();
    }

    String createMethodRow(ExecutedMethod method, Collection<String> testCaseNames) {
        StringBuilder row = new StringBuilder();
        row.append(ElementFactory.tableCell(method.name()));

        for (String testCaseName : testCaseNames) {
            String depthString = "";
            if (method.wasCalledBy(testCaseName)) {
                Range depth = method.depthOfCall(testCaseName);
                if (depth.min() != depth.max()) {
                    depthString = String.format("%d/%d", depth.min(), depth.max());
                } else {
                    depthString = Integer.toString(depth.min());
                }
            }
            row.append(ElementFactory.tableCell(depthString));
        }
        return row.toString();
    }

}
