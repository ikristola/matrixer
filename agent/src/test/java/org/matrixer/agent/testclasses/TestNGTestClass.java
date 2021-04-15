package org.matrixer.agent.testclasses;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.testng.annotations.*;

public class TestNGTestClass {

    @Test(enabled = false)
    public void testMethod1() {
        assertTrue(1 != 2);
    }

}
