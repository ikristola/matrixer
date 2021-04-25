package org.matrixer.agent;

import java.io.IOException;
import java.util.*;

/**
 * Logs method invocations from the target methods. Each instrumented method
 * calls the report method when it is invoked. Each invocation report will be handled
 * concurrently so as not to interfere with the normal flow of the SUT.
 *
 * This class has to be initialized by calling the static init method. For testing
 * purposes the awaitTermination method can be used for synchronization. In this case the
 * invocation logger has to be initialized again, since the thread pool is shutdown.
 */
public class InvocationLogger {

    private static InvocationLogger instance;

    // Maps each thread to a test case
    Map<Long, TestCase> threads = new HashMap<>();

    // Maps each test case to its name
    Map<String, TestCase> tests = new HashMap<>();

    // The writer to used to write the calls
    SynchronizedWriter writer;

    InvocationLogger(SynchronizedWriter writer) {
        this.writer = writer;
    }

    public static void init(SynchronizedWriter writer) {
        init(writer, false);
    }

    /**
     * Initializes the logger.
     */
    public static void init(SynchronizedWriter writer, boolean replace) {
        if (instance == null || replace) {
            instance = new InvocationLogger(writer);
        }
    }

    static InvocationLogger getInstance() {
        if (instance == null) {
            throw new RuntimeException("InvocationLogger: not initialized!");
        }
        return instance;
    }

    public static void pushMethod(String name) {
        System.out.println("Invocation logger: Entering " + name);
        getInstance().logPushMethod(name);
    }

    public void logPushMethod(String name) {
        long thread = Thread.currentThread().getId();
        TestCase tc = threads.get(thread);
        if (tc != null){
            tc.addCall(name);
        }
    }

    public static void popMethod(String name) {
        System.out.println("Invocation logger: Exiting " + name);
        getInstance().logPopMethod(name);
    }

    public void logPopMethod(String name) {
        TestCase tc = tests.get(name);
        if (tc != null) {
            tc.popLastCall();
        }
    }

    public static void beginTestCase(String name) {
        System.out.println("Invocation logger: Start test " + name);
        getInstance().logBeginTestCase(name);
    }

    public void logBeginTestCase(String name) {
        // Add test case
        TestCase tc = new TestCase(name);
        tests.put(tc.name, tc);

        // Map it to the current thread
        long thread = Thread.currentThread().getId();
        threads.put(thread, tc);
    }

    public static void endTestCase(String name) {
        System.out.println("Invocation logger: End test " + name);
        getInstance().logEndTestCase(name);
    }

    public static void endTestCase() {
        System.out.println("Invocation logger: End current test");
        getInstance().logEndTestCase();
    }

    public void logEndTestCase() {
        long thread = Thread.currentThread().getId();
        TestCase tc = threads.get(thread);
        logEndTestCase(tc);
    }

    public void logEndTestCase(String name) {
        TestCase tc = tests.get(name);
        logEndTestCase(tc);
    }

    public void logEndTestCase(TestCase tc) {
        if (tc == null) {
            return;
        }
        // Remove test case
        tests.remove(tc.name, tc);
        // Unmap threads
        for (long thread : tc.threads) {
            threads.remove(thread, tc);
        }

        tc.writeCalls(writer);
    }

    public static void newThread(Thread t) {
        long parent = Thread.currentThread().getId();
        long current = t.getId();
        System.out.println("InvocationLogger: Parent thread " + parent + " started thread " + current);

        // Use explicit instance here, since Threads need to be created even if not
        // initialized this class.
        if (instance != null) {
            instance.logNewThread(t);
        }
    }

    public void logNewThread(Thread t) {
        long parent = Thread.currentThread().getId();
        long current = t.getId();

        TestCase testCase = threads.get(parent);
        if (testCase == null) {
            // No test case mapped to current thread.
            return;
        }
        // Add new thread to the test case
        testCase.threads.add(current);
        // Map the thread to the test case
        threads.put(current, testCase);
    }

    private void logError(String msg) {
        System.err.println(msg);
    }

    class TestCase {
        int currentDepth = 0;
        final List<Call> calls = new ArrayList<>();
        final List<Long> threads = new ArrayList<>();
        final String name;

        TestCase(String testName) {
            this.name = testName;
        }

        void addCall(String methodName) {
            currentDepth++;
            calls.add(new Call(methodName, currentDepth));
            System.err.println("TestCase: " + name);
            System.err.println("Logging call (d=" + currentDepth + "): " + methodName);
        }

        void popLastCall() {
            currentDepth--;
        }

        // Should prob be in a separate thread. MAKE SURE that
        // the test case has been unmapped from any threads first, otherwise
        void writeCalls(SynchronizedWriter writer) {
            System.err.println("TestCase: " + name);
            System.err.println("Writing " + calls.size() + " calls");
            for (var call: calls) {
                String line = call.stackDepth + "|" + call.calledMethod + "<=" + name;
            System.err.println("Writing line:\n" + line);
                writeLine(writer, line);
            }
        }

        void writeLine(SynchronizedWriter writer, String line) {
            try {
                writer.writeLine(line);
            } catch (IOException e) {
                logError("Could not write method call:\n" + line);
            }
        }
    }

    class Call {
        final String calledMethod;
        final int stackDepth;

        Call(String calledMethod, int stackDepth) {
            this.calledMethod = calledMethod;
            this.stackDepth = stackDepth;
        }
    }
}
