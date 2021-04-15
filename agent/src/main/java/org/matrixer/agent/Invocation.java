package org.matrixer.agent;

import static org.matrixer.agent.MatrixerAgentUtils.isTestClass;

/**
 * Represents the invocation by a test case of a target method.
 */
class Invocation {
    public final String invokedMethod;
    public final String caller;
    public final int depth;

    private Invocation(String invocedMethod, String caller, int depth) {
        this.invokedMethod = invocedMethod;
        this.caller = caller;
        this.depth = depth;
    }

    /**
     * Analyzes the report to find the test case and returns the invocation
     *
     * @throws RuntimeException if no test case were found
     */
    static Invocation fromReport(InvocationReport report) {
        // First element in the trace is Thread.getStackTrace
        // the second element is the invoked method.
        StackTraceElement[] trace = report.trace;
        for (int i = trace.length - 1; i > 1; i--) {
            StackTraceElement elem = trace[i];
            Class<?> cls = getElementClass(elem);
            if (cls != null && isTestClass(cls)) {
                String caller = qualifiedMethodName(elem);
                int depth = i - 1;
                return new Invocation(report.invokedMethod, caller, depth);
            }
        }
        throw new RuntimeException("Did not find test case for " + report.invokedMethod);
    }

    public String asLine() {
        return depth + "|" + invokedMethod + "<=" + caller;
    }

    private static Class<?> getElementClass(StackTraceElement elem) {
        String className = elem.getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static String qualifiedMethodName(StackTraceElement elem) {
        return elem.getClassName() + ":" + elem.getMethodName();
    }
}
