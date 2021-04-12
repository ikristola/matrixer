package org.matrixer.core;

/**
 * Stores information about a method call
 */
public class MethodCall {
    /**
     * The call stack depth of the call
     */
    public final int depth;

    /**
     * The name of the called method
     */
    public final String methodName;

    /**
     * The name of the caller
     */
    public final String callerName;

    /**
     * Creates a new MethodCall
     *
     * @param depth
     *            the depth of the call
     * @param methodName
     *            the name of the called method
     * @param callerName
     *            the name of the caller
     */
    public MethodCall(int depth, String methodName, String callerName) {
        this.depth = depth;
        this.methodName = methodName;
        this.callerName = callerName;
    }
}
