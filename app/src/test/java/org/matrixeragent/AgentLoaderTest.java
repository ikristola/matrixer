package org.matrixeragent;

import com.sun.tools.attach.AgentLoadException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.matrixeragent.util.CustomTestAgent;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgentLoaderTest {

    @Test
    public void expectException_loadWithBadPath() {
        Throwable throwable = assertThrows(AgentLoadException.class,
                () -> AgentLoader.loadAgent("bad/path/agent.jar", "argument"));
        assertEquals("Agent JAR not found or no Agent-Class attribute", throwable.getMessage());
    }

    // TODO remove or make this test run first. other tests might load the agent before this test is run
//    @Test
//    @Order(1)
//    public void agentIsNotRunningBeforeLoad() {
//        assertFalse(CustomTestAgent.isRunning());
//    }

    @Test
    @Order(2)
    public void loadAgent() {
        assertDoesNotThrow(() -> AgentLoader.loadAgent("build/libs/testAgentJar.jar", "argument"));
    }

    @Test
    @Order(3)
    public void agentIsRunningAfterLoad() {
        assertDoesNotThrow(CustomTestAgent::getInstance);
    }
}
