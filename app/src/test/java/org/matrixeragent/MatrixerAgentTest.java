package org.matrixeragent;

import com.sun.tools.attach.AgentLoadException;
import org.junit.jupiter.api.*;
import org.matrixeragent.testclass.AnotherTestClass;
import org.matrixeragent.testclass.TestClass;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class MatrixerAgentTest {

    // For capturing output
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    /**
     *  Tests of agent run statically
     */
    @Nested
    class StaticAgentTest {

        @Test
        public void instrumentDoesNotBreakMethod() {
            String argument = "this is an argument";
            TestClass testClass = new TestClass();
            assertEquals(argument, testClass.returnInput(argument));
        }

        @Test
        public void instrumentDoesNotBreakMultipleClasses() {
            TestClass testClass = new TestClass();
            String argument = "this is an argument";
            assertEquals(argument, testClass.returnInput(argument));

            AnotherTestClass anotherTestClass = new AnotherTestClass();
            assertTrue(anotherTestClass.trueReturner());
            assertEquals(1, anotherTestClass.oneReturner());
        }

        @Test
        public void instrumentedMethodPrintsCallerMethod() {
            System.setOut(new PrintStream(outputStreamCaptor));

            TestClass testClass = new TestClass();
            String expected = "Looks like org.matrixeragent.testclass.TestClass.returnInput(java.lang.String) was" +
                    " called by test org.matrixeragent.MatrixerAgentTest$StaticAgentTest:instrumentedMethodPrintsCallerMethod";
            testClass.returnInput("Dummy string");
            assertEquals(expected, outputStreamCaptor.toString().trim());
            outputStreamCaptor.reset();

            System.setOut(standardOut);
        }

        @Test
        public void canInstrumentMultipleClasses() {
            System.setOut(new PrintStream(outputStreamCaptor));

            TestClass testClass = new TestClass();
            String expected = "Looks like org.matrixeragent.testclass.TestClass.returnInput(java.lang.String) was" +
                    " called by test org.matrixeragent.MatrixerAgentTest$StaticAgentTest:canInstrumentMultipleClasses";
            testClass.returnInput("Dummy string");
            assertEquals(expected, outputStreamCaptor.toString().trim());
            outputStreamCaptor.reset();

            AnotherTestClass anotherTestClass = new AnotherTestClass();
            String anotherExpected = "Looks like org.matrixeragent.testclass.AnotherTestClass.trueReturner() was" +
                    " called by test org.matrixeragent.MatrixerAgentTest$StaticAgentTest:canInstrumentMultipleClasses";
            anotherTestClass.trueReturner();
            assertEquals(anotherExpected, outputStreamCaptor.toString().trim());
            outputStreamCaptor.reset();

            String anotherExpected1 = "Looks like org.matrixeragent.testclass.AnotherTestClass.oneReturner() was" +
                    " called by test org.matrixeragent.MatrixerAgentTest$StaticAgentTest:canInstrumentMultipleClasses";
            anotherTestClass.oneReturner();
            assertEquals(anotherExpected1, outputStreamCaptor.toString().trim());
            outputStreamCaptor.reset();

            System.setOut(standardOut);
        }

    }

    /**
     *  Tests of agent run dynamically
     */
    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DynamicAgentTest {

        @Test
        @Order(1)   // This test must be run before the agent is loaded
        public void expectException_GetInstrumentationIfAgentNotLoaded() {
            String expected = "The Agent is not loaded or this method is not called via the system class loader";
            Throwable throwable = assertThrows(IllegalStateException.class, MatrixerAgent::getInstrumentation);
            assertEquals(expected, throwable.getMessage());
        }

        @Test
        public void dynamicLoadOfAgent() {
            assertDoesNotThrow(() -> AgentLoader.loadAgent("build/libs/agentJar.jar"));
        }

        @Test
        public void expectException_DynamicLoadAgentWithBadPath() {
            Throwable throwable = assertThrows(AgentLoadException.class,
                    () -> AgentLoader.loadAgent("bad/path/agent.jar"));
            assertEquals("Agent JAR not found or no Agent-Class attribute", throwable.getMessage());
        }

        @Test
        public void getAgentInstrumentation() {
            try {
                AgentLoader.loadAgent("build/libs/agentJar.jar");
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertDoesNotThrow(MatrixerAgent::getInstrumentation);
        }
    }

}
