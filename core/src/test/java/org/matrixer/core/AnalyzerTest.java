package org.matrixer.core;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.matrixer.core.testsupport.TestUtils;
import org.matrixer.core.util.Range;

class AnalyzerTest {

    @Test
    void collectsAllMethodsAndTestCases() {
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(3, "Class1", "TestCase1"),
                new MethodCall(3, "Class2", "TestCase1"),
                new MethodCall(3, "Class2", "TestCase2"),
                new MethodCall(3, "Class3", "TestCase3"),
                new MethodCall(3, "Class4", "TestCase4"),
                new MethodCall(3, "Class5", "TestCase4"),
                new MethodCall(3, "Class5", "TestCase5"),
                new MethodCall(3, "Class5", "TestCase6"),
        };
        InputStream source = asInputStream(calls);

        Analyzer analyzer = new Analyzer();
        ExecutionData result = analyzer.analyze(source);

        assertEquals(5, result.getAllTargetMethods().size());
        assertEquals(6, result.getAllTestCases().size());
    }

    @Test
    void deduplicatesRepeatingTargetMethods() {
        String targetMethodName = "package.Class.AppMethod()";
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(3, targetMethodName, "package.TestClass:TestMethod"),
                new MethodCall(2, targetMethodName, "package.AnotherTestClass:TestMethod"),
        };
        InputStream source = asInputStream(calls);

        Analyzer analyzer = new Analyzer();
        ExecutionData result = analyzer.analyze(source);

        assertEquals(1, result.getAllTargetMethods().size());
    }

    @Test
    void deduplicatesRepeatingTestCases() {
        String testCaseName = "package.TestClass:TestMethod";
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(3, "package.Class.AppMethod()", testCaseName),
                new MethodCall(2, "package.Class.AnotherAppMethod()", testCaseName),
        };
        InputStream source = asInputStream(calls);

        Analyzer analyzer = new Analyzer();
        ExecutionData result = analyzer.analyze(source);

        assertEquals(1, result.getAllTestCases().size());
    }

    @Test
    void collectsMinAndMaxDepthOfMethodCalls() {
        String targetMethodName = "package.Class.AppMethod()";
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(1, targetMethodName, "package.TestClass:TestMethod"),
                new MethodCall(5, targetMethodName, "package.AnotherTestClass:TestMethod"),
                new MethodCall(10, targetMethodName, "package.AThirdTestClass:TestMethod"),
        };
        InputStream source = asInputStream(calls);

        Analyzer analyzer = new Analyzer();
        ExecutionData result = analyzer.analyze(source);

        ExecutedMethod targetMethod = result.getTargetMethod(targetMethodName);
        Range depthRange = targetMethod.depthOfCalls();
        assertEquals(1, depthRange.min());
        assertEquals(10, depthRange.max());
    }

    @Test
    void collectsMinAndMaxDepthOfSameMethodTestCasePair() {
        String targetMethodName = "package.Class.AppMethod()";
        String testCaseName = "package.TestClass:TestMethod";
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(1, targetMethodName, testCaseName),
                new MethodCall(5, targetMethodName, testCaseName),
                new MethodCall(10, targetMethodName, testCaseName),
        };
        InputStream source = asInputStream(calls);

        Analyzer analyzer = new Analyzer();
        ExecutionData result = analyzer.analyze(source);

        ExecutedMethod targetMethod = result.getTargetMethod(targetMethodName);
        Range depthRange = targetMethod.depthOfCall(testCaseName);
        assertEquals(1, depthRange.min());
        assertEquals(10, depthRange.max());
    }

    @Test
    void mapsTargetMethodToTestCase() {
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(3, "ClassA", "TestCase1"),
                new MethodCall(3, "ClassA", "TestCase2"),
                new MethodCall(3, "ClassB", "TestCase3"),
        };
        InputStream source = asInputStream(calls);

        Analyzer analyzer = new Analyzer();
        ExecutionData result = analyzer.analyze(source);

        ExecutedMethod targetMethod = result.getTargetMethod("ClassA");
        assertTrue(targetMethod.wasCalledBy("TestCase1"), "False positive");
        assertTrue(targetMethod.wasCalledBy("TestCase2"), "False positive");
        assertFalse(targetMethod.wasCalledBy("TestCase3"), "False negative");
    }

    @Test
    void canAccessCallers() {
        MethodCall[] calls = new MethodCall[] {
                new MethodCall(3, "ClassA", "TestCase1"),
                new MethodCall(3, "ClassA", "TestCase2"),
                new MethodCall(3, "ClassB", "TestCase3"),
        };
        InputStream source = asInputStream(calls);

        Analyzer analyzer = new Analyzer();
        ExecutionData result = analyzer.analyze(source);

        ExecutedMethod targetMethod = result.getTargetMethod("ClassA");
        Collection<ExecutedMethod.Call> callers = targetMethod.callers();
        String[] includes = {"TestCase1", "TestCase2"};
        String[] excludes = {"TestCase3"};
        assertCollectionValues(callers, includes, excludes, ExecutedMethod.Call::caller);
    }

    <T, R> void assertCollectionValues(
            Collection<T> c, R[] includes, R[] excludes, Function<T, R> mapper) {
        Set<R> mapped = c.stream()
                .map(item -> mapper.apply(item))
                .collect(Collectors.toSet());

        List<R> incList = new ArrayList<>(Arrays.asList(includes));
        var incIterator = incList.iterator();
        while (incIterator.hasNext()) {
            if (mapped.contains(incIterator.next())) {
                incIterator.remove();
            }
        }
        assertTrue(incList.isEmpty(), "Did not include " + incList);

        List<R> exList = Arrays.asList(excludes);
        List<R> found = new ArrayList<>();
        var exIterator = exList.iterator();
        while (exIterator.hasNext()) {
            R val = exIterator.next();
            if (mapped.contains(val)) {
                found.add(val);
            }
        }
        assertTrue(found.isEmpty(), "Incorrectly included " + found);
    }

    InputStream asInputStream(MethodCall[] calls) {
        String data = asRawString(calls);
        return TestUtils.asInputStream(data);
    }

    String asRawString(MethodCall[] calls) {
        StringBuilder builder = new StringBuilder();
        for (var call : calls) {
            builder.append(asRawLine(call));
        }
        return builder.toString();
    }

    String asRawLine(MethodCall call) {
        return call.depth + "|" + call.methodName + "<=" + call.callerName + "\n";
    }
}
