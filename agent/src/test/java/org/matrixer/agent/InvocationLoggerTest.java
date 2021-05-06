package org.matrixer.agent;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.matrixer.core.runtime.Logger;
import org.matrixer.core.runtime.MethodCall;

class InvocationLoggerTest {

    ByteArrayOutputStream out;
    InvocationLogger logger;
    Random random = new Random();

    @BeforeEach
    void setup() {
        out = new ByteArrayOutputStream();
        SynchronizedWriter w = new SynchronizedWriter(new OutputStreamWriter(out));
        boolean debug = true;
        logger = new InvocationLogger(w, debug, new Logger(System.out));
    }

    @Test
    void logsTestCase() {
        String testCase = "TestMethod" + getUniqueId();
        String method = "Method" + getUniqueId();

        logger.logBeginTestCase(testCase);
        logger.logPushMethod(method);
        logger.logPopMethod(method);
        logger.logEndTestCase(testCase);

        String[] output = finish();
        assertFound(output, 1, method, testCase);
    }

    @Test
    void stackDepthLimit() {
        int depthLimit = 2;
        int nestCount = 10;
        logger.setDepthLimit(depthLimit);
        String testCase = "TestMethod" + getUniqueId();
        List<String> nestedMethods = createTargetMethods(nestCount);

        logger.logBeginTestCase(testCase);
        callNested(nestedMethods);
        logger.logEndTestCase(testCase);

        String[] output = finish();
        assertIncreasingDepth(output);
        assertEquals(depthLimit, output.length);
    }

    @Test
    void noStackDepthLimit() {
        int nestCount = 10;
        String testCase = "TestMethod" + getUniqueId();
        List<String> nestedMethods = createTargetMethods(nestCount);

        logger.logBeginTestCase(testCase);
        callNested(nestedMethods);
        logger.logEndTestCase(testCase);

        String[] output = finish();
        assertIncreasingDepth(output);
        assertEquals(nestCount, output.length);
    }

    @Test
    void callConsequtive() {
        String testCase = "TestCase" + getUniqueId();
        List<String> methods = createTargetMethods(10);

        logger.logBeginTestCase(testCase);
        callConsequtive(methods);
        logger.logEndTestCase(testCase);
        String[] output = finish();

        assertEquals(10, output.length, "Did not log every method");
        assertEqualDepth(1, output);
    }

    @Test 
    void mapsNewThreadToCurrentTestCase() throws InterruptedException {
        String testCase = "TestCase" + getUniqueId();
        String method = "Method" + getUniqueId();
        logger.logBeginTestCase(testCase);
        Thread t = newThread(() -> {
            logger.logPushMethod(method);
            logger.logPopMethod(method);
        });
        t.start();
        t.join();
        logger.logEndTestCase(testCase);

        String output = finish()[0];
        assertFalse(output.isEmpty(), "Output was empty");
        assertFalse(output.isEmpty(), "Output was empty");
    }

    @Test
    void callDepthOnNewThread() throws InterruptedException {
        int nestCount = 10;
        String testCase = "TestMethod" + getUniqueId();
        List<String> sequentialMethods = createTargetMethods(nestCount);
        List<String> concurrentMethods = createTargetMethods(nestCount);

        logger.logBeginTestCase(testCase);
        pushMethods(sequentialMethods);
        Thread t = new Thread(() -> {
            callNested(concurrentMethods);
        });
        logger.logNewThread(t);
        t.start();
        t.join();
        popMethods(sequentialMethods);
        logger.logEndTestCase(testCase);

        String[] output = finish();
        assertEquals(2*nestCount, output.length);
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
            int expectedDepth = 1;
            for (var line : output) {
                assertFalse(line.isEmpty(), "Line was empty");
                MethodCall call = new MethodCall(line);
                assertEquals(expectedDepth, call.depth, "Call depth incorrect");
                expectedDepth++;
            }
        } catch (Throwable e) {
            System.out.println("Output:\n" + String.join("\n", output));
            throw e;
        }
    }

    List<String> createTargetMethods(int count) {
        List<String> nestedMethods = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            nestedMethods.add("Method" + getUniqueId());
        }
        return nestedMethods;
    }

    public void callNested(List<String> methods) {
        pushMethods(methods);
        popMethods(methods);
    }

    public void pushMethods(List<String> methods) {
        for (var method : methods) {
            logger.logPushMethod(method);
        }
    }

    public void popMethods(List<String> methods) {
        for (var method : methods) {
            logger.logPopMethod(method);
        }
    }

    public void callConsequtive(List<String> methods) {
        for (var method : methods) {
            logger.logPushMethod(method);
            logger.logPopMethod(method);
        }
    }

    String[] finish() {
        String output = out.toString();
        int size = logger.threads.size();
        assertTrue(logger.threads.isEmpty(), "Logger.threads not empty " + size);
        return output.split("\n");
    }

    Thread newThread(Runnable runnable) {
        Thread newThread = new Thread(runnable);
        logger.logNewThread(newThread);
        return newThread;
    }

    int getUniqueId() {
        return random.nextInt(Integer.MAX_VALUE);
    }

}
