package org.matrixeragent;

import org.junit.jupiter.api.*;
import org.matrixeragent.dynamictargets.AnotherTestClassDynamic;
import org.matrixeragent.dynamictargets.TestClassDynamic;
import org.matrixeragent.statictargets.AnotherTestClassStatic;
import org.matrixeragent.statictargets.TestClassStatic;
import org.matrixeragent.util.StreamHijacker;

import static org.junit.jupiter.api.Assertions.*;

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
            streamHijacker.outputCapture();
            String expected =
                    "Looks like org.matrixeragent.statictargets.TestClassStatic.returnInput" +
                            "(java.lang.String) was called by test org.matrixeragent." +
                            "MatrixerAgentTest$StaticAgentTest:" +
                            "instrumentedMethodPrintsCallerMethod";
            TestClassStatic.returnInput("Dummy string");
            assertEquals(expected, streamHijacker.getHijackedOutput());
            streamHijacker.stopOutputCapture();
        }

        @Test
        public void canInstrumentMultipleClasses() {
            streamHijacker.outputCapture();

            String expected =
                    "Looks like org.matrixeragent.statictargets.TestClassStatic.returnInput" +
                            "(java.lang.String) was called by test org.matrixeragent." +
                            "MatrixerAgentTest$StaticAgentTest:canInstrumentMultipleClasses";
            TestClassStatic.returnInput("Dummy string");
            assertEquals(expected, streamHijacker.getHijackedOutput());
            streamHijacker.reset();

            String anotherExpected =
                    "Looks like org.matrixeragent.statictargets.AnotherTestClassStatic." +
                            "trueReturner() was called by test org.matrixeragent." +
                            "MatrixerAgentTest$StaticAgentTest:canInstrumentMultipleClasses";
            assertTrue(AnotherTestClassStatic.trueReturner());
            assertEquals(anotherExpected, streamHijacker.getHijackedOutput());
            streamHijacker.reset();

            String anotherExpected1 =
                    "Looks like org.matrixeragent.statictargets.AnotherTestClassStatic" +
                            ".oneReturner() was called by test org.matrixeragent." +
                            "MatrixerAgentTest$StaticAgentTest:canInstrumentMultipleClasses";
            assertEquals(1, AnotherTestClassStatic.oneReturner());
            assertEquals(anotherExpected1, streamHijacker.getHijackedOutput());

            streamHijacker.stopOutputCapture();
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
            assertDoesNotThrow(() -> AgentLoader.loadAgent(
                    "build/libs/agentJar.jar",
                    "arg1:org.matrixeragent.dynamictargets"));
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
            streamHijacker.outputCapture();
            String expected =
                    "Looks like org.matrixeragent.dynamictargets.TestClassDynamic.returnInput" +
                            "(java.lang.String) was called by test org.matrixeragent.Matrixer" +
                            "AgentTest$DynamicAgentTest:instrumentedMethodPrintsCallerMethod";
            TestClassDynamic.returnInput("Dummy string");
            assertEquals(expected, streamHijacker.getHijackedOutput());
            streamHijacker.stopOutputCapture();
        }

        @Test
        public void canInstrumentMultipleClasses() {
            streamHijacker.outputCapture();
            String expected =
                    "Looks like org.matrixeragent.dynamictargets.TestClassDynamic.returnInput" +
                            "(java.lang.String) was called by test org.matrixeragent.Matrixer" +
                            "AgentTest$DynamicAgentTest:canInstrumentMultipleClasses";
            TestClassDynamic.returnInput("Dummy string");
            assertEquals(expected, streamHijacker.getHijackedOutput());
            streamHijacker.reset();

            String anotherExpected =
                    "Looks like org.matrixeragent.dynamictargets.AnotherTestClassDynamic." +
                            "trueReturner() was called by test org.matrixeragent.Matrixer" +
                            "AgentTest$DynamicAgentTest:canInstrumentMultipleClasses";
            assertTrue(AnotherTestClassDynamic.trueReturner());
            assertEquals(anotherExpected, streamHijacker.getHijackedOutput());
            streamHijacker.reset();

            String anotherExpected1 =
                    "Looks like org.matrixeragent.dynamictargets.AnotherTestClassDynamic." +
                            "oneReturner() was called by test org.matrixeragent." +
                            "MatrixerAgentTest$DynamicAgentTest:canInstrumentMultipleClasses";
            assertEquals(1, AnotherTestClassDynamic.oneReturner());
            assertEquals(anotherExpected1, streamHijacker.getHijackedOutput());

            streamHijacker.stopOutputCapture();
        }
    }
}
