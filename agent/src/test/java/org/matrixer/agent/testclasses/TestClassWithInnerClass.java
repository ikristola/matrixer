package org.matrixer.agent.testclasses;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestClassWithInnerClass {

    @Test
    @Disabled
    public void testMethod1() {
        assertTrue(1 != 2);
    }

    public static class Inner {
        public void doSomething() {

        }
    }

}
