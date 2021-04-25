package org.matrixer.agent.instrumentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.matrixer.agent.util.Assertions.assertFoundTestCase;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.matrixer.agent.InvocationLogger;
import org.matrixer.agent.SynchronizedWriter;
import org.matrixer.agent.testclasses.Wrapped;
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
            out = new ByteArrayOutputStream();
            writer = new SynchronizedWriter(new BufferedWriter(new OutputStreamWriter(out)));
            boolean replace = true;
            InvocationLogger.init(writer, replace);

            customTestAgent = CustomTestAgent.getInstance();
            customTestAgent.transformClass(MethodMapTransformerTest.class, new TestCaseTransformer(MethodMapTransformerTest.class));
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
        ClassFileTransformer transformer = new CallLoggingTransformer(targetClass);
        customTestAgent.transformClass(targetClass, transformer);
        String caller = getClass().getName() + ".transformedMethodPrintsCallerMethod";
        String callee = targetClass.getName() + ".trueReturner";

        MethodMapTransformerTestClass.trueReturner();
        InvocationLogger.endTestCase(caller);
        String output = getOutput();

        assertFoundTestCase(output, caller, callee);
    }

    @Test
    void testWrapMethodBody() throws IOException, InterruptedException {
        try {
            ClassFileTransformer tf = new CallLoggingTransformer(Wrapped.class);
            customTestAgent.transformClass(Wrapped.class, tf);
            assertToWrapWorks();
            assertWrappedWorks();
        } catch (ClassFormatError e) {
            System.out.println("Format error!");
        }
    }

    private static void assertToWrapWorks() throws InterruptedException {
        System.out.println("Creating class wrapped method towrap");
        Wrapped w = new Wrapped();
        Thread t = new Thread( () -> assertEquals(1, w.towrap(1)));
        t.start();
        t.join();

        assertThrows(RuntimeException.class, () -> w.towrap(2));
        assertThrows(IllegalArgumentException.class, () -> w.towrap(3));
        assertThrows(RuntimeException.class, () -> w.towrap(4));
        assertEquals(100, w.towrap(5));
        System.out.println("Done");
    }

    private static void assertWrappedWorks() throws InterruptedException {
        System.out.println("Creating class wrapped method wrapped");
        Wrapped w = new Wrapped();

        System.out.println("\nwrapped - return in new thread:");
        Thread t = new Thread( () -> assertEquals(1, w.wrapped(1)));
        t.start();
        t.join();

        System.out.println("\nwrapped - RuntimeException:");
        assertThrows(RuntimeException.class, () -> w.wrapped(2));

        System.out.println("\nwrapped - IllegalArgumentException:");
        assertThrows(IllegalArgumentException.class, () -> w.wrapped(3));

        System.out.println("\nwrapped - RuntimeException:");
        assertThrows(RuntimeException.class, () -> w.wrapped(4));

        System.out.println("\nwrapped - Return:");
        assertEquals(100, w.wrapped(5));

        System.out.println("Done");
    }

    private static class MethodMapTransformerTestClass {
        static boolean trueReturner() {
            return true;
        }
    }
}
