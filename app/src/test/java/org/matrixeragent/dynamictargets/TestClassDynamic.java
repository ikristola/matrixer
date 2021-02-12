package org.matrixeragent.dynamictargets;

/**
 * Used by the test suite as target for instrumentation by the agent
 */
public class TestClassDynamic {

    public static String returnInput(String argument) {
        return argument;
    }
}
