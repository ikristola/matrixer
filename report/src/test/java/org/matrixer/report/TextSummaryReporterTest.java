package org.matrixer.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.matrixer.core.Analyzer;
import org.matrixer.core.ExecutionData;
import org.matrixer.core.runtime.MethodCall;

public class TextSummaryReporterTest {

    @Test
    void printsErrorMessageIfDataIsEmpty() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);
        TextSummaryReporter tr = new TextSummaryReporter();

        tr.reportTo(new ExecutionData(), stream);

        assertTrue(out.toString().contains("No executed methods"));
    }

    @Test
    void printsNumberOfExecutedMethodsAndTests() {
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(1, "ClassA", "TestCase1"),
                new MethodCall(2, "ClassA", "TestCase2"),
                new MethodCall(3, "ClassB", "TestCase3"),
        };
        ExecutionData data = new Analyzer().analyze(asInputStream(calls));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);

        TextSummaryReporter tr = new TextSummaryReporter();
        tr.reportTo(data, stream);

        assertContains(out.toString(), "Executed methods: " + 2);
        assertContains(out.toString(), "Executed tests: " + 3);
    }

    @Test
    void printsMinAndMaxCallStackDepths() {
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(1, "ClassA", "TestCase1"),
                new MethodCall(2, "ClassA", "TestCase2"),
                new MethodCall(3, "ClassB", "TestCase3"),
        };
        ExecutionData data = new Analyzer().analyze(asInputStream(calls));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);

        TextSummaryReporter tr = new TextSummaryReporter();
        tr.reportTo(data, stream);

        assertContains(out.toString(), "Min stack depth: " + 1);
        assertContains(out.toString(), "Max stack depth: " + 3);
    }

    @Test
    void printsMedianAndAverageStacKDepths() {
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(1, "ClassA", "TestCase1"),
                new MethodCall(1, "ClassA", "TestCase2"),
                new MethodCall(1, "ClassB", "TestCase3"),
                new MethodCall(1, "ClassB", "TestCase3"),
                new MethodCall(1, "ClassB", "TestCase3"),
                new MethodCall(3, "ClassB", "TestCase3"),
                new MethodCall(4, "ClassB", "TestCase3"),
                new MethodCall(4, "ClassB", "TestCase3"),
                new MethodCall(4, "ClassB", "TestCase3"),
                new MethodCall(4, "ClassB", "TestCase3"),
        };
        ExecutionData data = new Analyzer().analyze(asInputStream(calls));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);

        TextSummaryReporter tr = new TextSummaryReporter();
        tr.reportTo(data, stream);

        assertContains(out.toString(), "Average stack depth: 2.4");
        assertContains(out.toString(), "Median stack depth: 2.0");
    }

    @Test
    void printsAverageWithOnlyOneDecimal() {
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(8, "ClassA", "TestCase1"),
                new MethodCall(7, "ClassA", "TestCase2"),
                new MethodCall(10, "ClassB", "TestCase3"),
        };
        ExecutionData data = new Analyzer().analyze(asInputStream(calls));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);

        TextSummaryReporter tr = new TextSummaryReporter();
        tr.reportTo(data, stream);

        assertContains(out.toString(), "Average stack depth: 8.3\n");
    }

    @Test
    void calcMedianEvenElements() {
        var reporter = new TextSummaryReporter();
        var ints = Arrays.asList(new Integer[] {1, 2, 4, 5, 6, 7});
        Collections.shuffle(ints);
        assertEquals(4.5, reporter.calcMedian(ints));
    }

    @Test
    void calcMedianOddElements() {
        var reporter = new TextSummaryReporter();
        var ints = Arrays.asList(new Integer[] {1, 3, 3, 4, 6, 6, 7});
        Collections.shuffle(ints);
        assertEquals(4, reporter.calcMedian(ints));
    }

    @Test
    void calcMedianTwoElements() {
        var reporter = new TextSummaryReporter();
        var ints = Arrays.asList(new Integer[] {1, 3});
        Collections.shuffle(ints);
        assertEquals(2, reporter.calcMedian(ints));
    }

    @Test
    void calcMedianOneElement() {
        var reporter = new TextSummaryReporter();
        var ints = Arrays.asList(new Integer[] {3});
        Collections.shuffle(ints);
        assertEquals(3, reporter.calcMedian(ints));
    }

    void assertContains(String haystack, String needle) {
        assertTrue(haystack.contains(needle), "\n==== output ===\n" + haystack
                + "\n=== did not contain ===\n" + needle + "\n===\n");
    }

    public static InputStream asInputStream(MethodCall[] calls) {
        String data = asRawString(calls);
        return asInputStream(data);
    }

    public static String asRawString(MethodCall[] calls) {
        StringBuilder builder = new StringBuilder();
        for (var call : calls) {
            builder.append(call.asLine());
            builder.append('\n');
        }
        return builder.toString();
    }

    public static InputStream asInputStream(String s) {
        return new ByteArrayInputStream(s.getBytes());
    }

}
