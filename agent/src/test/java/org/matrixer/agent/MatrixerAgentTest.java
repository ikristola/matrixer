package org.matrixer.agent;

import static org.junit.jupiter.api.Assertions.*;
import static org.matrixer.agent.util.Assertions.assertFoundTestCase;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.matrixer.agent.dynamictargets.AnotherTestClassDynamic;
import org.matrixer.agent.dynamictargets.TestClassDynamic;
import org.matrixer.agent.statictargets.AnotherTestClassStatic;
import org.matrixer.agent.statictargets.TestClassStatic;
import org.matrixer.core.FileUtils;

public class MatrixerAgentTest {

    ByteArrayOutputStream out;
    SynchronizedWriter writer;

    @BeforeEach
    void startLogger() {
        out = new ByteArrayOutputStream();
        writer = new SynchronizedWriter(new BufferedWriter(new OutputStreamWriter(out)));
        boolean replace = true;
        InvocationLogger.init(writer, "org.matrixer.agent", replace);
    }

    String getOutput() throws IOException {
        await();
        writer.lock();
        try {
            String output = out.toString();
            out.reset();
            return output;
        } finally {
            writer.unlock();
        }
    }

    static void await() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            System.out.println("Sleep failed");
            e.printStackTrace();
        }
        InvocationLogger.awaitFinished(10, TimeUnit.SECONDS);
    }

    /**
     * Tests of agent run statically
     */
    @Nested
    class StaticAgentTest {

        @Test
        public void instrumentDoesNotBreakMethod() {
            String argument = "this is an argument";
            assertEquals(argument, TestClassStatic.returnInput(argument));
        }

        @Test
        public void instrumentDoesNotBreakMultipleClasses() {
            String argument = "this is an argument";
            assertEquals(argument, TestClassStatic.returnInput(argument));
            assertTrue(AnotherTestClassStatic.trueReturner());
            assertEquals(1, AnotherTestClassStatic.oneReturner());
        }

        @Test
        public void instrumentedMethodPrintsCallerMethod() throws IOException {
            String caller = getClass().getName() + ":instrumentedMethodPrintsCallerMethod";
            String callee = TestClassStatic.class.getName() + ".returnInput";

            TestClassStatic.returnInput("Dummy string");
            String output = getOutput();

            assertFoundTestCase(output, caller, callee);
        }

        @Test
        public void canInstrumentMultipleClasses() throws IOException {
            String caller = getClass().getName() + ":canInstrumentMultipleClasses";

            String callee1 = TestClassStatic.class.getName() + ".returnInput";
            TestClassStatic.returnInput("Dummy string");

            String callee2 = AnotherTestClassStatic.class.getName() + ".trueReturner";
            AnotherTestClassStatic.trueReturner();

            String callee3 = AnotherTestClassStatic.class.getName() + ".oneReturner";
            AnotherTestClassStatic.oneReturner();

            String[] output = getOutput().split("\n");

            assertEquals(3, output.length, "Did not write 3 lines\n" + String.join("\n", output));
            assertFoundTestCase(output[0], caller, callee1);
            assertFoundTestCase(output[1], caller, callee2);
            assertFoundTestCase(output[2], caller, callee3);
        }
    }

    /**
     * Tests of agent run dynamically
     */
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DynamicAgentTest {

        @Test
        @Order(1)
        public void dynamicLoadOfMatrixerAgent() {
            String agentPath = "build/libs/agentJar.jar";
            Path outputFile = FileUtils.createTempDirectory();
            String targetPackage = "org.matrixer.agent.dynamictargets";
            String testPackage = "org.matrixer.agent";
            String args = String.format("%s:%s:%s", outputFile, targetPackage, testPackage);
            assertDoesNotThrow(() -> AgentLoader.loadAgent(agentPath, args));
        }

        @Test
        @Order(2)
        public void invocationLoggerIsInitialized() {
            assertNotNull(InvocationLogger.getInstance(), "InvocationLogger not initialized");
        }

        @Test
        public void instrumentDoesNotBreakMethod() {
            String argument = "this is an argument";
            assertEquals(argument, TestClassDynamic.returnInput(argument));
        }

        @Test
        public void instrumentDoesNotBreakMultipleClasses() {
            String argument = "this is an argument";
            assertEquals(argument, TestClassDynamic.returnInput(argument));
            assertTrue(AnotherTestClassDynamic.trueReturner());
            assertEquals(1, AnotherTestClassDynamic.oneReturner());
        }

        @Test
        public void instrumentedMethodPrintsCallerMethod() throws IOException {
            String caller = getClass().getName() + ":instrumentedMethodPrintsCallerMethod";
            String callee = TestClassDynamic.class.getName() + ".returnInput";

            TestClassDynamic.returnInput("Dummy string");
            String output = getOutput();

            assertFoundTestCase(output, caller, callee);
        }

        @Test
        public void canInstrumentMultipleClasses() throws IOException {
            String caller = getClass().getName() + ":canInstrumentMultipleClasses";

            String callee1 = TestClassStatic.class.getName() + ".returnInput";
            TestClassStatic.returnInput("Dummy string");

            String callee2 = AnotherTestClassStatic.class.getName() + ".trueReturner";
            AnotherTestClassStatic.trueReturner();

            String callee3 = AnotherTestClassStatic.class.getName() + ".oneReturner";
            AnotherTestClassStatic.oneReturner();

            String[] output = getOutput().split("\n");

            assertEquals(3, output.length, "Did not write 3 lines\n" + String.join("\n", output));
            assertFoundTestCase(output[0], caller, callee1);
            assertFoundTestCase(output[1], caller, callee2);
            assertFoundTestCase(output[2], caller, callee3);
        }
    }
}
