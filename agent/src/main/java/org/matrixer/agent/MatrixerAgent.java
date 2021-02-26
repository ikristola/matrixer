package org.matrixer.agent;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static org.matrixer.agent.MatrixerAgentUtils.getClassesInPackage;
import static org.matrixer.agent.MatrixerAgentUtils.isTestClass;

/**
 * Agent for transforming classes in target package. The transformer
 * used by the agent makes class methods in target package print out the
 * caller class when they are called.
 */
public class MatrixerAgent {

    final private Instrumentation inst;
    final private String args;

    private MatrixerAgent(String agentArgs, Instrumentation inst) {
        this.args = agentArgs;
        this.inst = inst;
    }

    /**
     * Run when the agent is started statically
     * 
     * @param agentArgs Agent arguments
     * @param inst      Instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("[Agent] started statically:" + "\n\tArgs: "
                + agentArgs + "\n\tInstrumentation: " + inst);
        var agent = new MatrixerAgent(agentArgs, inst);
        agent.run();
    }

    /**
     * Run when the agent is started dynamically
     *
     * @param agentArgs Agent arguments
     * @param inst      Instrumentation instance
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("[Agent] started dynamically:" + "\n\tArgs: "
                + agentArgs + "\n\tInstrumentation: " + inst);
        var agent = new MatrixerAgent(agentArgs, inst);
        agent.run();
    }

    private void run() {
        /* Return a list of all classes in the target package */
        String[] argumentsArray = args.split(":"); // Get target project package
                                                   // name
        List<Class<?>> classes = getClassesInPackage(argumentsArray[1]);
        String outputPath = argumentsArray[0];
        for (Class<?> cls : classes) {
            System.out.println("[Agent] class found: " + cls);
            if (!isTestClass(cls)) {
                System.out.println("[Agent] This is not a test class. Transforming it..");
                transformClass(cls.getName(), inst, outputPath);
            } else {
                System.out.println("[Agent] This is a test class. Skipping transform");
            }
        }

    }

    private static void transformClass(String className, Instrumentation inst, String outputPath) {
        Class<?> targetCls = null;
        ClassLoader targetClassLoader = null;

        try {
            targetCls = Class.forName(className);
            targetClassLoader = targetCls.getClassLoader();
            transform(targetCls, targetClassLoader, inst, outputPath);
            return;
        } catch (Exception e) {
            System.err.println("Class [" + className + "] not found with Class.forName");
        }
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                System.out.println("Found class " + clazz.getName());
                targetCls = clazz;
                targetClassLoader = targetCls.getClassLoader();
                transform(targetCls, targetClassLoader, inst, outputPath);
                return;
            }
        }
        throw new RuntimeException("Failed to find class [" + className + "]");
    }

    private static void transform(Class<?> targetCls,
            ClassLoader targetClassLoader, Instrumentation inst, String outputPath) {
        try {
            var transformer = new MethodMapTransformer(targetCls.getName(), targetClassLoader, outputPath);
            inst.addTransformer(transformer, true);
            inst.retransformClasses(targetCls);
        } catch (Exception e) {
            throw new RuntimeException("Transform failed for: [" + targetCls.getName() + "]", e);
        }
    }
}
