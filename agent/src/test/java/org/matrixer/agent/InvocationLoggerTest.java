package org.matrixer.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.matrixer.core.runtime.PrintLogger;
import org.matrixer.core.runtime.MethodCall;

class InvocationLoggerTest {

    ByteArrayOutputStream out;
    StackRecorder recorder;
    Random random = new Random();

    @BeforeEach
    void setup() {
        out = new ByteArrayOutputStream();
        SynchronizedWriter w = new SynchronizedWriter(new OutputStreamWriter(out));
        recorder = new StackRecorderImpl(w, new PrintLogger(System.out));
    }

    @Test
    void logsTestCase() {
        String testCase = "TestMethod" + getUniqueId();
        String method = "Method" + getUniqueId();
        long thread = Thread.currentThread().getId();

        recorder.beginTestCase(testCase, thread);
        recorder.pushMethod(method, thread);
        recorder.popMethod(method, thread);
        recorder.endTestCase(testCase, thread);

        String[] output = finish();
        assertFound(output, 1, method, testCase);
    }

    @Test
    void stackDepthLimit() {
        int depthLimit = 2;
        int nestCount = 10;
        recorder.setDepthLimit(depthLimit);
        String testCase = "TestMethod" + getUniqueId();
        List<String> nestedMethods = createTargetMethods(nestCount);

        long thread = Thread.currentThread().getId();
        recorder.beginTestCase(testCase, thread);
        callNested(nestedMethods, thread);
        recorder.endTestCase(testCase, thread);

        String[] output = finish();
        assertIncreasingDepth(output);
        assertEquals(depthLimit, output.length);
    }

    @Test
    void noStackDepthLimit() {
        int nestCount = 10;
        String testCase = "TestMethod" + getUniqueId();
        List<String> nestedMethods = createTargetMethods(nestCount);

        long thread = Thread.currentThread().getId();
        recorder.beginTestCase(testCase, thread);
        callNested(nestedMethods, thread);
        recorder.endTestCase(testCase, thread);

        String[] output = finish();
        assertIncreasingDepth(output);
        assertEquals(nestCount, output.length);
    }

    @Test
    void callConsequtive() {
        String testCase = "TestCase" + getUniqueId();
        List<String> methods = createTargetMethods(10);

        long thread = Thread.currentThread().getId();
        recorder.beginTestCase(testCase, thread);
        callConsequtive(methods, thread);
        recorder.endTestCase(testCase, thread);
        String[] output = finish();

        assertEquals(10, output.length, "Did not log every method");
        assertEqualDepth(1, output);
    }

    @Test
    void mapsNewThreadToCurrentTestCase() throws InterruptedException {
        String testCase = "TestCase" + getUniqueId();
        String method = "Method" + getUniqueId();

        long thread = Thread.currentThread().getId();
        recorder.beginTestCase(testCase, thread);
        Thread t = newThread(() -> {
            long child = Thread.currentThread().getId();
            recorder.pushMethod(method, child);
            recorder.popMethod(method, child);
        });
        t.start();
        t.join();
        recorder.endTestCase(testCase, thread);

        String output = finish()[0];
        assertFalse(output.isEmpty(), "Output was empty");
    }

    @Test
    void callDepthOnNewThread() throws InterruptedException {
        int nestCount = 10;
        String testCase = "TestMethod" + getUniqueId();
        List<String> sequentialMethods = createTargetMethods(nestCount);
        List<String> concurrentMethods = createTargetMethods(nestCount);

        long thread = Thread.currentThread().getId();
        recorder.beginTestCase(testCase, thread);
        pushMethods(sequentialMethods, thread);
        runInNewThread(thread, () -> {
            long child = Thread.currentThread().getId();
            callNested(concurrentMethods, child);
        });
        popMethods(sequentialMethods, thread);
        recorder.endTestCase(testCase, thread);

        String[] output = finish();
        assertEquals(2 * nestCount, output.length);
        assertIncreasingDepth(output);
    }

    @Test
    void nextTestStartsWithZeroDepth() throws InterruptedException {
        int nestCount = 10;
        String testCase = "TestMethod" + getUniqueId();
        List<String> sequentialMethods = createTargetMethods(nestCount);
        List<String> concurrentMethods = createTargetMethods(nestCount);

        final long thread = Thread.currentThread().getId();
        recorder.beginTestCase(testCase, thread);
        pushMethods(sequentialMethods, thread);
        runInNewThread(thread, () -> {
            long child = Thread.currentThread().getId();
            callNested(concurrentMethods, child);
        });
        popMethods(sequentialMethods, thread);
        recorder.endTestCase(testCase, thread);
        finish();


        recorder.beginTestCase(testCase, thread);
        recorder.pushMethod("NewMethod", thread);
        recorder.popMethod("NewMethod", thread);
        recorder.endTestCase(testCase, thread);

        String[] output = finish();
        assertFound(output, 1, "NewMethod", testCase);
    }

    @Test
    void doesNotLogCallAfterTestCaseEnds() {
        String testCase = "TestMethod" + getUniqueId();
        final long thread = Thread.currentThread().getId();

        recorder.beginTestCase(testCase, thread);
        recorder.pushMethod("NewMethod", thread);
        recorder.popMethod("NewMethod", thread);
        recorder.endTestCase(testCase, thread);
        String[] dummy = finish();
        out.reset();

        recorder.pushMethod("NewMethod", thread);
        recorder.popMethod("NewMethod", thread);
        String[] output = finish();

        assertEquals(0, output.length, "Output not empty: " + String.join("\n", output));

        recorder.beginTestCase("NewTestCase", thread);
        recorder.pushMethod("NewMethod", thread);
        recorder.popMethod("NewMethod", thread);
        recorder.endTestCase("NewTestCase", thread);
        String[] secondOutput = finish();
        assertEquals(1, secondOutput.length, "Output was empty: " + String.join("\n", secondOutput));

    }

    void assertFound(String[] output, int depth, String method, String testCase) {
        String expected = new MethodCall(depth, method, testCase).asLine();
        List<String> lines = List.of(output);
        assertTrue(lines.contains(expected), "Output did not contain method: " + expected);
    }

    void assertEqualDepth(int depth, String[] output) {
        try {
            Stream.of(output)
                    .map(MethodCall::new)
                    .map(mc -> mc.depth)
                    .forEach(actual -> assertEquals(depth, actual, "Depth not correct"));
        } catch (Throwable e) {
            System.out.println("Output:\n" + String.join("\n", output));
            throw e;
        }
    }

    void assertIncreasingDepth(String[] output) {
        try {
            for (int i = 0; i < output.length; i++) {
                String line = output[i];
                assertFalse(line.isEmpty(), "Line was empty");
                MethodCall call = new MethodCall(line);
                int expectedDepth = i + 1;
                assertEquals(expectedDepth, call.depth, "Call depth incorrect");
            }
        } catch (Throwable e) {
            System.out.println("Output:\n" + String.join("\n", output));
            throw e;
        }
    }

    void runInNewThread(long parentThread, Runnable runnable) throws InterruptedException {
        Thread t = new Thread(runnable);
        recorder.newThread(parentThread, t);
        t.start();
        t.join();
    }

    List<String> createTargetMethods(int count) {
        List<String> nestedMethods = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            nestedMethods.add("Method" + getUniqueId());
        }
        return nestedMethods;
    }

    public void callNested(List<String> methods) {
        callNested(methods, Thread.currentThread().getId());
    }

    public void callNested(List<String> methods, long thread) {
        pushMethods(methods, thread);
        popMethods(methods, thread);
    }

    public void pushMethods(List<String> methods, long thread) {
        for (var method : methods) {
            recorder.pushMethod(method, thread);
        }
    }

    public void popMethods(List<String> methods, long thread) {
        for (var method : methods) {
            recorder.popMethod(method, thread);
        }
    }

    public void callConsequtive(List<String> methods, long thread) {
        for (var method : methods) {
            recorder.pushMethod(method, thread);
            recorder.popMethod(method, thread);
        }
    }

    String[] finish() {
        String output = out.toString();
        int threadCount = recorder.activeThreadCount();
        assertEquals(0, threadCount, "All threads not released: " + threadCount);
        if (output.isEmpty()) {
            return new String[0];
        }
        return output.split("\n");
    }

    Thread newThread(Runnable runnable) {
        long parent = Thread.currentThread().getId();
        Thread newThread = new Thread(runnable);
        recorder.newThread(parent, newThread);
        return newThread;
    }

    int getUniqueId() {
        return random.nextInt(Integer.MAX_VALUE);
    }

}
