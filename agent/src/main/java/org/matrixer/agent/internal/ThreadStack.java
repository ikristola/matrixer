/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
