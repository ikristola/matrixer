package org.matrixer.agent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.matrixer.agent.MatrixerAgentUtils.isTestClass;

import org.junit.jupiter.api.Test;
import org.matrixer.agent.testclasses.*;

class MatrixerAgentUtilsTest {

    @Test
    void junitClassIsTestClass() {
        Class<?> cls = JunitTestClass.class;
        assertTrue(isTestClass(cls));
    }

    @Test
    void testNGClassIsTestClass() {
        Class<?> cls = TestNGTestClass.class;
        assertTrue(isTestClass(cls));
    }

    @Test
    void innerClassOfTestClassIsTestClass() {
        Class<?> cls = TestClassWithInnerClass.Inner.class;
        assertTrue(isTestClass(cls));
    }

}
