package org.matrixer.agent;

import static org.matrixer.agent.MatrixerAgentUtils.getClassesInPackage;
import static org.matrixer.agent.MatrixerAgentUtils.isTestClass;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

    private MatrixerAgent(String agentArgs, Instrumentation inst, String type) throws IOException {
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
        SynchronizedWriter w = new SynchronizedWriter(Files.newBufferedWriter(outputFile));
        InvocationLogger.init(w, testerPackage);
        addShutdownHook();
    }

    void addShutdownHook() {
        Runtime rt = Runtime.getRuntime();
        rt.addShutdownHook(new Thread(() -> {
            System.out.print("Stopping invocationlogger...");
            InvocationLogger.awaitFinished(10, TimeUnit.MINUTES);
            System.out.println("Done");
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
     */
    public static void premain(String agentArgs, Instrumentation inst) throws IOException {
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
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws IOException {
        var agent = new MatrixerAgent(agentArgs, inst, "dynamically");
        agent.run();
    }

    private void setupLog() throws IOException {
        try {
            var out = Files.newOutputStream(outputFile.resolveSibling(("matrixer-agent-log.txt")));
            log = new PrintStream(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        log("Starting agent transformation");
        List<Class<?>> classes = getClassesInPackage(targetPackage);
        log("# classes in package: " + classes.size());

        for (Class<?> cls : classes) {
            log("class found: " + cls);
            if (isTestClass(cls)) {
                log("This is a test class. Skipping transform");
                continue;
            }
            log("This is not a test class. Transforming it..");
            transformClass(cls.getName(), inst);
        }

    }

    private void transformClass(String className, Instrumentation inst) {
        var targetCls = findClassInCurrentLoader(className);
        if (targetCls.isEmpty()) {
            targetCls = findClassInInstrumentation(className, inst);
        }
        if (targetCls.isEmpty()) {
            throw new RuntimeException("Could not find class [" + className + "]");
        }
        transform(targetCls.get(), inst);
    }

    Optional<Class<?>> findClassInInstrumentation(String className, Instrumentation inst) {
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                System.out.println("Found class " + clazz.getName());
                return Optional.of(clazz);
            }
        }
        return Optional.empty();
    }

    Optional<Class<?>> findClassInCurrentLoader(String className) {
        try {
            var targetCls = Class.forName(className);
            return Optional.of(targetCls);
        } catch (Exception e) {
            log("Class not found with Class.forName");
            return Optional.empty();
        }
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

    private void log(String msg) {
        if (useLog) {
            log.println("[Agent] " + msg);
        }
    }
}
