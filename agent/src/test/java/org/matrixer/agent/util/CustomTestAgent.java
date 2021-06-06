/**
 * Copyright 2021 Patrik Bogren, Isak Kristola
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.matrixer.agent.util;

import org.matrixer.agent.AgentLoader;

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
    public void transformClass(Class<?> cls, ClassFileTransformer clsTransformer) {
        try {
            inst.addTransformer(clsTransformer, true);
            inst.retransformClasses(cls);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(
                    "Transform failed for: [" + cls.getName() + "]", e);
        }
    }
}
