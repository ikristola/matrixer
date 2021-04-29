package org.matrixer.agent;

import java.io.IOException;
import java.util.*;

/**
 * Logs method invocations from each test case.
 *
 * The logger is thread aware and maps each test case to a thread. When
 * that thread spawns child threads these will also be mapped to the
 * test case. When a method is called the logger detects from which
 * thread the call originated and thus in which test case the called
 * belonged to.
 *
 * When a test case ends the incocations that occured during that test
 * case are written on a separate line to the writer
 */
public class InvocationLogger {

    private static InvocationLogger instance;
    private static boolean debug;

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
        init(writer, false, false);
    }

    public static void init(SynchronizedWriter writer, boolean replace) {
        init(writer, replace, false);
    }

    /**
     * Initializes the logger.
     */
    public static void init(SynchronizedWriter writer, boolean replace, boolean debug) {
        if (instance == null || replace) {
            instance = new InvocationLogger(writer);
            InvocationLogger.debug = debug;
        }
    }

    static InvocationLogger getInstance() {
        if (instance == null) {
            throw new RuntimeException("InvocationLogger: not initialized!");
        }
        return instance;
    }

    public static void pushMethod(String name) {
        log("Invocation logger: Entering " + name);
        long thread = Thread.currentThread().getId();
        getInstance().logPushMethod(name, thread);
    }

    public void logPushMethod(String methodName) {
        logPushMethod(methodName, Thread.currentThread().getId());
    }

    public void logPushMethod(String methodName, long thread) {
        TestCase tc = threads.get(thread);
        if (tc != null) {
            tc.addCall(methodName);
        }
    }

    public static void popMethod(String methodName) {
        log("Invocation logger: Exiting " + methodName);
        long thread = Thread.currentThread().getId();
        getInstance().logPopMethod(methodName, thread);
    }

    public void logPopMethod(String methodName) {
        logPopMethod(methodName, Thread.currentThread().getId());
    }

    public void logPopMethod(String methodName, long thread) {
        TestCase tc = threads.get(thread);
        if (tc == null) {
            logError("Could not find test case " + methodName);
            return;
        }
        tc.popLastCall();
    }

    public static void beginTestCase(String name) {
        log("Invocation logger: Start test " + name);
        getInstance().logBeginTestCase(name);
    }

    public void logBeginTestCase(String name) {
        // Add test case
        TestCase tc = new TestCase(name);
        tests.put(tc.name, tc);

        // Map it to the current thread
        long thread = Thread.currentThread().getId();
        map(tc, thread);
    }

    private void map(TestCase tc, long thread) {
        threads.put(thread, tc);
        tc.threads.add(thread);
    }

    public static void endTestCase(String name) {
        log("Invocation logger: End test " + name);
        getInstance().logEndTestCase(name);
    }

    public static void endTestCase() {
        log("Invocation logger: End current test");
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
            logError("Could not find test case ");
            return;
        }
        removeTestCase(tc);
        tc.writeCalls(writer);
    }

    private void removeTestCase(TestCase tc) {
        tests.remove(tc.name, tc);
        unmapThreads(tc);
    }

    private void unmapThreads(TestCase tc) {
        System.out.println("Test case " + tc.name);
        for (long thread : tc.threads) {
            System.out.println("Removing thread " + thread);
            threads.remove(thread, tc);
        }
    }

    public static void newThread(Thread t) {
        // Use explicit instance here, since Threads need to be created even if
        // this class has not been initialized
        if (instance != null) {
            instance.logNewThread(t);
        }
    }

    public void logNewThread(Thread t) {
        long current = Thread.currentThread().getId();
        long child = t.getId();

        TestCase testCase = threads.get(current);
        if (testCase == null) {
            // No test case mapped to current thread.
            return;
        }
        // Add new thread to the test case
        testCase.threads.add(child);
        // Map the thread to the test case
        threads.put(child, testCase);
    }

    private static void logError(String msg) {
        if (debug) {
            System.err.println("ERROR: " + msg);
        }
    }

    private static void log(String msg) {
        if (debug) {
            System.out.println("INFO: " + msg);
        }
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
            log("TestCase: " + name);
            log("Logging call (d=" + currentDepth + "): " + methodName);
        }

        void popLastCall() {
            currentDepth--;
            if (currentDepth < 0) {
                throw new IllegalStateException("Current depth < 0");
            }
        }

        // Should prob be in a separate thread. MAKE SURE that
        // the test case has been unmapped from any threads first, otherwise
        void writeCalls(SynchronizedWriter writer) {
            log("TestCase: " + name + "\n\tWriting " + calls.size() + " calls");
            for (var call : calls) {
                String line = call.stackDepth + "|" + call.calledMethod + "<=" + name;
                log("Writing line:\n" + line);
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
