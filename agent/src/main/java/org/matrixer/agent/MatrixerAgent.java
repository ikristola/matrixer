package org.matrixer.agent;

import java.io.*;
import java.lang.instrument.*;
import java.nio.file.*;
import java.util.function.Consumer;

import org.matrixer.agent.instrumentation.CallLoggingTransformer;
import org.matrixer.agent.instrumentation.ThreadClassTransformer;
import org.matrixer.core.runtime.AgentOptions;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * MatrixerAgent is a agent that transforms classes in target package.
 *
 * The agent instruments class methods in the target package to print
 * out the qualified name of itself and the calling test case to file.
 *
 * The agent arguments are specified as
 *
 * outputDirectory:targetPackage:testPackage
 *
 * outputDirectory - the directory where the agent should place its
 * logfile and the results from the instrumented methods.
 *
 * targetPackage - the root package that will be tested, sub-packages
 * will be instrumented as well.
 *
 * testPackage - the package of the tests class, this will be used to
 * identify test methods
 */
public class MatrixerAgent {

    final static private boolean useLog = true;
    private static MatrixerAgent agent;

    /**
     * The stream that will be used for logging
     */
    private PrintStream log;

    /**
     * The instrumenation handle
     */
    final private Instrumentation inst;

    final AgentOptions options;

    private MatrixerAgent(String agentArgs, Instrumentation inst, String type) throws IOException,
            ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        this.inst = inst;
        options = new AgentOptions(agentArgs);
        setupLog();
        log("started " + type + ":\n\tArgs: " + agentArgs);
        log(
                String.format("OutputPath: %s\ntarget: %s\ntest: %s",
                        options.getDestFilename(), options.getTargetPackage(),
                        options.getTestPackage()));
    }

    private void setupLog() throws IOException {
        try {
            Path destFile = Path.of(options.getDestFilename());
            var out = Files.newOutputStream(
                    destFile.resolveSibling(("matrixer-agent-log.txt")), CREATE, APPEND);
            log = new PrintStream(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run when the agent is started statically
     *
     * @param agentArgs
     *            Agent arguments
     * @param inst
     *            Instrumentation instance
     * @throws IOException
     * @throws InterruptedException
     * @throws UnmodifiableClassException
     * @throws ClassNotFoundException
     */
    public static void premain(String agentArgs, Instrumentation inst) throws IOException,
            ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        agent = new MatrixerAgent(agentArgs, inst, "statically");
        agent.startup();
    }

    /**
     * Run when the agent is started dynamically
     *
     * @param agentArgs
     *            Agent arguments
     * @param inst
     *            Instrumentation instance
     * @throws IOException
     * @throws InterruptedException
     * @throws UnmodifiableClassException
     * @throws ClassNotFoundException
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws IOException,
            ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        agent = new MatrixerAgent(agentArgs, inst, "dynamically");
        agent.startup();
    }

    private void startup() {
        try {
            Path destFile = Path.of(options.getDestFilename());
            SynchronizedWriter w = new SynchronizedWriter(
                    Files.newBufferedWriter(destFile, CREATE, APPEND));
            InvocationLogger.init(w, options);
            inst.addTransformer(new CallLoggingTransformer(options));
            transformThreadClass(InvocationLogger::newThread);
        } catch (Throwable e) {
            log("Error: " + e.getMessage());
            e.printStackTrace(log);
        }
    }

    /**
     * The call logging transformer needs to know when new threads are
     * created and the parent thread.
     */
    void transformThreadClass(Consumer<Thread> callback) throws ClassNotFoundException, UnmodifiableClassException {
        ClassFileTransformer cf = new ThreadClassTransformer(callback);
        inst.addTransformer(cf, true);
        inst.retransformClasses(Thread.class);
        inst.removeTransformer(cf);
    }

    public static MatrixerAgent getAgent() {
        if (agent == null) {
            throw new RuntimeException("Agent not initialized!");
        }
        return agent;
    }

    private void log(String msg) {
        if (useLog) {
            log.println("[Agent] " + msg);
        }
    }
}
