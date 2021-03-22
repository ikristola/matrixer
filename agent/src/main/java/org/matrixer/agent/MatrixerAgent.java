package org.matrixer.agent;

import static org.matrixer.agent.MatrixerAgentUtils.getClassesInPackage;
import static org.matrixer.agent.MatrixerAgentUtils.isTestClass;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

/**
 * Agent for transforming classes in target package. The transformer
 * used by the agent makes class methods in target package print out the
 * caller class when they are called.
 *
 * The agent arguments are specified as
 *
 * outputDirectory:targetPackage:testPackage
 *
 * outputDirectory - the directory where all matrixer output files will
 * be stored
 *
 * targetPackage - the root package that will be tested, sub-packages
 * will be instrumented as well.
 *
 * testPackage - the package of the tests class, this will be used to
 * identify test methods
 */
public class MatrixerAgent {

    final static private boolean useLog = true;
    private PrintStream log;

    final private Instrumentation inst;

    /**
     * The path in which to store results
     */
    final private String outputPath;

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
        outputPath = args[0];
        targetPackage = args[1];
        testerPackage = args[2];
        setupLog();
        log("started " + type + ":\n\tArgs: " + agentArgs);
        log(String.format("OutputPath: %s\ntarget: %s\ntest: %s", outputPath, targetPackage,
                testerPackage));
    }

    /**
     * Run when the agent is started statically
     *
     * @param agentArgs Agent arguments
     * @param inst      Instrumentation instance
     * @throws IOException
     */
    public static void premain(String agentArgs, Instrumentation inst) throws IOException {
        var agent = new MatrixerAgent(agentArgs, inst, "statically");
        agent.run();
    }

    /**
     * Run when the agent is started dynamically
     *
     * @param agentArgs Agent arguments
     * @param inst      Instrumentation instance
     * @throws IOException
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws IOException {
        var agent = new MatrixerAgent(agentArgs, inst, "dynamically");
        agent.run();
    }

    private void setupLog() throws IOException {
        try {
            var out =
                    Files.newOutputStream(Path.of(outputPath).resolve(("matrixer-agent-log.txt")));
            log = new PrintStream(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void run() {
        log("Starting agent transformation");
        /* Return a list of all classes in the target package */
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
        transform(targetCls.get(), inst, outputPath);
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

    private void transform(Class<?> targetCls, Instrumentation inst, String outputPath) {
        try {
            var transformer =
                    new MethodMapTransformer(targetCls, outputPath, targetPackage, testerPackage);
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
