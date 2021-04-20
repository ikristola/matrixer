package org.matrixer.agent;

import java.util.concurrent.*;

/**
 * Logs method invocations from the target methods. Each instrumented method
 * calls the report method when it is invoked. Each invocation report will be handled
 * concurrently so as not to interfere with the normal flow of the SUT.
 *
 * This class has to be initialized by calling the static init method. For testing
 * purposes the awaitTermination method can be used for synchronization. In this case the
 * invocation logger has to be initialized again, since the thread pool is shutdown.
 */
public class InvocationLogger {

    private static InvocationLogger instance;
    private static int POOL_SIZE = 2;

    SynchronizedWriter writer;
    private ExecutorService pool;

    InvocationLogger(SynchronizedWriter writer, String testPkg) {
        this.pool = Executors.newFixedThreadPool(POOL_SIZE);
        this.writer = writer;
    }

    static InvocationLogger getInstance() {
        if (instance == null) {
            throw new RuntimeException("InvocationLogger: not initialized!");
        }
        return instance;
    }

    public static void init(SynchronizedWriter writer, String testPkg) {
        init(writer, testPkg, false);
    }

    /**
     * Initializes the logger.
     */
    public static void init(SynchronizedWriter writer, String testPkg, boolean replace) {
        if (instance == null || replace) {
            instance = new InvocationLogger(writer, testPkg);
        }
    }

    /**
     * Called by each invoked target method. The reports will be handled concurrently.
     */
    public static void report(String methodName, StackTraceElement[] trace) {
        getInstance().reportInvocation(methodName, trace);
    }

    public void reportInvocation(String methodName, StackTraceElement[] trace) {
        InvocationReport report = new InvocationReport(methodName, trace);
        pool.submit(new ReportHandler(report, writer));
    }

    /**
     * Blocks and waits the specified duration for the invocations to be
     * processed before forcefully shutting down if the duration has
     * expired. Note. This depletes the thread pool of workers so the
     * logger has to be initialized again by calling init.
     */
    static void awaitFinished(long duration, TimeUnit unit) {
        getInstance().await(duration, unit);
    }

    /**
     * Blocks and waits the specified duration for the invocations to be
     * processed before forcefully shutting down if the duration has
     * expired. Note. This depletes the thread pool of workers so the
     * logger has to be initialized again by calling init.
     */
    public void await(long duration, TimeUnit unit) {
        try {
            pool.shutdown(); // Disable new tasks from being submitted
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(duration, unit)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(duration, unit))
                    logError("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static void newThread(Thread t) {
        long parent = Thread.currentThread().getId();
        long current = t.getId();
        System.out.println("InvocationLogger: Parent thread " + parent + " started thread " + current);
    }

    public static void newThread(String s) {
        System.out.println("Invocation logger: Received " + s);
    }

    private void logError(String msg) {
        System.err.println(msg);
    }
}
