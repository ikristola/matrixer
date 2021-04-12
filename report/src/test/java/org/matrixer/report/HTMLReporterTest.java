package org.matrixer.report;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;

import org.junit.jupiter.api.Test;
import org.matrixer.core.*;

class HTMLReporterTest {

    @Test
    void createsEmptyTableFromEmptyData() throws IOException {
        ExecutionData data = new ExecutionData();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HTMLReporter reporter = new HTMLReporter(data);
        reporter.reportTo(out);

        String html = out.toString();
        String first = "<!DOCTYPE html><html><head></head><style>";
        String last = "</style><body><table><tr><th></th></tr></table>";
        assertSubstring(first, html);
        assertSubstring(last, html);
    }

    @Test
    void singleTestSingleMethod() throws IOException {
        MethodCall call = new MethodCall(3, "Method", "TestCase");
        ExecutionData data = new ExecutionData();
        data.addCall(call);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HTMLReporter reporter = new HTMLReporter(data);
        reporter.reportTo(out);

        String html = out.toString();
        String first = "<th></th><th class=\"rotate\"><div><span>TestCase</span></div></th>";
        String last = "<td>Method</td><td>3</td>";
        assertSubstring(first, html);
        assertSubstring(last, html);
    }

    @Test
    void multipleTestsMultipleMethods() throws IOException {
        MethodCall[] calls = {
                new MethodCall(1, "MethodA", "TestCase1"),
                new MethodCall(2, "MethodB", "TestCase2"),
                new MethodCall(3, "MethodC", "TestCase3"),
        };
        ExecutionData data = toData(calls);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HTMLReporter reporter = new HTMLReporter(data);
        reporter.reportTo(out);

        String html = out.toString();
        assertSubstring("<th></th><th class=\"rotate\"><div><span>TestCase1</span></div></th>",
                html);
        assertSubstring("<th class=\"rotate\"><div><span>TestCase2</span></div></th>",
                html);
        assertSubstring("<th class=\"rotate\"><div><span>TestCase3</span></div></th>",
                html);
        assertSubstring("<td>MethodA</td><td>1</td><td></td><td></td>", html);
        assertSubstring("<td>MethodB</td><td></td><td>2</td><td></td>", html);
        assertSubstring("<td>MethodC</td><td></td><td></td><td>3</td>", html);
    }

    @Test
    void createsCallStackDepthTable() throws IOException {
        MethodCall[] calls = {
                new MethodCall(1, "MethodA", "TestCase1"),
                new MethodCall(10, "MethodA", "TestCase1"),
                new MethodCall(12, "MethodB", "TestCase2"),
                new MethodCall(2, "MethodB", "TestCase2"),
                new MethodCall(5, "MethodC", "TestCase3"),
                new MethodCall(50, "MethodC", "TestCase3"),
                new MethodCall(10, "MethodC", "TestCase3"),
        };
        ExecutionData data = toData(calls);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HTMLReporter reporter = new HTMLReporter(data);
        reporter.reportTo(out);

        String html = out.toString();
        for (MethodCall call : calls) {
            ExecutedMethod exec = data.getTargetMethod(call.methodName);
            Range depthRange = exec.depthOfCalls();
            String sub = String.format("<td>%s</td><td>%d</td><td>%d</td>",
                    exec.name(), depthRange.min(), depthRange.max());
            assertSubstring(sub, html);
        }
    }

    ExecutionData toData(MethodCall[] calls) {
        ExecutionData data = new ExecutionData();
        for (var call : calls) {
            data.addCall(call);
        }
        return data;
    }

    void assertSubstring(String sub, String candidate) {
        assertTrue(candidate.contains(sub), sub + "\n not in \n" + candidate);
    }

}
