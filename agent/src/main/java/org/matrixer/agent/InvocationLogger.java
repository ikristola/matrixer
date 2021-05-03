package org.matrixer.agent;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.matrixer.core.runtime.*;

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
    public static void init(SynchronizedWriter writer, AgentOptions options, Logger logger) {
        instance = new InvocationLogger(writer, options, logger);
    }

    /**
     * Initializes the logger.
     */
    public static void init(SynchronizedWriter writer, int depthLimit, boolean debug, Logger logger) {
        instance = new InvocationLogger(writer, debug, logger);
        instance.setDepthLimit(depthLimit);
    }

    static InvocationLogger getInstance() {
        if (instance == null) {
            throw new RuntimeException("InvocationLogger: not initialized!");
        }
        return instance;
    }

    // Maps each thread to a stack
    final Map<Long, ThreadStack> threads = new ConcurrentHashMap<>();

    private int depthLimit = Integer.MAX_VALUE;

    // The writer to used to write the calls
    private final SynchronizedWriter writer;

    private final boolean debug;
    private Logger logger;


    InvocationLogger(SynchronizedWriter writer, boolean debug, Logger logger) {
        this.writer = writer;
        this.debug = debug;
        this.logger = logger;
    }

    InvocationLogger(SynchronizedWriter writer, AgentOptions options, Logger logger) {
        this(writer, options.getDebug(), logger);
        setDepthLimit(options.getDepthLimit());
    }

    public void setDepthLimit(int depthLimit) {
        if (depthLimit == 0) {
            depthLimit = Integer.MAX_VALUE;
        }
        log("New depth limit: " + depthLimit);
        this.depthLimit = depthLimit;
    }

    public int getDepthLimit() {
        return depthLimit;
    }

    public static void pushMethod(String name) {
        long thread = Thread.currentThread().getId();
        getInstance().logPushMethod(name, thread);
    }

    // For testing
    void logPushMethod(String methodName) {
        logPushMethod(methodName, Thread.currentThread().getId());
    }

    public void logPushMethod(String methodName, long thread) {
        log("::Entering method:: " + methodName);
        ThreadStack stack = threads.get(thread);
        if (stack == null) {
            // throw new IllegalStateException("Could not find test case " + methodName);
            logError("PushMethod: Test case not found");
            return;
        }
        TestCase tc = stack.mappedTestCase();
        int currentDepth = stack.push();
        if (currentDepth <= depthLimit) {
            tc.addCall(methodName, currentDepth);
        }
    }

    public static void popMethod(String methodName) {
        long thread = Thread.currentThread().getId();
        getInstance().logPopMethod(methodName, thread);
    }

    // For testing
    void logPopMethod(String methodName) {
        logPopMethod(methodName, Thread.currentThread().getId());
    }

    public void logPopMethod(String methodName, long thread) {
        log("::Exiting method:: " + methodName);
        ThreadStack stack = threads.get(thread);
        if (stack == null) {
            // throw new IllegalStateException("Could not find test case " + methodName);
            logError("PushMethod: Test case not found");
            return;
        }
        stack.pop();
    }

    public static void beginTestCase(String name) {
        long thread = Thread.currentThread().getId();
        getInstance().logBeginTestCase(name, thread);
    }

    // For testing
    void logBeginTestCase(String name) {
        logBeginTestCase(name, Thread.currentThread().getId());
    }

    public void logBeginTestCase(String name, long thread) {
        // Add test case
        log("::Starting test case:: " + name + " in thread " + thread);

        ThreadStack parentStack = new ThreadStack(thread);
        TestCase tc = new TestCase(name);
        map(tc, parentStack);

        threads.put(thread, parentStack);
    }

    private void map(TestCase tc, ThreadStack stack) {
        stack.mapTestCase(tc);
        tc.threads.add(stack);
    }

    public static void endTestCase(String name) {
        getInstance().logEndTestCase(name);
    }

    public static void endTestCase() {
        long thread = Thread.currentThread().getId();
        getInstance().logEndTestCase(thread);
    }

    public void logEndTestCase(long thread) {
        ThreadStack stack = threads.get(thread);
        TestCase tc = stack.mappedTestCase();
        log("::End current test:: " + tc.name);
        logEndTestCase(tc);
    }

    public void logEndTestCase(String name) {
        log("::Ending test case:: " + name);
        ThreadStack stack = threads.get(Thread.currentThread().getId());
        TestCase tc = stack.mappedTestCase();
        if (debug && !name.equals(tc.name)) {
            throw new IllegalStateException("Found wrong test case");
        }
        logEndTestCase(tc);
    }

    public void logEndTestCase(TestCase tc) {
        if (tc == null) {
            throw new IllegalStateException("endTestCase: Could not find test case ");
        }
        removeTestCase(tc);
        tc.writeCalls(writer);
    }

    private void removeTestCase(TestCase tc) {
        unmapThreads(tc);
    }

    private void unmapThreads(TestCase tc) {
        log("Test case " + tc.name);
        for (var threadStack : tc.threads) {
            log("Removing thread " + threadStack.id());
            threads.remove(threadStack.id(), threadStack);
        }
    }

    public static void newThread(Thread t) {
        // Use explicit instance here, since Threads need to be created even if
        // this class has not been initialized
        if (instance != null) {
            long current = Thread.currentThread().getId();
            instance.logNewThread(current, t);
        }
    }

    // For testing
    void logNewThread(Thread t) {
        logNewThread(Thread.currentThread().getId(), t);
    }

    public void logNewThread(long parent, Thread t) {
        long childId = t.getId();
        log("New thread " + childId + " started by " + parent);

        ThreadStack parentStack = threads.get(parent);
        if (parentStack == null) {
            log("No parent thread found for thread " + childId);
            // New parent thread
            // Test case threads will be added by beginTestCase()
        } else {
            log("Found parent thread " + parentStack.id());
            // The childstack inherits the stackdepth of its parent
            ThreadStack childStack = new ThreadStack(childId, parentStack);
            TestCase tc = parentStack.mappedTestCase();
            map(tc, childStack);
            threads.put(childId, childStack);
        }
    }

    private void logError(String msg) {
        if (debug && logger != null) {
            logger.logError("InvocationLogger: " + msg);
        }
    }

    private void log(String msg) {
        if (debug && logger != null) {
            logger.log("InvocationLogger " + msg);
        }
    }

    class TestCase {
        final List<Call> calls = new ArrayList<>();
        final List<ThreadStack> threads = new ArrayList<>();
        final String name;

        TestCase(String testName) {
            this.name = testName;
        }

        void addCall(String methodName, int depth) {
            synchronized (this) {
                calls.add(new Call(methodName, depth));
            }
            log("TestCase " + name + " Logging call (d=" + depth + "): " + methodName);
        }

        // Should prob be in a separate thread. MAKE SURE that
        // the test case has been unmapped from any threads first, otherwise
        void writeCalls(SynchronizedWriter writer) {
            log("TestCase: " + name + "\n\tWriting " + calls.size() + " calls");
            synchronized (this) {
                for (var call : calls) {
                    String line =
                            new MethodCall(call.stackDepth, call.calledMethod, this.name).asLine();
                    log("Writing line:\n" + line);
                    writeLine(writer, line);
                }
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

    /**
     * Keeps track of the stack depth for a thread
     */
    class ThreadStack {
        private final long threadId;

        // Current height of the stack
        // The first thread started by a test case begins with depth 0
        // Each child thread spawned by a parent thread mapped to a test case
        // will inherit the depth from its parent.
        AtomicInteger depth;

        // The test case that the thread is running in
        TestCase test;

        ThreadStack(long threadId) {
            this.threadId = threadId;
            this.depth = new AtomicInteger(0);
        }

        ThreadStack(long threadId, ThreadStack parent) {
            this.threadId = threadId;
            this.depth = new AtomicInteger(parent.depth());
            this.test = parent.test;
        }

        TestCase mappedTestCase() {
            return test;
        }

        void mapTestCase(TestCase tc) {
            test = tc;
        }

        int push() {
            return depth.incrementAndGet();
        }

        int pop() {
            return depth.decrementAndGet();
        }

        int depth() {
            return depth.get();
        }

        long id() {
            return threadId;
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
