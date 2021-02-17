package org.matrixeragent;

import com.sun.tools.attach.VirtualMachine;

import java.lang.management.ManagementFactory;

/**
 * Loads an java agent dynamically
 */
public class AgentLoader {

    /**
     * Load a java agent dynamically
     * 
     * @param jarFilePath Path to the agent jar file
     * @param agentArgs Arguments that the agent is started with
     * @throws Exception Throws exception if load fails
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
