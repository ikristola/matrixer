package org.matrixer.agent;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.matrixer.agent.internal.*;
import org.matrixer.core.runtime.*;

/**
 *
 */
public class StackRecorderImpl implements StackRecorder {

    // Maps each thread to a stack
    final Map<Long, ThreadStack> threads = new ConcurrentHashMap<>();

    private int depthLimit = Integer.MAX_VALUE;

    // The writer to used to write the calls
    private final SynchronizedWriter writer;

    private final boolean debug;
    private final Logger logger;


    public StackRecorderImpl(SynchronizedWriter writer, boolean debug, Logger logger) {
        this.writer = writer;
        this.debug = debug;
        this.logger = logger;
    }

    public StackRecorderImpl(SynchronizedWriter writer, AgentOptions options, Logger logger) {
        this(writer, options.getDebug(), logger);
        setDepthLimit(options.getDepthLimit());
    }

    @Override
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

    @Override
    public void newThread(long parent, Thread t) {
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

    @Override
    public void beginTestCase(String name, long thread) {
        log("::Starting test case:: " + name + " in thread " + thread);

        TestCase tc = new TestCase(name);
        ThreadStack parentStack = new ThreadStack(thread, tc);
        threads.put(thread, parentStack);
    }


    public void endTestCase(long thread) {
        ThreadStack stack = threads.get(thread);
        TestCase tc = stack.mappedTestCase();
        log("::End current test:: " + tc.name()  + " on thread " + thread);
        endTestCase(tc);
    }

    public void endTestCase(String name) {
        long thread = Thread.currentThread().getId();
        endTestCase(name, thread);
    }

    @Override
    public void endTestCase(String name, long thread) {
        log("::Ending test case:: " + name + " on thread " + thread);
        ThreadStack stack = threads.get(thread);
        TestCase tc = stack.mappedTestCase();
        if (debug && !name.equals(tc.name())) {
            throw new IllegalStateException("Found wrong test case");
        }
        endTestCase(tc);
    }

    public void endTestCase(TestCase tc) {
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

    // For testing
    void pushMethod(String methodName) {
        pushMethod(methodName, Thread.currentThread().getId());
    }

    @Override
    public void pushMethod(String methodName, long thread) {
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

    void logPopMethod(String methodName) {
        popMethod(methodName, Thread.currentThread().getId());
    }

    @Override
    public void popMethod(String methodName, long thread) {
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

    @Override
    public int activeThreadCount() {
        return threads.size();
    }

    private void logError(String msg) {
        if (debug) {
            logger.logError("InvocationLogger: " + msg);
        }
    }

    private void log(String msg) {
        if (debug) {
            logger.log("InvocationLogger " + msg);
        }
    }
}
