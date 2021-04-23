package org.matrixer.agent;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvocationLoggerTest {

    ByteArrayOutputStream out;
    InvocationLogger logger;

    @BeforeEach
    void setup() {
        out = new ByteArrayOutputStream();
        SynchronizedWriter w = new SynchronizedWriter(new OutputStreamWriter(out));
        logger = new InvocationLogger(w, "org.matrixer");
    }

    // @Test
    // void canLogTestCase() {
    //     String testCase = "org.matrixer.TestClass.testMethod()";
    //     logger.addTestCase(testCase);

    //     Collection<String> testCases = logger.getTestCases();
    //     assertTrue(testCases.contains(testCase));
    // }

}
