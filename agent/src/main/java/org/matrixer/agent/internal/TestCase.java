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

