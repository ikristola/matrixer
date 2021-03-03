package org.matrixer.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.matrixer.agent.util.CustomTestAgent;
import org.matrixer.agent.util.StreamHijacker;

public class VariableTransformerTest {

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
    public void canBeUsedToChangeClassOutput() {
        String insertString = "System.out.println(\"Im an injected string \");";
        Class<?> targetClass = VariableTransformerTestClass.class;
        VariableTransformer transformer = new VariableTransformer(
                targetClass.getName(), targetClass.getClassLoader(),
                insertString);
        customTestAgent.transformClass(targetClass, transformer);

        String expected = "Im an injected string";
        streamHijacker.outputCapture();
        VariableTransformerTestClass.trueReturner();
        assertEquals(expected, streamHijacker.getHijackedOutput());
        streamHijacker.stopOutputCapture();
    }

    @Test
    public void canBeUsedToChangeClassReturnVal() {
        String insertString = "return false;";
        Class<?> targetClass = VariableTransformerTestClass.class;
        VariableTransformer transformer = new VariableTransformer(
                targetClass.getName(), targetClass.getClassLoader(),
                insertString);
        customTestAgent.transformClass(targetClass, transformer);

        assertFalse(VariableTransformerTestClass.trueReturner());
    }

    private static class VariableTransformerTestClass {
        static boolean trueReturner() {
            return true;
        }
    }

}
