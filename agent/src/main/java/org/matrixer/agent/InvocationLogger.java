package org.matrixer.agent;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.matrixer.agent.internal.*;
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
    public static void init(SynchronizedWriter writer, int depthLimit, boolean debug,
            Logger logger) {
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
        try {
            long thread = Thread.currentThread().getId();
            getInstance().logPushMethod(name, thread);
        } catch (Throwable e) {
            getInstance().logger.logException(e);
        }
    }

    // For testing
    void logPushMethod(String methodName) {
        logPushMethod(methodName, Thread.currentThread().getId());
    }

    public void logPushMethod(String methodName, long thread) {
        log("::Entering method:: " + methodName + " on thread " + thread);
        ThreadStack stack = threads.get(thread);
        if (stack == null) {
            // throw new IllegalStateException("Could not find test case " +
            // methodName);
            logError("PushMethod: Test case not found for thread " + thread);
            return;
        }
        TestCase tc = stack.mappedTestCase();
        int currentDepth = stack.push();
        if (currentDepth <= depthLimit) {
            tc.addCall(methodName, currentDepth);
            log("TestCase " + tc.name() + " Logging call (d=" + currentDepth + "): " + methodName + " on thread " + thread);
        }
    }

    public static void popMethod(String methodName) {
        try {
            long thread = Thread.currentThread().getId();
            getInstance().logPopMethod(methodName, thread);
        } catch (Throwable e) {
            getInstance().logger.logException(e);
        }
    }

    // For testing
    void logPopMethod(String methodName) {
        logPopMethod(methodName, Thread.currentThread().getId());
    }

    public void logPopMethod(String methodName, long thread) {
        log("::Exiting method:: " + methodName + " on thread " + thread);
        ThreadStack stack = threads.get(thread);
        if (stack == null) {
            // throw new IllegalStateException("Could not find test case " +
            // methodName);
            logError("PushMethod: Test case not found for thread" + thread);
            return;
        }
        stack.pop();
    }

    public static void beginTestCase(String name) {
        try {
            long thread = Thread.currentThread().getId();
            getInstance().logBeginTestCase(name, thread);
        } catch (Throwable e) {
            getInstance().logger.logException(e);
        }
    }

    // For testing
    void logBeginTestCase(String name) {
        logBeginTestCase(name, Thread.currentThread().getId());
    }

    public void logBeginTestCase(String name, long thread) {
        log("::Starting test case:: " + name + " in thread " + thread);

        TestCase tc = new TestCase(name);
        ThreadStack parentStack = new ThreadStack(thread, tc);
        threads.put(thread, parentStack);
    }

    public static void endTestCase(String name) {
        try {
            long thread = Thread.currentThread().getId();
            getInstance().logEndTestCase(name, thread);
        } catch (Throwable e) {
            getInstance().logger.logException(e);
        }
    }

    public static void endTestCase() {
        try {
            long thread = Thread.currentThread().getId();
            getInstance().logEndTestCase(thread);
        } catch (Throwable e) {
            getInstance().logger.logException(e);
        }
    }

    public void logEndTestCase(long thread) {
        ThreadStack stack = threads.get(thread);
        TestCase tc = stack.mappedTestCase();
        log("::End current test:: " + tc.name()  + " on thread " + thread);
        logEndTestCase(tc);
    }

    public void logEndTestCase(String name, long thread) {
        log("::Ending test case:: " + name + " on thread " + thread);
        ThreadStack stack = threads.get(thread);
        TestCase tc = stack.mappedTestCase();
        if (debug && !name.equals(tc.name())) {
            throw new IllegalStateException("Found wrong test case");
        }
        logEndTestCase(tc);
    }

    public void logEndTestCase(TestCase tc) {
        if (tc == null) {
            throw new IllegalStateException("endTestCase: Could not find test case ");
        }
        removeTestCase(tc);
        writeCalls(tc);
    }

    private void writeCalls(TestCase tc) {
        try {
            Collection<Call> calls = tc.calls();
            int size = 0;
            for (var call : calls) {
                String line =
                        new MethodCall(call.stackDepth, call.calledMethod, tc.name()).asLine();
                log("Writing line:\n\t" + line);
                writer.writeLine(line);
                size++;
            }
            log("TestCase: " + tc.name() + "\n\tWrote " + size + " calls\n");
        } catch (IOException e) {
            logger.logException(e);
        }
    }


    private void removeTestCase(TestCase tc) {
        unmapThreads(tc);
    }

    private void unmapThreads(TestCase tc) {
        log("Test case " + tc.name());
        for (var threadStack : tc.threads()) {
            log("Removing thread " + threadStack.id());
            threads.remove(threadStack.id(), threadStack);
        }
    }

    public static void newThread(Thread t) {
        // Use explicit instance here, since Threads need to be created even if
        // this class has not been initialized we should not throw.
        if (instance == null) {
            return;
        }
        try {
            long current = Thread.currentThread().getId();
            instance.logNewThread(current, t);
        } catch (Throwable e) {
            instance.logger.logException(e);
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
}
