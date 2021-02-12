package org.matrixeragent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.matrixeragent.util.CustomTestAgent;
import org.matrixeragent.util.StreamHijacker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodMapTransformerTest {

    StreamHijacker streamHijacker = new StreamHijacker();
    private static CustomTestAgent customTestAgent;

    @BeforeAll
    static void setupClass() {
        try {
            customTestAgent = CustomTestAgent.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void transformedMethodPrintsCallerMethod() {
        Class<?> targetClass = MethodMapTransformerTestClass.class;
        MethodMapTransformer transformer = new MethodMapTransformer(targetClass.getName(), targetClass.getClassLoader());
        customTestAgent.transformClass(targetClass, transformer);

        String expected = "Looks like org.matrixeragent.MethodMapTransformerTest$MethodMapTransformerTestClass.trueReturner() " +
                "was called by test org.matrixeragent.MethodMapTransformerTest:transformedMethodPrintsCallerMethod";
        streamHijacker.outputCapture();
        assertTrue(MethodMapTransformerTestClass.trueReturner());
        assertEquals(expected, streamHijacker.getHijackedOutput());
        streamHijacker.stopOutputCapture();
    }

    private static class MethodMapTransformerTestClass {
        static boolean trueReturner() {
            return true;
        }
    }

}
