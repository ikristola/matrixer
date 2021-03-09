package org.matrixer.agent;

import static org.matrixer.agent.util.Assertions.assertFoundTestCase;

import java.nio.file.Path;

import org.matrixer.FileUtils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.matrixer.agent.util.CustomTestAgent;
import org.matrixer.agent.util.StreamHijacker;

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
        Path dummyOut = FileUtils.createTempDirectory(FileUtils.getSystemTempDir());
        Class<?> targetClass = MethodMapTransformerTestClass.class;
        String rootPkg = "org.matrixer";
        String testPkg = "org.matrixer";
        MethodMapTransformer transformer =
                new MethodMapTransformer(targetClass, dummyOut.toString(), rootPkg, testPkg);
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
