package org.matrixer.agent.internal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Keeps track of the stack depth for a thread
 */
public class ThreadStack {
    private final long threadId;

    // Current height of the stack
    // The first thread started by a test case begins with depth 0
    // Each child thread spawned by a parent thread mapped to a test case
    // will inherit the depth from its parent.
    private final AtomicInteger depth;

    // The test case that the thread is running in
    private final TestCase test;

    public ThreadStack(long threadId, TestCase tc) {
        this.threadId = threadId;
        this.depth = new AtomicInteger(0);
        this.test = tc;
        tc.mapThread(this);
    }

    public ThreadStack(long threadId, ThreadStack parent) {
        this.threadId = threadId;
        this.depth = new AtomicInteger(parent.depth());
        this.test = parent.test;
        parent.test.mapThread(this);
    }

    public TestCase mappedTestCase() {
        return test;
    }

    public int push() {
        return depth.incrementAndGet();
    }

    public int pop() {
        return depth.decrementAndGet();
    }

    public int depth() {
        return depth.get();
    }

    public long id() {
        return threadId;
    }
}
