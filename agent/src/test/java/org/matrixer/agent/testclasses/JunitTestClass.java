package org.matrixer.agent.testclasses;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class JunitTestClass {

    @Test
    @Disabled
    public void testMethod1() {
        assertTrue(1 != 2);
    }

}
