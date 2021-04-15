package org.matrixer.agent;

import static org.matrixer.agent.util.Assertions.assertFoundTestCase;

import java.io.*;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.matrixer.agent.util.CustomTestAgent;
import org.matrixer.agent.util.StreamHijacker;

public class MethodMapTransformerTest {

    StreamHijacker streamHijacker = new StreamHijacker();
    private static CustomTestAgent customTestAgent;
    static ByteArrayOutputStream out;
    static SynchronizedWriter writer;

    @BeforeAll
    static void loadAgent() {
        try {
            customTestAgent = CustomTestAgent.getInstance();
            out = new ByteArrayOutputStream();
            writer = new SynchronizedWriter(new BufferedWriter(new OutputStreamWriter(out)));
            boolean replace = true;
            InvocationLogger.init(writer, "org.matrixer", replace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void clearOutput() {
        writer.lock();
        try {
            out.reset();
        } finally {
            writer.unlock();
        }
    }

    static void await() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            System.out.println("Sleep failed");
            e.printStackTrace();
        }
        InvocationLogger.awaitFinished(1, TimeUnit.SECONDS);
    }

    String getOutput() throws IOException {
        await();
        writer.lock();
        try {
            String output = out.toString();
            return output;
        } finally {
            writer.unlock();
        }
    }

    @Test
    public void transformedMethodPrintsCallerMethod() throws IOException {
        Class<?> targetClass = MethodMapTransformerTestClass.class;
        String rootPkg = "org.matrixer";
        String testPkg = "org.matrixer";
        MethodMapTransformer transformer = new MethodMapTransformer(targetClass, rootPkg, testPkg);
        customTestAgent.transformClass(targetClass, transformer);
        String caller = getClass().getName() + ":transformedMethodPrintsCallerMethod";
        String callee = targetClass.getName() + ".trueReturner";

        MethodMapTransformerTestClass.trueReturner();
        String output = getOutput();

        assertFoundTestCase(output, caller, callee);
    }

    private static class MethodMapTransformerTestClass {
        static boolean trueReturner() {
            return true;
        }
    }
}
