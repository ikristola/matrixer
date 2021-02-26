package org.matrixeragent;

import static org.junit.jupiter.api.Assertions.*;
import static org.matrixeragent.util.Assertions.assertFoundTestCase;

import java.nio.file.Path;

import org.matrixer.FileUtils;

import org.junit.jupiter.api.*;
import org.matrixeragent.dynamictargets.AnotherTestClassDynamic;
import org.matrixeragent.dynamictargets.TestClassDynamic;
import org.matrixeragent.statictargets.AnotherTestClassStatic;
import org.matrixeragent.statictargets.TestClassStatic;
import org.matrixeragent.util.StreamHijacker;

public class MatrixerAgentTest {

    // For capturing output
    StreamHijacker streamHijacker = new StreamHijacker();

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
        public void instrumentedMethodPrintsCallerMethod() {
            String caller = getClass().getName() + ":instrumentedMethodPrintsCallerMethod";
            String callee = TestClassStatic.class.getName() + ".returnInput";

            String output = streamHijacker.getOutput(() -> {
                TestClassStatic.returnInput("Dummy string");
            });

            assertFoundTestCase(output, caller, callee);
        }

        @Test
        public void canInstrumentMultipleClasses() {
            String caller = getClass().getName() + ":canInstrumentMultipleClasses";

            String callee1 = TestClassStatic.class.getName() + ".returnInput";
            String output1 = streamHijacker.getOutput(() -> {
                TestClassStatic.returnInput("Dummy string");
            });
            assertFoundTestCase(output1, caller, callee1);

            String callee2 = AnotherTestClassStatic.class.getName() + ".trueReturner";
            String output2 = streamHijacker.getOutput(() -> {
                AnotherTestClassStatic.trueReturner();
            });
            assertFoundTestCase(output2, caller, callee2);


            String callee3 = AnotherTestClassStatic.class.getName();
            String output3 = streamHijacker.getOutput(() -> {
                AnotherTestClassStatic.oneReturner();
            });
            assertFoundTestCase(output3, caller, callee3);
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
            Path outputFile = FileUtils.createTempDirectory(FileUtils.getSystemTempDir());
            assertDoesNotThrow(() -> AgentLoader.loadAgent(
                    "build/libs/agentJar.jar",
                    outputFile.toString() + ":org.matrixeragent.dynamictargets"));
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
        public void instrumentedMethodPrintsCallerMethod() {
            String caller = getClass().getName() + ":instrumentedMethodPrintsCallerMethod";
            String callee = TestClassDynamic.class.getName() + ".returnInput";

            String output = streamHijacker.getOutput(() -> {
                TestClassDynamic.returnInput("Dummy string");
            });

            assertFoundTestCase(output, caller, callee);
        }

        @Test
        public void canInstrumentMultipleClasses() {
            String caller = getClass().getName() + ":canInstrumentMultipleClasses";

            String callee1 = TestClassStatic.class.getName() + ".returnInput";
            String output1 = streamHijacker.getOutput(() -> {
                TestClassStatic.returnInput("Dummy string");
            });
            assertFoundTestCase(output1, caller, callee1);

            String callee2 = AnotherTestClassStatic.class.getName() + ".trueReturner";
            String output2 = streamHijacker.getOutput(() -> {
                AnotherTestClassStatic.trueReturner();
            });
            assertFoundTestCase(output2, caller, callee2);

            String callee3 = AnotherTestClassStatic.class.getName();
            String output3 = streamHijacker.getOutput(() -> {
                AnotherTestClassStatic.oneReturner();
            });
            assertFoundTestCase(output3, caller, callee3);
        }
    }
}
