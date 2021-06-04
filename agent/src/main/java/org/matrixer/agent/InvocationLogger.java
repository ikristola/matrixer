package org.matrixer.agent;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.matrixer.agent.internal.*;
import org.matrixer.core.runtime.*;

/**
 * Uses a stack recorder to log calls to target methods
 */
public class InvocationLogger {

    private static StackRecorder recorder;
    private static Logger logger;

    /**
     * Initializes the logger.
     */
    public static void init(StackRecorder recorder, Logger logger) {
        InvocationLogger.recorder = recorder;
        InvocationLogger.logger = logger;
    }

    private static StackRecorder getRecorder() {
        if (recorder == null) {
            throw new RuntimeException("InvocationLogger: not initialized!");
        }
        return recorder;
    }

    public static void pushMethod(String name) {
        try {
            long thread = Thread.currentThread().getId();
            getRecorder().pushMethod(name, thread);
        } catch (Throwable e) {
            logger.logException(e);
        }
    }

    public static void popMethod(String methodName) {
        try {
            long thread = Thread.currentThread().getId();
            getRecorder().popMethod(methodName, thread);
        } catch (Throwable e) {
            logger.logException(e);
        }
    }

    public static void beginTestCase(String name) {
        try {
            long thread = Thread.currentThread().getId();
            getRecorder().beginTestCase(name, thread);
        } catch (Throwable e) {
            logger.logException(e);
        }
    }

    public static void endTestCase(String name) {
        try {
            long thread = Thread.currentThread().getId();
            getRecorder().endTestCase(name, thread);
        } catch (Throwable e) {
            logger.logException(e);
        }
    }

    public static void newThread(Thread t) {
        // Use explicit instance here, since Threads need to be created even if
        // this class has not been initialized we should not throw.
        if (recorder == null) {
            return;
        }
        try {
            long current = Thread.currentThread().getId();
            recorder.newThread(current, t);
        } catch (Throwable e) {
            logger.logException(e);
        }
    }

}
