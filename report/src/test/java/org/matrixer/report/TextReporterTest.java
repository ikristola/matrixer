package org.matrixer.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.matrixer.core.ExecutionData;
import org.matrixer.core.runtime.MethodCall;

class TextReporterTest {

    ByteArrayOutputStream out;

    @BeforeEach
    void setup() {
        out = new ByteArrayOutputStream();
    }

    @Test
    void testEmptyData() {
        var data = new ExecutionData();
        var reporter = new TextReporter(data);
        reporter.reportTo(new PrintStream(out));

        var results = out.toString();
        assertTrue(results.isEmpty(), "Not empty: " + results);
    }

    @Test
    void singleTestSingleMethod() throws IOException {
        MethodCall call = new MethodCall(3, "Method", "TestCase");
        ExecutionData data = new ExecutionData();
        data.addCall(call);

        var reporter = new TextReporter(data);
        reporter.reportTo(new PrintStream(out));

        String results = out.toString();
        String expected = "3\n";
        assertEquals(expected, results);
    }

    @Test
    void singleTestMultipleCalls() throws IOException {
        MethodCall[] calls = {
                new MethodCall(1, "Method", "TestCase"),
                new MethodCall(2, "Method", "TestCase"),
                new MethodCall(3, "Method", "TestCase"),
                new MethodCall(4, "Method", "TestCase")
        };
        ExecutionData data = new ExecutionData();
        for (var c : calls) {
            data.addCall(c);
        }

        var reporter = new TextReporter(data);
        reporter.reportTo(new PrintStream(out));

        String results = out.toString();
        String expected = "1\n";
        assertEquals(expected, results);
    }

    @Test
    void mulipleTestMultipleCalls() throws IOException {
        MethodCall[] calls = {
                new MethodCall(1, "Method1", "TestCase1"),
                new MethodCall(2, "Method2", "TestCase2"),
                new MethodCall(3, "Method3", "TestCase3"),
                new MethodCall(4, "Method4", "TestCase4"),
                new MethodCall(5, "Method5", "TestCase5"),
        };
        ExecutionData data = new ExecutionData();
        for (var c : calls) {
            data.addCall(c);
        }

        var reporter = new TextReporter(data);
        reporter.reportTo(new PrintStream(out));

        String results = out.toString();
        String expected =
                "1----\n"
                        + "-2---\n"
                        + "--3--\n"
                        + "---4-\n"
                        + "----5\n";
        assertEquals(expected, results);
    }
}
