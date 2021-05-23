package org.matrixer.report;

import java.util.*;

import org.matrixer.core.ExecutedMethod;
import org.matrixer.core.ExecutionData;

public class MatrixSplitter {

    Collection<ExecutedMethod> recordedMethods;
    Collection<String> recordedTests;

    public MatrixSplitter(ExecutionData data) {
        this.recordedMethods = data.getAllTargetMethods();
        this.recordedTests = data.getAllTestCases();
    }

    public List<Result> partition() {
        List<Result> results = new ArrayList<>();
        while (!recordedMethods.isEmpty()) {
            results.add(split());
        }
        return results;
    }

    private Result split() {
        Queue<ExecutedMethod> methodFrontier = new ArrayDeque<>();
        List<ExecutedMethod> exploredMethods = new ArrayList<>();
        List<String> exploredTests = new ArrayList<>();

        methodFrontier.add(recordedMethods.iterator().next());
        while (!methodFrontier.isEmpty()) {
            var method = methodFrontier.remove();
            // System.err.println("Examining " + method.name());
            recordedMethods.remove(method);
            exploredMethods.add(method);

            var it = recordedTests.iterator();
            while (it.hasNext()) {
                var test = it.next();
                if (method.wasCalledBy(test)) {
                    // System.err.println("    it was called by " + test);
                    it.remove();
                    exploredTests.add(test);
                    moveCalledMethodsToFrontier(methodFrontier, test);
                }
            }
        }
        return new Result(exploredMethods, exploredTests);
    }

    void moveCalledMethodsToFrontier(Queue<ExecutedMethod> frontier, String testCase) {
        // System.err.println("    Found methods of " + testCase + ":");
        for (var it = recordedMethods.iterator(); it.hasNext(); ) {
            var method = it.next();
            if (method.wasCalledBy(testCase)) {
                // System.err.println("        " + method.name());
                frontier.add(method);
                it.remove();
            }
        }
    }

    public class Result {

        public final List<ExecutedMethod> methods;
        public final List<String> tests;

        public Result(List<ExecutedMethod> methods, List<String> tests) {
            this.methods = Collections.unmodifiableList(methods);
            this.tests = Collections.unmodifiableList(tests);
        }

        public int matrixSize() {
            return methods.size() * tests.size();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (var m : methods) {
                for (var t : tests) {
                    if (m.wasCalledBy(t)) {
                        builder.append("x");
                    } else {
                        builder.append("-");
                    }
                }
                builder.append("\n");
            }
            return builder.toString();
        }
    }
}
