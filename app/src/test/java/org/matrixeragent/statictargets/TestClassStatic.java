package org.matrixeragent.statictargets;

/**
 * Used by the test suite as target for instrumentation by the agent
 */
public class TestClassStatic {

    public static String returnInput(String argument) {
        return argument;
    }
}
