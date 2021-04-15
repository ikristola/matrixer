package org.matrixer.agent;

/**
 * Represents an invocation report from the target method.
 */
public class InvocationReport {
    public final String invokedMethod;
    public final StackTraceElement[] trace;

    public InvocationReport(String invokedMethod, StackTraceElement[] trace) {
        this.invokedMethod = invokedMethod;
        this.trace = trace;
    }
}
