package org.matrixeragent;

import java.lang.instrument.Instrumentation;
import java.util.List;

import static org.matrixeragent.MatrixerAgentUtils.getClassesInPackage;
import static org.matrixeragent.MatrixerAgentUtils.isTestClass;

public class MatrixerAgent {

    private static volatile Instrumentation instrumentation;

    /*
     * agentArgs is passed as a single string. Additional parsing must be done by
     * the agent
     */
    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("[Agent] started:" + "\n\tArgs: " + agentArgs + "\n\tInstrumentation: " + inst);

        /* Return a list of all classes in the current package */
        String[] argumentsArray = agentArgs.split(":");     // Get target project package name
        List<Class<?>> classes = getClassesInPackage(argumentsArray[1]);
        for (Class<?> cls : classes) {
            System.out.println("[Agent] class found: " + cls);
            if (!isTestClass(cls)) {
                System.out.println("[Agent] This is not a test class. Transforming it..");
                transformClass(cls.getName(), inst);
            }
            else  {
                System.out.println("[Agent] This is a test class. Skipping transform");
            }
        }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("[Agent] agentmain started:" + "\n\tArgs: " + agentArgs + "\n\tInstrumentation: " + inst);
        System.out.println("[Agent] I'm running dynamically");
        instrumentation = inst;
    }

    public static Instrumentation getInstrumentation() {
        Instrumentation instrumentation = MatrixerAgent.instrumentation;
        if (instrumentation == null) {
            throw new IllegalStateException(
                    "The Agent is not loaded or this method is not called via the system class loader");
        }
        return instrumentation;
    }

    private static void transformClass(String className, Instrumentation inst) {
        Class<?> targetCls = null;
        ClassLoader targetClassLoader = null;

        try {
            targetCls = Class.forName(className);
            targetClassLoader = targetCls.getClassLoader();
            transform(targetCls, targetClassLoader, inst);
            return;
        } catch (Exception e) {
            System.err.println("Class [" + className + "] not found with Class.forName");
        }
        for(Class<?> clazz: inst.getAllLoadedClasses()) {
            if(clazz.getName().equals(className)) {
                System.out.println("Found class " + clazz.getName());
                targetCls = clazz;
                targetClassLoader = targetCls.getClassLoader();
                transform(targetCls, targetClassLoader, inst);
                return;
            }
        }
        throw new RuntimeException(
        "Failed to find class [" + className + "]");
    }

    private static void transform(Class<?> targetCls, ClassLoader targetClassLoader, Instrumentation inst) {
        try {
            var transformer = new Transformer(targetCls.getName(), targetClassLoader);
            inst.addTransformer(transformer, true);
            inst.retransformClasses(targetCls);
        } catch (Exception e) {
            throw new RuntimeException("Transform failed for: [" + targetCls.getName() + "]", e);
        }
    }
}
