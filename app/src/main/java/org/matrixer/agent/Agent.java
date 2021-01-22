package org.matrixer.agent;

import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.List;

import static org.matrixer.agent.AgentUtils.getClassesInPackage;
import static org.matrixer.agent.AgentUtils.isTestClass;

public class Agent {
    /*
     * agentArgs is passed as a single string. Additional parsing must be done by
     * the agent
     */
    public static void premain(String agentArgs, Instrumentation inst) {

        System.out.println("[Agent] started:" + "\n\tArgs: " + agentArgs + "\n\tInstrumentation: " + inst);

        /* Return a list of all classes in the current package */
        List<Class<?>> classes = getClassesInPackage("org.matrixer");
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
