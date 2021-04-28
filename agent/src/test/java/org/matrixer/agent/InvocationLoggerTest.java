package org.matrixer.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvocationLoggerTest {

    ByteArrayOutputStream out;
    InvocationLogger logger;
    Random random = new Random();

    @BeforeEach
    void setup() {
        out = new ByteArrayOutputStream();
        SynchronizedWriter w = new SynchronizedWriter(new OutputStreamWriter(out));
        logger = new InvocationLogger(w);
    }

    @Test
    void logsTestCase() {
        String testCase = "TestMethod" + getUniqueId();
        String method = "Method" + getUniqueId();

        logger.logBeginTestCase(testCase);
        logger.logPushMethod(method);
        logger.logPopMethod(method);
        logger.logEndTestCase(testCase);

        String line = out.toString();
        assertTrue(line.contains(testCase));
        assertTrue(line.contains(method));
    }

    Thread newThread(Runnable runnable) {
       Thread newThread = new Thread(runnable);
       logger.logNewThread(newThread);
       return newThread;
    }

    int getUniqueId() {
        return random.nextInt();
    }

}
