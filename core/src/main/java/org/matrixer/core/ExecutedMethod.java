package org.matrixer.core;

import java.util.Collection;
import java.util.HashMap;

import org.matrixer.core.util.Range;

/**
 * Stores data about calls to an executed target method
 */
public class ExecutedMethod implements Comparable<ExecutedMethod> {

    private String name;

    HashMap<String, Call> calls = new HashMap<>();
    Range depthRange;

    /**
     * Creates a new ExecutedMethod
     *
     * @param name
     *            The name of the method
     */
    public ExecutedMethod(String name) {
        this.name = name;
    }

    /**
     * Adds a new call to this method
     *
     * @param name
     *            the name of the caller
     * @param depth
     *            the call stack depth of the call
     */
    public void addCaller(String name, int depth) {
        addDepthOfCall(depth);
        Call call = calls.get(name);
        if (call == null) {
            call = new Call(name, depth);
            calls.put(name, call);
        } else {
            call.addCall(depth);
        }
    }

    private void addDepthOfCall(int depth) {
        if (depthRange == null) {
            depthRange = new Range(depth, depth);
        } else {
            depthRange.extendToInclude(depth);
        }
    }

    /**
     * Tests if this method was called by a method with the provided name
     * during execution
     *
     * @param callerName
     *            the name of the calling method
     * @returns true of this method was called by the method of the privided
     *          name, false otherwise
     */
    public boolean wasCalledBy(String callerName) {
        return calls.containsKey(callerName);
    }

    /**
     * Returns a range of call stack depths for the calls from another
     * method to this method during execution
     *
     * @param callerName
     *            the name of the calling method
     *
     * @returns a Range of the depths of calls to this method from the
     *          calling method. If no calls occured from the other method an
     *          empty range is returned.
     */
    public Range depthOfCall(String callerName) {
        if (!calls.containsKey(callerName)) {
            return Range.empty();
        }
        Call call = calls.get(callerName);
        return call.depth();
    }

    /**
     * Returns a range of call stack depths for every call to this method.
     * If no calls have been made to this method an empty range is returned.
     *
     * @returns the range of call stack depths for every call to this
     *          method.
     */
    public Range depthOfCalls() {
        if (depthRange == null) {
            return Range.empty();
        }
        return depthRange;
    }

    /**
     * @returns the name of this method
     */
    public String name() {
        return name;
    }

    /**
     * Returns a collection of every call to this method
     */
    public Collection<Call> callers() {
        return calls.values();
    }

    @Override
    public int compareTo(ExecutedMethod other) {
        return name.compareTo(other.name);
    }

    /**
     * Stores the name and range of the calls from a single caller to this
     * method.
     */
    public class Call {
        String callerName;
        Range depthRange;

        /**
         * Creates a new Call
         *
         * @param name
         *            the name of the caller
         * @param depth
         *            the depth of the first call
         */
        public Call(String name, int depth) {
            callerName = name;
            depthRange = new Range(depth, depth);
        }

        /**
         * Adds a new call from the same caller
         *
         * @param depth
         *            the call stack depth of the new call
         */
        private void addCall(int depth) {
            depthRange.extendToInclude(depth);
        }

        /**
         * @returns the name of the caller
         */
        String caller() {
            return callerName;
        }

        /**
         * @returns the range of call stack depths made from this caller.
         */
        Range depth() {
            return depthRange;
        }
    }
}
