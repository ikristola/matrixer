package org.matrixer.agent;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import org.junit.jupiter.api.BeforeEach;

class InvocationLoggerTest {

    ByteArrayOutputStream out;
    InvocationLogger logger;

    @BeforeEach
    void setup() {
        out = new ByteArrayOutputStream();
        SynchronizedWriter w = new SynchronizedWriter(new OutputStreamWriter(out));
        logger = new InvocationLogger(w);
    }

    // @Test
    // void canLogTestCase() {
    //     String testCase = "org.matrixer.TestClass.testMethod()";
    //     logger.addTestCase(testCase);

    //     Collection<String> testCases = logger.getTestCases();
    //     assertTrue(testCases.contains(testCase));
    // }

}
