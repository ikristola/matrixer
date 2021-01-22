package org.matrixer.agent;

import java.lang.instrument.Instrumentation;

public class Agent {
    /*
     * agentArgs is passed as a single string. Additional parsing must be done by
     * the agent
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        var className = "org.matrixer.App";
        transformClass(className, inst);
    }

    private static void transformClass(String className, Instrumentation inst) {
        Class<?> targetCls = findClassInCurrentLoader(className);
        if (targetCls == null) {
            targetCls = findClassInInstrumentation(className, inst);
        }
        if (targetCls == null) {
            throw new RuntimeException("Failed to find class [" + className + "]");
        }
        ClassLoader targetClassLoader = targetCls.getClassLoader();
        transform(targetCls, targetClassLoader, inst);
    }

    private static Class<?> findClassInCurrentLoader(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            System.err.println("Class [" + className + "] not found with Class.forName");
            return null;
        }
    }

    private static Class<?> findClassInInstrumentation(String className, Instrumentation inst) {
        for(Class<?> clazz: inst.getAllLoadedClasses()) {
            if(clazz.getName().equals(className)) {
                System.out.println("Found class " + clazz.getName());
                return clazz;
            }
        }
        System.err.println("Class [" + className + "] not found with Instrumentation");
        return null;
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
