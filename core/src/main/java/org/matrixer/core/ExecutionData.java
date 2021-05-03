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

}
