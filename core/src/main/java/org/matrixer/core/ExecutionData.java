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
package org.matrixer.core;

import java.util.*;

import org.matrixer.core.runtime.MethodCall;

/**
 * Stores coverage information collected by executing the test suite
 * with the matrixer agent
 */
public class ExecutionData {

    private HashMap<String, ExecutedMethod> targetMethods = new HashMap<>();
    private Set<String> testCases = new HashSet<>();
    private Collection<Integer> depths = new ArrayList<>();

    /**
     * Adds a new method call
     *
     * If the caller and called method has already been added, the depth of
     * the new call will be registered.
     *
     * @param call
     *            The call to add
     */
    public void addCall(MethodCall call) {
        testCases.add(call.callerName);
        if (targetMethods.containsKey(call.methodName)) {
            updateMethod(call.methodName, call.callerName, call.depth);
        } else {
            addNewMethod(call.methodName, call.callerName, call.depth);
        }
        depths.add(call.depth);
    }

    private void addNewMethod(String name, String caller, int depth) {
        ExecutedMethod method = new ExecutedMethod(name);
        method.addCaller(caller, depth);
        targetMethods.put(method.name(), method);
    }

    private void updateMethod(String name, String caller, int depth) {
        ExecutedMethod method = targetMethods.get(name);
        method.addCaller(caller, depth);
    }

    /**
     * Returns an executed target method
     *
     * @param methodName
     *            the name of the target method
     *
     * @returns The executed target method
     */
    public ExecutedMethod getTargetMethod(String methodName) {
        return targetMethods.get(methodName);
    }

    /**
     * Returns all target methods that where executed
     *
     * @returns every target method that where executed
     */
    public Collection<ExecutedMethod> getAllTargetMethods() {
        return targetMethods.values();
    }

    /**
     * Returns all test cases that where executed
     *
     * @returns every test case that where executed
     */
    public Collection<String> getAllTestCases() {
        return testCases;
    }

    public Collection<Integer> getCallStackDepths() {
        return depths;
    }

}
