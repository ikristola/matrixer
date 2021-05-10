package org.matrixer.agent.internal;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TestCase {
    final String name;

    final Collection<Call> calls = new ConcurrentLinkedQueue<>();
    final Collection<ThreadStack> threads = new ConcurrentLinkedQueue<>();

    public TestCase(String testName) {
        this.name = testName;
    }

    public String name() {
        return this.name;
    }

    public void addCall(String methodName, int depth) {
        calls.add(new Call(methodName, depth));
    }

    public void mapThread(ThreadStack thread) {
        threads.add(thread);
    }

    public Collection<ThreadStack> threads() {
        return Collections.unmodifiableCollection(threads);
    }

    public Collection<Call> calls() {
        return Collections.unmodifiableCollection(calls);
    }
}

