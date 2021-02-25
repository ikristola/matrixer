package org.matrixeragent;

import static org.matrixeragent.util.Assertions.assertFoundTestCase;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.matrixeragent.util.CustomTestAgent;
import org.matrixeragent.util.StreamHijacker;

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
        MethodMapTransformer transformer =
                new MethodMapTransformer(targetClass.getName(), targetClass.getClassLoader());
        customTestAgent.transformClass(targetClass, transformer);
        String caller = getClass().getName() + ":transformedMethodPrintsCallerMethod";
        String callee = targetClass.getName() + ".trueReturner";

        String output = streamHijacker.getOutput(() -> {
            MethodMapTransformerTestClass.trueReturner();
        });

        assertFoundTestCase(output, caller, callee);
    }

    private static class MethodMapTransformerTestClass {
        static boolean trueReturner() {
            return true;
        }
    }
}
