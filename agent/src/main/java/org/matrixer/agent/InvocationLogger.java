package org.matrixer.agent;

import java.io.IOException;
import java.util.*;

import org.matrixer.core.runtime.MethodCall;
import org.matrixer.core.runtime.AgentOptions;

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

    /**
     * Initializes the logger.
     */
    public static void init(SynchronizedWriter writer, AgentOptions options) {
        instance = new InvocationLogger(writer, options);
        instance.setDepthLimit(options.getDepthLimit());
    }

    /**
     * Initializes the logger.
     */
    public static void init(SynchronizedWriter writer, int depthLimit, boolean debug) {
        instance = new InvocationLogger(writer, debug);
        instance.setDepthLimit(depthLimit);
    }

    static InvocationLogger getInstance() {
        if (instance == null) {
            throw new RuntimeException("InvocationLogger: not initialized!");
        }
        return instance;
    }

    // Maps each thread to a test case
    Map<Long, TestCase> threads = new HashMap<>();

    // Maps each test case to its name
    Map<String, TestCase> tests = new HashMap<>();

    private int depthLimit = Integer.MAX_VALUE;

    // The writer to used to write the calls
    SynchronizedWriter writer;

    private boolean debug;


    InvocationLogger(SynchronizedWriter writer, boolean debug) {
        this.writer = writer;
        this.debug = debug;
    }

    InvocationLogger(SynchronizedWriter writer, AgentOptions options) {
        this.writer = writer;
        debug = options.getDebug();
        setDepthLimit(options.getDepthLimit());
    }

    public void setDepthLimit(int depthLimit) {
        if (depthLimit == 0) {
            depthLimit = Integer.MAX_VALUE;
        }
        this.depthLimit = depthLimit;
    }

    public int getDepthLimit() {
        return depthLimit;
    }

    public static void pushMethod(String name) {
        long thread = Thread.currentThread().getId();
        getInstance().logPushMethod(name, thread);
    }

    public void logPushMethod(String methodName) {
        logPushMethod(methodName, Thread.currentThread().getId());
    }

    public void logPushMethod(String methodName, long thread) {
        log("Invocation logger: Entering " + methodName);
        TestCase tc = threads.get(thread);
        if (tc != null) {
            tc.addCall(methodName);
        }
    }

    public static void popMethod(String methodName) {
        long thread = Thread.currentThread().getId();
        getInstance().logPopMethod(methodName, thread);
    }

    public void logPopMethod(String methodName) {
        logPopMethod(methodName, Thread.currentThread().getId());
    }

    public void logPopMethod(String methodName, long thread) {
        log("Invocation logger: Exiting " + methodName);
        TestCase tc = threads.get(thread);
        if (tc == null) {
            logError("Could not find test case " + methodName);
            return;
        }
        tc.popLastCall();
    }

    public static void beginTestCase(String name) {
        getInstance().logBeginTestCase(name);
    }

    public void logBeginTestCase(String name) {
        log("Invocation logger: Start test " + name);
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
        getInstance().logEndTestCase(name);
    }

    public static void endTestCase() {
        getInstance().logEndTestCase();
    }

    public void logEndTestCase() {
        log("Invocation logger: End current test");
        long thread = Thread.currentThread().getId();
        TestCase tc = threads.get(thread);
        logEndTestCase(tc);
    }

    public void logEndTestCase(String name) {
        log("Invocation logger: End test " + name);
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
        log("Test case " + tc.name);
        for (long thread : tc.threads) {
            log("Removing thread " + thread);
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

    private void logError(String msg) {
        if (debug) {
            System.err.println("ERROR: " + msg);
        }
    }

    private void log(String msg) {
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
            if (depthLimit == 0 || currentDepth <= depthLimit) {
                calls.add(new Call(methodName, currentDepth));
            }
            log("TestCase " + name + " Logging call (d=" + currentDepth + "): " + methodName);
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
                String line = new MethodCall(call.stackDepth, call.calledMethod, this.name).asLine();
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
