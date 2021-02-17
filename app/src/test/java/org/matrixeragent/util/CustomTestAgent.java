package org.matrixeragent.util;

import org.matrixeragent.AgentLoader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * This is a simple Java agent which facilitates testing of
 * instrumentation functionality such as agent launching and transformer
 * methods.
 */
public class CustomTestAgent {

    private static CustomTestAgent instance;

    final private Instrumentation inst;
    final private String args;

    private CustomTestAgent(String agentArgs, Instrumentation inst) {
        this.args = agentArgs;
        this.inst = inst;
    };

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[TestAgent] started statically with premain:" +
                "\n\tArgs: " + agentArgs + "\n\tInstrumentation: " + inst);
        instance = new CustomTestAgent(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("[TestAgent] started dynamically with agentmain:" +
                "\n\tArgs: " + agentArgs + "\n\tInstrumentation: " + inst);
        instance = new CustomTestAgent(agentArgs, inst);
    }

    /**
     * Get an instance of the agent. If its the first time its called it
     * launches the agent
     * 
     * @return
     */
    public static CustomTestAgent getInstance() {
        if (instance == null) {
            try {
                AgentLoader.loadAgent("build/libs/testAgentJar.jar", "args");
            } catch (Exception e) {
                throw new RuntimeException("Could not load agent ", e);
            }
        }
        return instance;
    }

    public static boolean isRunning() {
        return instance != null;
    }

    /**
     * Transforms a single class using the provided transformer
     *
     * @param cls
     * @param clsTransformer
     */
    public void transformClass(Class<?> cls,
            ClassFileTransformer clsTransformer) {
        try {
            inst.addTransformer(clsTransformer, true);
            inst.retransformClasses(cls);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(
                    "Transform failed for: [" + cls.getName() + "]", e);
        }
    }
}
