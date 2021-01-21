package org.matrixer.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
    /*
     * agentArgs is passed as a single string. Additional parsing must be done by
     * the agent
     */
    public static void premain(String agentArgs, Instrumentation inst) {

        // System.out.println("[Agent] started:" + "\n\tArgs: " + agentArgs + "\n\tInstrumentation: " + inst);

        var className = "org.matrixer.App";
        transformClass(className, inst);
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
