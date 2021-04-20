package org.matrixer.agent;

import static org.matrixer.agent.MatrixerAgentUtils.getClassesInPackage;
import static org.matrixer.agent.MatrixerAgentUtils.isTestClass;

import java.io.*;
import java.lang.instrument.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javassist.*;

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

    /**
     * The stream that will be used for logging
     */
    private PrintStream log;

    /**
     * The instrumenation handle
     */
    final private Instrumentation inst;

    /**
     * The path in which to store results
     */
    final private Path outputFile;

    /**
     * The package under test Will be used to match classes to instrument
     */
    final private String targetPackage;

    /**
     * The package that contains the tests Will be used to determine test
     * cases
     */
    final private String testerPackage;

    private MatrixerAgent(String agentArgs, Instrumentation inst, String type) throws IOException, ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        this.inst = inst;
        String[] args = agentArgs.split(":");
        if (args.length < 3) {
            throw new IllegalArgumentException("[Agent] Not enough arguments!");
        }
        outputFile = Path.of(args[0], "matrixer-results.txt");
        targetPackage = args[1];
        testerPackage = args[2];
        setupLog();
        log("started " + type + ":\n\tArgs: " + agentArgs);
        log(
                String.format("OutputPath: %s\ntarget: %s\ntest: %s",
                        outputFile, targetPackage, testerPackage));
        transformThreadClass();
        var t = new Thread(() -> System.out.println("Running thread: " + Thread.currentThread().getName()));
        t.start();
        t.join();
        SynchronizedWriter w = new SynchronizedWriter(Files.newBufferedWriter(outputFile));
        InvocationLogger.init(w, testerPackage);
        addShutdownHook();
    }

    private void setupLog() throws IOException {
        try {
            var out = Files.newOutputStream(outputFile.resolveSibling(("matrixer-agent-log.txt")));
            log = new PrintStream(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void addShutdownHook() {
        Runtime rt = Runtime.getRuntime();
        rt.addShutdownHook(new Thread(() -> {
            log("Stopping invocationlogger...");
            InvocationLogger.awaitFinished(10, TimeUnit.MINUTES);
            log("Done");
        }));
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
    public static void premain(String agentArgs, Instrumentation inst) throws IOException, ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        var agent = new MatrixerAgent(agentArgs, inst, "statically");
        agent.run();
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
    public static void agentmain(String agentArgs, Instrumentation inst) throws IOException, ClassNotFoundException, UnmodifiableClassException, InterruptedException {
        var agent = new MatrixerAgent(agentArgs, inst, "dynamically");
        agent.run();
    }

    private void run() {
        log("Starting class instrumentations");
        List<Class<?>> classes = getClassesInPackage(targetPackage);
        log("# classes in package: " + classes.size());

        for (Class<?> cls : classes) {
            if (isTestClass(cls)) {
                log("Skipping test class: \n\t" + cls.getName());
                continue;
            }
            transform(cls, inst);
        }
        log("Class instrumenations done");
    }

    /**
     * Retransforms the class using a new transformer.
     *
     * @param targetCls
     *            the class to transform
     * @param inst
     *            the instrumentation instance to use for registering the
     *            transformer
     * @param outputDir
     *            the directory where the results from the transformer
     *            should be stored
     */
    private void transform(Class<?> targetCls, Instrumentation inst) {
        try {
            var transformer = new MethodMapTransformer(targetCls, targetPackage, testerPackage);
            inst.addTransformer(transformer, true);
            inst.retransformClasses(targetCls);
        } catch (Exception e) {
            throw new RuntimeException("Transform failed for: [" + targetCls.getName() + "]", e);
        }
    }

    void transformThreadClass() throws ClassNotFoundException, UnmodifiableClassException {
        ClassFileTransformer cf = new ThreadClassTransformer();
        inst.addTransformer(cf, true);
        inst.retransformClasses(Thread.class);
        inst.removeTransformer(cf);
    }


    private void log(String msg) {
        if (useLog) {
            log.println("[Agent] " + msg);
        }
    }
}
