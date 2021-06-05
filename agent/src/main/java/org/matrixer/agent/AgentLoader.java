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
package org.matrixer.agent;

import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;

/**
 * Loads an java agent dynamically
 */
public class AgentLoader {

    /**
     * Load a java agent dynamically
     * 
     * @param jarFilePath
     *            Path to the agent jar file
     * @param agentArgs
     *            Arguments that the agent is started with
     * @throws Exception
     *             Throws exception if load fails
     */
    public static void loadAgent(String jarFilePath, String agentArgs)
            throws Exception {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(jarFilePath, agentArgs);
        vm.detach();
    }
}
